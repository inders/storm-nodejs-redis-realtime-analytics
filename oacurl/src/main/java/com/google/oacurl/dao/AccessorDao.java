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

package com.google.oacurl.dao;

import java.util.Properties;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;

/**
 * Class to load and save {@link OAuthAccessor} objects, which contain OAuth
 * access tokens, to and from a properties file.
 *
 * @author phopkins@google.com
 */
public class AccessorDao {
  private static final String ACCESS_TOKEN_PROPERTY = "accessToken";
  private static final String ACCESS_TOKEN_SECRET_PROPERTY = "accessTokenSecret";

  public OAuthAccessor newAccessor(OAuthConsumer consumer) {
    return new OAuthAccessor(consumer);
  }

  public OAuthAccessor loadAccessor(Properties properties, OAuthConsumer consumer) {
    OAuthAccessor accessor = newAccessor(consumer);

    accessor.accessToken = properties.getProperty(ACCESS_TOKEN_PROPERTY);
    accessor.tokenSecret = properties.getProperty(ACCESS_TOKEN_SECRET_PROPERTY);

    return accessor;
  }

  public void saveAccessor(OAuthAccessor accessor, Properties properties) {
    properties.setProperty(ACCESS_TOKEN_PROPERTY, accessor.accessToken);
    properties.setProperty(ACCESS_TOKEN_SECRET_PROPERTY, accessor.tokenSecret);
  }
}
