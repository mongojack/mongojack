/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson.internal.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * InputStream that reads the first integer to find out the size of the document, and then returns end of file when that
 * size is reached.
 *
 * @author James Roper
 * @since 1.1.2
 */
public class LimitingInputStream extends InputStream {

    private final InputStream is;
    private final int size;
    private final byte[] sizeBytes;
    private volatile int count;

    public LimitingInputStream(InputStream is) throws IOException {
        this.is = is;
        sizeBytes = new byte[4];
        int c = 0;
        while (c < 4) {
            int l = is.read(sizeBytes, c, 4 - c);
            if (l == -1) {
                throw new IOException("No size read");
            }
            c += l;
        }
        ByteBuffer buffer = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN);
        size = buffer.getInt();
    }

    @Override
    public synchronized int read() throws IOException {
        if (count < 4) {
            return (int) sizeBytes[count++] & 0xff;
        }
        if (count < size) {
            count++;
            int i = is.read();
            System.out.print((char) i);
            return i;
        } else {
            return -1;
        }
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        // If we've exceeded the size, return EOF
        if (count > size) {
            return -1;
        }
        if (len + count > size) {
            // Only what's remaining
            len = size - count;
        }

        int bytesRead = 0;

        // If we haven't read the first int yet, copy that into the buffer
        if (count < 4) {
            // Only copy the minimum of what's left, and len
            bytesRead = Math.min(4 - count, len);
            System.arraycopy(sizeBytes, count, b, off, bytesRead);
        }

        // If we still have bytes to read (we probably do), read them from the stream
        if (bytesRead < len) {
            int l = is.read(b, off + bytesRead, len - bytesRead);
            if (l != -1) {
                bytesRead += l;
            } else if (bytesRead == 0) {
                bytesRead = -1;
            }
        }
        if (bytesRead != -1) {
            count += bytesRead;
        }

        return bytesRead;
    }

    @Override
    public int available() throws IOException {
        return Math.min(size - count, is.available());
    }

    @Override
    public void close() throws IOException {
        // Don't close the underlying stream
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public void reset() throws IOException {
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
