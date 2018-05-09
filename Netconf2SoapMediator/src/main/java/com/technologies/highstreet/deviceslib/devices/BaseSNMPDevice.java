package com.technologies.highstreet.deviceslib.devices;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.technologies.highstreet.deviceslib.data.*;

public abstract class BaseSNMPDevice {

    protected final HashMap<String, Object> mValues;
    protected final NetconfProblemCollection mCurrentProblems;
    private final SNMPDeviceType mType;
    public SNMPDeviceType getType()
    {return this.mType;}
    public NetconfProblemCollection getCurrentProblems()
    {return this.mCurrentProblems;}

    public BaseSNMPDevice(SNMPDeviceType t,String[] oids,Object[] defaultValues)
    {
        this.mType=t;
        this.mValues=new HashMap<String, Object>();
        if(oids==null) {
			oids=new String[]{};
		}
        for(int i=0;i<oids.length;i++) {
			this.mValues.put(oids[i], defaultValues!=null&& defaultValues.length-1>i?defaultValues[i]:null);
		}
        this.mCurrentProblems=new NetconfProblemCollection();
    }

    public abstract String getBaseOID();

    /*
     * Indicates if SNMPTrapHost is possible to be configured via snmp set requests
     */
    public abstract boolean isSNMPTrapHostConfigureable();
    /*
     * Indicates if it SNMPTrapHost should be set remotely via snmp set requests
     */
    public abstract boolean doSetSNMPTrapHostConfig();
    /*
     * @return List of Commands for setting SNMPTrapHost Ip into device
     */
    public abstract List<SNMPKeyValuePair> getSNMPTrapHostSetCommands(String ip);

    public abstract String getSNMPPingRequestCommand();

    public abstract String ConvertValueSnmp2Netconf(String oid, String value);

    public abstract String ConvertValueNetconf2Snmp(String oid, String value);

    /*
     * Implement any DataConversions for SNMP values
     */
    abstract public String ConvertValue(String conversion, String value) throws Exception;

    /*
     * @return Array of all Available Alerts for this device
     */
    public SNMPAlert[] getAvailableAlerts()
    {
        switch(this.mType)
        {
        case EXAMPLEDEVICE:
            return ExampleDeviceAlerts.GetAll();
        case SIMULATOR:
            return SimulatorAlerts.GetAll();
        default:
            return null;
        }
    }
    /*
     * @param oid the object identifier of the snmp message
     *
     * Indicates if Trap with specific oid is a possible Trap from this device
     */
    public boolean isAlert(String oid) {
        return this.getAvailableAlert(oid)!=null;
    }
    /*
     * @param oid the object identifier of the snmp message
     *
     * @return the SNMPAlert object for the Trap oid, null if not found
     */
    public SNMPAlert getAvailableAlert(String oid)
    {
        for(SNMPAlert a:this.getAvailableAlerts()) {
			if(a.OID.equals(oid)) {
				return a;
			}
		}
        return null;
    }
    /*
     * Adds the SNMPAlert to the device object
     */
    public void onTrapReceived(SNMPAlert a) {
        this.mCurrentProblems.Add(a.ShortName,NetconfSeverity.MAJOR,new Date());
    }


    protected String intToBoolean1True(String value) throws Exception
    {
        int x;
            x = Integer.parseInt(value);
        if(x==1) {
			value="true";
		} else if(x==0) {
			value="false";
		} else {
			throw new Exception("invalid value");
		}
        return value;
    }
    public static class CREATOR
    {
        public static BaseSNMPDevice Create(SNMPDeviceType t)
        {
            switch(t)
            {
            case SIMULATOR:
                return new TestSimulatorSNMPDevice();
            case EXAMPLEDEVICE:
                return new ExampleSNMPDevice();
             default:
                return null;
            }
        }
    }




}
