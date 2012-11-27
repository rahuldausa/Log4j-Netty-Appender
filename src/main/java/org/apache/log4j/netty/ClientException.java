package org.apache.log4j.netty;

import org.apache.log4j.netty.NettyClient.ResponseCode;

public class ClientException extends Exception {

  private static final long serialVersionUID = 1L;

  private ResponseCode responseCode;

  public ClientException() {

  }

  public ClientException(String msg) {
    super(msg);
  }

  public ClientException(Throwable e) {
    super(e);
  }

  public ClientException(String msg, Throwable e) {
    super(msg, e);
  }

  public ClientException(String msg, Throwable e, ResponseCode responseCode) {
    super(msg, e);
    this.responseCode = responseCode;
  }

  public ClientException(String msg, ResponseCode responseCode) {
    super(msg);
    this.responseCode = responseCode;
  }

  public ResponseCode getRespnseCode() {
    return this.responseCode;
  }

}
