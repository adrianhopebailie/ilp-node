package com.hopebailie.ilp.camel;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java8 DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {
    private static final Object[] OBJECTS = new Object[]{
        "A string",
        new Integer(1),
        new Double(1.0)
    };

    private int index;

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which set a raondom body then performs content
        // based routing on the message using method references
        from("timer:simple?period=1000")
            .process()
                .message(m -> m.setHeader("index", index++ % 3))
            .transform()
                .message(this::randomBody)
            .choice()
                .when()
                    .body(String.class::isInstance)
                    .log("Got a String body")
                .when()
                    .body(Integer.class::isInstance)
                    .log("Got an Integer body")
                .when()
                    .body(Double.class::isInstance)
                    .log("Got a Double body")
                .otherwise()
                    .log("Other type message");
    }

    private Object randomBody(Message m) {
        return OBJECTS[m.getHeader("index", Integer.class)];
    }
}
