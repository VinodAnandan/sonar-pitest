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

import org.pitest.classinfo.CodeSource;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.DefaultCoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.CompoundListenerFactory;
import org.pitest.mutationtest.MutationClassPaths;
import org.pitest.mutationtest.MutationCoverage;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.SettingsFactory;
import org.pitest.mutationtest.Timings;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.mutationtest.incremental.XStreamHistoryStore;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.instrument.KnownLocationJavaAgentFinder;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.mutationtest.verify.DefaultBuildVerifier;
import org.pitest.util.JavaAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.io.Reader;
import java.net.URL;

import static org.sonar.plugins.pitest.PitestConstants.PITEST_JAR_NAME;


public class PitestExecutor implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(PitestExecutor.class);

  private final ReportOptionsBuilder builder;
  private final JarExtractor jarExtractor;


  public PitestExecutor(ReportOptionsBuilder builder, JarExtractor jarExtractor) {
    this.builder = builder;
    this.jarExtractor = jarExtractor;
  }

  private void extractPitJar() {
    URL jarURL = getClass().getResource("/META-INF/lib/"+PITEST_JAR_NAME);
    jarExtractor.extractJar(jarURL, PITEST_JAR_NAME);
  }

  public void execute() {
    extractPitJar();
    ReportOptions data = builder.build();
    LOG.debug("Running report with {}", data);
    File baseDir = builder.detectBaseDir();

    final SettingsFactory settings = new SettingsFactory(data);

    final ClassPath cp = data.getClassPath();


    final Option<Reader> reader = data.createHistoryReader();
    final WriterFactory historyWriter = data.createHistoryWriter();

    // workaround for apparent java 1.5 JVM bug . . . might not play nicely
    // with distributed testing
    final JavaAgent jac = new JarCreatingJarFinder(
        new ClassPathByteArraySource(cp));
    final KnownLocationJavaAgentFinder ja = new KnownLocationJavaAgentFinder(
        jac.getJarLocation().value());

    final ResultOutputStrategy reportOutput = settings.getOutputStrategy();

    final CompoundListenerFactory reportFactory = new CompoundListenerFactory(
        FCollection.map(data.getOutputFormats(),
            OutputFormat.createFactoryForFormat(reportOutput)));

    final CoverageOptions coverageOptions = data.createCoverageOptions();
    final LaunchOptions launchOptions = new LaunchOptions(ja, data.getJvmArgs());
    final MutationClassPaths cps = data.getMutationClassPaths();

    final CodeSource code = new CodeSource(cps, coverageOptions.getPitConfig()
        .testClassIdentifier());

    final Timings timings = new Timings();
    final CoverageGenerator coverageDatabase = new DefaultCoverageGenerator(
        baseDir, coverageOptions, launchOptions, code,
        settings.createCoverageExporter(), timings, !data.isVerbose());

    final HistoryStore history = new XStreamHistoryStore(historyWriter, reader);

    final MutationCoverage report = new MutationCoverage(baseDir, history,
        code, coverageDatabase, data, reportFactory, timings,
        new DefaultBuildVerifier());

    try {
      report.runReport();
    } catch (Exception e) {
      throw new SonarException("fail", e);
    } finally {
      jac.close();
      ja.close();
      historyWriter.close();
    }
  }

}
