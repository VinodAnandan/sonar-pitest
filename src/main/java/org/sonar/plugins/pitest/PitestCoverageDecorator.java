/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 SonarQubeCommunity
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;


/**
 * Mutation coverage decorator.
 *
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
@Deprecated
public class PitestCoverageDecorator implements Decorator {

	public boolean shouldExecuteOnProject(Project project) {
		return project.getAnalysisType().isDynamic(true);
	}

	@DependedUpon
	public Metric getCoverageMetric() {
		return PitestMetrics.MUTATIONS_COVERAGE;
	}

	@DependsUpon
	public List<Metric> getBaseMetrics() {
		return Arrays.asList(PitestMetrics.MUTATIONS_DETECTED, PitestMetrics.MUTATIONS_TOTAL);
	}

	public void decorate(Resource resource, DecoratorContext context) {
		Double elements = MeasureUtils.getValue(context.getMeasure(PitestMetrics.MUTATIONS_TOTAL), 0.0);

		if (elements > 0.0) {
			Double coveredElements = MeasureUtils.getValue(context.getMeasure(PitestMetrics.MUTATIONS_DETECTED), 0.0);
			context.saveMeasure(PitestMetrics.MUTATIONS_COVERAGE, (100.0 * coveredElements) / elements);
		}
	}
}
