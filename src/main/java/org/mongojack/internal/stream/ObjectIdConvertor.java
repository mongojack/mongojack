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

import org.bson.types.ObjectId;

/**
 * Converts bson4jackson ObjectIds to bson ObjectIds
 *
 * @author James Roper
 * @since 1.1.2
 */
public class ObjectIdConvertor {

    public static ObjectId convert(de.undercouch.bson4jackson.types.ObjectId objectId) {
        return new ObjectId(objectId.getTime(), objectId.getMachine(), objectId.getInc());
    }

    public static de.undercouch.bson4jackson.types.ObjectId convert(ObjectId objectId) {
        return new de.undercouch.bson4jackson.types.ObjectId(objectId.getTimeSecond(), objectId.getMachine(),
                objectId.getInc());
    }

}
