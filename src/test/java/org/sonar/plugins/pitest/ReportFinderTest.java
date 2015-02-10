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
package org.sonar.plugins.pitest;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.test.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ReportFinderTest {

    @InjectMocks
    private ReportFinder subject;

    // TODO use testrule for folder

    @Test
    public void testFindReport_existingReport() throws IOException {

        // prepare
        final Path xmlFile = TestUtils.getResource("mutations.xml").toPath();
        final Path directory = xmlFile.getParent();

        // act
        final Path report = subject.findReport(directory);

        // assert
        assertEquals(xmlFile, report);
    }

    @Test
    public void testFindReport_noReportInDirectory() throws IOException {

        final Path directory = TestUtils.getResource("fake_libs").toPath();

        // act
        final Path report = subject.findReport(directory);

        // assert
        assertThat(report).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindReport_nullPath_exception() throws IOException {

        subject.findReport(null);

    }
}
