
package deckman.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Using the Object clone method is not good enough because this will create a "shallow" clone of the Row instance.
 * The object fields of the clone would have references to the corresponding fields of the original Row being cloned.
 * Changes in these fields belonging to the clone would be reflected in the Row that was cloned.
 * A "deep" clone is required. One way of getting a deep clone is to serialize the object out to a byte array stream using
 * the writeUnshared method, and deserialize it back in to a new object using the readUnshared method.
 *
 * @author Warren
 */
public abstract class DeepCloneableObject {

    public Object deepClone() throws Exception {
        ByteArrayOutputStream bytestreamOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        Object clone = null;

        try{
            out = new ObjectOutputStream(bytestreamOut);
            out.writeUnshared(this);
            byte[] bytes = bytestreamOut.toByteArray();
            ByteArrayInputStream bytestreamIn = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(bytestreamIn);

            //try{
                clone = in.readUnshared();
                clone = this.getClass().getEnclosingClass().cast(clone);
            //}
            //catch(ClassNotFoundException e1){ /* Class of a Row object to deserialize cannot be found */ }
            //catch(StreamCorruptedException e2){ /* Control information in the stream is inconsistent while deserializing Row object */ }

            //catch(ObjectStreamException e3){ /* Object to deserialize has already appeared in stream */ }

            out.close();

        }
        //catch(OptionalDataException e4){ /* Primitive data is next in stream */ }
        //catch(IOException e5){ }
        finally{
            if(out != null){
                out.close();
            }
        }

        return clone;
    }
}