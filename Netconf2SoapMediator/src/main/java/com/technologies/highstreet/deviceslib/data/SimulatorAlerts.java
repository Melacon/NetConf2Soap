package com.technologies.highstreet.deviceslib.data;

public class SimulatorAlerts {
	private static final SNMPAlert[] mPossibleAlerts = {

	};
	private static String[] mOIDList = null;

	public static final String[] GetOIDs() {
		if (mOIDList == null) {
			mOIDList = new String[mPossibleAlerts.length];
			for (int i = 0; i < mPossibleAlerts.length; i++)
				mOIDList[i] = mPossibleAlerts[i].OID;
		}
		return mOIDList;
	}

	public static SNMPAlert[] GetAll() {
		return mPossibleAlerts;
	}
}
