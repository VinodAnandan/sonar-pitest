/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2018 Vinod Anandan
 * vinod@owasp.org
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

import com.google.common.collect.ImmutableList;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.pitest.scanner.PitestSensor;
import org.sonar.plugins.pitest.scanner.ProjectReport;
import org.sonar.plugins.pitest.scanner.XmlReportFinder;
import org.sonar.plugins.pitest.scanner.XmlReportParser;

import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_REUSE_REPORT;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;

/**
 * This class is the entry point for all PIT extensions
 */
public final class PitestPlugin implements Plugin {

  @Override
  public void define(Context context) {

    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    builder.add(
      PropertyDefinition.builder(MODE_KEY)
        .defaultValue(MODE_REUSE_REPORT)
        .name("PIT activation mode")
        .description("Possible values:  'reuseReport' and 'skip'")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(REPORT_DIRECTORY_KEY)
        .defaultValue(REPORT_DIRECTORY_DEF)
        .name("Output directory for the PIT reports")
        .description("This property is needed when the 'reuseReport' mode is activated and the reports are not " +
          "located in the default directory (i.e. target/pit-reports)")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      PitestComputer.class,
      PitestMetrics.class,
      ProjectReport.class,
      PitestRulesDefinition.class,
      PitestSensor.class,
      XmlReportParser.class,
      XmlReportFinder.class);

    context.addExtensions(builder.build());

  }
}
