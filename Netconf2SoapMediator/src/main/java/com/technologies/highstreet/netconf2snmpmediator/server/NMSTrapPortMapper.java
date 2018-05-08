package com.technologies.highstreet.netconf2snmpmediator.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class NMSTrapPortMapper {

	public static final int DEFAULT_NMS_DST_PORT = Config.getInstance().PortMapperPort;
	public static final String DEFAULT_NMS_IP = Config.getInstance().PortMapperIP;
	
	private static final int RESPONSE_LATENCY = Config.getInstance().PortMapperLatency;//in ms
	
	public static final int RET_VALUE_NORESPONSE = -1;
	public static final int RET_VALUE_SUCCEEDED = 0;
	public static final int RET_VALUE_INVALID_RESPONSE = -2;
	public static final int RET_VALUE_CHECKMASTER_YOUARE = 0;
	public static final int RET_VALUE_CHECKMASTER_YOUARENOT = 1;

	private static final int COMAND_PING = 0;
	private static final int COMMAND_REGISTER = 1;
	private static final int COMMAND_UNREGISTER = 2;
	private static final int COMMAND_CHECKMASTER = 3;

	private final String mIP;
	private final int mDstPort;
	private int mCurrentSNMPTrapPort;
	private String mCurrentSNMPMediatorIp;
	private String mCurrentSNMPDeviceIp;
	private boolean mIsRegistered = false;

	public boolean isRegistered() {
		return this.mIsRegistered;
	}

	public NMSTrapPortMapper() {
		this(DEFAULT_NMS_IP,DEFAULT_NMS_DST_PORT);
	}

	public NMSTrapPortMapper(String ip,int port) {
		this.mDstPort = port;
		this.mIP=ip;
	}

	public boolean registerSync(String myip, String ip, int port) {
		this.mCurrentSNMPMediatorIp = myip;
		this.mCurrentSNMPDeviceIp = ip;
		this.mCurrentSNMPTrapPort = port;
		int returnValue;
		// send tcp command to register trapMapping
		String line = this.tcpSend(String.format("%d;%s;%s;%d\n", COMMAND_REGISTER, this.mCurrentSNMPMediatorIp,
				this.mCurrentSNMPDeviceIp, this.mCurrentSNMPTrapPort));
		if (line != null) {
			try {
				returnValue = Integer.parseInt(line);
			} catch (Exception e) {
				e.printStackTrace();
				returnValue = RET_VALUE_INVALID_RESPONSE;
			}
		} else
			returnValue = RET_VALUE_NORESPONSE;
		this.mIsRegistered = returnValue == RET_VALUE_SUCCEEDED;

		return this.mIsRegistered;
	}

	public boolean unregisterSync() {
		int returnValue;
		// send tcp command to unregister trapMapping
		String line = this.tcpSend(String.format("%d;%s;%s;%d\n", COMMAND_UNREGISTER, this.mCurrentSNMPMediatorIp,
				this.mCurrentSNMPDeviceIp, this.mCurrentSNMPTrapPort));
		if (line != null) {
			try {
				returnValue = Integer.parseInt(line);
			} catch (Exception e) {
				returnValue = RET_VALUE_INVALID_RESPONSE;
			}
		} else
			returnValue = RET_VALUE_NORESPONSE;

		return returnValue == RET_VALUE_SUCCEEDED;
	}

	public int checkMaster() {
		int returnValue;
		// send tcp command to check if this instance is alertmaster
		String line = this.tcpSend(String.format("%d;%s;%s;%d\n", COMMAND_CHECKMASTER, this.mCurrentSNMPMediatorIp,
				this.mCurrentSNMPDeviceIp, this.mCurrentSNMPTrapPort));
		if (line != null) {
			try {
				returnValue = Integer.parseInt(line);
				if(!(returnValue==RET_VALUE_CHECKMASTER_YOUARE || returnValue==RET_VALUE_CHECKMASTER_YOUARENOT))
					throw new Exception("invalid response");
			} catch (Exception e) {
				returnValue = RET_VALUE_INVALID_RESPONSE;
			}
		} else
			returnValue = RET_VALUE_NORESPONSE;

		return returnValue;
	}

	public boolean ping() {
		int returnValue;
		// send tcp command to unregister trapMapping
		String line = this.tcpSend(String.format("%d\n", COMAND_PING));
		if (line != null) {
			try {
				returnValue = Integer.parseInt(line);
			} catch (Exception e) {
				returnValue = RET_VALUE_INVALID_RESPONSE;
			}
		} else
			returnValue = RET_VALUE_NORESPONSE;

		return returnValue == RET_VALUE_SUCCEEDED;
	}

	private String tcpSend(String linetosend) {
		String line = null;
		try {
			
			Socket socket = new Socket(InetAddress.getByName(this.mIP), this.mDstPort);

			// Create input and output streams to read from and write to the
			// server
			PrintStream out = new PrintStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// empty line
			out.println(linetosend);
			out.flush();
			//wait a little for response
			Thread.sleep(RESPONSE_LATENCY);
			// Read data from the port mapper
			line = in.readLine();

			// Close our streams
			in.close();
			out.close();
			socket.close();
		} catch (Exception err) {
err.printStackTrace();
		}
		return line;
	}

}
