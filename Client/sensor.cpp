#include "sensor.h"

int Sensor::sensor_calculate_temp(int ws, int judge, int permitted)
{
	Sleep(1000);

	if (judge == FALSE && permitted == TRUE)
	{
		temp = temp + ws;

		if (temp >= 10)
		{
			temp = 0;
			return 1;
		}
		else
			return 0;
	}
	
	return 0;
}
