package com.hopebalie.btp.camel;

import static com.hopebalie.btp.camel.BtpConstants.BTP_MESSAGE_TYPE_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_REQUEST_ID_HEADER;
import static com.hopebalie.btp.camel.BtpConstants.BTP_SUBPROTOCOL_COUNT_HEADER;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.encoding.asn.framework.CodecContextFactory;

import com.hopebalie.btp.BtpMessage;
import com.hopebalie.btp.BtpMessageType;
import com.hopebalie.btp.BtpSubProtocol;
import com.hopebalie.btp.BtpSubProtocols;
import com.hopebalie.btp.codecs.AsnBtpMessageCodec;
import com.hopebalie.btp.codecs.AsnBtpSubProtocolCodec;
import com.hopebalie.btp.codecs.AsnBtpSubProtocolsCodec;
import com.hopebalie.btp.serializers.oer.AsnBtpSubProtocolsOerSerializer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.DataFormat;

import java.io.InputStream;
import java.io.OutputStream;

public class BtpMessageDataFormat implements DataFormat {

  private static final CodecContext _context
      = CodecContextFactory.getContext(CodecContextFactory.OCTET_ENCODING_RULES);

  static {
    _context
        .register(BtpMessage.class, AsnBtpMessageCodec::new)
        .register(BtpSubProtocol.class, AsnBtpSubProtocolCodec::new)
        .register(BtpSubProtocols.class, AsnBtpSubProtocolsCodec::new, new AsnBtpSubProtocolsOerSerializer());
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
    BtpSubProtocols protocols = exchange.getContext().getTypeConverter().mandatoryConvertTo(BtpSubProtocols.class, graph);
    BtpMessage message = BtpMessage.builder()
      .requestId(exchange.getIn().getHeader(BTP_REQUEST_ID_HEADER, Long.class))
      .type(exchange.getIn().getHeader(BTP_MESSAGE_TYPE_HEADER, BtpMessageType.class))
      .subProtocols(protocols)
      .build();
    _context.write(message, stream);
  }

  /**
   * Unmarshals the given stream into an object.
   *
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
    BtpMessage message = _context.read(BtpMessage.class, stream);
    BtpSubProtocols protocols = message.getSubProtocols();
    exchange.getOut().setHeader(BTP_REQUEST_ID_HEADER, message.getRequestId());
    exchange.getOut().setHeader(BTP_MESSAGE_TYPE_HEADER, message.getType());
    exchange.getOut().setHeader(BTP_SUBPROTOCOL_COUNT_HEADER, protocols.size());
    return protocols;
  }
}
