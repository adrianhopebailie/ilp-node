package com.hopebailie.ilp.camel;

import org.apache.camel.builder.RouteBuilder;

public class MockInterledgerRoute extends RouteBuilder {



  /**
   * <b>Called on initialization to build the routes using the fluent builder syntax.</b>
   * <p/>
   * This is a central method for RouteBuilder implementations to implement
   * the routes using the Java fluent builder syntax.
   *
   * @throws Exception can be thrown during configuration
   */
  @Override
  public void configure() throws Exception {
    from("direct:ilp-consumer?exchangePattern=InOut")
      .process((exchange -> {
        byte[] body = exchange.getIn().getBody(byte[].class);
        exchange.getOut().setBody(body);
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
        exchange.getOut().setAttachments(exchange.getIn().getAttachments());
      }));
//    .to("direct:ilp-consumer");
  }
}
