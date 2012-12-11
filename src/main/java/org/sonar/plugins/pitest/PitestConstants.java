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

/**
 * Constants for the PIT plugins
 * There is a constant for each configuration key.
 * Most of thoses configuration keys, and the javadoc comments are
 * strongly inspired by the maven PIT plugin
 *
 * @author Alexandre Victoor
 */
public final class PitestConstants {

  private PitestConstants() {

  }

  public static final String PITEST_JAR_NAME = "pitest-0.29.jar";

  public static final String REPOSITORY_KEY = "pitest";
  public static final String REPOSITORY_NAME = "Pitest";

  public static final String RULE_KEY = "pitest.survived.mutant";

  public static final String MODE_KEY = "sonar.pitest.mode";

  public static final String MODE_ACTIVE = "active";

  public static final String MODE_SKIP = "skip";

  public static final String MODE_REUSE_REPORT = "reuseReport";

  public static final String REPORT_DIRECTORY_KEY = "sonar.pitest.reportsDirectory";

  public static final String REPORT_DIRECTORY_DEF = "target/pit-reports";

  /**
   * Classpath that contains either JUnit or TestNG as well as your code, tests and any dependencies
   */
  public static final String CLASSPATH = "sonar.pit.classpath";

  /**
   * Classes to include in mutation test
   *
   */
  public static final String TARGET_CLASSES = "sonar.pit.target.classes";

  /**
   * Tests to run
   *
   */
  public static final String TARGET_TESTS = "sonar.pit.target.tests";

  /**
   * Methods not to mutate
   *
   */
  public static final String EXCLUDED_METHODS = "sonar.pit.excluded.methods";

  /**
   * Classes not to mutate or run tests from
   *
   *
   */
  public static final String EXCLUDED_CLASSES = "sonar.pit.excluded.classes";

  /**
   * List of packages and classes which are to be considered outside the scope of mutation
   *
   */
  public static final String AVOID_CALLS_TO = "sonar.pit.avoid.calls.to";

  /**
   * Maximum distance to look from test to class. Relevant when mutating static
   * initializers
   *
   */
  public static final String MAX_DEPENDENCY_DISTANCE = "sonar.pit.max.dependency.distance";

  /**
   * Number of threads to use
   *
   */
  public static final String THREADS = "sonar.pit.threads";

  /**
   * Mutate static initializers
   *
   */
  public static final String MUTATE_STATIC_INITIALIZERS = "sonar.pit.mutate.static.initializers";

  /**
   * Mutation operators to apply
   *
   */
  public static final String MUTATORS = "sonar.pit.mutators";

  /**
   * Weighting to allow for timeouts
   *
   */
  public static final String TIMEOUT_FACTOR = "sonar.pit.timeout.factor";

  /**
   * Constant factor to allow for timeouts
   *
   */
  public static final String TIMEOUT_CONSTANT = "sonar.pit.timeout.constant";

  /**
   * Maximum number of mutations to allow per class
   *
   */
  public static final String MAX_MUTATIONS_PER_CLASS = "sonar.pit.max.mutations.per.class";

  /**
   * Arguments to pass to child processes
   *
   */
  public static final String JVM_ARGS = "sonar.pit.jvm.args";


  /**
   * Throw error if no mutations found
   *
   */
  public static final String FAIL_WHEN_NO_MUTATIONS = "sonar.pit.fail.when.no.mutations";

  /**
   * TestNG Groups to exclude
   *
   */
  public static final String EXCLUDED_TESTNG_GROUPS = "sonar.pit.excluded.testng.groups";

  /**
   * TestNG Groups to include
   *
   */
  public static final String INCLUDED_TESTNG_GROUPS = "sonar.pit.included.testng.groups";

}
