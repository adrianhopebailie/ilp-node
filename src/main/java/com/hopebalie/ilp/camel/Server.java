package com.hopebalie.ilp.camel;

import com.hopebalie.btp.camel.BtpWebsocketRoute;
import org.apache.camel.main.Main;

public class Server {

  public static void main(String... args) throws Exception {
    Main main = new Main();
//    CamelContext context = main.getOrCreateCamelContext();
//    context.setStreamCaching(true);
//    context.setTracing(true);
//        main.addRouteBuilder(new CommandLineSendingRoute());
    main.addRouteBuilder(new BtpWebsocketRoute()
        .websocket("websocket://localhost:9090/btp")
        .ilpConsumer("direct:ilp-consumer")
        .ilpProducer("direct:ilp-producer")
    );
    main.addRouteBuilder(new MockInterledgerRoute());
//    main.addRouteBuilder(new TestWebsocketRoute());
//    main.addRouteBuilder(new OtherWebsocketRoute());
    main.run(args);
  }

}

