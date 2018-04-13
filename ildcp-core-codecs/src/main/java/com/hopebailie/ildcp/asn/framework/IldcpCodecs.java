package com.hopebailie.ildcp.asn.framework;

import org.interledger.encoding.asn.framework.CodecContext;

import com.hopebailie.ildcp.asn.codecs.AsnIldcpResponseCodec;
import com.hopebalie.ildcp.IldcpResponse;

public class IldcpCodecs {

  public static void register(CodecContext context) {
    context.register(IldcpResponse.class, AsnIldcpResponseCodec::new);
  }

}
