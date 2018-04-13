package com.hopebailie.ildcp.camel;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import java.util.Map;

/**
 * Represents the component that manages {@link IldcpEndpoint}.
 */
public class IldcpComponent extends DefaultComponent {

  protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
    Endpoint endpoint = new IldcpEndpoint(uri, this);
    setProperties(endpoint, parameters);
    return endpoint;
  }
}
