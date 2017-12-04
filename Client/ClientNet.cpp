#include "ClientNet.h"


int CClientNet::Connect(const char* port, const char* address) {
	WSADATA wsaData;
	SOCKET ConnectSocket = INVALID_SOCKET;
	struct addrinfo *result = NULL,
		*ptr = NULL,
		hints;
	char recvbuf[1024];
	int recvbuflen;
	int iResult;

	// Initialize Winsock
	iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != 0) {
		printf("WSAStartup failed with error: %d\n", iResult);
		return 1;
	}

	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;

	// Resolve the server address and port
	iResult = getaddrinfo(address, port, &hints, &result);
	if (iResult != 0) {
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		return 1;
	}

	// Attempt to connect to an address until one succeeds
	for (ptr = result; ptr != NULL; ptr = ptr->ai_next) {

		// Create a SOCKET for connecting to server
		ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype,
			ptr->ai_protocol);
		if (ConnectSocket == INVALID_SOCKET) {
			printf("socket failed with error: %ld\n", WSAGetLastError());
			WSACleanup();
			return 1;
		}

		// Connect to server.
		iResult = connect(ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);
		if (iResult == SOCKET_ERROR) {
			closesocket(ConnectSocket);
			ConnectSocket = INVALID_SOCKET;
			continue;
		}
		break;
	}

	freeaddrinfo(result);

	if (ConnectSocket == INVALID_SOCKET) {
		printf("Unable to connect to server!\n");
		WSACleanup();
		return 1;
	}
	WCHAR abc = 'b';
	sendingMutex = CreateMutex(0, false, &abc);
	m_sock = ConnectSocket;
	return 0;
}

int CClientNet::SendMsg(string m) {
	if (m_sock == 0) {
		//printf("in SendMsg, m_sock=0\n");
		return -1;
	}
	int len = m.size();
	int a = htonl(len);
	char* length = (char*)(&a);
	WaitForSingleObject(sendingMutex, INFINITE);
	send(m_sock, length, sizeof(int), 0);
	ReleaseMutex(sendingMutex);
	int state = send(m_sock, m.c_str(), len, 0);
	cout << GetCurrentThreadId() <<" 发送信息:" << endl << m << endl;
	return state;
}

string CClientNet::RecMsg() {
	if (m_sock == 0) {
		printf("in RecMsg, m_sock=0\n");
		return "NoMsg";
	}
	char buf[1024];
	int length = 0;
	for (int i = 0; i < sizeof(int); ) {
		length = recv(m_sock, buf + i, sizeof(int) - i, 0);
		if (length <= 0)
			break;
		else i += length;
	}
	if (length <= 0)
		return "";
	else {
		int num = *(int*)buf;
		num = htonl(num);
		for (int i = 0; i < num;) {
			length = recv(m_sock, buf + i, num - i, 0);
			if (length <= 0)
				break;
			else i += length;
		}
		if (length <= 0)
			return "";
		buf[num] = 0;
		cout << "收到信息:" << endl << buf << endl;

		return string(buf);
	}
}

/*
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
/*
	int a = htonl(len);
    iErrMsg = send(m_sock,(char*)(&a),sizeof(int),0);*/
    /*for (int i = 0; i < sizeof(int) / 2; i++) {
		char* temp = (char*)&len;
		char t;
		t = temp[i];
		temp[i] = temp[sizeof(int) - 1 - i];
		temp[sizeof(int) - 1 - i] = t;
		}*//*
	iErrMsg = send(m_sock,msg,len,0);//发送消息，指定sock发送消息
	printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%d\n",len);
	//printf
	if (iErrMsg < 0)//发送失败
	{
		printf("send msg failed with error : %d\n",iErrMsg);

		rlt = 1;
		return rlt;
	}

	return rlt;
}*/

void CClientNet::Close()
{
	//closesocket(m_sock);
	;
}

/*int CClientNet::Init( const char* address,int port )
{
	int rlt = 0;

	//用于记录错误信息，并输出
	int iErrorMsg;

	//初始化WinSock
	WSAData wsaData;
	iErrorMsg = WSAStartup(MAKEWORD(1,1),&wsaData);
	
	if (iErrorMsg != NO_ERROR)
	{
		//初始化WinSock失败
		printf("wsastartup failed with error : %d\n",iErrorMsg);

		rlt = 1;
		return rlt;
	}

	//创建服务端Socket
	m_sock = socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
	if (m_sock == INVALID_SOCKET)
		
	{
		//创建Socket异常
		printf("socket failed with error : %d\n",WSAGetLastError());

		rlt = 2;
		return rlt;
	}

	//声明信息
	sockaddr_in serverAddr;
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_port = port;
	serverAddr.sin_addr.s_addr = inet_addr(address);

	//绑定
	iErrorMsg = bind(m_sock,(sockaddr*)&serverAddr,sizeof(serverAddr));
	if (iErrorMsg < 0)
	{
		//绑定失败
		printf("bind failed with error : %d\n",iErrorMsg);
		rlt = 3;
		return rlt;
	}

	

	return rlt;
}*/
/*
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
	
	newSocket = accept(m_sock,(sockaddr*)&tcpAddr,&len);//接收信息
		
	if (newSocket == INVALID_SOCKET){}//非可用socket
	else//新socket连接
	{
		printf("new socket connect : %d\n",newSocket);

		do//消息处理
		{
			memset(buf,0,sizeof(buf));//接收数据
			rval = recv(newSocket,buf,1024,0);
				
			if (rval == SOCKET_ERROR)//这应该是个异常，当客户端没有调用closeSocket就直接退出游戏的时候，将会进入这里
				printf("recv socket error\n");
				
			if (rval == 0)//recv返回0表示正常退出
				printf("ending connection");
			else
			{
				s = buf;//显示接收到的数据
				cout << s << endl;
			}
		}
		while(rval != 0);

		closesocket(newSocket);//关闭对应Accept的socket
		return s;
	}	*/
/*}*/
/*
void CClientNet::Run()
{
	//公开连接
	//listen(m_sock,5);

	

	while(1)
	{
		
	}

	//关闭自身的Socket
	closesocket(m_sock);
}*/
