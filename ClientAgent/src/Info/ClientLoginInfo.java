package Info;

import AC.UnimplementedException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public class ClientLoginInfo extends LoginInfo {

    private int uuid;

    public ClientLoginInfo(LoginInfo loginInfo, int uuid) {
        this.name = loginInfo.name;
        this.password = loginInfo.password;
        this.uuid = uuid;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        Document document = DocumentHelper.createDocument();
        Element login = document.addElement("Login");
        login.addElement("ID").setText(String.valueOf(uuid));
        login.addElement("User").setText(name);
        login.addElement("Password").setText(password);
        OutputFormat format = OutputFormat.createPrettyPrint();
        new XMLWriter(out, format).write(document);
    }

}
