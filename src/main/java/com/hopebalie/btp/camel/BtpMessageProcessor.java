package com.hopebalie.btp.camel;

import com.hopebalie.btp.BtpMessage;
import com.hopebalie.btp.BtpMessageType;
import com.hopebalie.btp.BtpSubProtocol;
import com.hopebalie.btp.BtpSubProtocolContentType;
import com.hopebalie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BtpMessageProcessor implements Processor {

  /**
   * Processes the message exchange
   *
   * @param exchange the message exchange
   * @throws Exception if an internal processing error has occurred.
   */
  @Override
  public void process(Exchange exchange) throws Exception {

    if (exchange.hasOut()) {

      //Wrap message in BTP envelope
      byte[] data = exchange.getOut().getBody(byte[].class);
      BtpSubProtocols subProtocols = new BtpSubProtocols();
      subProtocols.add(BtpSubProtocol.builder()
          .protocolName("ilp")
          .contentType(BtpSubProtocolContentType.MIME_APPLICATION_OCTET_STREAM)
          .subProtocolData(data)
          .build()
      );

      BtpMessage message = BtpMessage.builder()
          .requestId((Long) exchange.getIn().getHeader("btp.request.id"))
          .subProtocols(subProtocols)
          .type(BtpMessageType.MESSAGE)
          .build();

      exchange.getOut().setBody(message);

    } else {

      BtpMessage message = exchange.getIn().getBody(BtpMessage.class);
      if (!message.hasSubProtocol("ilp")) {
        //TODO Throw
      }

      byte[] data = message.getSubProtocol("ilp")
          .getSubProtocolData();
      long requestId = message.getRequestId();

      exchange.getIn().setHeader("btp.request.id", requestId);
      exchange.getIn().setBody(data);

    }

  }
}
