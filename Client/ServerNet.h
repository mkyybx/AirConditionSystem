#pragma comment (lib,"ws2_32.lib")
#include "functions.h"
#include "define.h"

class CServerNet
{
    public:
    	SOCKET m_sock;
	    int Init(const char*,int);//��ʼ��������,����0��ʾ�ɹ�
	    string RecMsg();
		void Run();//��������

    ///private:
	    //SOCKET m_sock;
};
