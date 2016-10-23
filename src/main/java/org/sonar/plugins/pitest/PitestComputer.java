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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.measures.Metric;

/**
 * Computer that processes the aggregated quantitative metric for a component from all the quantitative metrics
 * of its children.
 *
 * @author <a href="mailto:gerald.muecke@devcon5.io">Gerald M&uuml;cke</a>
 */
public class PitestComputer implements MeasureComputer {

  public MeasureComputerDefinition define(final MeasureComputerDefinitionContext defContext) {

    return defContext.newDefinitionBuilder()
      .setOutputMetrics(getQuantitativeKeys().toArray(new String[0]))
      .build();
  }

  public void compute(final MeasureComputerContext context) {
    for (String metricKey : getQuantitativeKeys()) {
      if (context.getMeasure(metricKey) == null) {
        Integer sum = 0;
        for( Measure m : context.getChildrenMeasures(metricKey)) {
          sum += m.getIntValue();
        }
        if(sum > 0) {
          context.addMeasure(metricKey, sum);
        }
      }
    }
  }

  public List<String> getQuantitativeKeys() {
    final List<String> result = new ArrayList<String>();
    for(Metric m : PitestMetrics.getQuantitativeMetrics()){
      result.add(m.getKey());
    }
    return result;
  }
}
