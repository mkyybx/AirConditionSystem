#ifndef SENSOR_H
#define SENSOR_H

#include "functions.h"
#include "define.h"

using namespace std;

class Sensor
{
	private:
		int temp;

	public:
		Sensor()
		{
			temp = 0;
		}
		int sensor_calculate_temp(int, int, int);
};

#endif
