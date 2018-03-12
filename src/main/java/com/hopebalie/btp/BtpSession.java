package com.hopebalie.btp;

import org.interledger.annotations.Immutable;

public interface BtpSession {

  static BtpSessionBuilder builder() {
    return new BtpSessionBuilder();
  }

  String getAccount();

  @Immutable
  abstract class AbstractBtpSession implements BtpSession {

  }

}
