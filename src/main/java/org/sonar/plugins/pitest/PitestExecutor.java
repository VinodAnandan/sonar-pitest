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

import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.functional.FCollection;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CompoundListenerFactory;
import org.pitest.mutationtest.CoverageDatabase;
import org.pitest.mutationtest.DefaultCoverageDatabase;
import org.pitest.mutationtest.MutationClassPaths;
import org.pitest.mutationtest.MutationCoverageReport;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.Timings;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.instrument.KnownLocationJavaAgentFinder;
import org.pitest.mutationtest.report.DatedDirectoryResultOutputStrategy;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.mutationtest.verify.DefaultBuildVerifier;
import org.pitest.util.JavaAgent;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;


public class PitestExecutor implements BatchExtension {

  private final ReportOptionsBuilder builder;
  
  
  private PitestExecutor(ReportOptionsBuilder builder) {
    this.builder = builder;
  }


  public void execute() {
    ReportOptions data = builder.build();
    System.out.println("Running report with " + data);
    final ClassPath cp = data.getClassPath();

    // workaround for apparent java 1.5 JVM bug . . . might not play nicely
    // with distributed testing
    final JavaAgent jac = new JarCreatingJarFinder(cp);
    final KnownLocationJavaAgentFinder ja = new KnownLocationJavaAgentFinder(
        jac.getJarLocation().value());

    final ResultOutputStrategy reportOutput = new DatedDirectoryResultOutputStrategy(
        data.getReportDir());
    final CompoundListenerFactory reportFactory = new CompoundListenerFactory(
        FCollection.map(data.getOutputFormats(),
            OutputFormat.createFactoryForFormat(reportOutput)));

    CoverageOptions coverageOptions = data.createCoverageOptions();
    LaunchOptions launchOptions = new LaunchOptions(ja, data.getJvmArgs());
    MutationClassPaths cps = data.getMutationClassPaths();

    Timings timings = new Timings();
    final CoverageDatabase coverageDatabase = new DefaultCoverageDatabase(
        coverageOptions, launchOptions, cps, timings);
    final MutationCoverageReport report = new MutationCoverageReport(
        coverageDatabase, data, reportFactory, timings, new DefaultBuildVerifier());

    // Create new classloader under boot
    final ClassLoader loader = new DefaultPITClassloader(cp,
        IsolationUtils.bootClassLoader());
    final ClassLoader original = IsolationUtils.getContextClassLoader();

    try {
      IsolationUtils.setContextClassLoader(loader);

      final Runnable run = (Runnable) IsolationUtils.cloneForLoader(report,
          loader);

      run.run();

    } catch (final Exception e) {
      throw new SonarException("fail", e);
    } finally {
      IsolationUtils.setContextClassLoader(original);
      jac.close();
      ja.close();

    }
  }

}
