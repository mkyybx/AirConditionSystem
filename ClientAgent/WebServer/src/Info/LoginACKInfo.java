package Info;

import AC.UnimplementedException;
import XML.XMLizable;
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
public class LoginACKInfo extends XMLizable {

    private int uuid;
    private Boolean isSuccessful;

    public LoginACKInfo(int uuid, Boolean isSuccessful) {
        this.uuid = uuid;
        this.isSuccessful = isSuccessful;
    }


    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        Document document = DocumentHelper.createDocument();
        Element login = document.addElement("Login_ACK");
        login.addElement("ID").setText(String.valueOf(uuid));
        login.addElement("Succeed").setText(isSuccessful ? "1" : "0");
        OutputFormat format = OutputFormat.createPrettyPrint();
        new XMLWriter(out, format).write(document);
    }

    public int getUuid() {
        return uuid;
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }
}
