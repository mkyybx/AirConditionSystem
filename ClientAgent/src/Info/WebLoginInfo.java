package Info;

import AC.UnimplementedException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public class WebLoginInfo extends LoginInfo {

    private int room;

    public WebLoginInfo(String user, String password, int room) {
        this.room = room;
        this.name = user;
        this.password = password;
    }

    public int getRoom() {
        return room;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        throw new UnimplementedException();
    }

}
