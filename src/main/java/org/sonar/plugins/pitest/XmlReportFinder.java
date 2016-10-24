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

import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.BatchSide;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicReference;

@BatchSide
@ExtensionPoint
public class XmlReportFinder  {

  public File findReport(File reportDirectory) {
    if (reportDirectory== null || !reportDirectory.exists() || !reportDirectory.isDirectory()) {
      return null;
    }
    final AtomicReference<Path> latestReport = new AtomicReference<>();
    try {
      Files.walkFileTree(reportDirectory.toPath(), new FileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {

          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          if(Files.isRegularFile(file)
              && file.toString().endsWith(".xml")
              && (latestReport.get() == null
                  || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(latestReport.get())) > 0)){
              latestReport.set(file);
            }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {

          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {

          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if(latestReport.get() != null){
      return latestReport.get().toFile();
    }
    return null;
  }

}
