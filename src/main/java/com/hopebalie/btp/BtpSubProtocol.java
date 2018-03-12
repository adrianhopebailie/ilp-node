package com.hopebalie.btp;

import org.interledger.annotations.Immutable;

import java.nio.charset.StandardCharsets;

public interface BtpSubProtocol {

  static BtpSubProtocolBuilder builder() {
    return new BtpSubProtocolBuilder();
  }

  String getProtocolName();

  BtpSubProtocolContentType getContentType();

  byte[] getSubProtocolData();

  default String getDataAsString() {
    return new String(getSubProtocolData(), StandardCharsets.UTF_8);
  }

  @Immutable
  abstract class AbstractBtpSubProtocol implements BtpSubProtocol {

  }

}
