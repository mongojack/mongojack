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
package org.mongojack;

import tools.jackson.core.JacksonException;

import tools.jackson.databind.DatabindException;
import com.mongodb.MongoException;

/**
 * Exception used to indicate a problem occurred when converting the MongoDB
 * objects to Jackson
 * 
 * @author James Roper
 * @since 1.0
 */
public class MongoDatabindException extends MongoException {

    public MongoDatabindException(String msg) {
        super(msg);
    }

    public MongoDatabindException(DatabindException e) {
        super("Error mapping BSON to POJOs", e);
    }

    public MongoDatabindException(String msg, DatabindException e) {
        super(msg, e);
    }

    public MongoDatabindException(String msg, JacksonException e) {
        super(msg, e);
    }

}
