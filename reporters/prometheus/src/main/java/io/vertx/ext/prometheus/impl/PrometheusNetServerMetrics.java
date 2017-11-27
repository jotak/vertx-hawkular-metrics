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
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.TCPMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusNetServerMetrics {
  private final Gauge connections;
  private final Histogram bytesReceived;
  private final Histogram bytesSent;
  private final Counter errorCount;

  PrometheusNetServerMetrics(CollectorRegistry registry) {
    connections = Gauge.build("vertx_net_server_connections", "Number of opened connections to the net server")
      .labelNames("local")
      .register(registry);
    bytesReceived = Histogram.build("vertx_net_server_bytes_received", "Number of bytes received by the net server")
      .labelNames("local")
      .register(registry);
    bytesSent = Histogram.build("vertx_net_server_bytes_sent", "Number of bytes sent by the net server")
      .labelNames("local")
      .register(registry);
    errorCount = Counter.build("vertx_net_server_error_count", "Number of errors")
      .labelNames("local")
      .register(registry);
  }

  TCPMetrics forAddress(SocketAddress localAddress) {
    String local = Util.addressToString(localAddress);
    return new Instance(local);
  }

  class Instance implements TCPMetrics<Void> {
    private final String local;

    Instance(String local) {
      this.local = local;
    }

    @Override
    public Void connected(SocketAddress remoteAddress, String remoteName) {
      connections.labels(local).inc();
      return null;
    }

    @Override
    public void disconnected(Void key, SocketAddress remoteAddress) {
      connections.labels(local).dec();
    }

    @Override
    public void bytesRead(Void key, SocketAddress remoteAddress, long numberOfBytes) {
      bytesReceived.labels(local).observe(numberOfBytes);
    }

    @Override
    public void bytesWritten(Void key, SocketAddress remoteAddress, long numberOfBytes) {
      bytesSent.labels(local).observe(numberOfBytes);
    }

    @Override
    public void exceptionOccurred(Void key, SocketAddress remoteAddress, Throwable t) {
      errorCount.labels(local).inc();
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public void close() {
    }
  }
}
