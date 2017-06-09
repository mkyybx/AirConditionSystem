
#include "control.h"

using namespace std;

void* Control::th_control_heart_temp_submit(void * object)
{
    return ((Control *) object)->control_heart_temp_submit();
}

void* Control::control_heart_temp_submit()
{
	while(1)
	{
		int feq = slave.get_slave_inspection_frequency();
		Sleep(feq*1000);
		control_masterClient("Temp_Submit",2);
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
		//printf("after num, slave=%d\n", slave);
		//cout << state << num << endl;
		//Sleep(5000);
		if(state == OPEN_WITHOUT_LOGIN && num > 0)
		{
			slave.update_slave_state(LOGINING);
			control_masterClient("Login",2);
		}
		else if (state == OPEN_WITH_LOGIN)
			break;  
	}
	return NULL;
}

void* Control::th_control_change_temp(void * object)
{
    return ((Control *) object)->control_change_temp();
}

void* Control::control_change_temp()
{
	while(1)
	{
		Sleep(1000);
		int a = slave.get_slave_current_wind_speed();
		int i = sensor.sensor_calculate_temp(slave.get_slave_current_wind_speed());

		if (i == 1)//温度改变
		{
			if (slave.get_slave_mode() == WINTER)
				slave.update_slave_current_temp(1);
			else
				slave.update_slave_current_temp(-1);

			control_agentClient("Sensor_Temp", 1);

			if (slave.get_slave_current_temp() == slave.get_slave_target_temp())
				control_masterClient("AC_Req", 0);

			if (slave.get_slave_mode() == WINTER && (slave.get_slave_target_temp() - slave.get_slave_current_temp() >= 2))
				control_masterClient("AC_Req", 1);

			if (slave.get_slave_mode() == SUMMER && (slave.get_slave_current_temp() - slave.get_slave_target_temp() >= 2))
				control_masterClient("AC_Req", 1);
		}	
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void* Control::th_control_masterServer(void * object)
{
    return ((Control *) object)->control_masterServer();
}

void* Control::control_masterServer()//主机服务器 
{
	//listen(masterClient.m_sock,5);

	while(1)
	{
		string msg = "NoMsg";
		msg = masterClient.RecMsg();

		if (msg == "") {
			printf("client socket lost, isAgentClosed=%d\n", isAgentClosed);
			if (isAgentClosed != 1) {
				isAgentClosed = 0;
				closesocket(agentClient.m_sock);
				agentClient.m_sock = 0;
			}
			break;
		}
		
		else if (msg != "NoMsg")//收到信息 
			control_get_master_msg_name(msg);//做相应处理 
	}

	//关闭自身的Socket
	if (isAgentClosed != 1) {
		closesocket(masterClient.m_sock);
		masterClient.m_sock = 0;
	}
	else isAgentClosed = -1;
	
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
		int suc = xmlinfo.load_N_Login_ACK_doc(pElement,&slave);
	    control_agentClient("Login_ACK",suc);
	}
	else if(name == "Mode")
	{
		int r = xmlinfo.load_N_Mode_doc(pElement,&slave);
			
		if(r != 0)
			control_agentClient("Mode",2);
			
		if(r == 2 || r == 4 || r == 6 || r == 8)
			control_agentClient("AC_Req",1);
	}
	else if(name == "Fare_Info")
	{
		xmlinfo.load_N_Fare_Info_doc(pElement,&slave);
		control_agentClient("Fare_Info",2); 
	}
	else if(name == "Temp_Submit_Freq")
	    xmlinfo.load_N_Temp_Submit_Freq_doc(pElement,&slave); 
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void* Control::th_control_agentServer(void * object)
{
    return ((Control *) object)->control_agentServer();
}

void* Control::control_agentServer()//从机服务器 
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
		else if(msg != "NoMsg")//收到信息 
			control_get_agent_msg_name(msg);//做相应处理 
	}

	//关闭自身的Socket
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
		int suc = xmlinfo.load_Login_doc(pElement,&slave); 
			
			if(suc == 0)//成功插入 
			{
				cout << "插入成功！" << endl;
				//control_masterClient("Login",suc);
			} 
			else 
			    cout << "等待队列已满！" << endl;
	}
	else if(name == "Set_Temp")
	{
		int suc = xmlinfo.load_Set_Temp_doc(pElement,&slave); 
			
		if(suc != 2)
			control_masterClient("AC_Req",1);
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void Control::control_masterClient(string name,int suc)//主机客户端 
{
	string msg; 
	
	if (name == "AC_Req")
		msg = xmlinfo.build_N_AC_Req_doc(&slave, suc);
	else if (name == "Temp_Submit")
		msg = xmlinfo.build_N_Temp_Submit_doc(&slave, 2);
	else if (name == "Login")
		msg = xmlinfo.build_N_Login_doc(&slave, 2);
			
	masterClient.SendMsg(msg);
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void Control::control_agentClient(string name,int suc)//从机客户端 
{
	string msg; 
	
	if(name == "Reg")
	    msg = xmlinfo.build_Reg_doc(&slave); 
	else if(name == "Login_ACK")
	    msg = xmlinfo.build_Login_ACK_doc(&slave,suc);
	else if(name == "Sensor_Temp")
	    msg = xmlinfo.build_Sensor_Temp_doc(&slave); 
	else if(name == "Mode")
	    xmlinfo.build_Mode_doc(&slave);
	else if(name == "Set_Temp")
	    msg = xmlinfo.build_Set_Temp_doc(&slave);
	else if(name == "Fare_Info")
        msg = xmlinfo.build_Fare_Info_doc(&slave); 
			printf("mky111\n");
			cout << msg << endl;
	agentClient.SendMsg(msg);	
}

int Control::control_init()
{	
	WCHAR c = 'a';
	mutex = CreateMutex(NULL, false, &c);
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_change_temp, this, 0, tids + 4);
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_heart_temp_submit, this, 0, tids + 2);
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_first_login_to_master, this, 0, tids + 3);
repeat:

	//string masterip, agentip;
	//cin << masterip;
	//cin << agentip;
	
	//int iRlt2 = masterClient.Connect("8888", masterip.c_str());//主机
	//int iRlt1 = agentClient.Connect("9999", agentip.c_str());//agent

	int iRlt2 = masterClient.Connect("8888","127.0.0.1");//主机
	if (iRlt2 == 0)//与主机成功建立连接
	{
		cout << "HLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLHLH" << endl;
		CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_masterServer, this, 0, tids);

	repeat1:
		int iRlt1 = agentClient.Connect("9999", "127.0.0.1");//agent

		if (iRlt1 == 0)//与从机成功建立连接
		{
			cout << "MKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKYMKY" << endl;
			CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_agentServer, this, 0, tids + 1);
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
		printf("serverNet init failed with error.\n");
		Sleep(500); 
		goto repeat;
	}		
}
