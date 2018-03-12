package com.hopebalie.btp.serializers.oer;

import static java.lang.String.format;

import org.interledger.encoding.asn.codecs.AsnSequenceOfSequenceCodec;
import org.interledger.encoding.asn.framework.AsnObjectSerializationContext;
import org.interledger.encoding.asn.framework.CodecException;
import org.interledger.encoding.asn.serializers.oer.AsnSequenceOfSequenceOerSerializer;
import org.interledger.encoding.asn.serializers.oer.OerLengthSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AsnBtpSubProtocolsOerSerializer extends AsnSequenceOfSequenceOerSerializer {

  @Override
  public void read(AsnObjectSerializationContext context, AsnSequenceOfSequenceCodec instance, InputStream inputStream) throws IOException {
    int length = OerLengthSerializer.readLength(inputStream);

    if(length == 0) {
      instance.setSize(0);
      return;
    }

    byte[] buffer = new byte[length];
    int bytesRead = inputStream.read(buffer);
    if (bytesRead < length) {
      throw new CodecException(
          format("Unexpected end of stream. Expected %s bytes but only read %s.",
              length, bytesRead));
    }
    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    super.read(context, instance, bais);
  }

  @Override
  public void write(AsnObjectSerializationContext context, AsnSequenceOfSequenceCodec instance, OutputStream outputStream) throws IOException {

    if(instance.size() == 0) {
      //Just write out a zero length and return
      OerLengthSerializer.writeLength(0, outputStream);
      return;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    super.write(context, instance, baos);
    byte[] buffer = baos.toByteArray();

    OerLengthSerializer.writeLength(buffer.length, outputStream);
    outputStream.write(buffer);

  }
}
