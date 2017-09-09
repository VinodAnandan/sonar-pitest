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

/**
 * Constants for the PIT plugins
 * There is a constant for each configuration key.
 * Most of these configuration keys, and the javadoc comments are
 * strongly inspired by the maven PIT plugin
 *
 * @author Alexandre Victoor
 */
public final class PitestConstants {

  private PitestConstants() {

  }

  public static final String REPOSITORY_KEY = "pitest";
  public static final String REPOSITORY_NAME = "Pitest";

  public static final String SURVIVED_MUTANT_RULE_KEY = "pitest.survived.mutant";

  public static final String INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY = "pitest.insufficient.mutation.coverage";

  public static final String COVERAGE_RATIO_PARAM = "minimumMutationCoverageRatio";

  public static final String MODE_KEY = "sonar.pitest.mode";

  public static final String MODE_SKIP = "skip";

  public static final String MODE_REUSE_REPORT = "reuseReport";

  public static final String REPORT_DIRECTORY_KEY = "sonar.pitest.reportsDirectory";

  public static final String REPORT_DIRECTORY_DEF = "target/pit-reports";


}
