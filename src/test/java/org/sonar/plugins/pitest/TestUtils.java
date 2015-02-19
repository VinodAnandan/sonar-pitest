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
package org.sonar.plugins.pitest;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Creates a temporary file with content from a classpath resource in a path relative to a {@link TemporaryFolder}.
     *
     * @param folder
     *            the {@link TemporaryFolder} rule in which the file should be created
     * @param filePath
     *            the path to the file relative to the root of the temporary folder
     * @param resource
     *            the absolute path to the classpath resource.
     * @return the created temporary file
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static File tempFileFromResource(final TemporaryFolder folder, final String filePath, final String resource)
            throws IOException, FileNotFoundException {

        return tempFileFromResource(folder, filePath, TestUtils.class, resource);
    }

    /**
     * Creates a temporary file with content from a classpath resource in a path relative to a {@link TemporaryFolder}.
     *
     * @param folder
     *            the {@link TemporaryFolder} rule in which the file should be created
     * @param filePath
     *            the path to the file relative to the root of the temporary folder
     * @param baseClass
     *            the class that is used to resolve the classpath resource.
     * @param resource
     *            the path to the classpath resource relative to the class. The content of the resource is written to
     *            the temporary file
     * @return the created file
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static File tempFileFromResource(final TemporaryFolder folder, final String filePath,
            final Class<?> baseClass, final String resource) throws IOException, FileNotFoundException {

        final File tempFile = newTempFile(folder, filePath);
        final URL url = baseClass.getResource(resource);
        assertNotNull("Resource " + resource + " not found", url);
        copyResourceToFile(url, tempFile);
        return tempFile;
    }

    /**
     * Copies the content from the URL to the specified file
     *
     * @param resource
     *            the location of the resource
     * @param tempFile
     *            the file to write the data from the resource to
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void copyResourceToFile(final URL resource, final File tempFile) throws IOException,
            FileNotFoundException {

        IOUtils.copy(resource.openStream(), new FileOutputStream(tempFile));
        LOG.info("Created temp file {}", tempFile.getAbsolutePath());
    }

    /**
     * Creates a new temporary file in the {@link TemporaryFolder}. The file may be specified as path relative to the
     * root of the temporary folder
     *
     * @param folder
     *            the temporary folder in which to create the new file
     * @param filePath
     *            the name of the file or a relative path to the file to be created
     * @return the {@link File} reference to the newly created file
     * @throws IOException
     */
    public static File newTempFile(final TemporaryFolder folder, final String filePath) throws IOException {

        String path;
        String filename;
        final int lastPathSeparator = filePath.lastIndexOf('/');
        if (lastPathSeparator != -1) {
            path = filePath.substring(0, lastPathSeparator);
            filename = filePath.substring(lastPathSeparator + 1);
        } else {
            path = null;
            filename = filePath;
        }
        return createTempFile(folder, path, filename);
    }

    /**
     * Creates a temporary file in the specified path relative to the {@link TemporaryFolder} rule
     *
     * @param folder
     *            the {@link TemporaryFolder} that provides the root for the file
     * @param path
     *            the relative to the file, without the filename itself
     * @param filename
     *            the name of the file to create
     * @return the {@link File} reference to the new file
     * @throws IOException
     */
    public static File createTempFile(final TemporaryFolder folder, final String path, final String filename)
            throws IOException {

        File tempFile;
        if (path != null) {
            final String[] pathSegments = path.split("\\/");
            final File newFolder = folder.newFolder(pathSegments);
            tempFile = new File(newFolder, filename);
        } else {
            tempFile = folder.newFile(filename);
        }
        return tempFile;
    }

}
