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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.metrics.collector.MetricsType;
import io.vertx.ext.prometheus.impl.PrometheusVertxMetrics;

import java.util.EnumSet;
import java.util.Set;

/**
 * Vert.x Prometheus monitoring configuration.
 * If no dedicated server is used, you can bind an existing {@link io.vertx.ext.web.Router} with {@link PrometheusVertxMetrics#createMetricsHandler()}<br/>
 * Ex:<br/>
 * {@code myRouter.route("/metrics").handler(PrometheusVertxMetrics.createMetricsHandler());}
 *
 * @author Joel Takvorian
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class VertxPrometheusOptions extends MetricsOptions {

  private static final String DEFAULT_ENDPOINT = "/metrics";

  private Set<MetricsType> disabledMetricsTypes;
  private boolean dedicatedServer;
  private String serverEndpoint;
  private int serverPort;

  public VertxPrometheusOptions() {
    disabledMetricsTypes = EnumSet.noneOf(MetricsType.class);
    serverEndpoint = DEFAULT_ENDPOINT;
  }

  public VertxPrometheusOptions(VertxPrometheusOptions other) {
    super(other);
    disabledMetricsTypes = other.disabledMetricsTypes != null ? EnumSet.copyOf(other.disabledMetricsTypes) : EnumSet.noneOf(MetricsType.class);
    dedicatedServer = other.dedicatedServer;
    serverEndpoint = other.serverEndpoint != null ? other.serverEndpoint : DEFAULT_ENDPOINT;
    serverPort = other.serverPort;
  }

  public VertxPrometheusOptions(JsonObject json) {
    this();
    VertxPrometheusOptionsConverter.fromJson(json, this);
  }

  /**
   * Set whether metrics will be enabled on the Vert.x instance. Metrics are not enabled by default.
   */
  @Override
  public VertxPrometheusOptions setEnabled(boolean enable) {
    super.setEnabled(enable);
    return this;
  }

  /**
   * @return the disabled metrics types.
   */
  public Set<MetricsType> getDisabledMetricsTypes() {
    return disabledMetricsTypes;
  }

  /**
   * Sets metrics types that are disabled.
   *
   * @param disabledMetricsTypes to specify the set of metrics types to be disabled.
   * @return a reference to this, so that the API can be used fluently
   */
  public VertxPrometheusOptions setDisabledMetricsTypes(Set<MetricsType> disabledMetricsTypes) {
    this.disabledMetricsTypes = disabledMetricsTypes;
    return this;
  }

  /**
   * Set metric that will not be registered. Schedulers will check the set {@code disabledMetricsTypes} when
   * registering metrics suppliers
   *
   * @param metricsType the type of metrics
   * @return a reference to this, so that the API can be used fluently
   */
  @GenIgnore
  public VertxPrometheusOptions addDisabledMetricsType(MetricsType metricsType) {
    if (disabledMetricsTypes == null) {
      disabledMetricsTypes = EnumSet.noneOf(MetricsType.class);
    }
    this.disabledMetricsTypes.add(metricsType);
    return this;
  }

  @GenIgnore
  public boolean isMetricsTypeDisabled(MetricsType metricsType) {
    return disabledMetricsTypes != null && disabledMetricsTypes.contains(metricsType);
  }

  public boolean hasDedicatedServer() {
    return dedicatedServer;
  }

  /**
   * A dedicated server will start to expose metrics on default endpoint /metrics
   * @param port HTTP port
   */
  public VertxPrometheusOptions startDedicatedServer(int port) {
    return startDedicatedServer(port, DEFAULT_ENDPOINT);
  }

  /**
   * A dedicated server will start to expose metrics on the provided endpoint
   * @param port HTTP port
   * @param endpoint metrics endpoint
   */
  public VertxPrometheusOptions startDedicatedServer(int port, String endpoint) {
    this.dedicatedServer = true;
    this.serverPort = port;
    this.serverEndpoint = endpoint;
    return this;
  }

  public String getServerEndpoint() {
    return serverEndpoint;
  }

  public int getServerPort() {
    return serverPort;
  }
}
