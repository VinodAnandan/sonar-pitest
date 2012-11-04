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

import org.sonar.api.resources.Qualifiers;
import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.DefaultTab;
import org.sonar.api.web.NavigationSection;
import org.sonar.api.web.RequiredMeasures;
import org.sonar.api.web.ResourceQualifier;
import org.sonar.api.web.RubyRailsPage;
import org.sonar.api.web.UserRole;

@NavigationSection(NavigationSection.RESOURCE_TAB)
@ResourceQualifier({Qualifiers.FILE, Qualifiers.CLASS})
@DefaultTab(
    metrics = {
     PitestMetricsKeys.MUTATIONS_TOTAL_KEY
    })
@RequiredMeasures(anyOf = { PitestMetricsKeys.MUTATIONS_DATA_KEY })
@UserRole(UserRole.CODEVIEWER)
public class PitSourceTab extends AbstractRubyTemplate implements RubyRailsPage {

  @Override
  protected String getTemplatePath() {
    return "/org/sonar/plugins/pitest/pitest_source_tab.rb";
  }

  public String getId() {
    return "Mutations Coverage";
  }

  public String getTitle() {
    return "Mutations Coverage";
  }

}
