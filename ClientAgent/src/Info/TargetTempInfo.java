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
public class TargetTempInfo extends XMLizable {

    private int temp;
    private byte windLevel;

    public TargetTempInfo(int temp, byte windLevel) {
        this.temp = temp;
        this.windLevel = windLevel;
    }

    @Override
    public void write(OutputStream out) throws IOException, UnimplementedException {
        Document document = DocumentHelper.createDocument();
        Element setTemp = document.addElement("Set_Temp");
        setTemp.addElement("Temp").setText(String.valueOf(temp));
        setTemp.addElement("Wind_Level").setText(String.valueOf(windLevel));
        OutputFormat format = OutputFormat.createPrettyPrint();
        new XMLWriter(out, format).write(document);
    }

    public int getTemp() {
        return temp;
    }

    public byte getWindLevel() {
        return windLevel;
    }
}
