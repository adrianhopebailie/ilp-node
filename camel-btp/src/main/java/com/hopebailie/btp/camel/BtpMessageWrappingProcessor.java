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
import com.hopebailie.btp.BtpResponse;
import com.hopebailie.btp.BtpRuntimeException;
import com.hopebailie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;

import java.time.Instant;
import java.util.Objects;

public class BtpMessageWrappingProcessor implements Processor {

  /**
   * Wrap a {@link BtpSubProtocols} body in the {@link BtpMessage} envelope.
   *
   * @param exchange the message exchange
   * @throws Exception if an internal processing error has occurred.
   */
  @Override
  public void process(Exchange exchange) throws Exception {

    try {

      Long requestId = exchange.getIn().getHeader(REQUEST_ID, Long.class);
      exchange.getIn().removeHeader(REQUEST_ID);
      BtpMessageType messageType = exchange.getIn().getHeader(MESSAGE_TYPE, BtpMessageType.class);
      exchange.getIn().removeHeader(MESSAGE_TYPE);

      BtpSubProtocols subProtocols = exchange.getIn().getBody(BtpSubProtocols.class);

      Objects.requireNonNull(requestId, format("Request ID must be stored in the %s header.", REQUEST_ID));
      Objects.requireNonNull(messageType, format("BTP Message Type must be stored in the %s header.", MESSAGE_TYPE));

      //If the exchange is failed with a BtpRuntimeError we ignore the message type header and always wrap an error message
      if (exchange.isFailed() && exchange.getException() instanceof BtpRuntimeException) {
        messageType = BtpMessageType.ERROR;
      }

      switch (messageType) {
        case ERROR:

          if(exchange.getException() != null && exchange.getException() instanceof BtpRuntimeException) {
            BtpRuntimeException bre = (BtpRuntimeException) exchange.getException();
            exchange.getIn().setBody(bre.toBtpError(requestId, subProtocols));
            return;
          }

          BtpErrorCode code = exchange.getIn().getHeader(ERROR_CODE, BtpErrorCode.class);
          String message = exchange.getIn().getHeader(ERROR_NAME, String.class);
          byte[] data = exchange.getIn().getHeader(ERROR_DATA, byte[].class);
          Instant timestamp = exchange.getIn().getHeader(ERROR_TIMESTAMP, Instant.class);

          Objects.requireNonNull(code, format("Error code must be stored in the %s header.", ERROR_CODE));
          Objects.requireNonNull(code, format("Error name must be stored in the %s header.", ERROR_NAME));
          Objects.requireNonNull(code, format("Error data must be stored in the %s header.", ERROR_DATA));
          Objects.requireNonNull(code, format("Error timestamp must be stored in the %s header.", ERROR_TIMESTAMP));

          exchange.getIn().setBody(BtpError.builder()
              .requestId(requestId)
              .errorCode(code)
              .errorName(message)
              .errorData(data)
              .triggeredAt(timestamp)
              .subProtocols(subProtocols)
              .build());
          return;
        case MESSAGE:
          exchange.getIn().setBody(BtpMessage.builder()
              .requestId(requestId)
              .subProtocols(subProtocols)
              .build());
          return;
        case RESPONSE:
          exchange.getIn().setBody(BtpResponse.builder()
              .requestId(requestId)
              .subProtocols(subProtocols)
              .build());
          return;
        case TRANSFER:
          throw new RuntimeException("Not yet implemented.");
      }

    } catch (TypeConversionException tce) {
      throw new Exception("Expected the message body to be an instance of SubProtocols.", tce);
    }

  }
}
