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

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author phopkins
 *
 */
public class LoggingConfig {
  public static void init(boolean verbose) throws SecurityException, IOException {
    System.setProperty("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.Jdk14Logger");

    Logger defaultLogger = Logger.getLogger("");
    if (verbose) {
      defaultLogger.setLevel(Level.INFO);
    } else {
      defaultLogger.setLevel(Level.SEVERE);
    }
  }

  public static void enableWireLog() {
    // For clarity, override the formatter so that it doesn't print the
    // date and method name for each line, and then munge the output a little
    // bit to make it nicer and more curl-like.
    Formatter wireFormatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        String message = record.getMessage();
        String trimmedMessage = message.substring(">> \"".length(), message.length() - 1);
        if (trimmedMessage.matches("[0-9a-f]+\\[EOL\\]")) {
          return "";
        }

        trimmedMessage = trimmedMessage.replace("[EOL]", "");
        if (trimmedMessage.isEmpty()) {
          return "";
        }

        StringBuilder out = new StringBuilder();
        out.append(message.charAt(0));
        out.append(" ");
        out.append(trimmedMessage);
        out.append(System.getProperty("line.separator"));
        return out.toString();
      }
    };

    ConsoleHandler wireHandler = new ConsoleHandler();
    wireHandler.setLevel(Level.FINE);
    wireHandler.setFormatter(wireFormatter);

    Logger wireLogger = Logger.getLogger("org.apache.http.wire");
    wireLogger.setLevel(Level.FINE);
    wireLogger.setUseParentHandlers(false);
    wireLogger.addHandler(wireHandler);
  }
}
