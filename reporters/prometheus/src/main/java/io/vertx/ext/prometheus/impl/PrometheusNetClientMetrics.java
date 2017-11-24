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
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.TCPMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusNetClientMetrics implements TCPMetrics<String> {
  private final Gauge connections;
  private final Histogram bytesReceived;
  private final Histogram bytesSent;
  private final Counter errorCount;

  PrometheusNetClientMetrics(MetricsStore store) {
    connections = store.gauge("net_client_connections", "Number of connections to the remote host currently opened",
      "remote");
    bytesReceived = store.histogram("net_client_bytes_received", "Number of bytes received from the remote host",
      "remote");
    bytesSent = store.histogram("net_client_bytes_sent", "Number of bytes sent to the remote host",
      "remote");
    errorCount = store.counter("net_client_error_count", "Number of errors",
      "remote");
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
}
