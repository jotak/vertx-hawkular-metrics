package io.vertx.kotlin.ext.prometheus

import io.vertx.ext.prometheus.VertxPrometheusOptions
import io.vertx.ext.metrics.collector.MetricsType

/**
 * A function providing a DSL for building [io.vertx.ext.prometheus.VertxPrometheusOptions] objects.
 *
 * Vert.x Prometheus monitoring configuration.
 * If no dedicated server is used, you can bind an existing [io.vertx.ext.web.Router] with <br/>
 * Ex:<br/>
 * <code>myRouter.route("/metrics").handler(PrometheusVertxMetrics.createMetricsHandler());</code>
 *
 * @param disabledMetricsTypes  Sets metrics types that are disabled.
 * @param enabled  Set whether metrics will be enabled on the Vert.x instance. Metrics are not enabled by default.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.prometheus.VertxPrometheusOptions original] using Vert.x codegen.
 */
fun VertxPrometheusOptions(
  disabledMetricsTypes: Iterable<MetricsType>? = null,
  enabled: Boolean? = null): VertxPrometheusOptions = io.vertx.ext.prometheus.VertxPrometheusOptions().apply {

  if (disabledMetricsTypes != null) {
    this.setDisabledMetricsTypes(disabledMetricsTypes.toSet())
  }
  if (enabled != null) {
    this.setEnabled(enabled)
  }
}

