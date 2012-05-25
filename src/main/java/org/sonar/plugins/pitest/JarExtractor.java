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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;

/**
 * Tool that extract the pitest jar from the sonar pitest plugin jar.
 * We need to extract this pitest jar because the pitest executor component
 * needs it to create a "all in one" jar.
 * 
 * 
 * @author Alexandre Victoor
 *
 */
public class JarExtractor implements BatchExtension {
  
  private static final Logger LOG = LoggerFactory.getLogger(JarExtractor.class);
  
  private static final int BUFFER_SIZE = 2048;

  private final ProjectFileSystem fileSystem;
  
  public JarExtractor(ProjectFileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }
  
  public void extractJar(URL jarURL, String pitestJarName) {
    LOG.info("Extracting {} from {}", pitestJarName, jarURL);
    File workingDir = fileSystem.getSonarWorkingDirectory();
    String sonarPitestJarPath = StringUtils.substringBefore(jarURL.getFile(), "!").substring(5);
    try {
      ZipFile zip = new ZipFile(new File(sonarPitestJarPath));
      ZipEntry entry = zip.getEntry("META-INF/lib/"+pitestJarName);
      BufferedInputStream is = null;
      BufferedOutputStream dest = null;
      try {
        is = new BufferedInputStream(zip.getInputStream(entry));
        int currentByte;
        // establish buffer for writing file
        byte data[] = new byte[BUFFER_SIZE];

        // write the current file to disk
        File destFile = new File(workingDir, pitestJarName);
        FileOutputStream fos = new FileOutputStream(destFile);
        dest = new BufferedOutputStream(fos, BUFFER_SIZE);

        // read and write until last byte is encountered
        while ((currentByte = is.read(data, 0, BUFFER_SIZE)) != -1) {
          dest.write(data, 0, currentByte);
        }
      } finally {
        if (dest != null) {
          dest.flush();
        }
        IOUtils.closeQuietly(dest);
        IOUtils.closeQuietly(is);
      }
      
      
    } catch (ZipException e) {
      LOG.error("Error while extracting pitest jar");
      throw new SonarException(e);
    } catch (IOException e) {
      LOG.error("Error while extracting pitest jar");
      throw new SonarException(e);
    }
  }
  
}
