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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

@RunWith(MockitoJUnitRunner.class)
public class PitestCoverageDecoratorTest {

    @Mock
    private Project project;
    @Mock
    private Measure<Integer> totalMutants;
    @Mock
    private Measure<Integer> detectedMutants;

    @Mock
    private DecoratorContext decoratorContext;
    @Mock
    private Resource resource;

    @InjectMocks
    private PitestCoverageDecorator subject;

    @Test
    public void testShouldExecuteOnProject() throws Exception {

        assertTrue(subject.shouldExecuteOnProject(project));
    }

    @Test
    public void testGetCoverageMetric() throws Exception {

        assertEquals(PitestMetrics.MUTATIONS_COVERAGE, subject.getCoverageMetric());
    }

    @Test
    public void testGetBaseMetrics() throws Exception {

        // act
        final List<Metric<Serializable>> baseMetrics = subject.getBaseMetrics();
        // assert
        assertNotNull(baseMetrics);
        assertTrue(baseMetrics.contains(PitestMetrics.MUTATIONS_DETECTED));
        assertTrue(baseMetrics.contains(PitestMetrics.MUTATIONS_TOTAL));

    }

    @Test
    public void testDecorate_noMutatedElements() throws Exception {

        // prepare
        when(totalMutants.getValue()).thenReturn(Double.valueOf(0.0));
        when(detectedMutants.getValue()).thenReturn(Double.valueOf(0.0));
        when(decoratorContext.getMeasure(PitestMetrics.MUTATIONS_TOTAL)).thenReturn(totalMutants);
        when(decoratorContext.getMeasure(PitestMetrics.MUTATIONS_DETECTED)).thenReturn(detectedMutants);

        // act
        subject.decorate(any(Resource.class), decoratorContext);
        // assert

        verify(decoratorContext, times(0)).saveMeasure(any(Metric.class), any(Double.class));

    }

    @Test
    public void testDecorate_withMutatedElements() throws Exception {

        // prepare
        when(totalMutants.getValue()).thenReturn(Double.valueOf(10.0));
        when(detectedMutants.getValue()).thenReturn(Double.valueOf(5.0));
        when(decoratorContext.getMeasure(PitestMetrics.MUTATIONS_TOTAL)).thenReturn(totalMutants);
        when(decoratorContext.getMeasure(PitestMetrics.MUTATIONS_DETECTED)).thenReturn(detectedMutants);

        // act
        subject.decorate(any(Resource.class), decoratorContext);
        // assert

        final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
        verify(decoratorContext).saveMeasure(metricsCaptor.capture(), captor.capture());
        assertEquals(PitestMetrics.MUTATIONS_COVERAGE, metricsCaptor.getValue());
        assertEquals(Double.valueOf(50.0), captor.getValue());

    }
}
