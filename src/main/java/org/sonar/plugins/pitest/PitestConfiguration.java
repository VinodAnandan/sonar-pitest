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

import java.io.File;
import java.util.List;


public class PitestConfiguration {

  /**
   * Classes to include in mutation test
   * 
   * @parameter
   * 
   */
  private List<String>          targetClasses;

  /**
   * Tests to run
   * 
   * @parameter
   * 
   */
  private List<String>          targetTests;

  /**
   * Methods not to mutate
   * 
   * @parameter
   * 
   */
  private List<String>          excludedMethods;

  /**
   * Classes not to mutate or run tests from
   * 
   * @parameter
   * 
   */
  private List<String>          excludedClasses;

  /**
   * 
   * @parameter
   * 
   */
  private List<String>          avoidCallsTo;


  /**
   * Base directory where all reports are written to.
   * 
   * @parameter default-value="${project.build.directory}/pit-reports"
   */
  private File                  reportsDirectory;

  /**
   * Maximum distance to look from test to class. Relevant when mutating static
   * initializers
   * 
   * @parameter default-value="-1"
   */
  private int                   maxDependencyDistance;

  /**
   * Number of threads to use
   * 
   * @parameter default-value="1"
   */
  private int                   threads;

  /**
   * Mutate static initializers
   * 
   * @parameter default-value="false"
   */
  private boolean               mutateStaticInitializers;

  /**
   * Mutation operators to apply
   * 
   * @parameter
   */
  private List<String>          mutators;

  /**
   * Weighting to allow for timeouts
   * 
   * @parameter default-value="1.25"
   */
  private float                 timeoutFactor;

  /**
   * Constant factor to allow for timeouts
   * 
   * @parameter default-value="3000"
   */
  private long                  timeoutConstant;

  /**
   * Maximum number of mutations to allow per class
   * 
   * @parameter default-value="-1"
   */
  private int                   maxMutationsPerClass;

  /**
   * Arguments to pass to child processes
   * 
   * @parameter
   */
  private List<String>          jvmArgs;

  /**
   * Formats to output during analysis phase
   * 
   * @parameter
   */
  private List<String>          outputFormats;

  /**
   * Output verbose logging
   * 
   * @parameter default-value="false"
   */
  private boolean               verbose;

  /**
   * Throw error if no mutations found
   * 
   * @parameter default-value="true"
   */
  private boolean               failWhenNoMutations;

  /**
   * TestNG Groups to exclude
   * 
   * @parameter
   */
  private List<String>          excludedTestNGGroups;

  /**
   * TestNG Groups to include
   * 
   * @parameter
   */
  private List<String>          includedTestNGGroups;
}
