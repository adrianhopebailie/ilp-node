package com.hopebailie.btp.camel;

import static java.lang.String.format;

import com.hopebailie.btp.BtpErrorCode;
import com.hopebailie.btp.BtpRuntimeException;
import com.hopebailie.btp.BtpSubProtocols;
import com.hopebailie.ilp.Account;
import com.hopebailie.ilp.AccountRelationship;
import com.hopebailie.ilp.camel.WebSocketSessionManager;
import com.hopebailie.ilp.camel.processors.InterledgerAuthenticationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.component.websocket.WebsocketConstants;

import javax.money.CurrencyUnit;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public class BtpAuthenticationProcessor extends InterledgerAuthenticationProcessor {

  private final WebSocketSessionManager sessions;

  public BtpAuthenticationProcessor(WebSocketSessionManager sessions) {
    Objects.requireNonNull(sessions, "Session manager can't be null");
    this.sessions = sessions;
  }

  @Override
  public Account authenticate(Exchange exchange) throws Exception {

    String connectionKey = exchange.getIn()
        .getHeader(WebsocketConstants.CONNECTION_KEY, String.class);


    Objects.requireNonNull(connectionKey,
        format("Missing header [%s] is required to authenticate BTP sessions.", WebsocketConstants.CONNECTION_KEY));
    Account account = sessions.getAccount(connectionKey);

    if (account == null) {

      BtpSubProtocols protocols = exchange.getIn().getBody(BtpSubProtocols.class);
      if (!protocols.getPrimarySubProtocol().getProtocolName().equals(BtpSubProtocols.AUTH) ||
          !protocols.hasSubProtocol(BtpSubProtocols.AUTH_TOKEN)) {
        //TODO Review error codes (https://github.com/interledger/rfcs/issues/301)
        throw new BtpRuntimeException(
            BtpErrorCode.F00_NotAcceptedError,
            "Unauthenticated session. First message from incoming connection must be an auth.");
      }

      byte[] token = protocols.getSubProtocol(BtpSubProtocols.AUTH_TOKEN).getData();

      String username;
      if (protocols.hasSubProtocol(BtpSubProtocols.AUTH_USERNAME)) {
        username = protocols.getSubProtocol(BtpSubProtocols.AUTH_USERNAME).getDataAsString();
      } else {
        // Base64Url Encode SHA 256 hash of token
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedToken = digest.digest(token);
        username = Base64.getUrlEncoder().encodeToString(hashedToken);
      }

      //TODO Authenticate properly and get account (BtpAuthenticationService?)
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
        public Optional<String> getAddressSegment() {
          return Optional.of(username);
        }

        @Override
        public int compareTo(Account o) {
          return 0;
        }
      };

      if(account == null) {
        //TODO Review error codes (https://github.com/interledger/rfcs/issues/301)
        throw new BtpRuntimeException(
            BtpErrorCode.F00_NotAcceptedError,
            "Invalid token or username.");
      }

      //Strip out username and password
      protocols.removeIf((protocol) -> protocol.getProtocolName().equals(BtpSubProtocols.AUTH_TOKEN));
      protocols.removeIf((protocol) -> protocol.getProtocolName().equals(BtpSubProtocols.AUTH_USERNAME));

      sessions.createSession(connectionKey, account);

    }

    return account;
  }

}
