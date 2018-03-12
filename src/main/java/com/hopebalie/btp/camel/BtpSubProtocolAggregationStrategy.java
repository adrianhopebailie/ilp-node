package com.hopebalie.btp.camel;

import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_NAME_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_TYPE_HEADER;

import com.hopebalie.btp.BtpSubProtocol;
import com.hopebalie.btp.BtpSubProtocolContentType;
import com.hopebalie.btp.BtpSubProtocols;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class BtpSubProtocolAggregationStrategy implements AggregationStrategy {

  /**
   * Aggregates an old and new exchange together to create a single combined exchange
   *
   * @param oldExchange the oldest exchange (is <tt>null</tt> on first aggregation as we only have the new exchange)
   * @param newExchange the newest exchange (can be <tt>null</tt> if there was no data possible to acquire)
   * @return a combined composite of the two exchanges, favor returning the <tt>oldExchange</tt> whenever possible
   */
  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

    if (oldExchange == null) {

      BtpSubProtocols protocols = new BtpSubProtocols();
      protocols.add(BtpSubProtocol.builder()
        .protocolName(newExchange.getIn().getHeader(BTP_SUBPROTOCOL_NAME_HEADER, String.class))
        .contentType(newExchange.getIn().getHeader(BTP_SUBPROTOCOL_TYPE_HEADER, BtpSubProtocolContentType.class))
        .subProtocolData(newExchange.getIn().getBody(byte[].class))
        .build()
      );
      newExchange.getIn().setBody(protocols);
      return newExchange;

    }

    oldExchange.getIn().getBody(BtpSubProtocols.class)
      .add(BtpSubProtocol.builder()
        .protocolName(newExchange.getIn().getHeader(BTP_SUBPROTOCOL_NAME_HEADER, String.class))
        .contentType(newExchange.getIn().getHeader(BTP_SUBPROTOCOL_TYPE_HEADER, BtpSubProtocolContentType.class))
        .subProtocolData(newExchange.getIn().getBody(byte[].class))
        .build()
    );
    return oldExchange;
  }
}