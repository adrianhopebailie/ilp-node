package com.hopebailie.btp.camel;

import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.MESSAGE_TYPE;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_NAME;

import com.hopebailie.btp.BtpMessageType;
import com.hopebailie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;

public class BtpPredicates {

  public static boolean isInterledgerSubprotocol(Exchange exchange) {
    return BtpSubProtocols.INTERLEDGER.equals(exchange.getIn().getHeader(SUBPROTOCOL_NAME, String.class));
  }

  public static boolean isBtpRequest(Exchange exchange) {
    BtpMessageType type = exchange.getIn().getHeader(MESSAGE_TYPE, BtpMessageType.class);
    return type == BtpMessageType.MESSAGE || type == BtpMessageType.TRANSFER;
  }

  public static boolean isBtpResponse(Exchange exchange) {
    BtpMessageType type = exchange.getIn().getHeader(MESSAGE_TYPE, BtpMessageType.class);
    return type == BtpMessageType.RESPONSE || type == BtpMessageType.ERROR;
  }

  public static boolean isAuthSubProtocol(Exchange exchange) {
    return BtpSubProtocols.AUTH.equals(exchange.getIn().getHeader(SUBPROTOCOL_NAME, String.class));
  }
}
