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
package org.mongojack;

import com.mongodb.*;
import org.mongojack.internal.query.CollectionQueryCondition;
import org.mongojack.internal.query.CompoundQueryCondition;
import org.mongojack.internal.query.QueryCondition;
import org.mongojack.internal.util.SerializationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An iterator over database results.
 * This class is not threadsafe and is intended to be used from a single thread or synchronized.
 * Doing a <code>find()</code> query on a collection returns a
 * <code>DBCursor</code> thus
 * <p/>
 * <blockquote><pre>
 * DBCursor cursor = collection.find( query );
 * if( cursor.hasNext() )
 *     T obj = cursor.next();
 * </pre></blockquote>
 * <p/>
 * <p><b>Warning:</b> Calling <code>toArray</code> or <code>length</code> on
 * a DBCursor will irrevocably turn it into an array.  This
 * means that, if the cursor was iterating over ten million results
 * (which it was lazily fetching from the database), suddenly there will
 * be a ten-million element array in memory.  Before converting to an array,
 * make sure that there are a reasonable number of results using
 * <code>skip()</code> and <code>limit()</code>.
 * <p>For example, to get an array of the 1000-1100th elements of a cursor, use
 * <p/>
 * <blockquote><pre>
 * List<DBObject> obj = collection.find( query ).skip( 1000 ).limit( 100 ).toArray();
 * </pre></blockquote>
 *
 * @author James Roper
 * @since 1.0
 */
public class DBCursor<T> extends DBQuery.AbstractBuilder<DBCursor<T>> implements Iterator<T>, Iterable<T> {
    private final com.mongodb.DBCursor cursor;
    private final JacksonDBCollection<T, ?> jacksonDBCollection;

    // For use in iterator mode
    private T current;
    // For use in array mode
    private final List<T> all = new ArrayList<T>();
    // Flag to indicate that the query has been executed
    private boolean executed;

    public DBCursor(JacksonDBCollection<T, ?> jacksonDBCollection, com.mongodb.DBCursor cursor) {
        this.jacksonDBCollection = jacksonDBCollection;
        this.cursor = cursor;
        if (jacksonDBCollection.isEnabled(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION)) {
            this.cursor.setDecoderFactory(jacksonDBCollection.getDecoderFactory());
        }
    }

    /**
     * Creates a copy of an existing database cursor.
     * The new cursor is an iterator, even if the original
     * was an array.
     *
     * @return the new cursor
     */
    public DBCursor<T> copy() {
        return new DBCursor<T>(jacksonDBCollection, cursor.copy());
    }

    /**
     * creates a copy of this cursor object that can be iterated.
     * Note:
     * - you can iterate the DBCursor itself without calling this method
     * - no actual data is getting copied.
     *
     * @return The iterator
     */
    public Iterator<T> iterator() {
        return this.copy();
    }

    // ---- query modifiers --------

    /**
     * Sorts this cursor'string elements.
     * This method must be called before getting any object from the cursor.
     *
     * @param orderBy the fields by which to sort
     * @return a cursor pointing to the first element of the sorted results
     */
    public DBCursor<T> sort(DBObject orderBy) {
        cursor.sort(orderBy);
        return this;
    }

    /**
     * adds a special operator like $maxScan or $returnKey
     * e.g. addSpecial( "$returnKey" , 1 )
     * e.g. addSpecial( "$maxScan" , 100 )
     *
     * @param name The name
     * @param o    The object
     * @return This object
     */
    public DBCursor<T> addSpecial(String name, Object o) {
        cursor.addSpecial(name, o);
        return this;
    }

    /**
     * Informs the database of indexed fields of the collection in order to improve performance.
     *
     * @param indexKeys a <code>DBObject</code> with fields and direction
     * @return same DBCursor for chaining operations
     */
    public DBCursor<T> hint(DBObject indexKeys) {
        cursor.hint(indexKeys);
        return this;
    }

    /**
     * Informs the database of an indexed field of the collection in order to improve performance.
     *
     * @param indexName the name of an index
     * @return same JacksonDBCursor<T>t for chaining operations
     */
    public DBCursor<T> hint(String indexName) {
        cursor.hint(indexName);
        return this;
    }

    /**
     * Use snapshot mode for the query. Snapshot mode assures no duplicates are
     * returned, or objects missed, which were present at both the start and end
     * of the query'string execution (if an object is new during the query, or deleted
     * during the query, it may or may not be returned, even with snapshot mode).
     * Note that short query responses (less than 1MB) are always effectively snapshotted.
     * Currently, snapshot mode may not be used with sorting or explicit hints.
     *
     * @return same JacksonDBCursor<T> for chaining operations
     */
    public DBCursor<T> snapshot() {
        cursor.snapshot();
        return this;
    }

