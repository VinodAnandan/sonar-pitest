package org.sonar.plugins.pitest.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PitestDashboardWidgetTest {

    @InjectMocks
    private PitestDashboardWidget subject;

    @Test
    public void testGetId() throws Exception {

        assertEquals("pitest", subject.getId());
    }

    @Test
    public void testGetTitle() throws Exception {

        assertEquals("Pitest report", subject.getTitle());
    }

    @Test
    public void testGetTemplatePath() throws Exception {

        final String templatePath = subject.getTemplatePath();
        assertNotNull(templatePath);
        assertNotNull("the template was not found", PitestDashboardWidget.class.getResource(templatePath));
    }

}
