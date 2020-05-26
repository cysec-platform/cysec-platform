package eu.smesec.platform.utils;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 * <p>Provides file downloading as entity in {@link javax.ws.rs.core.Response}.</p>
 */
public class FileResponse implements StreamingOutput {
  private byte[] data;

  /**
   * <p>Constructor</p>
   * @param data file content
   */
  public FileResponse(byte[] data) {
    this.data = data;
  }

  @Override
  public void write(OutputStream output) throws IOException, WebApplicationException {
    output.write(data);
    output.flush();
    output.close();
  }
}
