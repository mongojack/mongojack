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
package org.mongojack.internal;

import org.mongojack.JacksonDBCollection;

/**
 * Provides a JacksonDBCollection
 *
 * @author James Roper
 * @since 1.2
 */
public interface JacksonDBCollectionProvider {

    /**
     * Get the JacksonDBCollection that this object knows about
     *
     * @return The JackosnDBCollection
     */
    JacksonDBCollection getDBCollection();
}
