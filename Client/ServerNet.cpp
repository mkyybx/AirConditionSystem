#include "ServerNet.h"
#include "functions.h"

using namespace std;

int CServerNet::Init( const char* address,int port )
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
}

string CServerNet::RecMsg()
{
	sockaddr_in tcpAddr;
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
	}	
}

void CServerNet::Run()
{
	//��������
	listen(m_sock,5);

	

	while(1)
	{
		
	}

	//�ر������Socket
	closesocket(m_sock);
}
