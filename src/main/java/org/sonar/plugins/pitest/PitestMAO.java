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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Utility class to save mutation metrics and violations from the Mutants
 * collection.
 *
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
public class PitestMAO {

	private static final Logger LOG = LoggerFactory.getLogger(PitestMAO.class);

	private JavaFileMutants noResourceMetrics = new JavaFileMutants();

	/**
	 * Save Mutant info (violations and metrics).
	 *
	 * @param mutants
	 *            {@link Collection<Mutant>} Mutants collections.
	 * @param context
	 *            {@link SensorContext} Sensor context.
	 * @param rule
	 *            {@link Rule} Currently, the only violation rule in pitest
	 *            domain.
	 */
	public void saveMutantsInfo(Collection<Mutant> mutants, SensorContext context, List<ActiveRule> activeRules) {
		Map<Resource<?>, JavaFileMutants> metrics = collectMetrics(mutants, context, activeRules);
		for (Entry<Resource<?>, JavaFileMutants> entry : metrics.entrySet()) {
			saveMetricsInfo(context, entry.getKey(), entry.getValue());
		}
	}

	private void saveMetricsInfo(SensorContext context, Resource<?> resource, JavaFileMutants metricsInfo) {
		double detected = metricsInfo.getMutationsDetected();
		double total = metricsInfo.getMutationsTotal();
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_TOTAL, total);
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_NO_COVERAGE, metricsInfo.getMutationsNoCoverage());
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_KILLED, metricsInfo.getMutationsKilled());
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_SURVIVED, metricsInfo.getMutationsSurvived());
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_MEMORY_ERROR, metricsInfo.getMutationsMemoryError());
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_TIMED_OUT, metricsInfo.getMutationsTimedOut());
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_UNKNOWN, metricsInfo.getMutationsUnknown());
		context.saveMeasure(resource, PitestMetrics.MUTATIONS_DETECTED, detected);
		saveData(context, resource, metricsInfo.getMutants());
	}

	private void saveData(SensorContext context, Resource<?> resource, List<Mutant> mutants) {
		if ((mutants != null) && (!mutants.isEmpty())) {
		  String json = Mutant.toJSON(mutants);
		  Measure measure = new Measure(PitestMetrics.MUTATIONS_DATA, json);
      context.saveMeasure(resource, measure);
		}
	}

	private Map<Resource<?>, JavaFileMutants> collectMetrics(Collection<Mutant> mutants, SensorContext context, List<ActiveRule> activeRules) {
		Map<Resource<?>, JavaFileMutants> metricsByResource = new HashMap<Resource<?>, JavaFileMutants>();
		Rule rule = getSurvivedRule(activeRules); // Currently, only survived rule is applied
		JavaFile resource;
		for (Mutant mutant : mutants) {
			resource = context.getResource(new JavaFile(mutant.getSonarJavaFileKey()));
			if (resource == null) {
				LOG.warn("Mutation in an unknown resource: {}", mutant);
				processMutant(mutant, noResourceMetrics, resource, context, rule);
			}
			else {
				processMutant(mutant, getMetricsInfo(metricsByResource, resource), resource, context, rule);
			}
		}
		return metricsByResource;
	}

	private Rule getSurvivedRule(List<ActiveRule> activeRules) {
		Rule rule = null;
		if (activeRules != null && !activeRules.isEmpty()) {
			rule = activeRules.get(0).getRule();
		}
		return rule;
	}

	private void processMutant(Mutant mutant, JavaFileMutants resourceMetricsInfo, JavaFile resource, SensorContext context, Rule rule) {
		resourceMetricsInfo.addMutant(mutant);
		if (resource != null && rule != null && MutantStatus.SURVIVED.equals(mutant.getMutantStatus())) {
			// Only survived mutations are saved as violations
			Violation violation = Violation.create(rule, resource).setLineId(mutant.getLineNumber()).setMessage(mutant.getViolationDescription());
			context.saveViolation(violation);
		}
	}

	private static JavaFileMutants getMetricsInfo(Map<Resource<?>, JavaFileMutants> metrics, Resource<?> resource) {
		JavaFileMutants metricsInfo = null;
		if (resource != null) {
			metricsInfo = metrics.get(resource);
			if (metricsInfo == null) {
				metricsInfo = new JavaFileMutants();
				metrics.put(resource, metricsInfo);
			}
		}
		return metricsInfo;
	}
}
