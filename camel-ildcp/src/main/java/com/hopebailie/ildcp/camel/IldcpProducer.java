package com.hopebailie.ildcp.camel;


import static com.hopebailie.ilp.camel.InterledgerConstants.PropertyNames.INCOMING_ACCOUNT;
import static com.hopebailie.ilp.camel.InterledgerConstants.PropertyNames.OUTGOING_ACCOUNT;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.encoding.asn.framework.CodecContext;

import com.hopebailie.ildcp.asn.framework.IldcpCodecs;
import com.hopebailie.ilp.Account;
import com.hopebailie.ilp.AccountRelationship;
import com.hopebailie.ilp.RequestRejectedException;
import com.hopebalie.ildcp.IldcpResponse;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * The Ildcp producer.
 */
public class IldcpProducer extends DefaultProducer {
  private static final Logger LOG = LoggerFactory.getLogger(IldcpProducer.class);
  private static final CodecContext CODEC_CONTEXT = InterledgerCodecContextFactory.oer();

  static {
    IldcpCodecs.register(CODEC_CONTEXT);
  }

  private IldcpEndpoint endpoint;

  public IldcpProducer(IldcpEndpoint endpoint) {
    super(endpoint);
    this.endpoint = endpoint;
  }

  public void process(Exchange exchange) throws Exception {

    //TODO This is repeatable logic that should be in a helper and throw if the data is missing
    InterledgerPreparePacket request = exchange.getIn().getBody(InterledgerPreparePacket.class);
    Account child = exchange.getProperty(INCOMING_ACCOUNT, Account.class);

    LOG.debug("Processing ILDCP request from %s", child.getAccountId());

    if (!(child.getRelationship() == AccountRelationship.CHILD)) {
      throw new RequestRejectedException(
          InterledgerErrorCode.F00_BAD_REQUEST,
          "Can only provide ildcp configuration for child nodes.");
    }

    IldcpResponse response = IldcpResponse.builder()
        .interledgerAddress(getInterledgerAddress(child))
        .currencyUnit(child.getCurrencyUnit())
        .currencyScale(child.getCurrencyScale())
        .build();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CODEC_CONTEXT.write(response, baos);

    exchange.getOut().setBody(InterledgerFulfillPacket.builder()
        .fulfillment(PEER_PROTOCOL_FULFILLMENT)
        .data(baos.toByteArray())
        .build());
    exchange.setProperty(OUTGOING_ACCOUNT, child);

  }

  private InterledgerAddress getInterledgerAddress(Account child) {
    return this.endpoint.getBaseAddress().with(
        child.getAddressSegment()
            .orElse(child.getAccountId()));
  }


}
