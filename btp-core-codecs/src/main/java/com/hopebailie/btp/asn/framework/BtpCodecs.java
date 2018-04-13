package com.hopebailie.btp.asn.framework;

import org.interledger.encoding.asn.framework.CodecContext;

import com.hopebailie.btp.BtpError;
import com.hopebailie.btp.BtpMessage;
import com.hopebailie.btp.BtpPacket;
import com.hopebailie.btp.BtpResponse;
import com.hopebailie.btp.BtpSubProtocol;
import com.hopebailie.btp.BtpSubProtocols;
import com.hopebailie.btp.BtpTransfer;
import com.hopebailie.btp.asn.codecs.AsnBtpErrorCodec;
import com.hopebailie.btp.asn.codecs.AsnBtpMessageCodec;
import com.hopebailie.btp.asn.codecs.AsnBtpPacketCodec;
import com.hopebailie.btp.asn.codecs.AsnBtpResponseCodec;
import com.hopebailie.btp.asn.codecs.AsnBtpSubProtocolCodec;
import com.hopebailie.btp.asn.codecs.AsnBtpSubProtocolsCodec;
import com.hopebailie.btp.asn.codecs.AsnBtpTransferCodec;

public class BtpCodecs {

  public static void register(CodecContext context) {
    context
        .register(BtpError.class, AsnBtpErrorCodec::new)
        .register(BtpMessage.class, AsnBtpMessageCodec::new)
        .register(BtpPacket.class, AsnBtpPacketCodec::new)
        .register(BtpResponse.class, AsnBtpResponseCodec::new)
        .register(BtpSubProtocol.class, AsnBtpSubProtocolCodec::new)
        .register(BtpSubProtocols.class, AsnBtpSubProtocolsCodec::new)
        .register(BtpTransfer.class, AsnBtpTransferCodec::new);
  }

}
