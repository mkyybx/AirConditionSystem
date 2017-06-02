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
		
		
	public:
		Control()
		{
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
            cout << "Hello Runoob��" << endl;
        }
        
        int practice()
        {
	        // �����̵߳� id �������������ʹ������
            pthread_t tids[NUM_THREADS];
            for(int i = 0; i < NUM_THREADS; ++i)
            {
                //���������ǣ��������߳�id���̲߳��������õĺ���������ĺ�������
                int ret = pthread_create(&tids[i], NULL, say_hello, NULL);
                if (ret != 0)
                {
                   cout << "pthread_create error: error_code=" << ret << endl;
                }
            }
            //�ȸ����߳��˳��󣬽��̲Ž������������ǿ�ƽ����ˣ��߳̿��ܻ�û��Ӧ������
            pthread_exit(NULL);
        }*/
};

#endif