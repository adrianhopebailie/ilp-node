package com.hopebalie.btp.camel;

import static com.hopebalie.btp.camel.BtpConstants.BTP_MESSAGE_TYPE_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_REQUEST_ID_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_COUNT_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_NAME_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_TYPE_HEADER;
import static com.hopebalie.ilp.camel.InterledgerConstants.ILP_ACCOUNT_HEADER;
import static java.lang.String.format;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.encoding.asn.framework.CodecContextFactory;

import com.hopebalie.btp.BtpMessage;
import com.hopebalie.btp.BtpMessageType;
import com.hopebalie.btp.BtpSubProtocol;
import com.hopebalie.btp.BtpSubProtocolContentType;
import com.hopebalie.btp.BtpSubProtocols;
import com.hopebalie.btp.codecs.AsnBtpMessageCodec;
import com.hopebalie.btp.codecs.AsnBtpSubProtocolCodec;
import com.hopebalie.btp.codecs.AsnBtpSubProtocolsCodec;
import com.hopebalie.btp.serializers.oer.AsnBtpSubProtocolsOerSerializer;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates routes between the console and the WS plugin server
 */
@Singleton
public class BtpWebsocketRoute extends RouteBuilder {

  private static final CodecContext _context
      = CodecContextFactory.getContext(CodecContextFactory.OCTET_ENCODING_RULES);

  static {
    _context
        .register(BtpMessage.class, AsnBtpMessageCodec::new)
        .register(BtpSubProtocol.class, AsnBtpSubProtocolCodec::new)
        .register(BtpSubProtocols.class, AsnBtpSubProtocolsCodec::new, new AsnBtpSubProtocolsOerSerializer());
  }

  private AtomicLong requestIds = new AtomicLong();

  private Processor btpAuthProcessor = new BtpAuthenticationProcessor();
  private BtpSubProtocolAggregationStrategy subProtocolAggregationStrategy = new BtpSubProtocolAggregationStrategy();

  //TODO Auto-wire
  private String webSocketEndpoint;
  private String interledgerConsumer;
  private String interledgerProducer;


  public BtpWebsocketRoute websocket(String endpoint) {
    this.webSocketEndpoint = endpoint;
    return this;
  }

  public BtpWebsocketRoute ilpConsumer(String endpoint) {
    this.interledgerConsumer = endpoint;
    return this;
  }

  public BtpWebsocketRoute ilpProducer(String endpoint) {
    this.interledgerProducer = endpoint;
    return this;
  }

  public void configure() {

    BtpMessageDataFormat btpMessageDataFormat = new BtpMessageDataFormat();

    //Incoming from websocket
    from(webSocketEndpoint)

        //Decode
        .unmarshal(btpMessageDataFormat)
        .log(format("Received BTP message with request id:  ${header.%s}", BTP_REQUEST_ID_HEADER))

        .choice()

        //Incoming requests
        .when(BtpPredicates::isBtpRequest)
        //Determine ILP account (and strip username and password)
        .process(btpAuthProcessor)
        .to("direct:btp-incoming-request")

        //Incoming responses
        .otherwise()
        .to("direct:btp-incoming-response");

    //Incoming from connector
    from(interledgerProducer)
        .to("direct:btp-outgoing-request");


    //Handle sub-protocols
    from("direct:btp-incoming-request")
        .log(format("BTP request of type:  ${header.%s}", BTP_MESSAGE_TYPE_HEADER))
        .log(format("BTP sub-protocols:  ${header.%s}", BTP_SUBPROTOCOL_COUNT_HEADER))
        .split(bodyAs(BtpSubProtocols.class), subProtocolAggregationStrategy)

        //Extract name and type and set body to sub-protocol data
        .setHeader(BTP_SUBPROTOCOL_NAME_HEADER, bodyAs(BtpSubProtocol.class).method("getProtocolName"))
        .setHeader(BTP_SUBPROTOCOL_TYPE_HEADER, bodyAs(BtpSubProtocol.class).method("getContentType"))
        .setBody(bodyAs(BtpSubProtocol.class).method("getSubProtocolData"))

        .choice()
        //Auth just gets sent back to the caller
        .when(BtpPredicates::isAuthSubProtocol)
          .log(format("Processing auth sub-protocol"))
          .to("direct:btp-outgoing-response")

        //ILP needs to be sent to be queued for the connector
        .when(BtpPredicates::isInterledgerSubprotocol)
          .log(format("Processing ilp sub-protocol"))
          .to(ExchangePattern.InOut, interledgerConsumer) //Send to connector
          .setHeader(BTP_MESSAGE_TYPE_HEADER, () -> BtpMessageType.RESPONSE)
          .to("direct:btp-outgoing-response")

        .otherwise()
          .setHeader(BTP_MESSAGE_TYPE_HEADER, () -> BtpMessageType.ERROR)
          .throwException(new Exception("Unrecognized BTP sub protocol"))
          .to("direct:btp-outgoing-response");


    from("direct:btp-incoming-response")
        .log(format("BTP response of type:  ${header.%s}", BTP_MESSAGE_TYPE_HEADER))

        //Handle sub-protocols
        .split(bodyAs(BtpSubProtocols.class), subProtocolAggregationStrategy)
        .choice()
        .when(BtpPredicates::isAuthSubProtocol)
        //TODO: Handle auth attempts that we make
        .when(BtpPredicates::isInterledgerSubprotocol)
        .to(interledgerProducer) //Send to connector
        .otherwise()
        .setHeader(BTP_MESSAGE_TYPE_HEADER, () -> BtpMessageType.ERROR)
        .throwException(new Exception("Unrecognized BTP sub protocol"));


    from("direct:btp-outgoing-response")
        .log(format("Sending BTP response with request id:  ${header.%s}", BTP_REQUEST_ID_HEADER))
        //TODO .onException()
        .setHeader(BTP_MESSAGE_TYPE_HEADER, () -> BtpMessageType.RESPONSE)
        .process((exchange) -> {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] subprotocolData = exchange.getIn().getBody(byte[].class);

          BtpMessage message = BtpMessage.builder()
              .requestId(exchange.getIn().getHeader(BTP_REQUEST_ID_HEADER, Long.class))
              .type(exchange.getIn().getHeader(BTP_MESSAGE_TYPE_HEADER, BtpMessageType.class))
              .subProtocols(BtpSubProtocols.fromPrimarySubProtocol(
                  BtpSubProtocol.builder()
                      .protocolName(exchange.getIn()
                          .getHeader(BTP_SUBPROTOCOL_NAME_HEADER, String.class))
                      .contentType(exchange.getIn()
                          .getHeader(BTP_SUBPROTOCOL_TYPE_HEADER, BtpSubProtocolContentType.class))
                      .subProtocolData(subprotocolData)
                      .build()
              ))
              .build();
          _context.write(message, baos);
          exchange.getIn().setBody(baos.toByteArray());
        })
//          .marshal(btpMessageDataFormat)
        .log(format("Session created for ${header.%s}", ILP_ACCOUNT_HEADER))
        .to(webSocketEndpoint);


    from("direct:btp-outgoing-request")
        //TODO: Should the correlation id come from connector?
        .setHeader(BTP_REQUEST_ID_HEADER, () -> requestIds.incrementAndGet())
        .setHeader(BTP_MESSAGE_TYPE_HEADER, () -> BtpMessageType.MESSAGE)
        .marshal(btpMessageDataFormat)
        //TODO Get WebSocket connection ID from ILP account
        .to(webSocketEndpoint);

  }

}
