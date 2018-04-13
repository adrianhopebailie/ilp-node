package com.hopebailie.btp.asn.codecs;

import org.interledger.encoding.asn.codecs.AsnSequenceOfSequenceCodec;

import com.hopebailie.btp.BtpSubProtocol;
import com.hopebailie.btp.BtpSubProtocols;

public class AsnBtpSubProtocolsCodec
    extends AsnSequenceOfSequenceCodec<BtpSubProtocols, BtpSubProtocol> {

  public AsnBtpSubProtocolsCodec() {
    super(BtpSubProtocols::new, AsnBtpSubProtocolCodec::new);
  }
}