    /**
     * Returns an object containing basic information about the
     * execution of the query that created this cursor
     * This creates a <code>DBObject</code> with the key/value pairs:
     * "cursor" : cursor type
     * "nScanned" : number of records examined by the database for this query
     * "n" : the number of records that the database returned
     * "millis" : how long it took the database to execute the query
     *
     * @return a <code>DBObject</code>
     */
    public DBObject explain() {
        return cursor.explain();
    }

    /**
     * Limits the number of elements returned.
     * Note: parameter <tt>n</tt> should be positive, although a negative value is supported for legacy reason.
     * Passing a negative value will call {@link DBCursor <T>#batchSize(int)} which is the preferred method.
     *
     * @param n the number of elements to return
     * @return a cursor to iterate the results
     */
    public DBCursor<T> limit(int n) {
        cursor.limit(n);
        return this;
    }

    /**
     * Limits the number of elements returned in one batch.
     * A cursor typically fetches a batch of result objects and store them locally.
     * <p/>
     * If <tt>batchSize</tt> is positive, it represents the size of each batch of objects retrieved.
     * It can be adjusted to optimize performance and limit data transfer.
     * <p/>
     * If <tt>batchSize</tt> is negative, it will limit of number objects returned, that fit within the max batch size limit (usually 4MB), and cursor will be closed.
     * For example if <tt>batchSize</tt> is -10, then the server will return a maximum of 10 documents and as many as can fit in 4MB, then close the cursor.
     * Note that this feature is different from limit() in that documents must fit within a maximum size, and it removes the need to send a request to close the cursor server-side.
     * <p/>
     * The batch size can be changed even after a cursor is iterated, in which case the setting will apply on the next batch retrieval.
     *
     * @param n the number of elements to return in a batch
     * @return This object
     */
    public DBCursor<T> batchSize(int n) {
        cursor.batchSize(n);
        return this;
    }

    /**
     * Discards a given number of elements at the beginning of the cursor.
     *
     * @param n the number of elements to skip
     * @return a cursor pointing to the new first element of the results
     * @throws RuntimeException if the cursor has started to be iterated through
     */
    public DBCursor<T> skip(int n) {
        cursor.skip(n);
        return this;
    }

    /**
     * gets the cursor id.
     *
     * @return the cursor id, or 0 if there is no active cursor.
     */
    public long getCursorId() {
        return cursor.getCursorId();
    }

    /**
     * kills the current cursor on the server.
     */
    public void close() {
        cursor.close();
    }

    /**
     * adds a query option - see Bytes.QUERYOPTION_* for simpleList
     *
     * @param option The option
     * @return This object
     */
    public DBCursor<T> addOption(int option) {
        cursor.addOption(option);
        return this;
    }

    /**
     * sets the query option - see Bytes.QUERYOPTION_* for simpleList
     *
     * @param options The options
     * @return This object
     */
    public DBCursor<T> setOptions(int options) {
        cursor.setOptions(options);
        return this;
    }

    /**
     * resets the query options
     *
     * @return This object
     */
    public DBCursor<T> resetOptions() {
        cursor.resetOptions();
        return this;
    }

    /**
     * gets the query options
     *
     * @return The options
     */
    public int getOptions() {
        return cursor.getOptions();
    }

    /**
     * gets the number of times, so far, that the cursor retrieved a batch from the database
     *
     * @return The number of get mores
     */
    public int numGetMores() {
        return cursor.numGetMores();
    }

    /**
     * gets a simpleList containing the number of items received in each batch
     *
     * @return The sizes of each batch
     */
    public List<Integer> getSizes() {
        return cursor.getSizes();
    }

    /**
     * Returns the number of objects through which the cursor has iterated.
     *
     * @return the number of objects seen
     */
    public int numSeen() {
        return cursor.numSeen();
    }

    // ----- iterator api -----

    /**
     * Checks if there is another object available
     *
     * @return true if there is another object available
     * @throws MongoException
     */
    public boolean hasNext() throws MongoException {
        executed();
        return cursor.hasNext();
    }

    /**
     * Returns the object the cursor is at and moves the cursor ahead by one.
     *
     * @return the next element
     * @throws MongoException
     */
    public T next() throws MongoException {
        executed();
        current = jacksonDBCollection.convertFromDbObject(cursor.next());
        return current;
    }

    /**
     * Returns the element the cursor is at.
     *
     * @return the next element
     */
    public T curr() {
        // This triggers the checks to be done, we ignore the result
        cursor.curr();
        return current;
    }

    /**
     * Not implemented.
     */
    public void remove() {
        cursor.remove();
    }

    /**
     * pulls back all items into an array and returns the number of objects.
     * Note: this can be resource intensive
     *
     * @return the number of elements in the array
     * @throws MongoException Ig as error occurred
     * @see #count()
     * @see #size()
     */
    public int length() throws MongoException {
        executed();
        return cursor.length();
    }

