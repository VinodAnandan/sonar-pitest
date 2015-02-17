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
package org.sonar.plugins.pitest.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.plugins.pitest.model.Mutant;

@RunWith(MockitoJUnitRunner.class)
public class ReportsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testReadMutants_fromDirectory_noReport() throws Exception {

        // prepare

        // act
        final Collection<Mutant> mutants = Reports.readMutants(folder.getRoot().toPath());

        // assert
        assertNotNull(mutants);
        assertTrue(mutants.isEmpty());
    }

    @Test
    public void testReadMutants_fromDirectory_withReport() throws Exception {

        // prepare
        fileFromResource("ReportsTest_mutations.xml", "mutations.xml");

        // act
        final Collection<Mutant> mutants = Reports.readMutants(folder.getRoot().toPath());

        // assert
        assertNotNull(mutants);
        assertEquals(3, mutants.size());
    }

    @Test
    public void testReadMutants_fromFile() throws Exception {

        // prepare
        final File file = fileFromResource("ReportsTest_mutations.xml", "mutations.xml");

        // act
        final Collection<Mutant> mutants = Reports.readMutants(file.toPath());

        // assert
        assertNotNull(mutants);
        assertEquals(3, mutants.size());
    }

    private File fileFromResource(final String resourcePath, final String fileName) throws IOException,
            FileNotFoundException {

        final File newFile = folder.newFile(fileName);
        IOUtils.copy(getClass().getResourceAsStream(resourcePath), new FileOutputStream(newFile));
        return newFile;
    }

}
