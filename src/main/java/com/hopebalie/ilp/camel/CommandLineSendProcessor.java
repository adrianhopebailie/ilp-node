package com.hopebalie.ilp.camel;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.cryptoconditions.PreimageSha256Condition;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.inject.Singleton;
import java.math.BigInteger;
import java.time.Instant;

@Singleton
public class CommandLineSendProcessor implements Processor {

  /**
   * Processes the message exchange
   *
   * @param exchange the message exchange
   * @throws Exception if an internal processing error has occurred.
   */
  @Override
  public void process(Exchange exchange) throws Exception {
    String[] args = exchange.getIn().getBody(String.class).split(" ");
    if(args.length != 2) {
      throw new RuntimeException("Expected input in the form of <address> <amount>");
    }

    exchange.getIn().setBody(InterledgerPreparePacket.builder()
        .amount(BigInteger.valueOf(Long.getLong(args[1])))
        .destination(InterledgerAddress.of(args[0]))
        .executionCondition(PreimageSha256Condition.fromCostAndFingerprint(32, new byte[32]))
        .expiresAt(Instant.now().plusSeconds(30))
        .data(new byte[]{})
        .build());
  }

  public String getPrompt() {
    return "Pay <address> <amount>";
  }
}
