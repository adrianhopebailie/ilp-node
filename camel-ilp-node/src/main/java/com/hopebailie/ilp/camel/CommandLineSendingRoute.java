package com.hopebailie.ilp.camel;

import org.apache.camel.builder.RouteBuilder;

import javax.inject.Singleton;

/**
 * Creates routes between the console and the WS plugin server
 */
@Singleton
public class CommandLineSendingRoute extends RouteBuilder {

//  @Inject
//  private CommandLineSendProcessor commandLineSendProcessor;
  private CommandLineSendProcessor commandLineSendProcessor = new CommandLineSendProcessor();

  /**
   * Let's configure the Camel routing rules using Java code...
   */
  public void configure() {

    fromF("stream:in?promptMessage=%s: ", commandLineSendProcessor.getPrompt())
        .process(commandLineSendProcessor)
        .setHeader("Account", () -> "Alice")
        .to("direct:incoming-prepare");
  }

}
