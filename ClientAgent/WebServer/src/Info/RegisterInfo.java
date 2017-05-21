package Info;

import AC.UnimplementedException;
import XML.XMLizable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public class RegisterInfo extends XMLizable {

    private int clientNumber;

    public RegisterInfo(int clientNumber) {
        this.clientNumber = clientNumber;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        throw new UnimplementedException();
    }

    public int getClientNumber() {
        return clientNumber;
    }
}
