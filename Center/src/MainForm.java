import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte0.runnable;

/**
 * Created by Layne on 2017/5/31.
 */
public class MainForm implements ActionListener, ItemListener {
    private JPanel mainPanel;
    private JPanel panelState;
    private JPanel rightPanel;
    private JButton btnState;
    private JButton btnMode;
    private JButton btnFreq;
    private JTextField tfFreq;
    private JButton btnReport;
    private JButton btnCheckout;
    private JComboBox cbReport;
    private JComboBox cbCheckout;
    private JComboBox cbShowReport;
    private JTable tableShowReport;
    private JPanel panelCheckout;
    private JPanel panelReport;
    private JTextField tfAssignRoom;
    private JTextField tfAssignPass;
    private JButton btnAssign;
    private JPanel panelAssign;
    private JPanel panelAssignBtn;
    private JPanel panelFreq;
    private JScrollPane spShowReport;
    private JLabel labelSwitch;
    private JLabel labelFee;
    private JTextField tfSwitch;
    private JTextField tfFee;
    private JTextField tfCurrentState;
    private JTextField tfCurrentMode;
    private JPanel panelCurrentState;
    private JLabel labelCurrentState;
    private JLabel labelCurrentMode;
    private JTextField tfAssignName;
    private JLabel labelAssignRoom;
    private JLabel labelAssignName;
    private JLabel labelAssignPass;

    private HashMap<Integer, ArrayList<Log>> reports;
    private Thread serverThread;
    private int port = 8888;

    public MainForm() {
        btnState.addActionListener(this);
        btnMode.addActionListener(this);
        btnFreq.addActionListener(this);
        btnAssign.addActionListener(this);
        btnCheckout.addActionListener(this);
        btnReport.addActionListener(this);
        cbShowReport.addItemListener(this);
        tfCurrentState.setText("待机");
        tfCurrentMode.setText(Config.getMode() == 0 ? "制冷" : "制暖");

        serverStart();

        ArrayList<Object> roomList = Server.logHandler.selectColumnDistinct(Config.logTable, "Client_No",
                DatabaseHandler.map("checkOut", false));
        for (Object room : roomList)
            cbCheckout.addItem(room);
    }

