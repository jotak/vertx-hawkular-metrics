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
import io.prometheus.client.Gauge;
import io.vertx.core.Verticle;

/**
 * @author Joel Takvorian
 */
class PrometheusVerticleMetrics {
  private final Gauge verticles;

  PrometheusVerticleMetrics(CollectorRegistry registry) {
    verticles = Gauge.build("vertx_verticle", "Number of verticle instances deployed")
      .labelNames("name")
      .register(registry);
  }

  void verticleDeployed(Verticle verticle) {
    verticles.labels(verticle.getClass().getName()).inc();
  }

  void verticleUndeployed(Verticle verticle) {
    verticles.labels(verticle.getClass().getName()).inc();
  }
}
