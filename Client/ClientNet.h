#include "define.h"

#include "functions.h"

class CClientNet
{
    public:
    	SOCKET m_sock;
	    int Connect(const char*,const char*);//������ָ��������
		int SendMsg(string);//������Ϣ
	    void Close();//�ر�
	    //int Init(const char*,int);//��ʼ��������,����0��ʾ�ɹ�
	    string RecMsg();
		void Run();//��������
};
