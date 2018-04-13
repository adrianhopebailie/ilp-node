package com.hopebailie.ilp.camel;

import com.hopebailie.btp.camel.BtpWebsocketRoute;
import org.apache.camel.main.Main;

public class Server {

  public static void main(String... args) throws Exception {

    WebSocketSessionManager sessions = new WebSocketSessionManager();

    Main main = new Main();
//    CamelContext context = main.getOrCreateCamelContext();
//    context.setStreamCaching(true);
//    context.setTracing(true);
//        main.addRouteBuilder(new CommandLineSendingRoute());
    main.addRouteBuilder(new BtpWebsocketRoute()
        .sessions(sessions)
        .websocket("websocket://localhost:9090/btp?exchangePattern=InOut")
        .ilpConsumer("direct:ilp-consumer?exchangePattern=InOut")
        .ilpProducer("direct:ilp-producer?exchangePattern=InOut")
    );
    main.addRouteBuilder(new MockInterledgerRoute());
//    main.addRouteBuilder(new TestWebsocketRoute());
//    main.addRouteBuilder(new OtherWebsocketRoute());
    main.run(args);
  }

}

