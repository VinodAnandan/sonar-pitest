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
