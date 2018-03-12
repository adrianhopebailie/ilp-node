package com.hopebalie.btp.codecs;

import org.interledger.encoding.asn.codecs.AsnSequenceCodec;
import org.interledger.encoding.asn.codecs.AsnUint32Codec;
import org.interledger.encoding.asn.codecs.AsnUint8Codec;

import com.hopebalie.btp.BtpMessage;
import com.hopebalie.btp.BtpMessageType;

public class AsnBtpMessageCodec extends AsnSequenceCodec<BtpMessage> {


  public AsnBtpMessageCodec() {
    super(
        new AsnUint8Codec(),
        new AsnUint32Codec(),
        new AsnBtpSubProtocolsCodec()
    );
  }

  /**
   * Decode and return the value read into the codec during serialization.
   *
   * @return the decoded object
   */
  @Override
  public BtpMessage decode() {
    return BtpMessage.builder()
        .type(BtpMessageType.fromCode(getValueAt(0)))
        .requestId(getValueAt(1))
        .subProtocols(getValueAt(2))
        .build();
  }

  /**
   * Encode the provided value into the codec to be written during serialization.
   *
   * @param value the value to encode
   */
  @Override
  public void encode(BtpMessage value) {
    setValueAt(0, value.getType().getCode());
    setValueAt(1, value.getRequestId());
    setValueAt(2, value.getSubProtocols());
  }
}
