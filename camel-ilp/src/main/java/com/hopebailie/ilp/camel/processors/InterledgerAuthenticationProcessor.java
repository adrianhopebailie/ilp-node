package com.hopebailie.ilp.camel.processors;

import static com.hopebailie.ilp.camel.InterledgerConstants.PropertyNames.INCOMING_ACCOUNT;

import com.hopebailie.ilp.camel.InterledgerConstants;
import com.hopebailie.ilp.Account;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


/**
 * Base class for processors that determine the account of the caller.
 */
public abstract class InterledgerAuthenticationProcessor implements Processor {

  @Override
  public final void process(Exchange exchange) throws Exception {
    Account account = authenticate(exchange);
    exchange.setProperty(INCOMING_ACCOUNT, account);
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
