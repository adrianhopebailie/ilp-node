package com.hopebailie.ilp;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerRejectPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Thrown if an incoming request .
 */
public class RequestRejectedException extends Exception {

  private final InterledgerErrorCode code;


  /**
   * Constructs a new runtime exception with {@code F99 Application Error} as its error code and an empty string  as
   * detail message.  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   */
  public RequestRejectedException() {
    super();
    this.code = InterledgerErrorCode.F99_APPLICATION_ERROR;
  }

  /**
   * Constructs a new runtime exception with the given code and detail message.  The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause}.
   */
  public RequestRejectedException(InterledgerErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  /**
   * Constructs a new runtime exception with the specified detail message and cause.  <p>Note that
   * the detail message associated with {@code cause} is <i>not</i> automatically incorporated in
   * this runtime exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *                #getMessage()} method).
   * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()}
   *                method).  (A <tt>null</tt> value is permitted, and indicates that the cause is
   *                nonexistent or unknown.)
   *
   * @since 1.4
   */
  public RequestRejectedException(InterledgerErrorCode code, String message, Throwable cause,
                                  InterledgerRejectPacket rejectMessage) {
    super(message, cause);
    this.code = code;
  }

  /**
   * Constructs a new runtime exception with the specified cause and with {@code F99 Application Error} as its error
   * code and a detail message of <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the
   * class and detail message of <tt>cause</tt>).  This constructor is useful for runtime exceptions that are little
   * more than wrappers for other throwables.
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *              (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent
   *              or unknown.)
   *
   * @since 1.4
   */
  public RequestRejectedException(Throwable cause) {
    super(cause);
    this.code = InterledgerErrorCode.F99_APPLICATION_ERROR;
  }

  public InterledgerErrorCode getCode() {
    return code;
  }

  /**
   * Build a reject message from the given exception and ILP address of the triggering entity
   *
   * @param exception the exception causing the request to be rejected.
   * @param triggeredBy the entity that triggered the exception
   *
   * @return an ILP reject message
   */
  public static InterledgerRejectPacket toRejectPacket(RequestRejectedException exception,
                                                       InterledgerAddress triggeredBy) {

    byte[] trace;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         PrintStream stream = new PrintStream(baos) ) {
      exception.printStackTrace(stream);
      stream.flush();
      trace = baos.toByteArray();
    } catch (IOException e) {
      trace = new byte[]{};
    }

    return InterledgerRejectPacket.builder()
        .code(exception.getCode())
        .message(exception.getMessage())
        .triggeredBy(triggeredBy)
        .data(trace)
        .build();
  }
}