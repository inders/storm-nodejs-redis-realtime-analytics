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

package com.google.oacurl.options;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommonOptions {
  private static final String DEFAULT_LOGIN_FILE_NAME = ".oacurl.properties";

  protected final Options options;

  private String loginFileName;
  private boolean help;
  private boolean verbose;
  private boolean insecure;

  public CommonOptions() {
    options = new Options();
    options.addOption(null, "access-file", true, "properties file with access token and secret");
    options.addOption("h", "help", false, "This help text");
    options.addOption("v", "verbose", false, "Make the operation more talkative");
    options.addOption("k", "insecure", false,
        "Allow connections to SSL sites with non-matching hostnames");
  }

  public CommandLine parse(String[] args) throws ParseException {
    CommandLine line = new GnuParser().parse(options, args);

    loginFileName = line.getOptionValue("access-file",
        new File(System.getProperty("user.home"), DEFAULT_LOGIN_FILE_NAME).getAbsolutePath());
    help = line.hasOption("help");
    verbose = line.hasOption("verbose");
    insecure = line.hasOption("insecure");

    return line;
  }

  public String getLoginFileName() {
    return loginFileName;
  }

  public boolean isHelp() {
    return help;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public boolean isInsecure() {
    return insecure;
  }

  public Options getOptions() {
    return options;
  }
}
