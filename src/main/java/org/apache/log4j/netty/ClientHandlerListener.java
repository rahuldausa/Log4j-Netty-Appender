package org.apache.log4j.netty;


public interface ClientHandlerListener {

  void messageReceived(MessageEvent message);
}
