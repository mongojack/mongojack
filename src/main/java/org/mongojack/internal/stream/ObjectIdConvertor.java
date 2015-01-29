/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack.internal.stream;

import org.bson.types.ObjectId;

/**
 * Converts bson4jackson ObjectIds to bson ObjectIds
 * 
 * @author James Roper
 * @since 1.1.2
 */
public class ObjectIdConvertor {

    public static ObjectId convert(de.undercouch.bson4jackson.types.ObjectId objectId) {
        return ObjectId.createFromLegacyFormat(objectId.getTime(), objectId.getMachine(), objectId.getInc());
    }

    public static de.undercouch.bson4jackson.types.ObjectId convert(ObjectId objectId) {
        byte[] bytes = objectId.toByteArray();

        return new de.undercouch.bson4jackson.types.ObjectId(makeInt(bytes[0], bytes[1], bytes[2], bytes[3]),
                                                             makeInt(bytes[4], bytes[5], bytes[6], bytes[7]),
                                                             makeInt(bytes[8], bytes[9], bytes[10], bytes[11]));
    }

    private static int makeInt(final byte b3, final byte b2, final byte b1, final byte b0) {
        return (((b3) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) << 8) |
                ((b0 & 0xff)));
    }

}
