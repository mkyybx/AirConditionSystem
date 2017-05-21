package XML;

import AC.UnimplementedException;

import java.io.OutputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public abstract class XMLizable {
    private byte[] rawString;
    public abstract void write(OutputStream out) throws java.io.IOException, UnimplementedException;

    public byte[] getRawString() {
        return rawString;
    }

    public void setRawString(byte[] rawString) {
        this.rawString = rawString;
    }
}
