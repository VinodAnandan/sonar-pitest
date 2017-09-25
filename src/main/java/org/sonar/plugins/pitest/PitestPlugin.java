/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
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

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import static org.sonar.plugins.pitest.PitestConstants.*;

/**
 * This class is the entry point for all PIT extensions
 */
public final class PitestPlugin implements Plugin {

  @Override
  public void define(Context context) {

    context.addExtensions(
      PropertyDefinition.builder(MODE_KEY)
        .defaultValue(MODE_SKIP)
        .name("PIT activation mode")
        .description("Possible values:  empty (means skip) and 'reuseReport'")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      PropertyDefinition.builder(REPORT_DIRECTORY_KEY)
        .defaultValue(REPORT_DIRECTORY_DEF)
        .name("Output directory for the PIT reports")
        .description("This property is needed when the 'reuseReport' mode is activated and the reports are not " +
          "located in the default directory (i.e. target/pit-reports)")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      XmlReportParser.class,
      XmlReportFinder.class,
      PitestRulesDefinition.class,
      PitestSensor.class,
      PitestMetrics.class,
      PitestComputer.class,
      PitestCoverageComputer.class
    );
  }
}
