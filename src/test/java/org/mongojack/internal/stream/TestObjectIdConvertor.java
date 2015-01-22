package org.mongojack.internal.stream;

import de.undercouch.bson4jackson.types.ObjectId;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestObjectIdConvertor {
    @Test
    public void testConversion() {
        ObjectId jacksonObjectId = new ObjectId(Integer.MAX_VALUE, 0xff, 0xf);
        org.bson.types.ObjectId javaDriverObjectId = ObjectIdConvertor.convert(jacksonObjectId);

        byte[] expectedBytes = {127, -1, -1, -1, 0, 0, 0, -1, 0, 0, 0, 15};

        assertThat(javaDriverObjectId.toByteArray(), equalTo(expectedBytes));

        ObjectId convertedJacksonObjectId = ObjectIdConvertor.convert(javaDriverObjectId);

        assertThat(jacksonObjectId.getInc(), equalTo(convertedJacksonObjectId.getInc()));
        assertThat(jacksonObjectId.getMachine(), equalTo(convertedJacksonObjectId.getMachine()));
        assertThat(jacksonObjectId.getTime(), equalTo(convertedJacksonObjectId.getTime()));
    }
}
