package com.hopebalie.btp.codecs;

import org.interledger.encoding.asn.codecs.AsnSequenceOfSequenceCodec;

import com.hopebalie.btp.BtpSubProtocol;
import com.hopebalie.btp.BtpSubProtocols;

public class AsnBtpSubProtocolsCodec
    extends AsnSequenceOfSequenceCodec<BtpSubProtocols, BtpSubProtocol> {

  public AsnBtpSubProtocolsCodec() {
    super(BtpSubProtocols::new, AsnBtpSubProtocolCodec::new);
  }
}
