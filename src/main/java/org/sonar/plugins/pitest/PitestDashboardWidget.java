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

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.NavigationSection;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;
import org.sonar.api.web.WidgetCategory;

/**
 * Sonar user widget for pitest metrics.
 *   
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
@NavigationSection(NavigationSection.RESOURCE)
@UserRole(UserRole.USER)
@WidgetCategory("Pitest")
@Description("Pitest mutation coverage report.")
public class PitestDashboardWidget extends AbstractRubyTemplate implements RubyRailsWidget {

	public String getId() {
		return "pitest";
	}

	public String getTitle() {
		return "Pitest report";
	}

	@Override
	protected String getTemplatePath() {
		return "/org/sonar/plugins/pitest/pitest_dashboard_widget.html.erb";
	}
}
