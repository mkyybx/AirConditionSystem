#include "define.h"

#include "functions.h"

class CClientNet
{
    public:
    	SOCKET m_sock;
	    int Connect(const char*,const char*);//连接上指定服务器
		int SendMsg(string);//发送信息
	    void Close();//关闭
	    //int Init(const char*,int);//初始化服务器,返回0表示成功
	    string RecMsg();
		void Run();//更新数据
};
