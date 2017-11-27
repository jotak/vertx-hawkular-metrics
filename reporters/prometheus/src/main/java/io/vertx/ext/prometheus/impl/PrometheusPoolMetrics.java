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
import io.vertx.core.spi.metrics.PoolMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusPoolMetrics {
  private final Histogram queueDelay;
  private final Gauge queueSize;
  private final Histogram usage;
  private final Gauge inUse;
  private final Gauge usageRatio;
  private final Counter completed;

  PrometheusPoolMetrics(CollectorRegistry registry) {
    queueDelay = Histogram.build("vertx_pool_queue_delay", "Queue time for a resource")
      .labelNames("pool_type", "pool_name")
      .register(registry);
    queueSize = Gauge.build("vertx_pool_queue_size", "Number of elements waiting for a resource")
      .labelNames("pool_type", "pool_name")
      .register(registry);
    usage = Histogram.build("vertx_pool_usage", "Time using a resource")
      .labelNames("pool_type", "pool_name")
      .register(registry);
    inUse = Gauge.build("vertx_pool_in_use", "Number of resources used")
      .labelNames("pool_type", "pool_name")
      .register(registry);
    usageRatio = Gauge.build("vertx_pool_ratio", "Pool usage ratio, only present if maximum pool size could be determined")
      .labelNames("pool_type", "pool_name", "max_pool_size")
      .register(registry);
    completed = Counter.build("vertx_pool_completed", "Number of elements done with the resource")
      .labelNames("pool_type", "pool_name")
      .register(registry);
  }

  PoolMetrics forInstance(String poolType, String poolName, int maxPoolSize) {
    return new Instance(poolType, poolName, maxPoolSize);
  }

  class Instance implements PoolMetrics<Histogram.Timer> {
    private final String poolType;
    private final String poolName;
    private final int maxPoolSize;

    Instance(String poolType, String poolName, int maxPoolSize) {
      this.poolType = poolType;
      this.poolName = poolName;
      this.maxPoolSize = maxPoolSize;
    }

    @Override
    public Histogram.Timer submitted() {
      queueSize.labels(poolType, poolName).inc();
      return queueDelay.labels(poolType, poolName).startTimer();
    }

    @Override
    public void rejected(Histogram.Timer submitted) {
      queueSize.labels(poolType, poolName).dec();
      submitted.observeDuration();
    }

    @Override
    public Histogram.Timer begin(Histogram.Timer submitted) {
      queueSize.labels(poolType, poolName).dec();
      submitted.observeDuration();
      inUse.labels(poolType, poolName).inc();
      checkRatio();
      return usage.labels(poolType, poolName).startTimer();
    }

    @Override
    public void end(Histogram.Timer begin, boolean succeeded) {
      inUse.labels(poolType, poolName).dec();
      checkRatio();
      begin.observeDuration();
      completed.labels(poolType, poolName).inc();
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public void close() {
    }

    private void checkRatio() {
      if (maxPoolSize > 0) {
        usageRatio.labels(poolType, poolName, String.valueOf(maxPoolSize))
          .set(inUse.labels(poolType, poolName).get() / maxPoolSize);
      }
    }
  }
}
