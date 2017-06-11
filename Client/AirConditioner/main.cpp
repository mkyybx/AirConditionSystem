#include "functions.h" 
#include "control.h"

int main(int argc, char** argv) 
{
	ifstream in("config.ini");
	string serverIP;
	in >> serverIP;
	string serverPort;
	in >> serverPort;
	string clientIP;
	in >> clientIP;
	string clientPort;
	in >> clientPort;
	cout << "server:\n" << serverIP << ":" << serverPort << "\nclient:\n" << clientIP << ":" << clientPort << endl;

	if (argc == 2) {
		stringstream arg;
		arg << argv[1];
		int roomNum = -1;
		arg >> roomNum;
		if (roomNum > 0) {
			Control c;
			c.control_init(serverIP.c_str(), serverPort.c_str(), clientIP.c_str(), clientPort.c_str(), roomNum);
			return 0;
		}
		cout << "房间号不能小于0" << endl;
		return -2;
	}
	cout << "参数错误，参数为房间号！" << endl;
	return -1; 
}
