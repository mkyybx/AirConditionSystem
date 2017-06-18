/**
 * Created by Layne on 2017/6/6.
 */
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XMLParser {
    private int client_no;
    private int temp;
    private int min;
    private int sec;
    private String name;
    private String password;
    private String rootName;
    private boolean positive;
    private int level;

    public boolean parseText(String text) throws DocumentException {
        Document document = DocumentHelper.parseText(text);
        Element element = document.getRootElement();
        rootName = element.getName();
        switch (rootName) {
            case RequestHandler.AC_REQ:
                try {
                    positive = Integer.parseInt(element.elementText("Positive")) == 1;
                    level = Integer.parseInt(element.elementText("Wind_Level"));
                }
                catch (NumberFormatException e){
                    return false;
                }
                break;

            case RequestHandler.TEMP_SUBMIT:
                // Element childElement = element.element("Time");
                // min = Integer.parseInt(childElement.elementText("Min"));
                // sec = Integer.parseInt(childElement.elementText("Sec"));
                try {
                    client_no = Integer.parseInt(element.elementText("Client_No"));
                    temp = (int) Double.parseDouble(element.elementText("Temp"));
                }
                catch (NumberFormatException e){
                    return false;
                }
                // System.out.println("Client_no " + client_no + ", temp " + temp);
                break;

            case RequestHandler.LOGIN:
                try {
                    client_no = Integer.parseInt(element.elementText("Client_No"));
                }
                catch (NumberFormatException e){
                    return false;
                }
                name = element.elementText("Name");
                password = element.elementText("Password");
                System.out.println("Name " + name + ", password " + password + ", client_no " + client_no);
                break;
        }
        return true;
    }

    public int getLevel() {
        return level;
    }

    public boolean isPositive() {
        return positive;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public int getTemp() {
        return temp;
    }

    public int getClient_no(){
        return client_no;
    }

    public String getRootName(){
        return rootName;
    }
}

class XMLPacker {

    public static String packLoginACK(String name, String password, boolean succeed){
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(RequestHandler.ACK);
        root.addElement("Succeed").setText(succeed ? "1" : "0");
        root.addElement("Name").setText(name);
        root.addElement("Password").setText(password);
        root.addElement(RequestHandler.MODE).setText(Integer.toString(Config.getMode()));
        return doc.asXML().split("\n")[1];
    }

    public static String packFareInfo(double fare, double energy){
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(RequestHandler.FARE);
        root.addElement("Fare").setText(Double.toString(fare));
        root.addElement("Energy").setText(Double.toString(energy));

        return doc.asXML().split("\n")[1];
    }

    public static String packModeInfo(){
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(RequestHandler.MODE);
        root.addElement("Heater").setText(Integer.toString(Config.getMode()));
        return doc.asXML().split("\n")[1];
    }

    public static String packFreqInfo(){
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(RequestHandler.FREQ);
        root.addElement("Temp_Submit_Freq").setText(Integer.toString(Config.getFrequency()));
        return document.asXML().split("\n")[1];
    }

    public static String packQueueInfo(boolean isBlow, int level){
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(RequestHandler.WIND);
        root.addElement("Level").setText(Integer.toString(level));
        root.addElement("Start_Blowing").setText(Integer.toString(isBlow ? 1 : 0));
        // Level should be ignored in this case
        return doc.asXML().split("\n")[1];
    }

}