#include "ClientNet.h"

int CClientNet::Connect( int port,const char* address )
{
	int rlt = 0;

	//���ڼ�¼������Ϣ�����
	int iErrMsg;
	//����WinSock
	WSAData wsaData;
	iErrMsg = WSAStartup(MAKEWORD(1,1),&wsaData);
	if (iErrMsg != NO_ERROR)
		//�д���
	{
		printf("failed with wsaStartup error : %d\n",iErrMsg);

		rlt = 1;
		return rlt;
	}

	//����Socket
	m_sock = socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
	if (m_sock == INVALID_SOCKET)
		//����Socketʧ��
	{
		printf("socket failed with error : %d\n",WSAGetLastError());

		rlt = 2;
		return rlt;
	}

	//Ŀ�����������
	sockaddr_in sockaddrServer;
	sockaddrServer.sin_family = AF_INET;
	sockaddrServer.sin_port = port;
	sockaddrServer.sin_addr.s_addr = inet_addr(address);
	printf("%d\n",port);

	//����,sock��Ŀ�����������
	iErrMsg = connect(m_sock,(sockaddr*)&sockaddrServer,sizeof(sockaddrServer));
	if (iErrMsg < 0)
	{
		printf("connect failed with error : %d\n",iErrMsg);

		rlt = 3;
		return rlt;
	}

	return rlt;
}

int CClientNet::SendMsg(string m)
{
	const char *msg = m.c_str();
	int len = m.length();
	int rlt = 0;
	int iErrMsg = 0;
	/*for (int i = 0; i < sizeof(int) / 2; i++) {
		char* temp = (char*)&len;
		char t;
		t = temp[i];
		temp[i] = temp[sizeof(int) - 1 - i];
		temp[sizeof(int) - 1 - i] = t;
	}*/
	int a = htonl(len);
    iErrMsg = send(m_sock,(char*)(&a),sizeof(int),0);
    /*for (int i = 0; i < sizeof(int) / 2; i++) {
		char* temp = (char*)&len;
		char t;
		t = temp[i];
		temp[i] = temp[sizeof(int) - 1 - i];
		temp[sizeof(int) - 1 - i] = t;
	}*/
	iErrMsg = send(m_sock,msg,len,0);//������Ϣ��ָ��sock������Ϣ
	printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%d\n",len);
	//printf
	if (iErrMsg < 0)//����ʧ��
	{
		printf("send msg failed with error : %d\n",iErrMsg);

		rlt = 1;
		return rlt;
	}

	return rlt;
}

void CClientNet::Close()
{
	closesocket(m_sock);
}

/*int CClientNet::Init( const char* address,int port )
{
	int rlt = 0;

	//���ڼ�¼������Ϣ�������
	int iErrorMsg;

	//��ʼ��WinSock
	WSAData wsaData;
	iErrorMsg = WSAStartup(MAKEWORD(1,1),&wsaData);
	
	if (iErrorMsg != NO_ERROR)
	{
		//��ʼ��WinSockʧ��
		printf("wsastartup failed with error : %d\n",iErrorMsg);

		rlt = 1;
		return rlt;
	}

	//���������Socket
	m_sock = socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
	if (m_sock == INVALID_SOCKET)
		
	{
		//����Socket�쳣
		printf("socket failed with error : %d\n",WSAGetLastError());

		rlt = 2;
		return rlt;
	}

	//������Ϣ
	sockaddr_in serverAddr;
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_port = port;
	serverAddr.sin_addr.s_addr = inet_addr(address);

	//��
	iErrorMsg = bind(m_sock,(sockaddr*)&serverAddr,sizeof(serverAddr));
	if (iErrorMsg < 0)
	{
		//��ʧ��
		printf("bind failed with error : %d\n",iErrorMsg);
		rlt = 3;
		return rlt;
	}

	

	return rlt;
}*/

string CClientNet::RecMsg()
{
	char recData[1024];  
	
	for(int i = 0; i <= 1023; i++)
	    recData[i] = ' ';
	    
    int ret = recv(m_sock, recData, 1024, 0);  
    if(ret>0)
	{  
        recData[ret] = 0x00;  
        printf(recData);  
    }  
	
	string s = recData;
	return s;
	
	/*sockaddr_in tcpAddr;
	int len = sizeof(sockaddr);
	SOCKET newSocket;
	char buf[1024];
	int rval;
	string s;
	
	newSocket = accept(m_sock,(sockaddr*)&tcpAddr,&len);//������Ϣ
		
	if (newSocket == INVALID_SOCKET){}//�ǿ���socket
	else//��socket����
	{
		printf("new socket connect : %d\n",newSocket);

		do//��Ϣ����
		{
			memset(buf,0,sizeof(buf));//��������
			rval = recv(newSocket,buf,1024,0);
				
			if (rval == SOCKET_ERROR)//��Ӧ���Ǹ��쳣�����ͻ���û�е���closeSocket��ֱ���˳���Ϸ��ʱ�򣬽����������
				printf("recv socket error\n");
				
			if (rval == 0)//recv����0��ʾ�����˳�
				printf("ending connection");
			else
			{
				s = buf;//��ʾ���յ�������
				cout << s << endl;
			}
		}
		while(rval != 0);

		closesocket(newSocket);//�رն�ӦAccept��socket
		return s;
	}	*/
}

void CClientNet::Run()
{
	//��������
	listen(m_sock,5);

	

	while(1)
	{
		
	}

	//�ر������Socket
	closesocket(m_sock);
}
