package com.hopebailie.ildcp.camel;

import org.interledger.core.InterledgerAddress;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * Represents a Ildcp endpoint.
 */
@UriEndpoint(firstVersion = "1.0-SNAPSHOT",
    scheme = "ildcp", title = "Ildcp",
    syntax = "ildcp:baseAddress",
    producerOnly = true, label = "custom")
public class IldcpEndpoint extends DefaultEndpoint {

  @UriPath
  @Metadata(required = "true")
  private String name;

  @UriParam
  private InterledgerAddress baseAddress;

  public IldcpEndpoint() {
  }

  public IldcpEndpoint(String uri, IldcpComponent component) {
    super(uri, component);
  }

  public IldcpEndpoint(String endpointUri) {
    super(endpointUri);
  }

  public Producer createProducer() throws Exception {
    return new IldcpProducer(this);
  }

  public Consumer createConsumer(Processor processor) throws Exception {
    throw new RuntimeException("This component only supports producers.");
  }

  public boolean isSingleton() {
    return true;
  }

  public InterledgerAddress getBaseAddress() {
    return baseAddress;
  }

  /**
   * The base ILP address to use for ILDCP responses
   */
  public void setBaseAddress(InterledgerAddress baseAddress) {
    this.baseAddress = baseAddress;
  }
}
