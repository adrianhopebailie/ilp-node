package com.hopebalie.btp.camel;

import static com.hopebalie.btp.camel.BtpConstants.BTP_MESSAGE_TYPE_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_NAME_HEADER;

import com.hopebalie.btp.BtpMessageType;
import com.hopebalie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;

public class BtpPredicates {

  public static boolean isInterledgerSubprotocol(Exchange exchange) {
    return BtpSubProtocols.INTERLEDGER.equals(exchange.getIn().getHeader(BTP_SUBPROTOCOL_NAME_HEADER, String.class));
  }

  public static boolean isBtpRequest(Exchange exchange) {
    BtpMessageType type = exchange.getIn().getHeader(BTP_MESSAGE_TYPE_HEADER, BtpMessageType.class);
    return type == BtpMessageType.MESSAGE || type == BtpMessageType.TRANSFER;
  }

  public static boolean isBtpResponse(Exchange exchange) {
    BtpMessageType type = exchange.getIn().getHeader(BTP_MESSAGE_TYPE_HEADER, BtpMessageType.class);
    return type == BtpMessageType.RESPONSE || type == BtpMessageType.ERROR;
  }

  public static boolean isAuthSubProtocol(Exchange exchange) {
    return BtpSubProtocols.AUTH.equals(exchange.getIn().getHeader(BTP_SUBPROTOCOL_NAME_HEADER, String.class));
  }
}
