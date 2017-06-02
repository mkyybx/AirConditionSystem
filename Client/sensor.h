#ifndef SENSOR_H
#define SENSOR_H

#include "functions.h"
#include "define.h"

using namespace std;

class Sensor
{
	private:
		time_t sensor_start_time;
		time_t sensor_current_time;
		time_t sensor_past_time;
	public:
		Sensor(){}
		int sensor_calculate_temp(int);
};

#endif
