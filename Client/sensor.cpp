#include "sensor.h"

int Sensor::sensor_calculate_temp(int ws)
{
	sensor_start_time = time(NULL);
	
	while(1) 
	{
		sensor_current_time = time(NULL);
		sensor_past_time = sensor_current_time - sensor_start_time;
		
		if(sensor_past_time * ws >= 20)
		{
			return 0;
		}
	}
}
