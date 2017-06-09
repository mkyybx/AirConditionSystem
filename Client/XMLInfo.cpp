#include "XMLInfo.h"

const char * XMLInfo::int_to_const_char(int x)
{
    char* a = new char[11];
    const char *p=itoa(x,a,10);
    cout << "lalala" << p << endl;
    return a;
}
////////////////////////////////////////////////////////////////////////////////////////////////////
string XMLInfo::build_Reg_doc(Slave* s)//从机注册 
{
    TiXmlPrinter printer;
    string xmlstr;
    TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Reg" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Client_NO" );
	msg->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_num())));
	root->LinkEndChild( msg );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Login_ACK_doc(int isSucceed, string ID)//登录ACK 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Login_ACK" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "ID" );
	msg1->LinkEndChild(new TiXmlText(ID.c_str()));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Succeed");
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(isSucceed)));
	root->LinkEndChild( msg2 );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Sensor_Temp_doc(Slave* s)//传感器温度 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Sensor_Temp" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Sensor_temp" );
	msg->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_current_temp())));
	root->LinkEndChild( msg );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Mode_doc(Slave* s)//模式信息
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Mode" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Heater" );
	msg->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_mode())));
	root->LinkEndChild( msg );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Set_Temp_doc(Slave* s)//登录ACK 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Set_Temp" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Temp" );
	msg1->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_target_temp())));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Wind_Level");
	msg2->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_target_wind_speed())));
	root->LinkEndChild( msg2 );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Fare_Info_doc(Slave* s)//消费信息 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Fare_Info" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Fare" );
	msg1->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_fare())));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Energy");
	msg2->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_energy())));
	root->LinkEndChild( msg2 );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}
////////////////////////////////////////////////////////////////////////////////////////////////////
string XMLInfo::build_N_AC_Req_doc(Slave* s,int p)//温控请求
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "AC_Req" );
	doc.LinkEndChild( root );
	
	TiXmlElement * msg1 = new TiXmlElement( "Positive" );
	msg1->LinkEndChild( new TiXmlText(int_to_const_char(p)));
	root->LinkEndChild( msg1 );
	
	TiXmlElement * msg2 = new TiXmlElement( "Wind_Level");
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(s->get_slave_current_wind_speed())));
	root->LinkEndChild( msg2 );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_N_Temp_Submit_doc(Slave* s,int p)//温度上报（心跳）
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Temp_Submit" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Time" );
	msg1->LinkEndChild( new TiXmlText(int_to_const_char(0)));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Client_No");
	msg2->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_num())));
	root->LinkEndChild( msg2 );
	TiXmlElement * msg3 = new TiXmlElement( "Temp");
	msg3->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_current_temp())));
	root->LinkEndChild( msg3 );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Login_doc(Slave* s,int p)//登录
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Login" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Name" );
	msg1->LinkEndChild(new TiXmlText(s->get_slave_user().c_str()));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Password");
	msg2->LinkEndChild(new TiXmlText(s->get_slave_password().c_str()));
	root->LinkEndChild( msg2 );
	TiXmlElement * msg3 = new TiXmlElement( "Client_No");
	msg3->LinkEndChild(new TiXmlText(int_to_const_char(s->get_slave_num())));
	root->LinkEndChild( msg3 );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_N_Temp_Submit_Freq_doc(Slave* s)//监测频率 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Temp_Submit_Freq" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Temp_Submit_Freq" );
	msg->LinkEndChild( new TiXmlText(int_to_const_char(s->get_slave_inspection_frequency())));
	root->LinkEndChild( msg );

	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}
////////////////////////////////////////////////////////////////////////////////////////////////////
Userinfo XMLInfo::load_Login_doc(TiXmlElement* pElement,Slave* s)
{
	Userinfo userinfo;

	TiXmlNode* pRecord1 = pElement->FirstChild("ID");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	userinfo.slave_id = pElement1 -> GetText();
	
	TiXmlNode* pRecord2 = pElement->FirstChild("User");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	userinfo.slave_user = pElement2 -> GetText();
	
	TiXmlNode* pRecord3 = pElement->FirstChild("Password");
	TiXmlElement* pElement3 = pRecord3->ToElement();
	userinfo.slave_password	= pElement3 -> GetText();
	
	return userinfo;
}

int XMLInfo::load_Set_Temp_doc(TiXmlElement* pElement,Slave* s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Temp");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int tt = atoi(pElement1 -> GetText());
	int suc1, suc2;

	if (s->get_slave_mode() == WINTER)
	{
		if (tt <= s->get_slave_current_temp())
			suc1 = 1;
		else
		{
			//if (tt - s->get_slave_current_temp() > 1)
				suc1 = s->update_slave_target_temp(tt);
			//else
				//suc1 = 1;
		}
	}	
	else if (s->get_slave_mode() == SUMMER)
	{
		if (tt >= s->get_slave_current_temp())
			suc1 = 1;
		else 
		{
			//if (s->get_slave_current_temp() - tt > 1)
				suc1 = s->update_slave_target_temp(tt);
			//else
				//suc1 = 1;
		}
	}
	else
	    suc1 = s->update_slave_target_temp(tt);
	
	TiXmlNode* pRecord2 = pElement->FirstChild("Wind_Level");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	int ts = atoi(pElement2 -> GetText());
	suc2 = s->update_slave_target_wind_speed(ts);
	
	return suc1 + suc2;
}

//Userinfo 中 ID 为是否成功
Userinfo XMLInfo::load_Login_ACK_doc(TiXmlElement* pElement,Slave* s)
{
	Userinfo userinfo;
	TiXmlNode* pRecord1 = pElement->FirstChild("Succeed");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	userinfo.slave_id = pElement1->GetText();

	TiXmlNode* pRecord2 = pElement->FirstChild("Name");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	userinfo.slave_user = pElement2->GetText();

	TiXmlNode* pRecord3 = pElement->FirstChild("Password");
	TiXmlElement* pElement3 = pRecord3->ToElement();
	userinfo.slave_password = pElement3->GetText();

	TiXmlNode* pRecord4 = pElement->FirstChild("Mode");
	TiXmlElement* pElement4 = pRecord4->ToElement();
	int mode = atoi(pElement4->GetText());
	s->update_slave_mode(mode);   
	
	return userinfo;
}

int XMLInfo::load_N_Mode_doc(TiXmlElement* pElement,Slave* s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Heater");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int i = atoi(pElement1->GetText());  
	int r = s->update_slave_mode(i);
	return r;
}

void XMLInfo::load_N_Fare_Info_doc(TiXmlElement* pElement,Slave* s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Fare");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int fare = atoi(pElement1->GetText());
	s->update_slave_fare(fare);
	
	TiXmlNode* pRecord2 = pElement->FirstChild("Energy");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	int energy = atoi(pElement2->GetText());
	s->update_slave_energy(energy);
}

void XMLInfo::load_N_Temp_Submit_Freq_doc(TiXmlElement* pElement,Slave* s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Temp_Submit_Freq");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int i = atoi(pElement1->GetText());  
	
	s->update_slave_inspection_frequency(i);
}

void XMLInfo::load_N_Wind_doc(TiXmlElement* pElement, Slave* s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Level");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int l = atoi(pElement1->GetText());
	int suc1 = s->update_slave_target_wind_speed(l);
	
	TiXmlNode* pRecord2 = pElement->FirstChild("Start_Blowing");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	int p = atoi(pElement2->GetText());
	s->update_slave_wind_permitted(p);
}