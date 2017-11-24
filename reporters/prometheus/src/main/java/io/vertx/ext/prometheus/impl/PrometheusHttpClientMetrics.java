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

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpClientMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusHttpClientMetrics implements HttpClientMetrics<PrometheusHttpClientMetrics.Handler, String, String, Void, Void> {
  private final Gauge connections;
  private final Histogram bytesReceived;
  private final Histogram bytesSent;
  private final Counter errorCount;
  private final Gauge requests;
  private final Counter requestCount;
  private final Histogram responseTime;
  private final Gauge wsConnections;

  PrometheusHttpClientMetrics(MetricsStore store) {
    connections = store.gauge("http_client_connections", "Number of connections to the remote host currently opened",
      "remote");
    bytesReceived = store.histogram("http_client_bytes_received", "Number of bytes received from the remote host",
      "remote");
    bytesSent = store.histogram("http_client_bytes_sent", "Number of bytes sent to the remote host",
      "remote");
    errorCount = store.counter("http_client_error_count", "Number of errors",
      "remote");
    requests = store.gauge("http_client_requests", "Number of requests waiting for a response",
      "remote");
    requestCount = store.counter("http_client_request_count", "Number of requests sent",
      "remote");
    responseTime = store.histogram("http_client_reponse_time", "Response time",
      "remote");
    wsConnections = store.gauge("http_client_ws_connections", "Number of websockets currently opened",
      "remote");
  }

  @Override
  public Void createEndpoint(String host, int port, int maxPoolSize) {
    return null;
  }

  @Override
  public void closeEndpoint(String host, int port, Void endpointMetric) {
  }

  @Override
  public Void enqueueRequest(Void endpointMetric) {
    return null;
  }

  @Override
  public void dequeueRequest(Void endpointMetric, Void taskMetric) {
  }

  @Override
  public void endpointConnected(Void endpointMetric, String socketMetric) {
  }

  @Override
  public void endpointDisconnected(Void endpointMetric, String socketMetric) {
  }

  @Override
  public void requestEnd(Handler requestMetric) {
  }

  @Override
  public void responseBegin(Handler requestMetric, HttpClientResponse response) {
  }

  @Override
  public Handler requestBegin(Void endpointMetric, String key, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    requests.labels(key).inc();
    requestCount.labels(key).inc();
    Handler handler = new Handler(key);
    handler.timer = responseTime.labels(key).startTimer();
    return handler;
  }

  @Override
  public Handler responsePushed(Void endpointMetric, String key, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    return requestBegin(null, key, localAddress, remoteAddress, request);
  }

  @Override
  public void requestReset(Handler handler) {
    requests.labels(handler.address).dec();
  }

  @Override
  public void responseEnd(Handler handler, HttpClientResponse response) {
    requests.labels(handler.address).dec();
    handler.timer.observeDuration();
  }

  @Override
  public String connected(Void endpointMetric, String key, WebSocket webSocket) {
    wsConnections.labels(key).inc();
    return key;
  }

  @Override
  public void disconnected(String key) {
    wsConnections.labels(key).dec();
  }

  @Override
  public String connected(SocketAddress remoteAddress, String remoteName) {
    String key = Util.addressToString(new SocketAddressImpl(remoteAddress.port(), remoteName));
    connections.labels(key).inc();
    return key;
  }

  @Override
  public void disconnected(String key, SocketAddress remoteAddress) {
    connections.labels(key).dec();
  }

  @Override
  public void bytesRead(String key, SocketAddress remoteAddress, long numberOfBytes) {
    bytesReceived.labels(key).observe(numberOfBytes);
  }

  @Override
  public void bytesWritten(String key, SocketAddress remoteAddress, long numberOfBytes) {
    bytesSent.labels(key).observe(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(String key, SocketAddress remoteAddress, Throwable t) {
    errorCount.labels(key).inc();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
  }

  public static class Handler {
    private final String address;
    private Histogram.Timer timer;

    Handler(String address) {
      this.address = address;
    }
  }
}
