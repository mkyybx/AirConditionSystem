#include <stdio.h>
#include <windows.h>
#include <string>
#include <iostream>

#pragma comment(lib, "Ws2_32.lib")
#include "define.h"

#include "functions.h"

class CClientNet
{
    public:
    	SOCKET m_sock;
	    int Connect(int,const char*);//������ָ��������
		int SendMsg(string);//������Ϣ
	    void Close();//�ر�
	    //int Init(const char*,int);//��ʼ��������,����0��ʾ�ɹ�
	    string RecMsg();
		void Run();//��������
};
