package org.sonar.plugins.pitest.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantStatus;

@RunWith(MockitoJUnitRunner.class)
public class ResourceMutantMetricsTest {

    @Mock
    private InputFile resource;

    @Mock
    private Mutant mutant;

    @InjectMocks
    private ResourceMutantMetrics subject;

    @Before
    public void setUp() throws Exception {

        when(mutant.getMutantStatus()).thenReturn(MutantStatus.UNKNOWN);

    }

    @Test
    public void testAddMutant() throws Exception {

        // act
        subject.addMutant(mutant);
        subject.addMutant(mutant);
        subject.addMutant(mutant);

        // assert
        final Collection<Mutant> mutants = subject.getMutants();
        assertNotNull(mutants);
        assertEquals(3, mutants.size());

    }

    @Test
    public void testGetMutationsTotal() throws Exception {

        // prepare
        subject.addMutant(mutant);
        subject.addMutant(mutant);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsTotal();

        // assert
        assertEquals(3, value);
    }

    @Test
    public void testGetMutationsNoCoverage() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.NO_COVERAGE);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsNoCoverage();

        // assert
        assertEquals(1, value);
    }

    @Test
    public void testGetMutationsKilled() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.KILLED);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsKilled();

        // assert
        assertEquals(1, value);
    }

    @Test
    public void testGetMutationsSurvived() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.SURVIVED);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsSurvived();

        // assert
        assertEquals(1, value);
    }

    @Test
    public void testGetMutationsMemoryError() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.MEMORY_ERROR);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsMemoryError();

        // assert
        assertEquals(1, value);
    }

    @Test
    public void testGetMutationsTimedOut() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.TIMED_OUT);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsTimedOut();

        // assert
        assertEquals(1, value);
    }

    @Test
    public void testGetMutationsUnknown() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.UNKNOWN);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsUnknown();

        // assert
        assertEquals(1, value);
    }

    @Test
    public void testGetMutationsDetected() throws Exception {

        // prepare
        when(mutant.isDetected()).thenReturn(true);
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.KILLED);
        subject.addMutant(mutant);
        when(mutant.isDetected()).thenReturn(true);
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.SURVIVED);
        subject.addMutant(mutant);
        when(mutant.isDetected()).thenReturn(false);
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.NO_COVERAGE);
        subject.addMutant(mutant);

        // act
        final int value = subject.getMutationsDetected();

        // assert
        assertEquals(2, value);
    }

    @Test
    public void testGetMutationCoverage() throws Exception {

        // prepare
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.KILLED);
        subject.addMutant(mutant);
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.SURVIVED);
        subject.addMutant(mutant);
        when(mutant.getMutantStatus()).thenReturn(MutantStatus.NO_COVERAGE);
        subject.addMutant(mutant);

        // act
        final double value = subject.getMutationCoverage();

        // assert
        assertEquals(100.0 / 3.0, value, 0.000001);
    }

    @Test
    public void testGetResource() throws Exception {

        assertEquals(resource, subject.getResource());
    }

}
