#include "sensor.h"

int Sensor::sensor_calculate_temp(int ws)
{
	Sleep(1000);
	temp = temp + ws;

	if (temp >= 10)
	{
		temp = 0;
		return 1;
	}	
	else
		return 0;
}
