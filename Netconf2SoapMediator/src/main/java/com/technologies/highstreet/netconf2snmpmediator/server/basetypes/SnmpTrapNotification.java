package com.technologies.highstreet.netconf2snmpmediator.server.basetypes;

public class SnmpTrapNotification {
	public static final int TYPE_MICROWAVE = 0;
	public static final int TYPE_ETHERNET = 1;
	public static final int TYPE_NETWORKELEMENT = 2;

	private String timeStamp;
	private final String severity;
	private String counter;
	private final String problemName;
	private final String objectIdReference;
	private final int type;

	public void setCounter(String c) {this.counter=c;}
	public void setTimeStamp(String ts) {this.timeStamp=ts;}

	public SnmpTrapNotification(String name,String ref,String sev)
	{
		this.problemName=name;
		this.objectIdReference=ref;
		this.severity=sev;
		this.type=this.findType();
	}
	private int findType() {
		if(this.objectIdReference!=null)
		{
			if(this.objectIdReference.equals("LP-MWTN-Radio"))
				return TYPE_MICROWAVE;
			if(this.objectIdReference.equals("System"))
				return TYPE_NETWORKELEMENT;
			if(this.objectIdReference.equals("LTP-ETC-06-LP-1"))
				return TYPE_ETHERNET;
		}
		return TYPE_MICROWAVE;
	}
	public String toXML()
	{
		return String.format("<problem-notification xmlns=\"urn:onf:params:xml:ns:yang:microwave-model\">"+
				"<counter>%s</counter>"+
				"<time-stamp>%s</time-stamp>"+
				"<problem>%s</problem>"+
				"<severity>%s</severity>"+
				"</problem-notification>", this.counter,this.timeStamp,this.problemName,this.objectIdReference,this.severity);
	}
	public boolean isNonAlarmed() {
		return this.severity.equals("non-alarmed");
	}
	public int getType(){
		return this.type;
	}
	public String getProblemName() {
		return this.problemName;
	}
	public String getSeverity() {
		return this.severity;
	}
	public String getTimestamp() {
		return this.timeStamp;
	}
	public String getCounter() {
		return this.counter;
	}

}
