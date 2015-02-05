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

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.Settings;
import org.sonar.plugins.pitest.metrics.PitestCoverageDecorator;
import org.sonar.plugins.pitest.metrics.PitestDecorator;
import org.sonar.plugins.pitest.metrics.PitestMetrics;
import org.sonar.plugins.pitest.ui.PitSourceTab;
import org.sonar.plugins.pitest.ui.PitestDashboardWidget;

/**
 * This class is the entry point for all PIT extensions. The properties define, which {@link Settings} are configurable
 * for the plugin.
 *
 * @author <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 */
@Properties({
        @Property(key = PitestPlugin.SENSOR_ENABLED,
                name = "PIT Sensor enabled",
                description = "Possible values:  empty or 'skip'",
                type = PropertyType.BOOLEAN,
                defaultValue = "true",
                global = true,
                project = true),
        @Property(key = PitestPlugin.REPORT_DIRECTORY_KEY,
                defaultValue = PitestPlugin.REPORT_DIRECTORY_DEF,
                name = "Output directory for the PIT reports",
                description = "This property is needed when the 'reuseReport' mode is activated and the reports are not located in the default directory (i.e. target/pit-reports)",
                global = true,
                project = true)
})
public final class PitestPlugin extends SonarPlugin {

    public static final String SENSOR_ENABLED = "sonar.pitest.enabled";

    public static final String REPORT_DIRECTORY_KEY = "sonar.pitest.reports.directory";
    public static final String REPORT_DIRECTORY_DEF = "target/pit-reports";

    @SuppressWarnings("rawtypes")
    @Override
    public List getExtensions() {

        return Arrays.asList(ResultParser.class, ReportFinder.class, PitestRulesDefinition.class, PitestSensor.class,
                PitestMetrics.class, PitestDecorator.class, PitestCoverageDecorator.class, PitestDashboardWidget.class,
                PitSourceTab.class);
    }

}
