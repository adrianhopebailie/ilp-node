package com.hopebalie.ilp.camel.processors;

import com.hopebalie.ilp.Account;
import com.hopebalie.ilp.camel.InterledgerConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


/**
 * Base class for processors that determine the account of the caller.
 */
public abstract class InterledgerAuthenticationProcessor implements Processor{

  @Override
  public final void process(Exchange exchange) throws Exception {
    Account account = authenticate(exchange);
    exchange.getIn().setHeader(InterledgerConstants.ILP_ACCOUNT_HEADER, account);
  }

  /**
   * Get the account of the caller by inspecting the exchange.
   *
   * @param exchange the exchange with the incoming message
   *
   * @return the account of the caller
   * @throws Exception if there is an error determining the account.
   */
  protected abstract Account authenticate(Exchange exchange) throws Exception;

}
