package org.sonar.plugins.pitest.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.sonar.api.measures.Metric;

public class PitestMetricsTest {

    @Test
    public void testGetMetrics() throws Exception {

        final List<Metric> metrics = new PitestMetrics<Serializable>().getMetrics();
        assertNotNull(metrics);
        assertFalse(metrics.isEmpty());
    }

    @Test
    public void testGetQuantitativeMetrics() throws Exception {

        final List<Metric> metrics = PitestMetrics.getQuantitativeMetrics();
        assertNotNull(metrics);
        assertFalse(metrics.isEmpty());
    }

    @Test
    public void testGetSensorMetrics() throws Exception {

        final List<Metric> metrics = PitestMetrics.getSensorMetrics();
        assertNotNull(metrics);
        assertFalse(metrics.isEmpty());
    }

}
