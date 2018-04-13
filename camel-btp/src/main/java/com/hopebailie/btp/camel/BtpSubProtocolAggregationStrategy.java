package com.hopebailie.btp.camel;

import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_NAME;
import static com.hopebailie.btp.camel.BtpConstants.HeaderNames.SUBPROTOCOL_TYPE;

import com.hopebailie.btp.BtpSubProtocol;
import com.hopebailie.btp.BtpSubProtocolContentType;
import com.hopebailie.btp.BtpSubProtocols;
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

    BtpSubProtocol protocol = newExchange.getIn().getBody(BtpSubProtocol.class);

    if (oldExchange == null) {
      BtpSubProtocols protocols = new BtpSubProtocols();
      protocols.add(protocol);
      newExchange.getIn().setBody(protocols);
      return newExchange;
    }

    BtpSubProtocols protocols = oldExchange.getIn().getBody(BtpSubProtocols.class);
    protocols.add(protocol);

    if (newExchange.getException() != null) {
      oldExchange.setException(newExchange.getException());
    }

    return oldExchange;
  }
}