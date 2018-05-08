/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * @author herbert
 *
 */
public class Test {


	private static void testString()
	{
		String problemName1="ModemRxLossOfSignalLock";
		String problemName1Cleared="ModemRxLossOfSignalLockCleared";

		String sub="";
		int idx=problemName1Cleared.indexOf("Cleared");
		if(idx>0)
			sub=problemName1Cleared.substring(0, idx);
		System.out.println("substring=\""+sub+"\"");
		System.out.println(problemName1.equals(sub));

	}

	 private static void testTrap1() throws Exception {

	        System.out.println("Send a TRAP to Snmp Simulator");
	        // Create PDU
	        PDU trap = new PDU();
	        trap.setType(PDU.TRAP);

	        //OID oid = new OID("1.2.3.4.5");
	        //trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
	        //trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000))); // put your uptime here
	        //trap.add(new VariableBinding(SnmpConstants.sysDescr, new OctetString("System Description")));

	        //Add Payload
	        //Variable var = new OctetString("some string");
	        //trap.add(new VariableBinding(oid, var));

	        OID oid = new OID("1.3.6.1.4.1.47.2.9");
	        Variable var = new OctetString("Modulation is down shift");
	        trap.add(new VariableBinding(oid, var));


	        // Specify receiver
	        Address targetaddress = new UdpAddress("127.0.0.1/10162");
	        CommunityTarget target = new CommunityTarget();
	        target.setCommunity(new OctetString("public"));
	        target.setVersion(SnmpConstants.version2c);
	        target.setAddress(targetaddress);

	        // Send
	        System.out.println("Send now to "+targetaddress.toString()+" "+targetaddress.isValid());
	        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
	        snmp.send(trap, target, null, null);
	        System.out.println("Done");
	    }
	 private static void testTrapHCPRxLoss(String ip,int port,boolean alarmorclear) throws Exception {

	        System.out.println("Send a TRAP to Snmp Simulator");
	        // Create PDU
	        PDU trap = new PDU();
	        trap.setType(PDU.TRAP);


	        trap.add(new VariableBinding(new OID("1.3.6.1.2.1.1.3.0"), new TimeTicks(5000)));
	        if(alarmorclear)
	        	trap.add(new VariableBinding(new OID("1.3.6.1.6.3.1.1.4.1.0"),new OID("1.3.6.1.4.1.7262.2.5.11.31")));  //raise alarm
	        else
	        	trap.add(new VariableBinding(new OID("1.3.6.1.6.3.1.1.4.1.0"),new OID("1.3.6.1.4.1.7262.2.5.11.32")));  //clear alarm
		    trap.add(new VariableBinding(new OID("1.3.6.1.4.1.7262.2.5.5.2.1.3"),new Integer32(3)));
	        trap.add(new VariableBinding(new OID("1.3.6.1.4.1.7262.2.5.4.2.1.1.1"),new Integer32(1)));
	        trap.add(new VariableBinding(new OID("1.3.6.1.4.1.7262.2.100.1.4.1"),new Counter32(27)));


	        // Specify receiver
	        Address targetaddress = new UdpAddress(String.format("%s/%d",ip,port));
	        CommunityTarget target = new CommunityTarget();
	        target.setCommunity(new OctetString("public"));
	        target.setVersion(SnmpConstants.version2c);
	        target.setAddress(targetaddress);

	        // Send
	        System.out.println("Send now to "+targetaddress.toString()+" "+targetaddress.isValid());
	        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
	        snmp.send(trap, target, null, null);
	        System.out.println("Done");
	    }
	 private static void testTrapHQTMRxLoss(String ip,int port,boolean alarmorclear) throws Exception {

	        System.out.println("Send a TRAP to Snmp Simulator");
	        // Create PDU
	        PDU trap = new PDU();
	        trap.setType(PDU.TRAP);


	        trap.add(new VariableBinding(new OID("1.3.6.1.2.1.1.3.0"), new TimeTicks(5000)));
	        if(alarmorclear)
	        	trap.add(new VariableBinding(new OID("1.3.6.1.6.3.1.1.4.1.0"),new OID("1.3.6.1.4.1.7262.2.4.21.22")));  //raise alarm
	        else
	        	trap.add(new VariableBinding(new OID("1.3.6.1.6.3.1.1.4.1.0"),new OID("1.3.6.1.4.1.7262.2.4.21.23")));  //clear alarm
		    trap.add(new VariableBinding(new OID("1.3.6.1.4.1.7262.2.4.7.4.1.1.1.1"),new Integer32(1)));


	        // Specify receiver
	        Address targetaddress = new UdpAddress(String.format("%s/%d",ip,port));
	        CommunityTarget target = new CommunityTarget();
	        target.setCommunity(new OctetString("public"));
	        target.setVersion(SnmpConstants.version2c);
	        target.setAddress(targetaddress);

	        // Send
	        System.out.println("Send now to "+targetaddress.toString()+" "+targetaddress.isValid());
	        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
	        snmp.send(trap, target, null, null);
	        System.out.println("Done");
	    }

    public static void main(String[] args)
    {
    	//testTrap();
    	//testString();
    	try {
    		//testTrapHCPRxLoss("127.0.0.1", 10062);
    		testTrapHCPRxLoss("192.168.178.89", 10001,false);
    		//testTrapHQTMRxLoss("127.0.0.1", 10062,true);
    		//testTrapHQTMRxLoss("192.168.178.89", 10007,false);

    		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