    private void createUIComponents() {
        DefaultTableModel model = new DefaultTableModel() {     // define customized table
            String[] columnName = {"房间号", "起始时间", "结束时间", "起始温度", "结束温度", "风速等级", "此次费用"};

            @Override
            public int getColumnCount() {
                return columnName.length;
            }

            @Override
            public String getColumnName(int index) {
                return columnName[index];
            }
        };

        tableShowReport = new JTable(model);
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        MainForm mainForm = new MainForm();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame frame = new JFrame("中央空调");
        frame.setContentPane(mainForm.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void setState(ServerState state) {
        if(state == ServerState.Off)                // only stuffs can shut center down
            return;
        String str = (state == ServerState.Idle ? "待机" : "工作");
        tfCurrentState.setText(str);
        showMsg("中央空调模式变为"+str);
    }

    private void showMsg(String Msg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //JOptionPane.showMessageDialog(null, Msg);
            }
        }).start();
    }

    private void serverShutdown() throws SQLException {
        tfCurrentState.setText("关机");
        Config.setServerState(ServerState.Off);
        ArrayList<Object> roomList = Server.logHandler.selectColumnDistinct(Config.logTable, "Client_No",
                DatabaseHandler.map("checkOut", false));

        for (Object room : roomList){
            int i = (int) room;
            System.out.println("正在结算" + i + "号房间");
            roomCheckOut(i);
        }
        serverThread.interrupt();
        serverThread = null;
    }

    private void serverStart() {
        tfCurrentState.setText("待机");
        Config.setServerState(ServerState.On);
        Server.clients = new ConcurrentHashMap<>();
        Server.IPTable = new ConcurrentHashMap<>();
        Server.tempTable = new ConcurrentHashMap<>();
        Server.logTable = new ConcurrentHashMap<>();
        Server.queue = new CopyOnWriteArrayList<>();
        Server.energyTable = new ConcurrentHashMap<>();
        serverThread = new Thread(new Server(port, this));
        serverThread.start();
    }

    private void clientCheckout() throws SQLException{
        if(cbCheckout.getSelectedIndex() == 0){
            showMsg("请选择要结算的房间");
            return;
        }
        int client_no = (int) cbCheckout.getSelectedItem();
        double energy = roomCheckOut(client_no);
        double fee = energy * 5;

        showMsg("客户 "+ client_no + "结算成功，总共消耗的能量是 " + energy + ", 产生的费用为 " + fee);
    }

    private double roomCheckOut(int client_no) throws SQLException{
        double energy = 0;

        if (Server.clients.containsKey(client_no)){     // if client have login before
            // checkout
            try {
                Server.clients.get(client_no).close();
            }
            catch (IOException e) {
                System.out.println("Client socket is already shut down");
            }
            Server.removeClient(client_no);
            if(Server.logTable.containsKey(client_no)){
                RequestHandler.commitLog(client_no);
                Server.logTable.remove(client_no);
            }
            energy = Server.energyTable.get(client_no);
            Server.energyTable.remove(client_no);
            Server.tempTable.remove(client_no);
            Server.IPTable.remove(client_no);
        }
        else{
            HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no);
            ArrayList<Log> logs =  Server.logHandler.select(Config.logTable, conditionSet);
            for (Log log : logs)
                energy += log.energy;
        }

        // set checkOut to true in database
        HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no);
        HashMap<String, Object> valueSet = DatabaseHandler.map("checkOut", true);
        Server.logHandler.update(Config.logTable, conditionSet, valueSet);
        Server.roomInfoHandler.delete(Config.roomTable, conditionSet);

        // refresh combo box
        cbCheckout.removeItem(client_no);
        //cbCheckout.invalidate();

        return energy;
    }

    private void assignClient() throws SQLException {
        int client_no;
        try {
            client_no = Integer.parseInt(tfAssignRoom.getText());
        }
        catch (NumberFormatException e){
            showMsg("房间号不应为空或非正整数的字符串");
            return;
        }

        HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no);
        ArrayList<RoomInfo> res = Server.roomInfoHandler.select(Config.roomTable, conditionSet);
        if(res.size() == 0){                    // the room is available
            RoomInfo info = new RoomInfo();
            info.Client_No = client_no;
            info.Name = tfAssignName.getText();
            info.Password = tfAssignPass.getText();
            ArrayList<RoomInfo> tempList = new ArrayList<>(Arrays.asList(info));
            Server.roomInfoHandler.insert(Config.roomTable, tempList);
            cbCheckout.addItem(client_no);
            tfAssignName.setText("");
            tfAssignRoom.setText("");
            tfAssignPass.setText("");
            showMsg("房间" + client_no + "登记成功");
        }
        else{                                   // the room is occupied, inform stuff
            showMsg("该房间已被占用");
        }
    }

    private void getReport() throws SQLException {
        cbShowReport.removeAllItems();
        cbShowReport.addItem("房间编号");
        cbShowReport.addItem("所有房间");
        int reportType = cbReport.getSelectedIndex();
        if(reportType == 0){
            showMsg("请选择要查看的报表类型");
            return;
        }
        ArrayList<Object> roomList = Server.logHandler.selectColumnDistinct(Config.logTable, "Client_No", new HashMap<>());
        MyDate now = new MyDate(LocalDateTime.now());
        reports = new HashMap<>();
        System.out.println("Room list size " + roomList.size());

        for (Object object: roomList){
            int client_no = (int) object;
            HashMap<String, Object> conditionSet = DatabaseHandler.map("Client_No", client_no, "startMonth", now.month);
            //conditionSet = new HashMap<>();
            if(reportType > 1)
                conditionSet.put("startWeek", now.week);
            if(reportType > 2)
                conditionSet.put("startDay", now.day);

            ArrayList<Log> logs = Server.logHandler.select(Config.logTable, conditionSet);
            if (logs.size() > 0){
                System.out.println("Record found.");
                reports.put(client_no, logs);
                cbShowReport.addItem(client_no);
            }
        }

        if (reports.size() > 0)
            showMsg("报表已在右边窗口生成，选择具体的房间查看详细信息");
        else
            showMsg("没有对应类型的报表");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        try {
            if(source == btnState){
                if(Config.getServerState() == ServerState.Off)
                    serverStart();
                else
                    serverShutdown();
                showMsg("主机状态变为" + tfCurrentState.getText());
            }
            else if(source == btnMode){
                setMode();
            }
            else if(source == btnFreq){
                setFreq();
            }
            else if(source == btnAssign){
                assignClient();
            }
            else if(source == btnCheckout){
                clientCheckout();
            }
            else if(source == btnReport){
                getReport();
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        // only cbShowReport have a listener
        if(e.getStateChange() == ItemEvent.SELECTED){
            if (reports == null)
                return;

            ArrayList<Log> logs;
            try {
                int client_no = (int) e.getItem();
                logs = reports.get(client_no);
            }
            catch (ClassCastException exp){
                if (e.getItem().equals("房间编号")){
                    updateTable(new ArrayList<>());
                    tfSwitch.setText("");
                    tfFee.setText("");
                    return;
                }
                logs = new ArrayList<>();
                for(HashMap.Entry<Integer, ArrayList<Log>> arr : reports.entrySet())
                    logs.addAll(arr.getValue());
            }
            double fee = updateTable(logs);
            tfSwitch.setText(Integer.toString(logs.size()));
            tfFee.setText(Double.toString(fee));
        }
    }

    private void setFreq() {
        int freq;
        try {
            freq = Integer.parseInt(tfFreq.getText());
        }
        catch (NumberFormatException e){
            showMsg("频率不应为空或非正整数的字符串");
            System.out.println("Back..");
            return;
        }

        if(freq != Config.getFrequency()){  // set new frequency
            try {
                Server.produceLock.lock();
                System.out.println("freq lock");
                Config.setFrequency(freq);
                WakeUpTable.setFrequencyChanged(true);
                Server.wakeCond.signal();
            }
            finally {
                Server.produceLock.unlock();
                System.out.println("freq unlock");
            }
            showMsg("频率变为" + freq);
        }
        else
            showMsg("频率已经为" + freq);
        tfFreq.setText("");
    }

    private void setMode() {
        int mode = 1 - Config.getMode();
        try {
            Server.produceLock.lock();
            System.out.println("mode lock");
            Config.setMode(mode);
            WakeUpTable.setModeChanged(true);
            Server.wakeCond.signal();
        }
        finally {
            System.out.println("mode unlock");
            Server.produceLock.unlock();
        }

        tfCurrentMode.setText(mode == 0 ? "制冷" : "制暖");
        showMsg("模式设置为" + (mode == 0 ? "制冷" : "制暖"));
    }

    private double updateTable(ArrayList<Log> logs){
        DefaultTableModel tableModel = (DefaultTableModel) tableShowReport.getModel();
        // remove previous data
        tableModel.setRowCount(0);
        double fee = 0;

        for(Log log : logs){
            String[] arr = new String[]{Integer.toString(log.Client_No), log.startDate.toString(), log.endDate.toString(), Integer.toString(log.startTemp),
                            Integer.toString(log.endTemp), Integer.toString(log.level), Double.toString(log.fee)};
            fee += log.fee;
            // insert into table
            tableModel.addRow(arr);
        }

        // update table
        tableShowReport.invalidate();

        return fee;
    }
}
