package org.apache.log4j.netty;

import org.apache.log4j.netty.NettyClient.ResponseCode;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

public class ClientHandler extends SimpleChannelUpstreamHandler {

  private Channel channel;
  private ChannelGroup channelGroup;
  private ClientHandlerListener listener;

  private boolean channelConnected;
  private boolean channelClosed;

  public ClientHandler(ChannelGroup channelGroup) {
    this.channelGroup = channelGroup;
  }

  public ClientHandler(ClientHandlerListener listener, ChannelGroup channelGroup) {
    this.listener = listener;
    this.channelGroup = channelGroup;
  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    this.channel = e.getChannel();
    this.channel.setReadable(true);
    this.channelGroup.add(this.channel);
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    this.channelClosed = true;
    super.channelClosed(ctx, e);
  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    this.channelConnected = false;
    super.channelDisconnected(ctx, e);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    if (e.getMessage() instanceof org.apache.log4j.netty.MessageEvent && this.listener != null) {
      this.listener.messageReceived((org.apache.log4j.netty.MessageEvent) e.getMessage());
    } else {
      super.messageReceived(ctx, e);
    }
  }

  public void sendMessage(org.apache.log4j.netty.MessageEvent message) throws ClientException {
    if (this.channel != null) {
      this.channel.write(message);
    } else {
      throw new ClientException("Channel is not initialized properly.", ResponseCode.SEND_FAILURE);
    }
  }

  public boolean isChannelConnected() {
    return channelConnected;
  }

  public boolean isChannelClosed() {
    return channelClosed;
  }

}
