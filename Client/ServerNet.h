#pragma comment (lib,"ws2_32.lib")
#include "functions.h"
#include "define.h"

class CServerNet
{
    public:
    	SOCKET m_sock;
	    int Init(const char*,int);//初始化服务器,返回0表示成功
	    string RecMsg();
		void Run();//更新数据

    ///private:
	    //SOCKET m_sock;
};
