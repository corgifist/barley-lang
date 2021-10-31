package com.barley.utils;

import java.io.*;

public class SerializeUtils {

    public static byte[] serialize(Serializable value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try(ObjectOutputStream outputStream = new ObjectOutputStream(out)) {
            outputStream.writeObject(value);
        }

        return out.toByteArray();
    }

    public static <T extends Serializable> T deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            //noinspection unchecked
            return (T) new ObjectInputStream(bis).readObject();
        }
    }
}
