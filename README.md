Log4j-Netty-Appender
====================

Log4j-Netty Appender

log4j.properties configuration.

======================================

log4j.rootLogger=DEBUG, netty

log4j.appender.netty=org.apache.log4j.net.NettyAppender
log4j.appender.netty.remoteHost=127.0.0.1
log4j.appender.netty.remotePort=8998
log4j.appender.netty.layout=org.apache.log4j.PatternLayout
log4j.appender.netty.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n