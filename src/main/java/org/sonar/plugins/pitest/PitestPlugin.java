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

import org.sonar.api.Extension;
import org.sonar.api.SonarPlugin;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * This class is the entry point for all PIT rextensions
 * TODO : add annotation for all the configuration keys
 */
public final class PitestPlugin extends SonarPlugin {

  // This is where you're going to declare all your Sonar extensions
  public List<Class<? extends Extension>> getExtensions() {
    return Lists.newArrayList(
        ResultParser.class, 
        PitestRuleRepository.class, 
        PitestSensor.class,
        PitestConfigurationBuilder.class,
        PitestExecutor.class,
        ReportOptionsBuilder.class,
        JarExtractor.class
    );
  }
}