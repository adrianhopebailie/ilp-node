package com.hopebailie.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Traceable;
import org.apache.camel.spi.IdAware;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.AsyncProcessorHelper;

/**
 * A generic processor for wrapping a message body in a new envelope type
 */
public class WrapProcessor extends ServiceSupport implements AsyncProcessor, Traceable,
    CamelContextAware, IdAware {

  private String id;
  private CamelContext camelContext;
  private Wrapper wrapper;

  public void process(Exchange exchange) throws Exception {
    AsyncProcessorHelper.process(this, exchange);
  }

  public boolean process(Exchange exchange, AsyncCallback callback) {
    //TODO Implement this
    return false;
  }

  public String toString() {
    return "Wrap[" + "TODO" + "]";
  }

  public String getTraceLabel() {
    return "Wrap[" + "TODO" + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public CamelContext getCamelContext() {
    return camelContext;
  }

  public void setCamelContext(CamelContext camelContext) {
    this.camelContext = camelContext;
  }

  @Override
  protected void doStart() throws Exception {
    //TODO Implement this
  }

  @Override
  protected void doStop() throws Exception {
    //TODO Implement this
  }

}
