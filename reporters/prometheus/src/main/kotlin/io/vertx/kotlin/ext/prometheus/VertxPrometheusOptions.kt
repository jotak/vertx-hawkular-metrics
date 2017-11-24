package io.vertx.kotlin.ext.prometheus

import io.vertx.ext.prometheus.VertxPrometheusOptions
import io.vertx.ext.metrics.collector.MetricsType

/**
 * A function providing a DSL for building [io.vertx.ext.prometheus.VertxPrometheusOptions] objects.
 *
 * Vert.x Prometheus monitoring configuration.
 *
 * @param disabledMetricsTypes 
 * @param enabled 
 * @param metricsBridgeAddress 
 * @param metricsBridgeEnabled 
 * @param prefix 
 * @param schedule 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.prometheus.VertxPrometheusOptions original] using Vert.x codegen.
 */
fun VertxPrometheusOptions(
  disabledMetricsTypes: Iterable<MetricsType>? = null,
  enabled: Boolean? = null,
  metricsBridgeAddress: String? = null,
  metricsBridgeEnabled: Boolean? = null,
  prefix: String? = null,
  schedule: Int? = null): VertxPrometheusOptions = io.vertx.ext.prometheus.VertxPrometheusOptions().apply {

  if (disabledMetricsTypes != null) {
    this.setDisabledMetricsTypes(disabledMetricsTypes.toSet())
  }
  if (enabled != null) {
    this.setEnabled(enabled)
  }
  if (metricsBridgeAddress != null) {
    this.setMetricsBridgeAddress(metricsBridgeAddress)
  }
  if (metricsBridgeEnabled != null) {
    this.setMetricsBridgeEnabled(metricsBridgeEnabled)
  }
  if (prefix != null) {
    this.setPrefix(prefix)
  }
  if (schedule != null) {
    this.setSchedule(schedule)
  }
}

