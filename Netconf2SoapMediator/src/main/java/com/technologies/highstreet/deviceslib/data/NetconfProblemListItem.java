package com.technologies.highstreet.deviceslib.data;

import java.util.Date;

public class NetconfProblemListItem {

	public final int SequenceNumber;
	public final Date TimeStamp;
	public final String Description;
	public final NetconfSeverity Severity;
	
	public NetconfProblemListItem(int seqno,String desc,NetconfSeverity s)
	{
		this(seqno,desc,s,new Date());
	}
	public NetconfProblemListItem(int seqno,String desc, NetconfSeverity s,Date ts)
	{
		this.SequenceNumber=seqno;
		this.Description=desc;
		this.Severity = s;
		this.TimeStamp=ts;
	}
}
