package com.hopebalie.btp.camel;

import static com.hopebalie.btp.BtpSubProtocols.AUTH;
import static com.hopebalie.btp.BtpSubProtocols.AUTH_TOKEN;
import static com.hopebalie.btp.BtpSubProtocols.AUTH_USERNAME;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_COUNT_HEADER;

import org.interledger.core.InterledgerAddress;

import com.hopebalie.btp.BtpSubProtocols;
import com.hopebalie.ilp.Account;
import com.hopebalie.ilp.AccountRelationship;
import com.hopebalie.ilp.camel.processors.InterledgerAuthenticationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.component.websocket.WebsocketConstants;

import javax.money.CurrencyUnit;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BtpAuthenticationProcessor extends InterledgerAuthenticationProcessor {

  //Map of WebSocket Connection ID -> Account
  private final Map<String, Account> sessions;

  public BtpAuthenticationProcessor() {
    sessions = new HashMap<>();
  }

  @Override
  public Account authenticate(Exchange exchange) throws Exception {

    String connectionKey = exchange.getIn()
        .getHeader(WebsocketConstants.CONNECTION_KEY, String.class);

    Account account;
    if (sessions.containsKey(connectionKey)) {
      account = sessions.get(connectionKey);
    } else {

      BtpSubProtocols protocols = exchange.getIn().getBody(BtpSubProtocols.class);
      if (!protocols.getPrimarySubProtocol().getProtocolName().equals(AUTH) ||
          !protocols.hasSubProtocol(AUTH_TOKEN)) {
        //TODO Better exception
        throw new Exception("Unauthenticated session. First message from incoming connection must be an auth.");
      }

      byte[] token = protocols.getSubProtocol(AUTH_TOKEN).getSubProtocolData();

      String username;
      if (protocols.hasSubProtocol(AUTH_USERNAME)) {
        username = protocols.getSubProtocol(AUTH_USERNAME).getDataAsString();
      } else {
        // Base64Url Encode SHA 256 hash of token
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedToken = digest.digest(token);
        username = Base64.getUrlEncoder().encodeToString(hashedToken);
      }

      //TODO Authenticate and get account (BtpAuthenticationService?)
      account = new Account() {
        @Override
        public String getAccountId() {
          return username;
        }

        @Override
        public CurrencyUnit getCurrencyUnit() {
          return null;
        }

        @Override
        public Integer getCurrencyScale() {
          return null;
        }

        @Override
        public AccountRelationship getRelationship() {
          return AccountRelationship.PEER;
        }

        @Override
        public Optional<InterledgerAddress> getAddressSegment() {
          return Optional.empty();
        }

        @Override
        public int compareTo(Account o) {
          return 0;
        }
      };

      //Strip out username and password
      protocols.removeIf((protocol) -> protocol.getProtocolName().equals(AUTH_TOKEN));
      protocols.removeIf((protocol) -> protocol.getProtocolName().equals(AUTH_USERNAME));

      exchange.getIn().setHeader(BTP_SUBPROTOCOL_COUNT_HEADER, 1);

      sessions.put(connectionKey, account);

    }

    return account;
  }

}
