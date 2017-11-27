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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.ext.prometheus.VertxPrometheusOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

import static io.vertx.ext.metrics.collector.MetricsType.*;

/**
 * Metrics SPI implementation for Prometheus.
 *
 * @author Joel Takvorian
 */
public class PrometheusVertxMetrics extends DummyVertxMetrics {
  private final CollectorRegistry registry;
  private final Optional<EventBusMetrics> eventBusMetrics;
  private final Optional<DatagramSocketMetrics> datagramSocketMetrics;
  private final Optional<TCPMetrics> netClientMetrics;
  private final Optional<PrometheusNetServerMetrics> netServerMetrics;
  private final Optional<HttpClientMetrics> httpClientMetrics;
  private final Optional<PrometheusHttpServerMetrics> httpServerMetrics;
  private final Optional<PrometheusPoolMetrics> poolMetrics;
  private final Optional<PrometheusVerticleMetrics> verticleMetrics;
  private final Vertx vertx;
  private final VertxPrometheusOptions options;

  /**
   * @param options Vertx Prometheus options
   */
  PrometheusVertxMetrics(Vertx vertx, VertxPrometheusOptions options) {
    this.vertx = vertx;
    this.options = options;
    registry = CollectorRegistry.defaultRegistry;
    eventBusMetrics = options.isMetricsTypeDisabled(EVENT_BUS) ? Optional.empty()
      : Optional.of(new PrometheusEventBusMetrics(registry));
    datagramSocketMetrics = options.isMetricsTypeDisabled(DATAGRAM_SOCKET) ? Optional.empty()
      : Optional.of(new PrometheusDatagramSocketMetrics(registry));
    netClientMetrics = options.isMetricsTypeDisabled(NET_CLIENT) ? Optional.empty()
      : Optional.of(new PrometheusNetClientMetrics(registry));
    netServerMetrics = options.isMetricsTypeDisabled(NET_SERVER) ? Optional.empty()
      : Optional.of(new PrometheusNetServerMetrics(registry));
    httpClientMetrics = options.isMetricsTypeDisabled(HTTP_CLIENT) ? Optional.empty()
      : Optional.of(new PrometheusHttpClientMetrics(registry));
    httpServerMetrics = options.isMetricsTypeDisabled(HTTP_SERVER) ? Optional.empty()
      : Optional.of(new PrometheusHttpServerMetrics(registry));
    poolMetrics = options.isMetricsTypeDisabled(NAMED_POOLS) ? Optional.empty()
      : Optional.of(new PrometheusPoolMetrics(registry));
    verticleMetrics = options.isMetricsTypeDisabled(VERTICLES) ? Optional.empty()
      : Optional.of(new PrometheusVerticleMetrics(registry));
  }

  @Override
  public void eventBusInitialized(EventBus bus) {
    // We don't actually care about the eventbus here, but we assume it's a good point to start the HTTP server
    if (options.hasDedicatedServer()) {
      // Start dedicated server
      Router router = Router.router(vertx);
      router.route(options.getServerEndpoint()).handler(createMetricsHandler());
      vertx.createHttpServer().requestHandler(router::accept).listen(options.getServerPort());
    }
  }

  @Override
  public void verticleDeployed(Verticle verticle) {
    verticleMetrics.ifPresent(vm -> vm.verticleDeployed(verticle));
  }

  @Override
  public void verticleUndeployed(Verticle verticle) {
    verticleMetrics.ifPresent(vm -> vm.verticleUndeployed(verticle));
  }

  @Override
  public void timerCreated(long l) {
  }

  @Override
  public void timerEnded(long l, boolean b) {
  }

  @Override
  public EventBusMetrics createMetrics(EventBus eventBus) {
    return eventBusMetrics.orElseGet(() -> super.createMetrics(eventBus));
  }

  @Override
  public HttpServerMetrics<?, ?, ?> createMetrics(HttpServer httpServer, SocketAddress socketAddress, HttpServerOptions httpServerOptions) {
    return httpServerMetrics
      .map(servers -> servers.forAddress(socketAddress))
      .orElseGet(() -> super.createMetrics(httpServer, socketAddress, httpServerOptions));
  }

  @Override
  public HttpClientMetrics<?, ?, ?, ?, ?> createMetrics(HttpClient httpClient, HttpClientOptions httpClientOptions) {
    return httpClientMetrics.orElseGet(() -> super.createMetrics(httpClient, httpClientOptions));
  }

  @Override
  public TCPMetrics<?> createMetrics(SocketAddress socketAddress, NetServerOptions netServerOptions) {
    return netServerMetrics
      .map(servers -> servers.forAddress(socketAddress))
      .orElseGet(() -> super.createMetrics(socketAddress, netServerOptions));
  }

  @Override
  public TCPMetrics<?> createMetrics(NetClientOptions netClientOptions) {
    return netClientMetrics.orElseGet(() -> super.createMetrics(netClientOptions));
  }

  @Override
  public DatagramSocketMetrics createMetrics(DatagramSocket datagramSocket, DatagramSocketOptions datagramSocketOptions) {
    return datagramSocketMetrics.orElseGet(() -> super.createMetrics(datagramSocket, datagramSocketOptions));
  }

  @Override
  public <P> PoolMetrics<?> createMetrics(P pool, String poolType, String poolName, int maxPoolSize) {
    return poolMetrics
      .map(pools -> pools.forInstance(poolType, poolName, maxPoolSize))
      .orElseGet(() -> super.createMetrics(pool, poolType, poolName, maxPoolSize));
  }

  @Override
  public boolean isMetricsEnabled() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
    registry.clear();
  }

  public static Handler<RoutingContext> createMetricsHandler() {
    // Create handler from io.prometheus:simpleclient_vertx
    return new MetricsHandler();
  }
}
