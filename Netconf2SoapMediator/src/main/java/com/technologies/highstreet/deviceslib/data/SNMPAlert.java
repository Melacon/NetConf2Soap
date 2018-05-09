package com.technologies.highstreet.deviceslib.data;

import java.util.Date;

public class SNMPAlert {

	public static class CREATOR
	{
		public static SNMPAlert Create(String sn,String oid,String desc)
		{
			return new SNMPAlert(sn,oid,desc);
		}
	}
	public final String ShortName;
	public final String OID;
	public final String Description;
	public final Date TimeStamp;

	private SNMPAlert(String sn,String oid, String desc)
	{
		this(sn,oid,desc,new Date());
	}
	private SNMPAlert(String sn,String oid,String desc,Date ts)
	{
		this.ShortName=sn;
		this.OID = oid;
		this.Description = desc;
		this.TimeStamp = ts;
	}


}
