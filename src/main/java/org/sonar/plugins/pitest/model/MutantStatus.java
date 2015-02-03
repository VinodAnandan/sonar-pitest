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
package org.sonar.plugins.pitest.model;

public enum MutantStatus {
    NO_COVERAGE(true),
    KILLED(false),
    SURVIVED(true),
    MEMORY_ERROR(false),
    TIMED_OUT(false),
    UNKNOWN(true);

    private boolean alive;

    MutantStatus(final boolean alive) {

        this.alive = alive;
    }

    public boolean isAlive() {

        return alive;
    }

    public static MutantStatus parse(final String statusName) {

        if (statusName == null) {
            return UNKNOWN;
        }
        for (final MutantStatus mutantStatus : MutantStatus.values()) {
            if (mutantStatus.name().equals(statusName)) {
                return mutantStatus;
            }
        }
        return UNKNOWN;
    }
}