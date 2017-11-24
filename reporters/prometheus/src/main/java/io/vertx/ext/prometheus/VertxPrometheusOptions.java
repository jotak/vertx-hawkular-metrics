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
package io.vertx.ext.prometheus;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.metrics.collector.MetricsOptionsBase;

/**
 * Vert.x Prometheus monitoring configuration.
 *
 * @author Joel Takvorian
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class VertxPrometheusOptions extends MetricsOptionsBase {

  public VertxPrometheusOptions() {
  }

  public VertxPrometheusOptions(VertxPrometheusOptions other) {
    super(other);
  }

  public VertxPrometheusOptions(JsonObject json) {
    this();
    VertxPrometheusOptionsConverter.fromJson(json, this);
  }
}
