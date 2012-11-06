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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.oauth.OAuth;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import com.google.oacurl.util.MultipartRelatedInputStream;


public class FetchOptions extends CommonOptions {
  public enum Method {
    GET,
    POST,
    PUT,
    DELETE,
  }

  private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<String, String>();
  static {
    CONTENT_TYPE_MAP.put("ATOM", "application/atom+xml");
    CONTENT_TYPE_MAP.put("XML", "application/xml");
    CONTENT_TYPE_MAP.put("JSON", "application/json");
    CONTENT_TYPE_MAP.put("CSV", "text/csv");
    CONTENT_TYPE_MAP.put("TEXT", "text/plain");
    CONTENT_TYPE_MAP.put("BMP", "image/bmp");
    CONTENT_TYPE_MAP.put("GIF", "image/gif");
    CONTENT_TYPE_MAP.put("JPEG", "image/jpeg");
    CONTENT_TYPE_MAP.put("PNG", "image/png");
  }

  private Method method = Method.GET;
  private String contentType = "application/atom+xml";
  private String file;
  private List<OAuth.Parameter> headers;
  private List<Map.Entry<String, String>> related;
  private boolean include;

  @SuppressWarnings("static-access")
  public FetchOptions() {
    options.addOption("f", "file", true, "File name to POST, rather than stdin");
    options.addOption("X", "request", true, "HTTP method: GET, POST, PUT, or DELETE");
    options.addOption(OptionBuilder.withArgName("method")
        .withLongOpt("header")
        .hasArg()
        .withDescription("Custom header to pass to server").create("H"));
    options.addOption("R", "related", true, "File name (;content/type) for multipart/related");
    options.addOption("t", "content-type", true,
        "Content-Type header (or ATOM, XML, JSON, CSV, TEXT)");
    options.addOption("i", "include", false, "Include protocol headers in the output");
  }

  @Override
  public CommandLine parse(String[] args) throws ParseException {
    CommandLine line = super.parse(args);

    headers = new ArrayList<OAuth.Parameter>();

    if (line.hasOption("file")) {
      file = line.getOptionValue("file");
      contentType = guessContentType(file);
      method = Method.POST;
      headers.add(new OAuth.Parameter("Slug", new File(file).getName()));
    }

    contentType = line.getOptionValue("content-type", contentType);
    if (CONTENT_TYPE_MAP.containsKey(contentType)) {
      contentType = CONTENT_TYPE_MAP.get(contentType);
    }

    String[] headerArray = line.getOptionValues("header");
    if (headerArray != null) {
      for (String header : headerArray) {
        String[] headerBits = header.split(":", 2);
        headers.add(new OAuth.Parameter(headerBits[0].trim(), headerBits[1].trim()));
      }
    }

    String[] relatedArray = line.getOptionValues("related");
    if (relatedArray != null) {
      method = Method.POST;
      related = new ArrayList<Map.Entry<String, String>>();
      contentType = "multipart/related; boundary=\"" + MultipartRelatedInputStream.BOUNDARY + "\"";
      headers.add(new OAuth.Parameter("MIME-version", "1.0"));

      for (String relatedFile : relatedArray) {
        String[] fileBits = relatedFile.split(";", 2);

        String fileName = fileBits[0];
        String contentType;
        if (fileBits.length == 2) {
          contentType = fileBits[1];
        } else {
          contentType = guessContentType(fileName);
        }

        related.add(new OAuth.Parameter(fileName, contentType));
      }
    }

    include = line.hasOption("include");

    if (line.hasOption("request")) {
      method = Method.valueOf(line.getOptionValue("request"));
    }

    return line;
  }

  private String guessContentType(String fileName) {
    String contentType;
    String lowerCaseFileName = fileName.toLowerCase();
    if (lowerCaseFileName.endsWith(".bmp")) {
      contentType = CONTENT_TYPE_MAP.get("BMP");
    } else if (lowerCaseFileName.endsWith(".gif")) {
      contentType = CONTENT_TYPE_MAP.get("GIF");
    } else if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
      contentType = CONTENT_TYPE_MAP.get("JPEG");
    } else if (lowerCaseFileName.endsWith(".png")) {
      contentType = CONTENT_TYPE_MAP.get("PNG");
    } else if (lowerCaseFileName.endsWith(".txt")) {
      contentType = CONTENT_TYPE_MAP.get("TEXT");
    } else if (lowerCaseFileName.endsWith(".xml")) {
      // With this tool, if you're sending something as .xml you probably are
      // sending Atom.
      contentType = CONTENT_TYPE_MAP.get("ATOM");
    } else {
      contentType = "application/octet-stream";
    }
    return contentType;
  }

  public Method getMethod() {
    return method;
  }

  public String getContentType() {
    return contentType;
  }

  public List<OAuth.Parameter> getHeaders() {
    return headers;
  }

  public List<Map.Entry<String, String>> getRelated() {
    return related;
  }

  public boolean isInclude() {
    return include;
  }

  public String getFile() {
    return file;
  }
}
