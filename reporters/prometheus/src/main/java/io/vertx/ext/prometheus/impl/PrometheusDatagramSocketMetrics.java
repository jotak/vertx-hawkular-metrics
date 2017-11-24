/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.ext.prometheus.impl;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusDatagramSocketMetrics implements DatagramSocketMetrics {

  private final Histogram bytesReceived;
  private final Histogram bytesSent;
  private final Counter errorCount;

  private volatile String localAddress;

  PrometheusDatagramSocketMetrics(MetricsStore store) {
    bytesReceived = store.histogram("datagram_bytes_received", "Total number of datagram bytes received",
      "local", "remote");
    bytesSent = store.histogram("datagram_bytes_sent", "Total number of datagram bytes sent",
      "remote");
    errorCount = store.counter("datagram_error_count", "Total number of datagram errors");
  }

  @Override
  public void listening(String localName, SocketAddress localAddress) {
    this.localAddress = Util.addressToString(new SocketAddressImpl(localAddress.port(), localName));
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (localAddress != null) {
      bytesReceived.labels(localAddress, Util.addressToString(remoteAddress))
        .observe(numberOfBytes);
    }
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesSent.labels(Util.addressToString(remoteAddress))
      .observe(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    errorCount.inc();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
  }
}
