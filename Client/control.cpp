#include <pthread.h>
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
		int state = slave.get_slave_state();
		int num = slave.get_slave_queuenum();
		
		if(state == OPEN_WITHOUT_LOGIN && num > 0)
		{
			slave.update_slave_state(LOGINING);
			printf("mky");
			control_masterClient("Login",2);
		}
		else if(state == OPEN_WITH_LOGIN)
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
		//printf("mky\n"); 
		int a = slave.get_slave_current_wind_speed();
		printf("mky %d\n",a); 
		int i = sensor.sensor_calculate_temp(slave.get_slave_current_wind_speed());
		printf("mky\n"); 
		if(slave.get_slave_mode() == WINTER)
		    slave.update_slave_current_temp(1);
		else
		    slave.update_slave_current_temp(-1);
		    
	    control_agentClient("Sensor_Temp",1);
	    
	    if(slave.get_slave_current_temp() == slave.get_slave_target_temp())
	        control_masterClient("AC_Req",0);
	    
	    if(slave.get_slave_mode() == WINTER && (slave.get_slave_target_temp() - slave.get_slave_current_temp() >= 2))
	        control_masterClient("AC_Req",1);
	    
	    if(slave.get_slave_mode() == SUMMER && (slave.get_slave_current_temp() - slave.get_slave_target_temp() >= 2))
	        control_masterClient("AC_Req",1);
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void* Control::th_control_masterServer(void * object)
{
    return ((Control *) object)->control_masterServer();
}

void* Control::control_masterServer()//主机服务器 
{
	listen(masterClient.m_sock,5);

	while(1)
	{
		string msg = "NoMsg";
		msg = masterClient.RecMsg();
		
		if(msg != "NoMsg")//收到信息 
			control_get_master_msg_name(msg);//做相应处理 
		else
		    cout << "haofanya" << endl;
	}

	//关闭自身的Socket
	closesocket(masterClient.m_sock);
	
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
		int suc = xmlinfo.load_N_Login_ACK_doc(pElement,slave);
	    control_agentClient("Login_ACK",suc);
	}
	else if(name == "Mode")
	{
		int r = xmlinfo.load_N_Mode_doc(pElement,slave);
			
		if(r != 0)
			control_agentClient("Mode",2);
			
		if(r == 2 || r == 4 || r == 6 || r == 8)
			control_agentClient("AC_Req",1);
	}
	else if(name == "Fare_Info")
	{
		xmlinfo.load_N_Fare_Info_doc(pElement,slave);
		control_agentClient("Fare_Info",2); 
	}
	else if(name == "Temp_Submit_Freq")
	    xmlinfo.load_N_Temp_Submit_Freq_doc(pElement,slave); 
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void* Control::th_control_agentServer(void * object)
{
    return ((Control *) object)->control_agentServer();
}

void* Control::control_agentServer()//从机服务器 
{
	listen(agentClient.m_sock,5);

	while(1)
	{
		string msg = "NoMsg";
		msg = agentClient.RecMsg();
		
		if(msg != "NoMsg")//收到信息 
			control_get_agent_msg_name(msg);//做相应处理 
	}

	//关闭自身的Socket
	closesocket(agentClient.m_sock);
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
		int suc = xmlinfo.load_Login_doc(pElement,slave); 
			
			if(suc == 0)//成功插入 
			{
				control_masterClient("Login",suc);
			} 
			else 
			    cout << "等待队列已满！" << endl;
	}
	else if(name == "Set_Temp")
	{
		int suc = xmlinfo.load_Set_Temp_doc(pElement,slave); 
			
		if(suc != 2)
			control_masterClient("AC_Req",1);
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void Control::control_masterClient(string name,int suc)//主机客户端 
{
	string msg; 
	
	if(name == "AC_Req")
	    msg = xmlinfo.build_N_AC_Req_doc(slave,suc); 
	else if(name == "Temp_Submit")
	    msg = xmlinfo.build_N_Temp_Submit_doc(slave,2); 
	else if(name == "Login")
	    msg = xmlinfo.build_N_Login_doc(slave,2); 
			
	masterClient.SendMsg(msg);	
}
////////////////////////////////////////////////////////////////////////////////////////////////////
void Control::control_agentClient(string name,int suc)//从机客户端 
{
	string msg; 
	
	if(name == "Reg")
	    msg = xmlinfo.build_Reg_doc(slave); 
	else if(name == "Login_ACK")
	    msg = xmlinfo.build_Login_ACK_doc(slave,suc);
	else if(name == "Sensor_Temp")
	    msg = xmlinfo.build_Sensor_Temp_doc(slave); 
	else if(name == "Mode")
	    xmlinfo.build_Mode_doc(slave);
	else if(name == "Set_Temp")
	    msg = xmlinfo.build_Set_Temp_doc(slave);
	else if(name == "Fare_Info")
        msg = xmlinfo.build_Fare_Info_doc(slave); 
			printf("mky111\n");
			cout << msg << endl;
	agentClient.SendMsg(msg);	
}

int Control::control_init()
{	
	int iRlt2 = masterClient.Connect(1111,"127.0.0.1");//建立主机和agent的服务器 
	int iRlt1 = agentClient.Connect(0x0f27,"10.28.197.143");//建立主机和agent的服务器 
	//int iRlt1=0;
	 //iRlt2=0;
	
	
	
	//if (iRlt1 == 0 && iRlt2 == 0)//成功建立连接 
	//{
		printf("init ok...\n"); 
		//int a = pthread_create(&tids[0], NULL,th_control_masterServer, NULL);//创建server线程 
		//Control* tempControl0 = new Control();
		//CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_masterServer, tempControl0, 0, tids);
		printf("0...\n");
		Control* tempControl1 = new Control();
		CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_agentServer, tempControl1, 0, tids + 1);
		//pthread_create(&tids[1], NULL, th_control_agentServer, NULL);
		printf("1...\n");
		Control* tempControl2 = new Control();
		//CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_change_temp, tempControl2, 0, tids + 4);
		//pthread_create(&tids[4], NULL, th_control_change_temp, NULL);
		printf("4...\n");
		
		control_agentClient("Reg",1);
				Control* tempControl3 = new Control();
				//CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_heart_temp_submit, tempControl3, 0, tids + 2);
				//pthread_create(&tids[2], NULL,th_control_heart_temp_submit, NULL);
				printf("2...\n");
				{
					Control* tempControl4 = new Control();
					//CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)th_control_first_login_to_master, tempControl4, 0, tids + 3);
					//pthread_create(&tids[3], NULL, th_control_first_login_to_master, NULL);
					printf("3...\n");
				    //pthread_exit(NULL);
				}
				
				while (1){
					Sleep(1000);
				}
		return 0;
		
		/*int ctm1 = masterClient.Connect(8888,"127.0.0.1");//与主机建立连接
		
		if(ctm1 != 0) 
		{
			cout << "与主机无法建立连接！" << endl;
			return 2;
		}
		else
		{
			int ctm2 = agentClient.Connect(8888,"127.0.0.1");
			
			if(ctm2 != 0)
			{
				cout << "与从机无法建立连接！" << endl;
			    return 3;
			}
			else 
			{
				control_agentClient("Reg",1);
				pthread_create(&tids[2], NULL,th_control_heart_temp_submit, NULL);
				
				{
					pthread_create(&tids[3], NULL, th_control_first_login_to_master, NULL);
				    pthread_exit(NULL);
				}
				
				return 0;
			}		    
		}*/	
	/*}
	else
	{
		printf("serverNet init failed with error.\n");
		return 1;
	}*/		
}
