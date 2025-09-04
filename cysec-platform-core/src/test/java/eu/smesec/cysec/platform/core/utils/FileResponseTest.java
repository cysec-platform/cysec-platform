/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileResponseTest {

    @Test
    void constructor_shouldStoreData() {
        byte[] data = "test content".getBytes();
        
        FileResponse response = new FileResponse(data);
        
        assertThat(response).isNotNull();
    }

    @Test
    void write_shouldWriteDataToOutputStream() throws IOException {
        byte[] data = "test file content".getBytes();
        FileResponse response = new FileResponse(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        response.write(outputStream);
        
        assertThat(outputStream.toByteArray()).isEqualTo(data);
    }

    @Test
    void write_shouldWriteEmptyData() throws IOException {
        byte[] data = new byte[0];
        FileResponse response = new FileResponse(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        response.write(outputStream);
        
        assertThat(outputStream.toByteArray()).isEmpty();
    }

    @Test
    void write_shouldWriteBinaryData() throws IOException {
        byte[] data = {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};
        FileResponse response = new FileResponse(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        response.write(outputStream);
        
        assertThat(outputStream.toByteArray()).isEqualTo(data);
    }

    @Test
    void write_shouldFlushAndCloseOutputStream() throws IOException {
        byte[] data = "test".getBytes();
        FileResponse response = new FileResponse(data);
        OutputStream mockOutput = mock(OutputStream.class);
        
        response.write(mockOutput);
        
        verify(mockOutput).write(data);
        verify(mockOutput).flush();
        verify(mockOutput).close();
    }

    @Test
    void write_shouldHandleNullData() throws IOException {
        FileResponse response = new FileResponse(null);
        OutputStream mockOutput = mock(OutputStream.class);
        
        assertThatThrownBy(() -> response.write(mockOutput))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void write_shouldHandleLargeData() throws IOException {
        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        
        FileResponse response = new FileResponse(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        response.write(outputStream);
        
        assertThat(outputStream.toByteArray()).isEqualTo(data);
        assertThat(outputStream.size()).isEqualTo(10000);
    }

    @Test
    void write_shouldPropagateIOException() throws IOException {
        byte[] data = "test".getBytes();
        FileResponse response = new FileResponse(data);
        OutputStream mockOutput = mock(OutputStream.class);
        
        doThrow(new IOException("Write failed")).when(mockOutput).write(data);
        
        assertThatThrownBy(() -> response.write(mockOutput))
            .isInstanceOf(IOException.class)
            .hasMessage("Write failed");
    }
}
