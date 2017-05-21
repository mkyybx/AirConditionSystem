package XML;

import Info.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by Ice on 5/18/2017.
 */
public class XMLizableParser {

    private static XMLizableParser instance;

    static {
        instance = new XMLizableParser();
    }

    private XMLizableParser() {

    }

    public static XMLizableParser getInstance() {
        return instance;
    }

    private XMLizableHandler<WebLoginInfo> WebLoginInfoHandler = null;
    private XMLizableHandler<LoginACKInfo> LoginACKInfoHandler = null;
    private XMLizableHandler<ModeInfo> ModeInfoHandler = null;
    private XMLizableHandler<RegisterInfo> RegisterInfoHandler = null;
    private XMLizableHandler<SensorTempInfo> SensorTempInfoHandler = null;
    private XMLizableHandler<TargetTempInfo> TargetTempInfoHandler = null;
    private XMLizableHandler<FareInfo> FareInfoHandler = null;
    private XMLizableHandler<UserLogoutInfo> LogoutInfoHandler = null;

    public void setWebLoginInfoHandler(XMLizableHandler<WebLoginInfo> webLoginInfoHandler) {
        WebLoginInfoHandler = webLoginInfoHandler;
    }

    public void setLoginACKInfoHandler(XMLizableHandler<LoginACKInfo> loginACKInfoHandler) {
        LoginACKInfoHandler = loginACKInfoHandler;
    }

    public void setModeInfoHandler(XMLizableHandler<ModeInfo> modeInfoHandler) {
        ModeInfoHandler = modeInfoHandler;
    }

    public void setRegisterInfoHandler(XMLizableHandler<RegisterInfo> registerInfoHandler) {
        RegisterInfoHandler = registerInfoHandler;
    }

    public void setSensorTempInfoHandler(XMLizableHandler<SensorTempInfo> sensorTempInfoHandler) {
        SensorTempInfoHandler = sensorTempInfoHandler;
    }

    public void setTargetTempInfoHandler(XMLizableHandler<TargetTempInfo> targetTempInfoHandler) {
        TargetTempInfoHandler = targetTempInfoHandler;
    }

    public void setFareInfoHandler(XMLizableHandler<FareInfo> fareInfoHandler) {
        FareInfoHandler = fareInfoHandler;
    }

    public void setLogoutInfoHandler(XMLizableHandler<UserLogoutInfo> logoutInfoHandler) {
        LogoutInfoHandler = logoutInfoHandler;
    }


    public void parse(byte[] inputString, int num, boolean isWebSide) throws DocumentException {
        SAXReader reader = new SAXReader();
        InputStream in = new ByteArrayInputStream(inputString);
        Document document = reader.read(in);
        Element root = document.getRootElement();
        String qName = root.getName();
        switch (qName) {
            case "Reg":
                RegisterInfo reg = new RegisterInfo(Integer.parseInt(root.element("Client_NO").getText()));
                reg.setRawString(inputString);
                RegisterInfoHandler.onParseComplete(reg, num, isWebSide);
                break;
            case "Login":
                //Weblogin
                String user = root.element("User").getText();
                String password = root.element("Password").getText();
                String room = root.element("Room").getText();
                WebLoginInfo login = new WebLoginInfo(user, password, Integer.parseInt(room));
                login.setRawString(inputString);
                WebLoginInfoHandler.onParseComplete(login, num, isWebSide);
                break;
            case "Login_ACK":
                String ID = root.element("ID").getText();
                String succeed = root.element("Succeed").getText();
                LoginACKInfo ack = new LoginACKInfo(Integer.parseInt(ID), succeed.equals("1"));
                ack.setRawString(inputString);
                LoginACKInfoHandler.onParseComplete(ack, num, isWebSide);
                break;
            case "Sensor_Temp": {
                String sensorTemp = root.element("Sensor_temp").getText();
                SensorTempInfo temp = new SensorTempInfo(Integer.parseInt(sensorTemp));
                temp.setRawString(inputString);
                SensorTempInfoHandler.onParseComplete(temp, num, isWebSide);
                break;
            }
            case "Mode":
                String heater = root.element("Heater").getText();
                ModeInfo mode = new ModeInfo(heater.equals("1"));
                mode.setRawString(inputString);
                ModeInfoHandler.onParseComplete(mode, num, isWebSide);
                break;
            case "Set_Temp": {
                String temp = root.element("Temp").getText();
                String windLevel = root.element("Wind_level").getText();
                TargetTempInfo tempInfo = new TargetTempInfo(Integer.parseInt(temp), Byte.parseByte(windLevel));
                tempInfo.setRawString(inputString);
                TargetTempInfoHandler.onParseComplete(tempInfo, num, isWebSide);
                break;
            }
            case "Fare_Info":
                String fare = root.element("Fare").getText();
                String energy = root.element("Energy").getText();
                FareInfo fareinfo = new FareInfo(Double.parseDouble(fare), Double.parseDouble(energy));
                fareinfo.setRawString(inputString);
                FareInfoHandler.onParseComplete(fareinfo, num, isWebSide);
                break;
            case "User_Logout":
                UserLogoutInfo logoutInfo = new UserLogoutInfo();
                logoutInfo.setRawString(inputString);
                LogoutInfoHandler.onParseComplete(logoutInfo, num, isWebSide);
                break;
        }
    }
}
