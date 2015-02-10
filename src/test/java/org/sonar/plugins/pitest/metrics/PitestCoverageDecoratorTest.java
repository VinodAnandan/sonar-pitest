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
