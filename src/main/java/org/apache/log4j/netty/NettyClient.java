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
package org.apache.log4j.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 *
 * @author Rahul Jain
 */
public class NettyClient implements ClientHandlerListener {

  public static enum ResponseCode {
    SUCCESS, CONNECT_ERROR, SEND_FAILURE, UNKNOWN_ERROR;
  }

  private String host;
  private int port;

  private ChannelFactory channelFactory;
  private ChannelGroup channelGroup;
  private ClientHandler clientHandler;

  public NettyClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public boolean start() {
    Executor bossExecutor = Executors.newFixedThreadPool(1);
    Executor workerExecutor = Executors.newFixedThreadPool(10);
    int workerCount = 10;
    this.channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor, workerCount);
    this.channelGroup = new DefaultChannelGroup(this + "-channelGroup");
    this.clientHandler = new ClientHandler(this, channelGroup);

    ClientBootstrap bootstrap = new ClientBootstrap(this.channelFactory);
    bootstrap.setOption("reuseAddress", true);
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("keepAlive", true);
    bootstrap.setOption("sendBufferSize", 1048576);
    bootstrap.setOption("receiveBufferSize", 1048576);
    bootstrap.setOption("writeBufferLowWaterMark", 32 * 1024);
    bootstrap.setOption("writeBufferHighWaterMark", 64 * 1024);

    final ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(2,
        8 * 1024 * 1024, 16 * 1024 * 1024));
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addFirst("pipelineExecutor", executionHandler);
        pipeline.addLast("encoder", new ObjectEncoder());
        // pipeline.addLast("decoder", new ObjectDecoder(1024*1024,
        // ClassResolvers.weakCachingConcurrentResolver(null)));
        pipeline.addLast("handler", clientHandler);
        return pipeline;
      }
    });

    ChannelFuture future = bootstrap.connect(new InetSocketAddress(this.host, this.port)).awaitUninterruptibly();
    boolean connected = future.isSuccess();
    if (!connected) {
      this.stop();
    }
    return connected;
  }
  public void stop() {
    if (this.channelGroup != null) {
      this.channelGroup.close();
    }
    if (this.channelFactory != null) {
      this.channelFactory.releaseExternalResources();
    }
  }

  // TODO: handle properly
  public ResponseCode log(MessageEvent message) throws ClientException {
    clientHandler.sendMessage(message);
    return ResponseCode.SUCCESS;
  }

  public void close() {
    this.stop();
  }

  @Override
  public void messageReceived(MessageEvent message) {

  }
}
