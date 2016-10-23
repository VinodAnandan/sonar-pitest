/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 Alexandre Victoor
 * alexvictoor@gmail.com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

/**
 * This class is the entry point for all PIT extensions
 */
@Properties({
  @Property(key = MODE_KEY, defaultValue = MODE_SKIP,
    name = "PIT activation mode", description = "Possible values:  empty (means skip) and 'reuseReport'", global = true,
    project = true),
  @Property(key = REPORT_DIRECTORY_KEY, defaultValue = REPORT_DIRECTORY_DEF,
    name = "Output directory for the PIT reports", description = "This property is needed when the 'reuseReport' mode is activated and the reports are not located in the default directory (i.e. target/pit-reports)", global = true,
    project = true)
})
public final class PitestPlugin extends SonarPlugin {

  // This is where you're going to declare all your Sonar extensions
  @SuppressWarnings("unchecked")
  public List<Class<?>> getExtensions() {
    return Arrays.asList(
        XmlReportParser.class,
        XmlReportFinder.class,
        PitestRulesDefinition.class,
        PitestSensor.class,
        PitestMetrics.class,
        PitestComputer.class,
        PitestCoverageComputer.class,
        PitestDashboardWidget.class,
        PitSourceTab.class
    );
  }
}
