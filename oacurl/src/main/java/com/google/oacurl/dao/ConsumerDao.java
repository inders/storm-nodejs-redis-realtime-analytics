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

import com.google.oacurl.options.LoginOptions;

import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;

/**
 * Small wrapper around a properties file that contains the consumerKey and
 * consumerSecret for an OAuth client.
 *
 * @author phopkins@google.com
 */
public class ConsumerDao {
  private static final String CONSUMER_KEY_PROPERTY = "consumerKey";
  private static final String CONSUMER_SECRET_PROPERTY = "consumerSecret";

  private final String defaultConsumerKey;
  private final String defaultConsumerSecret;

  public ConsumerDao() {
    defaultConsumerKey = "anonymous";
    defaultConsumerSecret = "anonymous";
  }

  public ConsumerDao(LoginOptions options) {
    defaultConsumerKey = (options.getConsumerKey() != null)
        ? options.getConsumerKey()
        : "anonymous";
    defaultConsumerSecret = (options.getConsumerSecret() != null)
        ? options.getConsumerSecret()
        : "anonymous";
  }

  public OAuthConsumer loadConsumer(Properties properties, OAuthServiceProvider serviceProvider) {
    String consumerKey = properties.getProperty(CONSUMER_KEY_PROPERTY, defaultConsumerKey);
    String consumerSecret = properties.getProperty(CONSUMER_SECRET_PROPERTY,
        defaultConsumerSecret);

    return new OAuthConsumer(null, consumerKey, consumerSecret, serviceProvider);
  }

  public void saveConsumer(OAuthConsumer consumer, Properties properties) {
    properties.setProperty(CONSUMER_KEY_PROPERTY, consumer.consumerKey);
    properties.setProperty(CONSUMER_SECRET_PROPERTY, consumer.consumerSecret);
  }
}
