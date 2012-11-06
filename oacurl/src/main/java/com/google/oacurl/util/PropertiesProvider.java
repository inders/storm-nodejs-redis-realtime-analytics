// Copyright 2010 Google, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.oacurl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Guice-ish wrapper around properties. Mostly convenience for property file
 * I/O and getting empty {@link Property} objects from null file names.
 *
 * @author phopkins@google.com
 */
public class PropertiesProvider {
  private final File file;
  private final InputStream inputStream;
  private Properties properties;

  /**
   * @param fileName file name to load, may be null for empty
   *     {@link Properties}.
   */
  public PropertiesProvider(String fileName) throws FileNotFoundException {
    this(fileName, null, null);
  }

  /**
   * Constructor that, if the file does not exist, searches for it as a
   * resource (with a ".properties" file extension).
   *
   * @param fileName File name to load, may be null for empty
   *     {@link Properties}.
   * @param resourceClass If the file is not found, a class to start the
   *     resource search on.
   * @param resourcePrefix A prefix to add to the file name.
   */
  public PropertiesProvider(String fileName, Class<?> resourceClass, String resourcePrefix)
      throws FileNotFoundException {
    if (fileName == null) {
      this.file = null;
      this.inputStream = null;
    } else {
      File namedFile = new File(fileName);
      if (namedFile.exists()) {
        this.file = namedFile;
        this.inputStream = new FileInputStream(this.file);
      } else {
        if (resourceClass != null && resourcePrefix != null) {
          this.file = null;
          this.inputStream = resourceClass.getResourceAsStream(resourcePrefix + fileName + ".properties");
        } else {
          // If no resource search was provided for, we probably want to
          // create a new file when saving.
          this.file = namedFile;
          this.inputStream = null;
        }
      }
    }
  }

  public Properties get() throws IOException {
    if (properties == null) {
      properties = new Properties();

      if (inputStream != null) {
        properties.load(inputStream);
        inputStream.close();
      }
    }

    return properties;
  }

  public void overwrite(Properties properties) throws IOException {
    if (file == null) {
      throw new IllegalStateException("Trying to save properties to null file");
    }

    file.setWritable(true, true);
    FileOutputStream out = new FileOutputStream(file);
    properties.store(out, null);
    out.close();

    this.properties = properties;
  }
}
