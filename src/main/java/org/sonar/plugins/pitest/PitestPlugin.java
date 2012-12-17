/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009 Alexandre Victoor
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

import static org.sonar.plugins.pitest.PitestConstants.*;

import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

import com.google.common.collect.Lists;

/**
 * This class is the entry point for all PIT extensions
 */
@Properties({
  @Property(key = MODE_KEY, defaultValue = MODE_SKIP,
    name = "PIT activation mode", description = "Possible values:  empty (means skip), 'active' and 'reuseReport'", global = true,
    project = true),
  @Property(key = REPORT_DIRECTORY_KEY, defaultValue = REPORT_DIRECTORY_DEF,
    name = "Output directory for the PIT reports", description = "This property is needed only when the 'reuseReport' mode is activated", global = true,
    project = true),
  @Property(key = CLASSPATH, defaultValue = "",
    name = "PIT classpath", description = "Comma seperated list of classpath entries to use when looking for tests and mutable code. Not useful with maven bith only with the sonar java runner", global = false,
    project = true),
  @Property(key = TARGET_CLASSES, defaultValue = "",
    name = "Target classes", description = "The classes to be mutated. This is expressed as a comma seperated list of globs. For example com.mycompany.* or com.mycompany.package.*, com.mycompany.packageB.Foo, com.partner.*. When maven is used, the default value is 'groupId*'", global = false,
    project = true),
  @Property(key = TARGET_TESTS, defaultValue = "",
    name = "Tests to run", description = "A comma seperated list of globs can be supplied to this parameter to limit the tests available to be run. If not specified, the value of " + TARGET_CLASSES + " is used (not recommended)", global = false,
    project = true),
  @Property(key = EXCLUDED_METHODS, defaultValue = "",
    name = "Methods not to mutate", description = "List of globs to match against method names. Methods matching the globs will be exluded from mutation.", global = false,
    project = true),
  @Property(key = EXCLUDED_CLASSES, defaultValue = "",
    name = "Classes not to mutate or run tests from", description = "List of globs to match against class names. Matching classes will be excluded from mutation. Matching test classes will not be run (note if a suite includes an excluded class, then it will leak back in).", global = false,
    project = true),
  @Property(key = AVOID_CALLS_TO, defaultValue = "(major logging framework APIs)",
    name = "List of packages and classes which are to be considered outside the scope of mutation", description = "", global = true,
    project = true),
  @Property(key = MAX_DEPENDENCY_DISTANCE, defaultValue = "-1",
    name = "Maximum distance to look from test to class. Relevant when mutating static initializers",
    description = "", global = false,
    project = true),
  @Property(key = THREADS, defaultValue = "1",
    name = "Number of threads to use", description = "", global = true,
    project = true),
  @Property(key = MUTATE_STATIC_INITIALIZERS, defaultValue = "false",
    name = "Mutate static initializers", description = "Whether or not to create mutations in static initializers.", global = true,
    project = true),
  @Property(key = MUTATORS, defaultValue = "INVERT_NEGS, RETURN_VALS, MATH, VOID_METHOD_CALLS, NEGATE_CONDITIONALS, CONDITIONALS_BOUNDARY, INCREMENTS",
    name = "Mutation operators to apply", description = "See official PIT documentation for the list of available mutators (http://pitest.org/quickstart/mutators/)", global = true,
    project = true),
  @Property(key = TIMEOUT_FACTOR, defaultValue = "1.25",
    name = "Weighting to allow for timeouts", description = "A factor to apply to the normal runtime of a test when considering if it is stuck in an infinite loop.", global = true,
    project = true),
  @Property(key = TIMEOUT_CONSTANT, defaultValue = "3000",
    name = "Constant amount of additional time to allow for timeouts", description = "Constant amount of additional time to allow a test to run for (after the application of the timeoutFactor) before considering it to be stuck in an infinite loop.", global = true,
    project = true),
  @Property(key = MAX_MUTATIONS_PER_CLASS, defaultValue = "-1",
    name = "Maximum number of mutations to allow per class", description = "The maximum number of mutations to create per class. Use 0 or -ve number to set no limit.", global = true,
    project = true),
  @Property(key = JVM_ARGS, defaultValue = "",
    name = "Arguments to pass to child processes", description = "List of arguments to use when PIT launches child processes. This is most commonly used to increase the amount of memory available to the process, but may be used to pass any valid JVM argument.", global = true,
    project = true),
  @Property(key = FAIL_WHEN_NO_MUTATIONS, defaultValue = "true",
    name = "Throw error if no mutations found", description = "Whether to throw error when no mutations found.", global = true,
    project = true),
  @Property(key = EXCLUDED_TESTNG_GROUPS, defaultValue = "",
    name = "TestNG Groups to exclude", description = "List of TestNG groups to exclude from mutation analysis.", global = true,
    project = true),
  @Property(key = INCLUDED_TESTNG_GROUPS, defaultValue = "",
    name = "TestNG Groups to include", description = "List of TestNG groups to include in mutation analysis.", global = true,
    project = true)
})
public final class PitestPlugin extends SonarPlugin {

  // This is where you're going to declare all your Sonar extensions
  @SuppressWarnings("unchecked")
  public List<Class<? extends Extension>> getExtensions() {
    return Lists.newArrayList(
        ResultParser.class,
        PitestRuleRepository.class,
        PitestSensor.class,
        PitestConfigurationBuilder.class,
        PitestExecutor.class,
        ReportOptionsBuilder.class,
        JarExtractor.class,
        PitestMetrics.class,
        PitestDecorator.class,
        PitestCoverageDecorator.class,
        PitestDashboardWidget.class,
        PitSourceTab.class
    );
  }
}
