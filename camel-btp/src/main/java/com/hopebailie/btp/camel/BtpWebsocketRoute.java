package com.hopebailie.btp.camel;

import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.MESSAGE_TYPE;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.REQUEST_ID;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_NAME;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_TYPE;
import static com.hopebailie.ilp.camel.InterledgerConstants.PropertyNames.INCOMING_ACCOUNT;
import static java.lang.String.format;

import com.hopebailie.btp.BtpErrorCode;
import com.hopebailie.btp.BtpMessageType;
import com.hopebailie.btp.BtpRuntimeException;
import com.hopebailie.btp.BtpSubProtocolContentType;
import com.hopebailie.btp.BtpSubProtocols;
import com.hopebailie.ilp.camel.InterledgerPacketDataFormat;
import com.hopebailie.ilp.camel.WebSocketSessionManager;
import org.apache.camel.builder.RouteBuilder;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates routes between the console and the WS plugin server
 */
@Singleton
public class BtpWebsocketRoute extends RouteBuilder {

  private AtomicLong requestIds = new AtomicLong();

  //TODO Auto-wire
  private String webSocketEndpoint;
  private String connectorIncoming;
  private String connectorOutgoing;
  private WebSocketSessionManager sessions;

  public BtpWebsocketRoute sessions(WebSocketSessionManager sessions) {
    this.sessions = sessions;
    return this;
  }

  public BtpWebsocketRoute websocket(String endpoint) {
    this.webSocketEndpoint = endpoint;
    return this;
  }

  public BtpWebsocketRoute ilpConsumer(String endpoint) {
    this.connectorIncoming = endpoint;
    return this;
  }

  public BtpWebsocketRoute ilpProducer(String endpoint) {
    this.connectorOutgoing = endpoint;
    return this;
  }

  public void configure() {

    BtpMessageDataFormat btpMessageDataFormat = new BtpMessageDataFormat();

    //Incoming from websocket
    from(webSocketEndpoint)
        .unmarshal(new BtpMessageDataFormat())
        .process(new BtpMessageUnwrappingProcessor())
        .process(new BtpAuthenticationProcessor(sessions))
        .log(format(
            "Received BTP message from ${exchangeProperty.%s.%s} with request id ${header.%s}",
            INCOMING_ACCOUNT, "accountId", REQUEST_ID))
        .log(format("BTP request of type:  ${header.%s}", MESSAGE_TYPE))
        .split(bodyAs(BtpSubProtocols.class), new BtpSubProtocolAggregationStrategy())
          //Extract name and type and set body to sub-protocol data
          .process(new BtpSubProtocolUnwrappingProcessor())
          .choice()
            //Auth just gets sent back to the caller
            .when(BtpPredicates::isAuthSubProtocol)
              .log(format("Processing auth sub-protocol"))
              .setHeader(MESSAGE_TYPE, () -> BtpMessageType.RESPONSE)

              //ILP needs to be sent to the connector
            .when(BtpPredicates::isInterledgerSubprotocol)
              .log(format("Processing ilp sub-protocol"))
              .unmarshal(new InterledgerPacketDataFormat())
              .to(connectorIncoming) //Send to connector
              .marshal(new InterledgerPacketDataFormat())
              .setHeader(MESSAGE_TYPE, () -> BtpMessageType.RESPONSE)

            .otherwise()
              .throwException(new BtpRuntimeException(
                  BtpErrorCode.F00_NotAcceptedError,
                  "Unrecognized BTP sub protocol"))
          .end()
          .process(new BtpSupProtocolWrappingProcessor())
        .end() //End Splitter
        .log(format(
            "Sending BTP response with request id:  ${header.%s} and type: ${header.%s} ",
            REQUEST_ID, MESSAGE_TYPE))
//       .process((exchange) -> {
//          ByteArrayOutputStream baos = new ByteArrayOutputStream();
//          byte[] subprotocolData = exchange.getIn().getBody(byte[].class);
//
//          BtpMessage message = BtpMessage.builder()
//              .requestId(exchange.getIn().getHeader(BTP_REQUEST_ID_HEADER, Long.class))
//              .type(exchange.getIn().getHeader(BTP_MESSAGE_TYPE_HEADER, BtpMessageType.class))
//              .subProtocols(BtpSubProtocols.fromPrimarySubProtocol(
//                  BtpSubProtocol.builder()
//                      .protocolName(exchange.getIn()
//                          .getHeader(BTP_SUBPROTOCOL_NAME_HEADER, String.class))
//                      .contentType(exchange.getIn()
//                          .getHeader(BTP_SUBPROTOCOL_TYPE_HEADER, BtpSubProtocolContentType.class))
//                      .data(subprotocolData)
//                      .build()
//              ))
//              .build();
//          _context.write(message, baos);
//          exchange.getIn().setBody(baos.toByteArray());
//        });
        .process(new BtpMessageWrappingProcessor())
        .marshal(new BtpMessageDataFormat())
        .to(webSocketEndpoint);
//        .wireTap("stream:out");


    //From connector
    from(connectorOutgoing)
      .marshal(new InterledgerPacketDataFormat())
      .setHeader(SUBPROTOCOL_NAME, () -> BtpSubProtocols.INTERLEDGER)
      .setHeader(SUBPROTOCOL_TYPE, () -> BtpSubProtocolContentType.MIME_APPLICATION_OCTET_STREAM)
      .process(new BtpSupProtocolWrappingProcessor())
      .setHeader(REQUEST_ID, () -> requestIds.incrementAndGet())
      .setHeader(MESSAGE_TYPE, () -> BtpMessageType.MESSAGE)
      //Use the aggregator to create a SubProtocols set of size = 1
      .aggregate(header(REQUEST_ID), new BtpSubProtocolAggregationStrategy()).completionSize(1)
//        .process(//TODO Figure out the connection ID to use based on the account)
      .log(format(
          "Sending BTP message from ${exchangeProperty.%s.%s} with request id ${header.%s}",
          INCOMING_ACCOUNT, "accountId", REQUEST_ID))
      .process(new BtpMessageWrappingProcessor())
      .marshal(new BtpMessageDataFormat())
      .to(webSocketEndpoint)
      .unmarshal(new BtpMessageDataFormat())
      .process(new BtpMessageUnwrappingProcessor())
      .log(format(
          "Received BTP response with request id ${header.%s}",
          REQUEST_ID))
      .log(format("BTP request of type:  ${header.%s}", MESSAGE_TYPE))
      .split(bodyAs(BtpSubProtocols.class))
        .process(new BtpSubProtocolUnwrappingProcessor())
        .choice()
          //ILP needs to be sent to the connector
          .when(BtpPredicates::isInterledgerSubprotocol)
            .log(format("Processing ilp sub-protocol"))
            .unmarshal(new InterledgerPacketDataFormat())
          .otherwise()
            .throwException(new BtpRuntimeException(
                BtpErrorCode.F00_NotAcceptedError,
                "Unknown sub-protocol in response"))
    .to(connectorOutgoing);

  }

}
