package com.hopebailie.btp.camel;

import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_NAME;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_TYPE;
import static java.lang.String.format;

import com.hopebailie.btp.BtpMessage;
import com.hopebailie.btp.BtpSubProtocol;
import com.hopebailie.btp.BtpSubProtocolContentType;
import com.hopebailie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;

import java.util.Objects;

public class BtpSupProtocolWrappingProcessor implements Processor {

  /**
   * Wrap a {@link BtpSubProtocols} body in the {@link BtpMessage} envelope.
   *
   * @param exchange the message exchange
   * @throws Exception if an internal processing error has occurred.
   */
  @Override
  public void process(Exchange exchange) throws Exception {

    try {

      String subProtocolName =
          exchange.getIn().getHeader(SUBPROTOCOL_NAME, String.class);
      exchange.getIn().removeHeader(SUBPROTOCOL_NAME);

      BtpSubProtocolContentType subProtocolType =
          exchange.getIn().getHeader(SUBPROTOCOL_TYPE, BtpSubProtocolContentType.class);
      exchange.getIn().removeHeader(SUBPROTOCOL_TYPE);

      byte[] subProtocolData = exchange.getIn().getBody(byte[].class);

      Objects.requireNonNull(subProtocolName,
          format("BTP Sub-Protocol Name must be stored in the %s header.", SUBPROTOCOL_NAME));
      Objects.requireNonNull(subProtocolType,
          format("BTP Sub-Protocol Type must be stored in the %s header.", SUBPROTOCOL_TYPE));

      exchange.getIn().setBody(BtpSubProtocol.builder()
          .protocolName(subProtocolName)
          .contentType(subProtocolType)
          .data(subProtocolData)
          .build());

    } catch (TypeConversionException tce) {
      throw new Exception("Expected the message body to be an instance of byte[].", tce);
    }

  }
}
