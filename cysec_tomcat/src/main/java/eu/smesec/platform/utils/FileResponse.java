package eu.smesec.platform.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides file downloading as entity in {@link javax.ws.rs.core.Response}.
 */
public class FileResponse implements StreamingOutput {
  private final byte[] data;

  /**
   * File response constructor.
   *
   * @param data file content
   */
  public FileResponse(byte[] data) {
    this.data = data;
  }

  /**
   * Writes the file data to the output stream.
   *
   * @param output The output stream
   * @throws IOException If an io error occurs
   * @throws WebApplicationException If an general error occurs
   */
  @Override
  public void write(OutputStream output) throws IOException, WebApplicationException {
    output.write(data);
    output.flush();
    output.close();
  }
}

