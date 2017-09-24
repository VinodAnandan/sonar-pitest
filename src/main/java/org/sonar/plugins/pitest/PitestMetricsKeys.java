/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
 * vinod@owasp.org
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
 * {@link PitestMetrics} keys.
 * 
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
public final class PitestMetricsKeys {
	
	private PitestMetricsKeys() {
		// Hide utility class constructor
	}
	
	public static final String MUTATIONS_DATA_KEY = "pitest_mutations_data";
	public static final String MUTATIONS_TOTAL_KEY = "pitest_mutations_total";
	public static final String MUTATIONS_DETECTED_KEY = "pitest_mutations_detected";
	public static final String MUTATIONS_NO_COVERAGE_KEY = "pitest_mutations_noCoverage";
	public static final String MUTATIONS_KILLED_KEY = "pitest_mutations_killed";
	public static final String MUTATIONS_SURVIVED_KEY = "pitest_mutations_survived";
	public static final String MUTATIONS_MEMORY_ERROR_KEY = "pitest_mutations_memoryError";
	public static final String MUTATIONS_TIMED_OUT_KEY = "pitest_mutations_timedOut";
	public static final String MUTATIONS_UNKNOWN_KEY = "pitest_mutations_unknown";
	public static final String MUTATIONS_COVERAGE_KEY = "pitest_mutations_coverage";
}
