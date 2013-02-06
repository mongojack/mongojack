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
package org.mongojack.internal.stream;

import org.bson.io.OutputBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream that writes to a MongoDB OutputBuffer
 */
public class OutputBufferOutputStream extends OutputStream {
    private final OutputBuffer outputBuffer;
    private int count;

    public OutputBufferOutputStream(OutputBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        outputBuffer.write(b);
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputBuffer.write(b, off, len);
        count += len;
    }

    public int getCount() {
        return count;
    }
}
