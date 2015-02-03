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
package org.sonar.plugins.pitest.metrics;

import java.util.List;

import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

import com.google.common.collect.Lists;

/**
 * Mutation coverage decorator.
 *
 * @author <a href="mailto:gerald@moskito.li">Gerald Muecke</a>
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
public class PitestCoverageDecorator implements Decorator {

    @Override
    public boolean shouldExecuteOnProject(final Project project) {

        return true;
    }

    @DependedUpon
    public Metric<Double> getCoverageMetric() {

        return PitestMetrics.MUTATIONS_COVERAGE;
    }

    @SuppressWarnings("unchecked")
    @DependsUpon
    public List<Metric<Integer>> getBaseMetrics() {

        return Lists.newArrayList(PitestMetrics.MUTATIONS_DETECTED, PitestMetrics.MUTATIONS_TOTAL);
    }

    @Override
    public void decorate(final Resource resource, final DecoratorContext context) {

        final Double elements = MeasureUtils.getValue(context.getMeasure(PitestMetrics.MUTATIONS_TOTAL), 0.0);

        if (elements > 0.0) {
            final Double coveredElements = MeasureUtils.getValue(context.getMeasure(PitestMetrics.MUTATIONS_DETECTED),
                    0.0);
            context.saveMeasure(PitestMetrics.MUTATIONS_COVERAGE, 100.0 * coveredElements / elements);
        }
    }
}
