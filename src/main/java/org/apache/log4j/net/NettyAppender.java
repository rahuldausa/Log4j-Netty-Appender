/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j.net;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.netty.ClientException;
import org.apache.log4j.netty.MessageEvent;
import org.apache.log4j.netty.NettyClient;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A basic Log4j Appender for sending log messages to a remote server based on netty instance.
 * @author Rahul Jain
 */
public class NettyAppender extends AppenderSkeleton {

  private static final String DEFAULT_REMOTE_HOST = "127.0.0.1";
  private static final int DEFAULT_REMOTE_PORT = 8998;

  private NettyClient client;
  private String remoteHost;
  private int remotePort;

  public NettyAppender() {

  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  @Override
  public void activateOptions() {
    remoteHost = (remoteHost == null) ? DEFAULT_REMOTE_HOST : remoteHost;
    remotePort = (remotePort == 0) ? DEFAULT_REMOTE_PORT : remotePort;
    connect();
    super.activateOptions();
  }

  private boolean connect() {
    if (isConnected()) {
      return true;
    }
    client = new NettyClient(remoteHost, remotePort);
    connected = client.start();
    if (!connected) {
      getErrorHandler().error("Can not establish connection to LoggingServer at[" + remoteHost + ":" + remotePort + "]");
    }
    return connected;
  }

  @Override
  public boolean requiresLayout() {
    return true;
  }

  @Override
  protected void append(LoggingEvent event) {
    MessageEvent message = buildMessage(event);
    boolean connected = connect();
    if (!connected) {
      getErrorHandler().error("Can not connect to logging server");
    } else {
      try {
        client.log(message);
      } catch (ClientException e) {
        if (e.getRespnseCode() != null) {
          getErrorHandler().error("[" + e.getRespnseCode() + "]" + e.getMessage());
        } else {
          getErrorHandler().error(e.getMessage());
        }
      }
    }
  }

  @Override
  public void close() {
    client.close();
  }

  private MessageEvent buildMessage(LoggingEvent event) {
    MessageEvent message = new MessageEvent();
    message.message = layout.format(event);
    return message;
  }

  private boolean connected;

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

}
