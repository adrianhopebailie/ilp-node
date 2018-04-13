package com.hopebailie.ilp.camel;

import org.interledger.core.InterledgerPacket;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.encoding.asn.framework.CodecContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.DataFormat;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Marshals and unmarshals an ILP packet
 */
public class InterledgerDataFormat implements DataFormat {

  private static final CodecContext _context = InterledgerCodecContextFactory.oer();

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
    InterledgerPacket message = exchange.getContext().getTypeConverter().mandatoryConvertTo(InterledgerPacket.class,
        graph);
    _context.write(message, stream);
  }

  /**
   * Unmarshals the given stream into an object.
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
    return _context.read(InterledgerPacket.class, stream);
  }
}
