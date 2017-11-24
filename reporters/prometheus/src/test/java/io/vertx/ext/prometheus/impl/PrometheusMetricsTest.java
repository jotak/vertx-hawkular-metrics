/*
 * Copyright (c) 2011-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.prometheus.impl;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.prometheus.VertxPrometheusOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// TODO
@RunWith(VertxUnitRunner.class)
public class PrometheusMetricsTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
      new VertxPrometheusOptions().setEnabled(true).setPrefix("vertx"))
    );

  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void test() throws InterruptedException {
  }

//  public static void main(String[] args) {
//    Vertx vertx = Vertx.vertx(new VertxOptions()
//      .setMetricsOptions(new VertxPrometheusOptions().setEnabled(true).setPrefix("vertx")));
//    Router router = Router.router(vertx);
//    router.route("/").handler(routingContext -> routingContext.response().putHeader("content-type", "text/html").end("Hello World!"));
//    router.route("/metrics").handler(new MetricsHandler());
//    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
//    vertx.eventBus().consumer("test-eb", msg -> {
//      try {
//        Thread.sleep(500);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    });
////    vertx.setPeriodic(200, l -> {
//      vertx.eventBus().publish("test-eb", "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
////    });
////    vertx.setPeriodic(500, l -> {
////      vertx.eventBus().send("test-eb", "idddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
////    });
//
//    HttpClientRequest req = vertx.createHttpClient()
//      .get("www.homeblocks.net", "/")
//      .handler(res -> {
//        res.bodyHandler(System.out::println);
//      });
//    req.end();
//
//    vertx.deployVerticle(new Verticle() {
//      @Override
//      public Vertx getVertx() {
//        return vertx;
//      }
//
//      @Override
//      public void init(Vertx vertx, Context context) {
//
//      }
//
//      @Override
//      public void start(Future<Void> startFuture) throws Exception {
//
//      }
//
//      @Override
//      public void stop(Future<Void> stopFuture) throws Exception {
//
//      }
//    });
//  }
}