    /**
     * Converts this cursor to an array.
     *
     * @return an array of elements
     * @throws MongoException If an error occurred
     */
    public List<T> toArray() throws MongoException {
        executed();
        return toArray(Integer.MAX_VALUE);
    }

    /**
     * Converts this cursor to an array.
     *
     * @param max the maximum number of objects to return
     * @return an array of objects
     * @throws MongoException If an error occurred
     */
    public List<T> toArray(int max) throws MongoException {
        executed();
        if (max > all.size()) {
            List<DBObject> objects = cursor.toArray(max);
            for (int i = all.size(); i < objects.size(); i++) {
                all.add(jacksonDBCollection.convertFromDbObject(objects.get(i)));
            }
        }
        return all;
    }

    /**
     * for testing only!
     * Iterates cursor and counts objects
     *
     * @return num objects
     * @see #count()
     */
    public int itcount() {
        executed();
        return cursor.itcount();
    }

    /**
     * Counts the number of objects matching the query
     * This does not take limit/skip into consideration
     *
     * @return the number of objects
     * @throws MongoException
     * @see #size()
     */
    public int count() {
        executed();
        return cursor.count();
    }

    /**
     * Counts the number of objects matching the query
     * this does take limit/skip into consideration
     *
     * @return the number of objects
     * @throws MongoException
     * @see #count()
     */
    public int size() {
        executed();
        return cursor.size();
    }


    /**
     * gets the fields to be returned
     *
     * @return The keys wanted
     */
    public DBObject getKeysWanted() {
        return cursor.getKeysWanted();
    }

    /**
     * gets the query
     *
     * @return The query
     */
    public DBObject getQuery() {
        return cursor.getQuery();
    }

    /**
     * gets the collection
     *
     * @return The collection
     */
    public JacksonDBCollection getCollection() {
        return jacksonDBCollection;
    }

    /**
     * Gets the Server Address of the server that data is pulled from.
     * Note that this information is not available if no data has been retrieved yet.
     * Availability is specific to underlying implementation and may vary.
     *
     * @return The server address
     */
    public ServerAddress getServerAddress() {
        return cursor.getServerAddress();
    }

    /**
     * Sets the read preference for this cursor.
     * See the * documentation for {@link ReadPreference}
     * for more information.
     *
     * @param preference Read Preference to use
     * @return This object
     */
    public DBCursor<T> setReadPreference(ReadPreference preference) {
        cursor.setReadPreference(preference);
        return this;
    }

    /**
     * Gets the default read preference
     *
     * @return The read preference
     */
    public ReadPreference getReadPreference() {
        return cursor.getReadPreference();
    }

    public DBCursor<T> setDecoderFactory(DBDecoderFactory fact) {
        cursor.setDecoderFactory(fact);
        return this;
    }

    public DBDecoderFactory getDecoderFactory() {
        return cursor.getDecoderFactory();
    }

    /**
     * Get the underlying MongoDB cursor.  Note, if this is an iterator cursor, calling next() on the underlying cursor
     * will cause this iterator to also progress forward, however, curr() will still return the last object that was
     * loaded by this cursor, not the underlying cursor.
     *
     * @return The underlying MongoDB cursor
     */
    public com.mongodb.DBCursor getCursor() {
        return cursor;
    }

    private void executed() {
        executed = true;
    }

    private void checkExecuted() {
        if (executed) {
            throw new MongoException("Cannot modify query after it's been executed");
        }
    }

    protected DBCursor<T> put(String op, QueryCondition value) {
        checkExecuted();
        cursor.getQuery().put(op, jacksonDBCollection.serializeQueryCondition(op, value));
        return this;
    }

    protected DBCursor<T> put(String field, String op, QueryCondition value) {
        checkExecuted();
        DBObject subQuery;
        Object saved = cursor.getQuery().get(field);
        if (!(saved instanceof DBObject)) {
            subQuery = new BasicDBObject();
            cursor.getQuery().put(field, subQuery);
        } else {
            subQuery = (DBObject) saved;
        }
        subQuery.put(op, jacksonDBCollection.serializeQueryCondition(field, value));
        return this;
    }

    protected DBCursor<T> putGroup(String op, DBQuery.Query... expressions) {
        checkExecuted();
        List<DBObject> conditions = new ArrayList<DBObject>();
        Object existing = cursor.getQuery().get(op);
        if (existing == null) {
            cursor.getQuery().put(op, conditions);
        } else if (existing instanceof List) {
            conditions.addAll((List) existing);
        } else {
            throw new IllegalStateException("Expecting collection for " + op);
        }
        for (DBQuery.Query query : expressions) {
            conditions.add(jacksonDBCollection.serializeQuery(query));
        }
        return this;
    }
}
