package com.hopebailie.btp.camel;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.encoding.asn.framework.CodecContextFactory;

import com.hopebailie.btp.BtpError;
import com.hopebailie.btp.BtpMessage;
import com.hopebailie.btp.BtpMessageType;
import com.hopebailie.btp.BtpPacket;
import com.hopebailie.btp.BtpResponse;
import com.hopebailie.btp.BtpTransfer;
import com.hopebailie.btp.asn.framework.BtpCodecs;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.DataFormat;

import java.io.InputStream;
import java.io.OutputStream;

public class BtpMessageDataFormat implements DataFormat {

  private static final CodecContext _context
      = CodecContextFactory.getContext(CodecContextFactory.OCTET_ENCODING_RULES);

  static {
    BtpCodecs.register(_context);
  }

  /**
   * Marshals the object to the given Stream.
   *
   * @param exchange the current exchange
   * @param graph    the object to be marshalled
   * @param stream   the output stream to write the marshalled result to
   * @throws Exception can be thrown
   */
  @Override
  public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {

    BtpPacket packet = exchange.getContext().getTypeConverter().mandatoryConvertTo(BtpPacket.class, graph);
    BtpMessageType type = packet.getType();

    switch (type) {
      case RESPONSE:
        _context.write(BtpResponse.class.cast(packet), stream);
        return;
      case ERROR:
        _context.write(BtpError.class.cast(packet), stream);
        return;
      case MESSAGE:
        _context.write(BtpMessage.class.cast(packet), stream);
        return;
      case TRANSFER:
        _context.write(BtpTransfer.class.cast(packet), stream);
    }

  }

  /**
   * Unmarshals the given stream into an object.
   * <p>
   * <p/>
   * <b>Notice:</b> The result is set as body on the exchange OUT message.
   * It is possible to mutate the OUT message provided in the given exchange parameter.
   * For instance adding headers to the OUT message will be preserved.
   * <p/>
   * It's also legal to return the <b>same</b> passed <tt>exchange</tt> as is but also a
   * {@link Message} object as well which will be used as the OUT message of <tt>exchange</tt>.
   *
   * @param exchange the current exchange
   * @param stream   the input stream with the object to be unmarshalled
   * @return the unmarshalled object
   * @throws Exception can be thrown
   */
  @Override
  public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
    return _context.read(BtpPacket.class, stream);
  }
}
