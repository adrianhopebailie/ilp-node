package com.hopebailie.ilp.camel.processors;

import static com.hopebailie.ilp.camel.InterledgerConstants.PropertyNames.INCOMING_ACCOUNT;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRuntimeException;

import com.hopebailie.ilp.Account;
import com.hopebailie.ilp.InterledgerNodeContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


/**
 * Base class for processors that determine the account of the caller.
 */
public abstract class InterledgerProcessor implements Processor {

  @Override
  public final void process(Exchange exchange) throws Exception {
    InterledgerPreparePacket prepare = exchange.getIn().getBody(InterledgerPreparePacket.class);
    Account requestingAccount = exchange.getProperty(INCOMING_ACCOUNT, Account.class);
    InterledgerNodeContext context = null; //TODO

    try {
      InterledgerFulfillPacket fulfillment = fulfill(context, requestingAccount, prepare);
    } catch (InterledgerRuntimeException e) {
      e.ge
    }

  }

  /**
   * Fulfill the request or throw.
   *
   * @param requestingAccount the account of the entity that sent the ILP packet
   * @param prepare the incoming ILP packet
   *
   * @return the ILP Fulfill packet
   * @throws Exception if there is an error determining the account.
   */
  protected abstract InterledgerFulfillPacket fulfill(
      InterledgerNodeContext context, Account requestingAccount, InterledgerPreparePacket prepare) throws Exception;

}
