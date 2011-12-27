package net.vz.mongodb.jackson.mock;

public class MockByteArray {

    private byte[] data;

    public MockByteArray() {

    }

    public MockByteArray(byte[] data) {

        this();
        this.data = data;
    }

    public byte[] getData() {

        return data;
    }

    public void setData(byte[] data) {

        this.data = data;
    }

}
