package com.hopebailie.btp.camel;

import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_CODE;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_DATA;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_NAME;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_TIMESTAMP;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.MESSAGE_TYPE;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.REQUEST_ID;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_NAME;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_TYPE;

import com.hopebailie.btp.BtpError;
import com.hopebailie.btp.BtpMessage;
import com.hopebailie.btp.BtpMessageType;
import com.hopebailie.btp.BtpPacket;
import com.hopebailie.btp.BtpSubProtocol;
import com.hopebailie.btp.BtpSubProtocolContentType;
import com.hopebailie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;

import java.nio.charset.StandardCharsets;

public class BtpSubProtocolUnwrappingProcessor implements Processor {

  /**
   * Wrap a {@link BtpSubProtocols} body in the {@link BtpMessage} envelope.
   *
   * @param exchange the message exchange
   * @throws Exception if an internal processing error has occurred.
   */
  @Override
  public void process(Exchange exchange) throws Exception {

    try {

      BtpSubProtocol subProtocol = exchange.getIn().getBody(BtpSubProtocol.class);

      String protocolName = subProtocol.getProtocolName();
      exchange.getIn().setHeader(SUBPROTOCOL_NAME, protocolName);

      BtpSubProtocolContentType contentType = subProtocol.getContentType();
      exchange.getIn().setHeader(SUBPROTOCOL_TYPE, contentType);

      byte[] data = subProtocol.getData();

      switch (contentType) {
        case MIME_APPLICATION_OCTET_STREAM:
          exchange.getIn().setBody(data);
          return;
        case MIME_APPLICATION_JSON:
        case MIME_TEXT_PLAIN_UTF8:
          exchange.getIn().setBody(new String(data, StandardCharsets.UTF_8));
      }

    } catch (TypeConversionException tce) {
      throw new Exception("Expected the message body to be an instance of BtpSubProtocol.", tce);
    }

  }
}
