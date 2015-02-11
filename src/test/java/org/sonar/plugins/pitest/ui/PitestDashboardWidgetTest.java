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
