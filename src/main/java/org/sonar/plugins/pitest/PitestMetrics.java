/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2018 Vinod Anandan
 * vinod@owasp.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

/**
 * Metrics for the sonar pitest plugin.
 * 
 */
public class PitestMetrics implements Metrics {

  public static final String PITEST_DOMAIN = "Mutation analysis";

  public static final String MUTATIONS_NOT_COVERED_KEY = "pitest_mutations_noCoverage";
  public static final Metric<Serializable> MUTATIONS_NOT_COVERED = new Metric.Builder(MUTATIONS_NOT_COVERED_KEY, "Non Covered Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations not covered by any test.")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_GENERATED_KEY = "pitest_mutations_total";
  public static final Metric<Serializable> MUTATIONS_GENERATED = new Metric.Builder(MUTATIONS_GENERATED_KEY, "Total Mutations", Metric.ValueType.INT)
    .setDescription("Total number of mutations generated")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_KILLED_KEY = "pitest_mutations_killed";
  public static final Metric<Serializable> MUTATIONS_KILLED = new Metric.Builder(MUTATIONS_KILLED_KEY, "Killed Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations killed by a test.")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_SURVIVED_KEY = "pitest_mutations_survived";
  public static final Metric<Serializable> MUTATIONS_SURVIVED = new Metric.Builder(MUTATIONS_SURVIVED_KEY, "Survived Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations survived")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_ERROR_KEY = "pitest_mutations_error";
  public static final Metric<Serializable> MUTATIONS_ERROR = new Metric.Builder(MUTATIONS_ERROR_KEY, "Error Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations that caused an error")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_UNKNOWN_KEY = "pitest_mutations_unknown";
  public static final Metric<Serializable> MUTATIONS_UNKNOWN = new Metric.Builder(MUTATIONS_UNKNOWN_KEY, "Mutations with unknown status", Metric.ValueType.INT)
    .setDescription("Number of mutations for which status is unknown")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_DATA_KEY = "pitest_mutations_data"; // needed?
  public static final Metric<Serializable> MUTATIONS_DATA = new Metric.Builder(MUTATIONS_DATA_KEY, "Mutations Data", Metric.ValueType.DATA)
    .setDescription("Mutations Data")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(true)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final String MUTATIONS_KILLED_PERCENT_KEY = "pitest_mutations_killed_percent";
  public static final Metric<Serializable> MUTATIONS_KILLED_RATIO = new Metric.Builder(MUTATIONS_KILLED_PERCENT_KEY, "Mutations Coverage Ratio", Metric.ValueType.PERCENT)
    .setDescription("Ratio of mutations found by tests")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(PITEST_DOMAIN)
    .setBestValue(100d)
    .setWorstValue(0d)
    .create();

  private static final List<Metric> METRICS;

  static {
    METRICS = new LinkedList<>();
    METRICS.add(MUTATIONS_NOT_COVERED);
    METRICS.add(MUTATIONS_GENERATED);
    METRICS.add(MUTATIONS_KILLED);
    METRICS.add(MUTATIONS_SURVIVED);
    METRICS.add(MUTATIONS_ERROR);
    METRICS.add(MUTATIONS_UNKNOWN);
    METRICS.add(MUTATIONS_DATA);
    METRICS.add(MUTATIONS_KILLED_RATIO);
  }

  @Override
  public List<Metric> getMetrics() {
    return METRICS;
  }

  public static Metric getMetric(final String key) {
    return METRICS.stream().filter(metric -> metric != null && metric.getKey().equals(key)).findFirst().orElseThrow(NoSuchElementException::new);
  }
}
