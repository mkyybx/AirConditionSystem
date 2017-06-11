#include "slave.h"


void Slave::setSlaveNum(int roomNum) {
	slave_num = roomNum;
}

Slave::Slave()//初始化，从机开机 
{
	isCurrentTempChanged = false;
	slave_state = OPEN_WITHOUT_LOGIN;
	slave_mode = 1;
	slave_queuenum = 0;
	slave_current_temp = 24;
	slave_target_temp = slave_current_temp;
	slave_current_wind_speed = 1;
	slave_target_wind_speed = slave_current_wind_speed;
	slave_inspection_frequency = 5;
	slave_wind_permitted = FALSE;
}

void Slave::update_slave_wind_permitted(int p)
{ 
	slave_wind_permitted = p;

	if (p == 1)
		cout << "开始吹风！" << endl;
	else
		cout << "停止吹风！" << endl;
}

int Slave::update_slave_target_temp(int tt)
{
	if(tt != slave_target_temp)
	{
		slave_target_temp = tt;
		return 0;
	}
	
	return 1;
}

int Slave::update_slave_current_wind_speed(int ts)
{
	if(ts != slave_current_wind_speed)
	{
		slave_current_wind_speed = ts;
		slave_target_wind_speed = slave_current_wind_speed;
		return 0;
	}
	
	return 1;
}

int Slave::update_slave_mode(int m)
{
	if (slave_mode != m)
	{
		slave_mode = m;

		if (m == SUMMER)
			slave_target_temp = 22;
		else
			slave_target_temp = 28;

		return 2;
	}
	
	
	/*slave_mode = m;

	if (m == SUMMER && slave_target_temp > 25)
	{
		slave_target_temp = 18;
		
		
		if(slave_current_temp <= 18)//不吹风 
		    return 1;
		else 
		    return 2;
	}
	/*else if(m == SUMMER && slave_target_temp < 18)
	{
		slave_target_temp = 25;
		
		if(slave_current_temp <= 25)//不吹风 
		    return 3;
		else 
		    return 4;
	}*/
	/*else if(m == WINTER && slave_target_temp < 25 )
	{
		slave_target_temp = 25;
		
		if(slave_current_temp >= 25)//不吹风 
		    return 5;
		else 
		    return 6;
	}
	/*else if(m == WINTER && slave_target_temp > 30 )
	{
		slave_target_temp = 30;
		
		if(slave_current_temp >= 30)//不吹风 
		    return 7;
		else 
		    return 8;
	}*/
	//else
		return 9;
}

int Slave::update_slave_userinfo_queue(string i,string u,string p)
{
	if(slave_queuenum >= NUM_QUEUE)
	    return 1;
	    
	int j = 0;
	
	for(j = 0;j < slave_queuenum;j++) 
		if (slave_userinfo_queue[j].slave_id == i)
		{
			return 0;
		}
	  
	
	if(j == slave_queuenum)//相同id不在队列中
	{
		slave_userinfo_queue[j].slave_id = i;
		slave_userinfo_queue[j].slave_user = u;
		slave_userinfo_queue[j].slave_password = p;
		slave_queuenum++;
		return 0;
	}
}

void Slave::delete_queue()
{
	int i;
	
	for(i = 0;i < slave_queuenum;i++)
	    slave_userinfo_queue[i] = slave_userinfo_queue[i+1];
	
	slave_queuenum--;
}

void Slave::clearQueue() {
	memset(slave_userinfo_queue, 0, sizeof(Userinfo) * slave_queuenum);
	slave_queuenum = 0;
}

void Slave::reset_slave()
{
	slave_target_temp = slave_current_temp;
	slave_current_wind_speed = 1;
	slave_target_wind_speed = slave_current_wind_speed;
}

int Slave::judge_slave_info(string u,string p)
{
	if (u == slave_user && p == slave_password)
		return TRUE;
	else
		return FALSE;
}

int Slave::judge_slave_temp()
{
	if (slave_current_temp == slave_target_temp)
		return TRUE;
	else
		return FALSE;
}
