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

import org.bson.types.ObjectId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Converts bson4jackson ObjectIds to bson ObjectIds
 *
 * @author James Roper
 * @since 1.1.2
 */
public class ObjectIdConvertor {

    public static ObjectId convert(de.undercouch.bson4jackson.types.ObjectId objectId) {
        // bson4jackson uses little endian to decode the ids, while org.bson uses big endian... need to convert
        byte[] buf = new byte[12];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(objectId.getTime()).putInt(objectId.getMachine()).putInt(objectId.getInc());
        return new ObjectId(buf);
    }
}
