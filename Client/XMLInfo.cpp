#include "XMLInfo.h"

const char * XMLInfo::int_to_const_char(int x)
{
    char* a = new char[11];
    const char *p=itoa(x,a,10);
    cout << "lalala" << p << endl;
    return a;
}
////////////////////////////////////////////////////////////////////////////////////////////////////
string XMLInfo::build_Reg_doc(Slave s)//从机注册 
{
    TiXmlPrinter printer;
    string xmlstr;
    TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Reg" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Client_NO" );
	cout << "111111111111111 " << int_to_const_char(s.get_slave_num()) << endl;
	msg->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_num())));
	root->LinkEndChild( msg );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Login_ACK_doc(Slave s,int suc)//登录ACK 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Login_ACK" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "ID" );
	msg1->LinkEndChild( new TiXmlText(s.get_slave_queue_id().c_str()));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Succeed");
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(suc)));
	root->LinkEndChild( msg2 );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Sensor_Temp_doc(Slave s)//传感器温度 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Sensor_temp" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Sensor_temp" );
	msg->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_current_temp())));
	root->LinkEndChild( msg );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Mode_doc(Slave s)//模式信息
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Mode" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Heater" );
	msg->LinkEndChild(new TiXmlText(int_to_const_char(s.get_slave_mode())));
	root->LinkEndChild( msg );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Set_Temp_doc(Slave s)//登录ACK 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Set_Temp" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Temp" );
	msg1->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_target_temp())));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Wind_Level");
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_target_wind_speed())));
	root->LinkEndChild( msg2 );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_Fare_Info_doc(Slave s)//消费信息 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Fare_Info" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Fare" );
	msg1->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_fare())));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Energy");
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_energy())));
	root->LinkEndChild( msg2 );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}
////////////////////////////////////////////////////////////////////////////////////////////////////
string XMLInfo::build_N_AC_Req_doc(Slave s,int p)//温控请求
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
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_current_wind_speed())));
	root->LinkEndChild( msg2 );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_N_Temp_Submit_doc(Slave s,int p)//温度上报（心跳）
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Temp_Submit" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Time" );
	//msg1->LinkEndChild( new TiXmlText(int_to_const_char(p)));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Client_No");
	msg2->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_num())));
	root->LinkEndChild( msg2 );
	TiXmlElement * msg3 = new TiXmlElement( "Temp");
	msg3->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_current_temp())));
	root->LinkEndChild( msg3 );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_N_Login_doc(Slave s,int p)//登录
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Login" );
	doc.LinkEndChild( root );
	TiXmlElement * msg1 = new TiXmlElement( "Name" );
	msg1->LinkEndChild( new TiXmlText(s.get_slave_userinfo_queue().slave_user.c_str()));
	root->LinkEndChild( msg1 );
	TiXmlElement * msg2 = new TiXmlElement( "Password");
	msg2->LinkEndChild( new TiXmlText(s.get_slave_userinfo_queue().slave_password.c_str()));
	root->LinkEndChild( msg2 );
	TiXmlElement * msg3 = new TiXmlElement( "Client_No");
	msg3->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_num())));
	root->LinkEndChild( msg3 );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}

string XMLInfo::build_N_Temp_Submit_Freq_doc(Slave s)//监测频率 
{
    TiXmlPrinter printer;
    string xmlstr;
	TiXmlDocument doc;
	
	TiXmlElement * root = new TiXmlElement( "Temp_Submit_Freq" );
	doc.LinkEndChild( root );
	TiXmlElement * msg = new TiXmlElement( "Temp_Submit_Freq" );
	msg->LinkEndChild( new TiXmlText(int_to_const_char(s.get_slave_inspection_frequency())));
	root->LinkEndChild( msg );
	root->Accept(&printer);
    xmlstr = printer.CStr();
	
	return xmlstr;
}
////////////////////////////////////////////////////////////////////////////////////////////////////
int XMLInfo::load_Login_doc(TiXmlElement* pElement,Slave s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("ID");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	string id(pElement1 -> GetText());
	
	TiXmlNode* pRecord2 = pElement1->FirstChild("User");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	string user(pElement2 -> GetText());
	
	TiXmlNode* pRecord3 = pElement2->FirstChild("Password");
	TiXmlElement* pElement3 = pRecord3->ToElement();
	string password(pElement3 -> GetText());
	
	int suc = s.update_slave_userinfo_queue(id,user,password);
	return suc;
}

int XMLInfo::load_Set_Temp_doc(TiXmlElement* pElement,Slave s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Temp");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int tt = atoi(pElement1 -> GetText());
	int suc1 = s.update_slave_target_temp(tt);
	
	TiXmlNode* pRecord2 = pElement1->FirstChild("Wind_Level");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	int ts = atoi(pElement2 -> GetText());
	int suc2 = s.update_slave_target_wind_speed(ts);
	
	return suc1 + suc2;
}

int XMLInfo::load_N_Login_ACK_doc(TiXmlElement* pElement,Slave s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Succeed");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int suc = atoi(pElement1->GetText());
	
	if(suc == 1) 
	{
		TiXmlNode* pRecord2 = pElement1->FirstChild("Name");
	    TiXmlElement* pElement2 = pRecord2->ToElement();
	    string user(pElement2->GetText());
	    s.update_slave_user(user);
	    
	    TiXmlNode* pRecord3 = pElement2->FirstChild("Password");
	    TiXmlElement* pElement3 = pRecord3->ToElement();
	    string password(pElement3->GetText());
	    s.update_slave_password(password);
	    
	    s.update_slave_state(OPEN_WITH_LOGIN);
	} 
	else
	{
		s.delete_queue();
		s.update_slave_state(OPEN_WITHOUT_LOGIN);
	}   
	
	return suc;
}

int XMLInfo::load_N_Mode_doc(TiXmlElement* pElement,Slave s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Heater");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int i = atoi(pElement1->GetText());  
	int r = s.update_slave_mode(i);
	return r;
}

void XMLInfo::load_N_Fare_Info_doc(TiXmlElement* pElement,Slave s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Fare");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int fare = atoi(pElement1->GetText());
	s.update_slave_fare(fare);
	
	TiXmlNode* pRecord2 = pElement1->FirstChild("Energy");
	TiXmlElement* pElement2 = pRecord2->ToElement();
	int energy = atoi(pElement2->GetText());
	s.update_slave_energy(energy);
}

void XMLInfo::load_N_Temp_Submit_Freq_doc(TiXmlElement* pElement,Slave s)
{
	TiXmlNode* pRecord1 = pElement->FirstChild("Temp_Submit_Freq");
	TiXmlElement* pElement1 = pRecord1->ToElement();
	int i = atoi(pElement1->GetText());  
	
	s.update_slave_inspection_frequency(i);
}
