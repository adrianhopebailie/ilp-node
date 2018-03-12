package com.hopebalie.btp;

import org.interledger.annotations.Immutable;

public interface BtpMessage {

  static BtpMessageBuilder builder() {
    return new BtpMessageBuilder();
  }

  BtpMessageType getType();

  long getRequestId();

  BtpSubProtocols getSubProtocols();

  default BtpSubProtocol getPrimarySubProtocol() {
    if (getSubProtocols().isEmpty()) {
      throw new IndexOutOfBoundsException("No sub-protocols");
    }
    return getSubProtocols().get(0);
  }

  default BtpSubProtocol getSubProtocol(String protocolName) {
    for (BtpSubProtocol protocol : getSubProtocols()) {
      if (protocol.getProtocolName().equals(protocolName)) {
        return protocol;
      }
    }
    return null;
  }

  default boolean hasSubProtocol(String protocolName) {
    for (BtpSubProtocol protocol : getSubProtocols()) {
      if (protocol.getProtocolName().equals(protocolName)) {
        return true;
      }
    }
    return false;
  };

  @Immutable
  abstract class AbstractBtpMessage implements BtpMessage {

  }

}
