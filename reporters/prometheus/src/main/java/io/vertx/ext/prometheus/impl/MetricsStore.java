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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * @author Joel Takvorian
 */
class MetricsStore {
  private final CollectorRegistry registry;

  MetricsStore(CollectorRegistry registry) {
    this.registry = registry;
  }

  CollectorRegistry getRegistry() {
    return registry;
  }

  Gauge gauge(String name, String help, String... labels) {
    String fullName = "vertx_" + name;
    return Gauge.build().name(fullName).help(help).labelNames(labels).register(registry);
  }

  Counter counter(String name, String help, String... labels) {
    String fullName = "vertx_" + name;
    return Counter.build().name(fullName).help(help).labelNames(labels).register(registry);
  }

  Histogram histogram(String name, String help, String... labels) {
    String fullName = "vertx_" + name;
    return Histogram.build().name(fullName).help(help).labelNames(labels).register(registry);
  }
}
