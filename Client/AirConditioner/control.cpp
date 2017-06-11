
#include "control.h"

using namespace std;

Control* control;

bool Control::getFirstTempSubmitSent() {
	return isFirstTempSubmitSent;
}

CClientNet* Control::getAgentClient() {
	return &agentClient;
}
CClientNet* Control::getMasterClient() {
	return &masterClient;
}


void Slave::setCurrentTemp(int temp) {
	slave_current_temp = temp;
	isCurrentTempChanged = true;
	XMLInfo xmlinfo;
	control->getAgentClient()->SendMsg(xmlinfo.build_Sensor_Temp_doc(this));
}

void Slave::loginReqHandler(Userinfo userInfo) {
	XMLInfo xmlinfo;
	string s;
	while (control->getFirstTempSubmitSent() == false)
		Sleep(100);
	if (slave_state == OPEN_WITHOUT_LOGIN) {
		slave_user = userInfo.slave_user;
		slave_password = userInfo.slave_password;
		slave_id = userInfo.slave_id;
		s = xmlinfo.build_Login_doc(this, 0);
		control->getMasterClient()->SendMsg(s);
		slave_state = LOGINING;
		return;
	}
	else if (slave_state == LOGINING) {
		static int retryTimes = 0;
		retryTimes++;
		s = xmlinfo.build_Login_ACK_doc(0, "-1");
		if (retryTimes > 3)
			closesocket(control->getMasterClient()->m_sock);
	}
	else if (slave_state == OPEN_WITH_LOGIN) {
		if (userInfo.slave_user == slave_user && userInfo.slave_password == slave_password)
		{
			s = xmlinfo.build_Login_ACK_doc(1, userInfo.slave_id);
			control->control_agentClient("Mode", 1);
			control->control_agentClient("Set_Temp", 1);
			control->control_agentClient("Sensor_Temp", 1);
		}
			
		else s = xmlinfo.build_Login_ACK_doc(0, userInfo.slave_id);
	}
	control->getAgentClient()->SendMsg(s);
}

void Slave::loginACKHandler(Userinfo userInfo, bool isSucceed) {
	if (slave_state == LOGINING) {
		if (userInfo.slave_user == slave_user && userInfo.slave_password == slave_password) {
			slave_state = isSucceed ? OPEN_WITH_LOGIN : OPEN_WITHOUT_LOGIN;
			XMLInfo xmlinfo;
			control->getAgentClient()->SendMsg(xmlinfo.build_Login_ACK_doc(isSucceed ? 1 : 0, slave_id));

			if (isSucceed == 1)
			{
				control->control_agentClient("Mode", 1);
				control->control_agentClient("Set_Temp", 1);
				control->control_agentClient("Sensor_Temp", 1);
			}
		}
	}
}

void* Control::th_control_heart_temp_submit(void * object)
{
    return ((Control *) object)->control_heart_temp_submit();
}

void* Control::control_heart_temp_submit()
{
	while(1)
	{
		int feq = slave.get_slave_inspection_frequency();
		control_masterClient("Temp_Submit",2);
		if (!isFirstTempSubmitSent)
			isFirstTempSubmitSent = true;
		Sleep(feq * 1000);
	}
}

void* Control::th_control_first_login_to_master(void * object)
{
    return ((Control *) object)->control_first_login_to_master();
}

void* Control::control_first_login_to_master()
{
	while(1)
	{
		Sleep(1000);
		int state = slave.get_slave_state();
		int num = slave.get_slave_queuenum();
		
		if(state == OPEN_WITHOUT_LOGIN && num > 0)
		{
			slave.update_slave_state(LOGINING);
			control_masterClient("Login",2);
		}
		else if (state == OPEN_WITH_LOGIN && num > 0)
		{
			string user = slave.get_slave_queue_user();
			string password = slave.get_slave_queue_password();
			int suc = slave.judge_slave_info(user, password);
			control_agentClient("Login_ACK", suc);
			slave.delete_queue();
		}
			 
	}
	return NULL;
}

void* Control::th_userInputListener(void* object) {
	return ((Control *)object)->userInputListener();
}

