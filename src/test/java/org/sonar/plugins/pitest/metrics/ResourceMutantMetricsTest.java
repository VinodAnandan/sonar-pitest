package org.sonar.plugins.pitest.metrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.InputFile;

@RunWith(MockitoJUnitRunner.class)
public class ResourceMutantMetricsTest {

    @Mock
    private InputFile resource;
    @InjectMocks
    private ResourceMutantMetrics resourceMutantMetrics;

    @Test
    public void testAddMutant() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutants() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsTotal() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsNoCoverage() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsKilled() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsSurvived() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsMemoryError() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsTimedOut() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsUnknown() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationsDetected() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetMutationCoverage() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetResource() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

}
