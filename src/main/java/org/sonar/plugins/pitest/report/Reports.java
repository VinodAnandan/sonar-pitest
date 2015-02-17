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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.pitest.model.Mutant;

public final class Reports {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(Reports.class);

    private Reports() {

    }

    public static Collection<Mutant> readMutants(final Path reportsDirectory) throws IOException {

        LOG.debug("Searching pit reports in {}", reportsDirectory);

        final Path xmlReport;
        if (Files.isDirectory(reportsDirectory)) {
            xmlReport = new ReportFinder().findReport(reportsDirectory);
        } else {
            xmlReport = reportsDirectory;
        }

        if (xmlReport == null) {
            LOG.warn("No XML PIT report found in directory {} !", reportsDirectory);
            LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
            return Collections.emptyList();
        }

        return new PitestReportParser().parseMutants(xmlReport);
    }
}