void* Control::userInputListener() {
	int i;
	while (true) {
		cout << "����ֱ�Ӽ��������¶Ⱥ󰴻س����趨�����¶ȡ�" << endl;
		scanf("%d", &i);
		slave.setCurrentTemp(i);
		cout << "��ǰ�����¶�Ϊ" << i << "��" << endl;
	}
}

void* Control::th_control_change_temp(void * object)
{
    return ((Control *) object)->control_change_temp();
}

void* Control::control_change_temp()
{
	while(1)
	{
		int a = slave.get_slave_current_wind_speed();
		int j = slave.judge_slave_temp();
		int i = sensor.sensor_calculate_temp(slave.get_slave_current_wind_speed(), j, slave.get_slave_wind_permitted());


		cout << "��ǰ�¶�" << slave.get_slave_current_temp() << endl;
		cout << "Ŀ���¶�" << slave.get_slave_target_temp() << endl;
		cout << "��ǰ����" << slave.get_slave_current_wind_speed() << endl;




		if (slave.get_slave_mode() == WINTER && (slave.get_slave_target_temp() - slave.get_slave_current_temp() >= 2) && slave.get_isCurrentTempChanged() == true)
		{
			control_masterClient("AC_Req", 1);
			slave.update_isCurrentTempChanged();
		}


		if (slave.get_slave_mode() == SUMMER && (slave.get_slave_current_temp() - slave.get_slave_target_temp() >= 2) && slave.get_isCurrentTempChanged() == true)
		{
			control_masterClient("AC_Req", 1);
			slave.update_isCurrentTempChanged();
		}
		

		if (i == TRUE && j == FALSE)//�¶ȸı�
		{
			if (slave.get_slave_mode() == WINTER)
				slave.update_slave_current_temp(1);
			else
				slave.update_slave_current_temp(-1);
			cout << "��ǰ�¶�" << slave.get_slave_current_temp() << endl;
			cout << "Ŀ���¶�" << slave.get_slave_target_temp() << endl;
			cout << "��ǰ����" << slave.get_slave_current_wind_speed()<< endl;
			control_agentClient("Sensor_Temp", 1);

			if (slave.get_slave_current_temp() == slave.get_slave_target_temp())
				control_masterClient("AC_Req", 0);

			if (slave.get_slave_mode() == WINTER && (slave.get_slave_target_temp() - slave.get_slave_current_temp() >= 2) && slave.get_slave_wind_permitted() == 0)
				control_masterClient("AC_Req", 1);

			if (slave.get_slave_mode() == SUMMER && (slave.get_slave_current_temp() - slave.get_slave_target_temp() >= 2) && slave.get_slave_wind_permitted() == 0)
				control_masterClient("AC_Req", 1);

			
		}	
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void* Control::th_control_masterServer(void * object)
{
    return ((Control *) object)->control_masterServer();
}

void* Control::control_masterServer()//���������� 
{
	while(1)
	{
		string msg = "NoMsg";
		msg = masterClient.RecMsg();

		if (msg == "") 
		{
			printf("master socket lost, isAgentClosed=%d\n", isAgentClosed);
			if (isAgentClosed != 1) 
			{
				isAgentClosed = 0;
				closesocket(agentClient.m_sock);
				agentClient.m_sock = 0;
			}
			break;
		}
		
		else if (msg != "NoMsg")//�յ���Ϣ 
			control_get_master_msg_name(msg);//����Ӧ���� 
	}

	//�ر������Socket
	if (isAgentClosed != 1) {
		closesocket(masterClient.m_sock);
		masterClient.m_sock = 0;
	}
	else isAgentClosed = -1;
	slave.update_slave_state(OPEN_WITHOUT_LOGIN);
	slave.clearQueue();
	slave.reset_slave();
	return NULL;
}

void Control::control_get_master_msg_name(string xmlstr) 
{
	const char *content = xmlstr.c_str();
    TiXmlDocument *doc = new TiXmlDocument();
    doc->Parse(content);
    
    if (&doc == NULL)
        cout << "doc == NULL" << endl;
	    
	TiXmlHandle hDoc(doc);
	TiXmlHandle hRoot(0);
	
	TiXmlElement* pElement = hDoc.FirstChildElement().Element();
	if (!pElement) return;
	
	port_of_masterServer(pElement);	
}

void Control::port_of_masterServer(TiXmlElement* pElement) 
{
	string name = pElement->Value();
	
	if(name == "Login_ACK")
	{
		Userinfo userinfo = xmlinfo.load_Login_ACK_doc(pElement, &slave);
		slave.loginACKHandler(userinfo, userinfo.slave_id == "1" ? true : false);
		/*
		int suc = xmlinfo.load_N_Login_ACK_doc(pElement,&slave);
	    control_agentClient("Login_ACK",suc);

		if (suc == TRUE)
		{
			slave.delete_queue();
			control_agentClient("Set_Temp", suc);
		}*/
			
	}
	else if(name == "Mode")
	{
		int r = xmlinfo.load_N_Mode_doc(pElement,&slave);
		control_agentClient("Mode",2);
		control_agentClient("Set_Temp", 2);

		if(r == 2 || r == 6)
			control_masterClient("AC_Req",1);
	}
	else if(name == "Fare_Info")
	{
		xmlinfo.load_N_Fare_Info_doc(pElement,&slave);
		control_agentClient("Fare_Info",2); 
	}
	else if(name == "Temp_Submit_Freq")
	    xmlinfo.load_N_Temp_Submit_Freq_doc(pElement,&slave); 
	else if (name == "Wind")
		xmlinfo.load_N_Wind_doc(pElement, &slave);
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void* Control::th_control_agentServer(void * object)
{
    return ((Control *) object)->control_agentServer();
}

void* Control::control_agentServer()//�ӻ������� 
{
	//listen(agentClient.m_sock,5);
	WaitForSingleObject(mutex, INFINITE);
	printf("client get mutex!\n");

	while(1)
	{
		string msg = "NoMsg";
		msg = agentClient.RecMsg();
		if (msg == "") {
			printf("client socket lost, isAgentClosed=%d\n", isAgentClosed);
			if (isAgentClosed != 0) {
				isAgentClosed = 1;
				closesocket(masterClient.m_sock);
				masterClient.m_sock = 0;
			}
			break;
		}
		else if(msg != "NoMsg")//�յ���Ϣ 
			control_get_agent_msg_name(msg);//����Ӧ���� 
	}

	//�ر������Socket
	if (isAgentClosed != 0) {
		closesocket(agentClient.m_sock);
		agentClient.m_sock = 0;
	}
	else isAgentClosed = -1;
	ReleaseMutex(mutex);
	printf("client release mutex!\n");
	return NULL;
}

void Control::control_get_agent_msg_name(string xmlstr) 
{
	const char *content = xmlstr.c_str();
    TiXmlDocument *doc = new TiXmlDocument();
    doc->Parse(content);
    
    if (&doc == NULL)
        cout << "doc == NULL" << endl;
	    
	TiXmlHandle hDoc(doc);
	TiXmlHandle hRoot(0);
	
	TiXmlElement* pElement = hDoc.FirstChildElement().Element();
	if (!pElement) return;
	
	port_of_agentServer(pElement);	
}

void Control::port_of_agentServer(TiXmlElement* pElement) 
{
	string name = pElement->Value();
	
	if(name == "Login")
	{
		slave.loginReqHandler(xmlinfo.load_Login_doc(pElement, &slave));
	/*	slave
		int suc = xmlinfo.load_Login_doc(pElement,&slave); 
			
			if(suc == 0)//�ɹ����� 
			{
				cout << "����ɹ���" << endl;
				//control_masterClient("Login",suc);
			} 
			else 
			    cout << "�ȴ�����������" << endl;
		if (slave.get_slave_state() == LOGINING) {
			//�ظ�ACK=-1��Succeed=0
		}
		else if (slave.get_slave_state() == OPEN_WITHOUT_LOGIN) {

		}*/
	}
	else if(name == "Set_Temp")
	{
		int suc = xmlinfo.load_Set_Temp_doc(pElement,&slave); 
			
		if(suc == 0 || suc == 1)
			control_masterClient("AC_Req", 1);

		if (suc >= 999 )
			control_masterClient("AC_Req", 0);
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void Control::control_masterClient(string name,int suc)//�����ͻ��� 
{
	string msg; 
	
	if (name == "AC_Req")
		msg = xmlinfo.build_N_AC_Req_doc(&slave, suc);
	else if (name == "Temp_Submit")
		msg = xmlinfo.build_N_Temp_Submit_doc(&slave, 2);
//	else if (name == "Login")
//		msg = xmlinfo.build_N_Login_doc(&slave, 2);
			
	masterClient.SendMsg(msg);
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void Control::control_agentClient(string name,int suc)//�ӻ��ͻ��� 
{
	string msg; 
	
	if(name == "Reg")
	    msg = xmlinfo.build_Reg_doc(&slave); 
//	else if(name == "Login_ACK")
//	    msg = xmlinfo.build_Login_ACK_doc(&slave,suc);
	else if(name == "Sensor_Temp")
	    msg = xmlinfo.build_Sensor_Temp_doc(&slave); 
	else if(name == "Mode")
		msg = xmlinfo.build_Mode_doc(&slave);
	else if (name == "Set_Temp")
	    msg = xmlinfo.build_Set_Temp_doc(&slave);
	else if(name == "Fare_Info")
        msg = xmlinfo.build_Fare_Info_doc(&slave); 

	agentClient.SendMsg(msg);	
}

int Control::control_init(const char* serverIP, const char* serverPort, const char* ClientIP, const char* ClientPort, int roomNum)
{	
	slave.setSlaveNum(roomNum);
	control = this;
	WCHAR c = 'a';
	mutex = CreateMutex(NULL, false, &c);
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_change_temp, this, 0, tids + 4);
	printf("�ı��¶��̳߳�ʼ���ɹ��� change temp=%d\n", tids[4]);
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_heart_temp_submit, this, 0, tids + 2);
	printf("�¶��ϱ��̳߳�ʼ���ɹ���  heart temp submit=%d\n", tids[2]);
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_userInputListener, this, 0, tids + 3);
	printf("�û������̳߳�ʼ���ɹ���  heart temp submit=%d\n", tids[3]);
	//CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_first_login_to_master, this, 0, tids + 3);
	//printf("first login to master=%d\n", tids[3]);
repeat:
	isFirstTempSubmitSent = false;
	//string masterip, agentip;
	//cin << masterip;
	//cin << agentip;
	
	//int iRlt2 = masterClient.Connect("8888", masterip.c_str());//����
	//int iRlt1 = agentClient.Connect("9999", agentip.c_str());//agent

	int iRlt2 = masterClient.Connect(serverPort, serverIP);//����
	 
	if (iRlt2 == 0)//�������ɹ���������
	{
		cout << "HLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLH" << endl;
		CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_masterServer, this, 0, tids);
		cout << "�������ɹ��������ӣ�" << endl;
		printf("master server=%d\n", tids[0]);
		
	repeat1:
		if (masterClient.m_sock == 0) {
			closesocket(agentClient.m_sock);
			goto repeat;
		}

		int iRlt1 = agentClient.Connect(ClientPort, ClientIP);//agent

		if (iRlt1 == 0)//��ӻ��ɹ���������
		{
			cout << "MKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKY" << endl;
			Sleep(100);
			CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_agentServer, this, 0, tids + 1);
			cout << "��ӻ��ɹ��������ӣ�" << endl;
			printf("agent server=%d\n", tids[1]);
			Sleep(500);
			control_agentClient("Reg", 1);

			while (1)
			{
				WaitForSingleObject(mutex, INFINITE);
				printf("main thread get mutex!\n");
				ReleaseMutex(mutex);
				printf("main thread release mutex!\n");
				printf("restart client!\n");
				Sleep(500);
				goto repeat;
			}

			return 0;
		}
		else {
			printf("connect to agent faild\n");
			Sleep(500);
			goto repeat1;
		}
	}
	else
	{
		printf("connect to server faild\n");
		Sleep(500); 
		goto repeat;
	}		
}
