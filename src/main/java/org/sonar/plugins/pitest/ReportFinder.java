/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009 Alexandre Victoor
 * dev@sonar.codehaus.org
 * Copyright (C) 2015 Gerald Muecke
 * gerald@moskito.li
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
/*
 * Modifications:
 * gerald: - added javadoc,
 *         - added todo comment
 *         - externalized extensions array
 */
package org.sonar.plugins.pitest;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.sonar.api.BatchExtension;

/**
 * Finder to determine the latest report file in the reports directory.
 *
 * @author <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 * @author <a href="mailto:gerald@moskito.li">Gerald Muecke</a>
 *
 */
public class ReportFinder implements BatchExtension {

    private static final String[] FILE_EXTENSIONS = new String[] {
        "xml"
    };

    public File findReport(final File reportDirectory) {

        if (reportDirectory == null || !reportDirectory.exists() || !reportDirectory.isDirectory()) {
            // TODO null shouldnt be returned
            return null;
        }
        final Collection<File> reports = FileUtils.listFiles(reportDirectory, FILE_EXTENSIONS, true);
        File latestReport = null;
        for (final File report : reports) {
            if (latestReport == null || FileUtils.isFileNewer(report, latestReport)) {
                latestReport = report;
            }
        }
        return latestReport;
    }
}
