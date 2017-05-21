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
public class ModeInfo extends XMLizable {

    private boolean isHeating;

    public ModeInfo(boolean isHeating) {
        this.isHeating = isHeating;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        Document document = DocumentHelper.createDocument();
        Element mode = document.addElement("Mode");
        mode.addElement("Heater").setText(isHeating ? "1" : "0");
        OutputFormat format = OutputFormat.createPrettyPrint();
        new XMLWriter(out, format).write(document);
    }

    public boolean isHeating() {
        return isHeating;
    }

}
