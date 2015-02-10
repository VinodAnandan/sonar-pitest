package org.sonar.plugins.pitest.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.measures.Metric;

@RunWith(MockitoJUnitRunner.class)
public class PitestDecoratorTest {

    @InjectMocks
    private PitestDecorator subject;

    @Test
    public void testShouldSaveZeroIfNoChildMeasures() throws Exception {

        assertFalse(subject.shouldSaveZeroIfNoChildMeasures());
    }

    @Test
    public void testGeneratesMetrics() throws Exception {

        final List<Metric> metrics = subject.generatesMetrics();

        assertNotNull(metrics);
        assertFalse(metrics.isEmpty());
    }

}
