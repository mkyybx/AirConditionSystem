#ifndef XMLINFO_H
#define XMLINFO_H

#include "functions.h"
#include "define.h"
#include "slave.h"
#include "sensor.h"
#include "tinystr.h"
#include "tinyxml.h"

using namespace std;

class XMLInfo
{
	private:
		
		
	public:
		XMLInfo(){} 
		const char *int_to_const_char(int);
		string build_Reg_doc(Slave*);
		string build_Login_ACK_doc(Slave*,int);
		string build_Sensor_Temp_doc(Slave*);
		string build_Mode_doc(Slave*);
		string build_Set_Temp_doc(Slave*);
		string build_Fare_Info_doc(Slave*);
		string build_N_AC_Req_doc(Slave*,int);
		string build_N_Temp_Submit_doc(Slave*,int);
		string build_N_Login_doc(Slave*,int);
		string build_N_Temp_Submit_Freq_doc(Slave*);
		int load_Login_doc(TiXmlElement*,Slave*);
		int load_Set_Temp_doc(TiXmlElement*,Slave*);
		int load_N_Login_ACK_doc(TiXmlElement*,Slave*);
		int load_N_Mode_doc(TiXmlElement*,Slave*);
		void load_N_Fare_Info_doc(TiXmlElement*,Slave*);
		void load_N_Temp_Submit_Freq_doc(TiXmlElement*,Slave*);
};

#endif
