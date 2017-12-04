#ifndef SLAVE_H
#define SLAVE_H

#include "functions.h" 
#include "define.h"


using namespace std;

typedef struct{
    string slave_id;
	string slave_user;
	string slave_password;
	}Userinfo;

class Slave
{
	private:
		
		int slave_num;//从机编号 
		int slave_state;//从机当前状态 
		int slave_current_temp;//从机当前温度 
		int slave_target_temp;//从机设定温度 
		int slave_current_wind_speed;//从机当前风速
		int slave_target_wind_speed;//从机设定风速
		int slave_mode;//从机模式 
		string slave_user;
		string slave_password;
		string slave_id;
		Userinfo slave_userinfo_queue[NUM_QUEUE];
		int slave_queuenum;
		int slave_inspection_frequency;
		int slave_fare;
		int slave_energy;
		int slave_wind_permitted;
		int isCurrentTempChanged;
		bool isModeChanged;
						
	public:
		Slave();
		void setSlaveNum(int roomNum);
		void clearQueue();
		void loginReqHandler(Userinfo userInfo);
		void loginACKHandler(Userinfo userInfo, bool isSucceed);//Userinfo的ID字段随意
		int get_slave_num(){return slave_num;}
		int get_slave_state(){return slave_state;}
		int get_slave_current_temp(){return slave_current_temp;}
		int get_slave_target_temp(){return slave_target_temp;}
		int get_slave_current_wind_speed(){return slave_current_wind_speed;}
		int get_slave_target_wind_speed(){return slave_target_wind_speed;}
		int get_slave_mode(){return slave_mode;}
		string get_slave_user(){return slave_user;}
		string get_slave_password(){return slave_password;}	
		Userinfo get_slave_userinfo_queue(){return slave_userinfo_queue[0];}
		string get_slave_queue_id(){return slave_userinfo_queue[0].slave_id;}
		string get_slave_queue_user(){ return slave_userinfo_queue[0].slave_user;}
		string get_slave_queue_password(){ return slave_userinfo_queue[0].slave_password;}
		int get_slave_queuenum(){return slave_queuenum;}
		int get_slave_inspection_frequency(){return slave_inspection_frequency;}
		int get_slave_fare(){return slave_fare;}
		int get_slave_energy(){return slave_energy;}
		int get_slave_wind_permitted(){ return slave_wind_permitted; }
		int get_isCurrentTempChanged(){ return isCurrentTempChanged; }
		int get_isModeChanged(){ return isModeChanged; }
		
		void update_slave_num(int n){slave_num = n;}
		void update_slave_state(int s){slave_state = s;}
		void update_slave_current_temp(int ct){slave_current_temp += ct;}
		void setCurrentTemp(int temp);
		int update_slave_target_temp(int);
		int update_slave_current_wind_speed(int);
		void update_slave_target_wind_speed(int ts){ slave_current_wind_speed = ts; }
		int update_slave_mode(int);
		void update_slave_user(string u){slave_user = u;}
		void update_slave_password(string p){slave_password = p;}
		int update_slave_id_queue(string,int);
		//void update_slave_queuenum(int q){slave_queuenum = q;}
		void update_slave_inspection_frequency(int f){slave_inspection_frequency = f;}
		void update_slave_fare(int f){slave_fare = f;}
		void update_slave_energy(int e){slave_energy = e;}	
		void update_slave_wind_permitted(int);
		void update_isCurrentTempChanged(){ isCurrentTempChanged = 0; }
		void update_isModeChanged(int c){ isModeChanged = c; }
		int update_slave_userinfo_queue(string,string,string);
		
		void delete_queue();
		void reset_slave();
		int judge_slave_info(string, string);
		int judge_slave_temp();
};

#endif
