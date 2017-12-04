package Info;

import AC.UnimplementedException;
import XML.XMLizable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ice on 5/21/2017.
 */
public class UserLogoutInfo extends XMLizable {
    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        throw new UnimplementedException();
    }
}
