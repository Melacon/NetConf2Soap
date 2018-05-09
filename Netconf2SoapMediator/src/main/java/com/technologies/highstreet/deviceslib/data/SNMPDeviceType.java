package com.technologies.highstreet.deviceslib.data;

public enum SNMPDeviceType {
	SIMULATOR,
	EXAMPLEDEVICE;

	public static SNMPDeviceType FromInt(int x)
	{
		switch(x)
		{
		case 0:
			return SIMULATOR;
		case 1:
			return EXAMPLEDEVICE;
		}
		return SIMULATOR;
	}
}
