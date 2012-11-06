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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wrapper around separate {@link InputStream}s to make a stream of several
 * files for a multipart/related form post.
 * <p>
 * We're using the deprecated {@link StringBufferInputStream} because we're ok
 * with the boundary and content type being all ASCII.
 *
 * @author phopkins@google.com
 */
@SuppressWarnings("deprecation")
public class MultipartRelatedInputStream extends InputStream {
  public static final String BOUNDARY = "END_OF_PART";

  private final LinkedList<InputStream> streams;

  public MultipartRelatedInputStream(List<Entry<String, String>> related) throws IOException {
    this.streams = new LinkedList<InputStream>();

    pushCommentStream();

    for (Map.Entry<String, String> part : related) {
      pushBoundaryStream();
      pushHeaderStream("Content-Type", part.getValue());
      pushNewlineStream();
      pushFileStream(part.getKey());
      pushNewlineStream();
    }

    pushEndBoundaryStream();
  }

  private void pushCommentStream() {
    streams.add(new StringBufferInputStream("Media multipart posting\n"));
  }

  private void pushBoundaryStream() {
    streams.add(new StringBufferInputStream("--" + BOUNDARY + "\n"));
  }

  private void pushEndBoundaryStream() {
    streams.add(new StringBufferInputStream("--" + BOUNDARY + "--\n"));
  }

  private void pushHeaderStream(String name, String value) {
    streams.add(new StringBufferInputStream(name + ": " + value + "\n"));
  }

  private void pushNewlineStream() {
    streams.add(new StringBufferInputStream("\n"));
  }

  private void pushFileStream(String fileName) throws FileNotFoundException {
    streams.add(new FileInputStream(fileName));
  }
  
  @Override
  public int read() throws IOException {
    if (streams.isEmpty()) {
      return -1;
    }

    InputStream stream = streams.getFirst();
    int ret = stream.read();

    if (ret == -1) {
      stream.close();
      streams.removeFirst();
      if (streams.isEmpty()) {
        return ret;
      } else {
        return read();
      }
    } else {
      return ret;
    }
  }

  @Override
  public int available() throws IOException {
    if (streams.isEmpty()) {
      return 0;
    }

    return streams.getFirst().available();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (streams.isEmpty()) {
      return -1;
    }

    InputStream stream = streams.getFirst();
    int ret = stream.read(b, off, len);

    if (ret == -1) {
      stream.close();
      streams.removeFirst();
      if (streams.isEmpty()) {
        return ret;
      } else {
        return read(b, off, len);
      }
    } else {
      return ret;
    }
  }

  @Override
  public void close() throws IOException {
    for (InputStream stream : streams) {
      stream.close();
    }

    streams.clear();
  }
}
