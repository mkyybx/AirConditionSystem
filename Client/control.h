#ifndef CONTROL_H
#define CONTROL_H

#include "functions.h"
#include "define.h"
#include "slave.h"
#include "sensor.h"
#include "ClientNet.h"
//#include "ServerNet.h"
#include "XMLInfo.h" 

using namespace std;

class Control
{
	private:
		CClientNet agentClient;
		CClientNet masterClient;
		//CServerNet agentServer;
		//CServerNet masterServer;
		Slave slave;
		XMLInfo xmlinfo;
		DWORD tids[NUM_THREADS];
		int indexes[NUM_THREADS];
		int ind;
		Sensor sensor;
		int flag;
		HANDLE mutex;
		int isAgentClosed;
		
	public:
		Control()
		{
			isAgentClosed = -1;
			//Slave slave(1);	
		}
		static void* th_control_heart_temp_submit(void*);
		void* control_heart_temp_submit();
		static void* th_control_first_login_to_master(void*);
		void* control_first_login_to_master();
		static void* th_control_change_temp(void*);
		void* control_change_temp();
		void* control_masterServer();
		static void* th_control_masterServer(void*);
		void control_get_master_msg_name(string);
		void port_of_masterServer(TiXmlElement*);
		void* control_agentServer();
		static void* th_control_agentServer(void*);
		void control_get_agent_msg_name(string);
		void port_of_agentServer(TiXmlElement*);
		void control_masterClient(string,int);
		void control_agentClient(string,int);
		int control_init();
		
		/*static void* say_hello(void* args)
        {
            cout << "Hello Runoob！" << endl;
        }
        
        int practice()
        {
	        // 定义线程的 id 变量，多个变量使用数组
            pthread_t tids[NUM_THREADS];
            for(int i = 0; i < NUM_THREADS; ++i)
            {
                //参数依次是：创建的线程id，线程参数，调用的函数，传入的函数参数
                int ret = pthread_create(&tids[i], NULL, say_hello, NULL);
                if (ret != 0)
                {
                   cout << "pthread_create error: error_code=" << ret << endl;
                }
            }
            //等各个线程退出后，进程才结束，否则进程强制结束了，线程可能还没反应过来；
            pthread_exit(NULL);
        }*/
};

#endif
