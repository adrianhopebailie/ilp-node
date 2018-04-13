package com.hopebailie.btp.camel;

import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_CODE;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_DATA;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_NAME;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.ERROR_TIMESTAMP;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.MESSAGE_TYPE;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.REQUEST_ID;
import static java.lang.String.format;

import com.hopebailie.btp.BtpError;
import com.hopebailie.btp.BtpErrorCode;
import com.hopebailie.btp.BtpMessage;
import com.hopebailie.btp.BtpMessageType;
import com.hopebailie.btp.BtpPacket;
import com.hopebailie.btp.BtpResponse;
import com.hopebailie.btp.BtpRuntimeException;
import com.hopebailie.btp.BtpSubProtocol;
import com.hopebailie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;

import java.util.Objects;

public class BtpMessageUnwrappingProcessor implements Processor {

  /**
   * Wrap a {@link BtpSubProtocols} body in the {@link BtpMessage} envelope.
   *
   * @param exchange the message exchange
   * @throws Exception if an internal processing error has occurred.
   */
  @Override
  public void process(Exchange exchange) throws Exception {

    try {

      BtpPacket btpPacket = exchange.getIn().getBody(BtpPacket.class);

      Long requestId = btpPacket.getRequestId();
      exchange.getIn().setHeader(REQUEST_ID, requestId);

      BtpMessageType messageType = btpPacket.getType();
      exchange.getIn().setHeader(MESSAGE_TYPE, messageType);

      BtpSubProtocols subProtocols = btpPacket.getSubProtocols();
      exchange.getIn().setBody(subProtocols);

      switch (messageType) {
        case ERROR:
          BtpError error = (BtpError) btpPacket;
          exchange.getIn().setHeader(ERROR_CODE, error.getErrorCode());
          exchange.getIn().setHeader(ERROR_NAME, error.getErrorName());
          exchange.getIn().setHeader(ERROR_DATA, error.getErrorData());
          exchange.getIn().setHeader(ERROR_TIMESTAMP, error.getTriggeredAt());
          return;
        case TRANSFER:
          throw new RuntimeException("Not yet implemented.");
      }

    } catch (TypeConversionException tce) {
      throw new Exception("Expected the message body to be an instance of BtpPacket.", tce);
    }

  }
}
