package com.hopebailie.btp.asn.codecs;

import org.interledger.encoding.asn.codecs.AsnSequenceCodec;
import org.interledger.encoding.asn.framework.AsnObjectCodec;

import com.hopebailie.btp.BtpPacket;

public abstract class AsnBtpPacketDataCodec<T extends BtpPacket> extends AsnSequenceCodec<T> {

  private final long requestId;

  public AsnBtpPacketDataCodec(long requestId, AsnObjectCodec... fields) {
    super(fields);
    this.requestId = requestId;
  }

  protected final long getRequestId() {
    return requestId;
  }

}
