/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

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
