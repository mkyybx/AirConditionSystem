#include "slave.h"

Slave::Slave()//初始化，从机开机 
{
	slave_num = 1;
	slave_state = OPEN_WITHOUT_LOGIN;
	slave_mode = 1;
	slave_queuenum = 0;
	slave_current_temp = 26;
	slave_target_temp = slave_current_temp;
	slave_current_wind_speed = 0;
	slave_target_wind_speed = 0;
	slave_inspection_frequency = 5;
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

int Slave::update_slave_target_wind_speed(int ts)
{
	if(ts != slave_target_wind_speed)
	{
		slave_target_wind_speed = ts;
		return 0;
	}
	
	return 1;
}

int Slave::update_slave_mode(int m)
{
	if(slave_mode == 0 && m == 1 && slave_target_temp < 18 )
	{
		slave_target_temp = 18;
		slave_mode = m;
		
		if(slave_current_temp <= 18)//不吹风 
		    return 1;
		else 
		    return 2;
	}
	else if(slave_mode == 0 && m == 1 && slave_target_temp > 25 )
	{
		slave_target_temp = 25;
		slave_mode = m;
		
		if(slave_current_temp <= 25)//不吹风 
		    return 3;
		else 
		    return 4;
	}
	else if(slave_mode == 1 && m == 0 && slave_target_temp < 25 )
	{
		slave_target_temp = 25;
		slave_mode = m;
		
		if(slave_current_temp >= 25)//不吹风 
		    return 5;
		else 
		    return 6;
	}
	else if(slave_mode == 1 && m == 0 && slave_target_temp > 30 )
	{
		slave_target_temp = 30;
		slave_mode = m;
		
		if(slave_current_temp >= 30)//不吹风 
		    return 7;
		else 
		    return 8;
	}
	else
		return 0;
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
