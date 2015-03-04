/*
 * Sonar Pitest Plugin
 * Copyright (C) 2015 SonarCommunity
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pitest.metrics;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metrics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.sonar.api.measures.Metric.DIRECTION_BETTER;
import static org.sonar.api.measures.Metric.DIRECTION_NONE;
import static org.sonar.api.measures.Metric.DIRECTION_WORST;
import static org.sonar.api.measures.Metric.ValueType.DATA;
import static org.sonar.api.measures.Metric.ValueType.INT;
import static org.sonar.api.measures.Metric.ValueType.PERCENT;

/**
 * Metrics for the sonar pitest plugin.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
public class PitestMetrics<T extends Serializable> implements Metrics {

    public static final String MUTATIONS_DATA_KEY = "pitest_mutations_data";
    public static final String MUTATIONS_TOTAL_KEY = "pitest_mutations_total";
    public static final String MUTATIONS_DETECTED_KEY = "pitest_mutations_detected";
    public static final String MUTATIONS_NO_COVERAGE_KEY = "pitest_mutations_noCoverage";
    public static final String MUTATIONS_KILLED_KEY = "pitest_mutations_killed";
    public static final String MUTATIONS_SURVIVED_KEY = "pitest_mutations_survived";
    public static final String MUTATIONS_MEMORY_ERROR_KEY = "pitest_mutations_memoryError";
    public static final String MUTATIONS_TIMED_OUT_KEY = "pitest_mutations_timedOut";
    public static final String MUTATIONS_UNKNOWN_KEY = "pitest_mutations_unknown";
    public static final String MUTATIONS_COVERAGE_KEY = "pitest_mutations_coverage";

    @SuppressWarnings("rawtypes") private static final List<Metric> METRICS;
    @SuppressWarnings("rawtypes") private static final List<Metric> QUANTITATIVE_METRICS;

    public static final String PITEST_DOMAIN = "Mutation analysis";

    public static final Metric<Serializable> MUTATIONS_DATA = new Builder(MUTATIONS_DATA_KEY, "Mutations Data", DATA)
            .setDomain(PITEST_DOMAIN).setDirection(DIRECTION_NONE).setQualitative(true).create();

    public static final Metric<Serializable> MUTATIONS_TOTAL = new Builder(MUTATIONS_TOTAL_KEY, "Total Mutations", INT)
            .setDomain(PITEST_DOMAIN).setDescription("Total number of mutations generated")
            .setDirection(DIRECTION_BETTER).create();

    public static final Metric<Serializable> MUTATIONS_DETECTED = new Builder(MUTATIONS_DETECTED_KEY,
            "Detected Mutations", INT).setDomain(PITEST_DOMAIN).setDescription("Total number of mutations detected")
            .setDirection(DIRECTION_BETTER).create();

    public static final Metric<Serializable> MUTATIONS_NO_COVERAGE = new Builder(MUTATIONS_NO_COVERAGE_KEY,
            "Non Covered Mutations", INT).setDomain(PITEST_DOMAIN)
            .setDescription("Number of mutations non covered by any test.").setDirection(DIRECTION_WORST).create();

    public static final Metric<Serializable> MUTATIONS_KILLED = new Builder(MUTATIONS_KILLED_KEY, "Killed Mutations",
            INT).setDomain(PITEST_DOMAIN).setDescription("Number of mutations killed by tests")
            .setDirection(DIRECTION_BETTER).create();

    public static final Metric<Serializable> MUTATIONS_SURVIVED = new Builder(MUTATIONS_SURVIVED_KEY,
            "Survived Mutations", INT).setDomain(PITEST_DOMAIN).setDescription("Number of mutations survived.")
            .setDirection(Metric.DIRECTION_WORST).create();

    public static final Metric<Serializable> MUTATIONS_MEMORY_ERROR = new Builder(MUTATIONS_MEMORY_ERROR_KEY,
            "Memory Error Mutations", INT).setDomain(PITEST_DOMAIN)
            .setDescription("Number of mutations detected by memory errors.").setDirection(DIRECTION_BETTER).create();

    public static final Metric<Serializable> MUTATIONS_TIMED_OUT = new Builder(MUTATIONS_TIMED_OUT_KEY,
            "Timed Out Mutations", INT).setDomain(PITEST_DOMAIN)
            .setDescription("Number of mutations detected by time outs.").setDirection(DIRECTION_BETTER).create();

    public static final Metric<Serializable> MUTATIONS_UNKNOWN = new Builder(MUTATIONS_UNKNOWN_KEY,
            "Unknown Status Mutations", INT).setDomain(PITEST_DOMAIN)
            .setDescription("Number of mutations with unknown status.").setDirection(DIRECTION_WORST).create();

    public static final Metric<Serializable> MUTATIONS_COVERAGE = new Builder(MUTATIONS_COVERAGE_KEY,
            "Mutations Coverage", PERCENT).setDomain(PITEST_DOMAIN).setDescription("Mutations coverage percentage")
            .setDirection(DIRECTION_BETTER).setQualitative(true).setBestValue(100d).setWorstValue(0d).create();

    static {
        //@formatter:off
        @SuppressWarnings("rawtypes")
        final List<Metric> metrics = Arrays.<Metric> asList(
                MUTATIONS_DATA,
                MUTATIONS_TOTAL,
                MUTATIONS_DETECTED,
                MUTATIONS_NO_COVERAGE,
                MUTATIONS_KILLED,
                MUTATIONS_SURVIVED,
                MUTATIONS_MEMORY_ERROR,
                MUTATIONS_TIMED_OUT,
                MUTATIONS_UNKNOWN,
                MUTATIONS_COVERAGE);

        @SuppressWarnings("rawtypes")
        final List<Metric> quantitativeMetric = new ArrayList<>();
        for(@SuppressWarnings("rawtypes") final Metric m : metrics){
            if(!m.getQualitative()) {
                quantitativeMetric.add(m);
            }
        }

        // @formatter:on
        METRICS = Collections.unmodifiableList(metrics);
        QUANTITATIVE_METRICS = Collections.unmodifiableList(quantitativeMetric);

    }

    /**
     * @see Metrics#getMetrics()
     */
    @SuppressWarnings("rawtypes") @Override public List<Metric> getMetrics() {

        return METRICS;
    }

    /**
     * Returns the pitest quantitative metrics list.
     *
     * @return {@link List<Metric>} The pitest quantitative metrics list.
     */
    @SuppressWarnings("rawtypes") public static List<Metric> getQuantitativeMetrics() {

        return QUANTITATIVE_METRICS;
    }

    /**
     * Returns the all metrics the pitest sensor provides .
     *
     * @return {@link List<Metric>} The pitest sensor metrics list.
     */
    @SuppressWarnings("rawtypes") public static List<Metric> getSensorMetrics() {

        return METRICS;
    }
}
