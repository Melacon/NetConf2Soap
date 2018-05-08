package com.technologies.highstreet.netconf2soapmediator.server.streamProcessing;

import com.technologies.highstreet.netconf.server.streamprocessing.NetconfStreamCodecThread;

public interface MediatorConnectionListener {

	public void netconfOnConnect(NetconfStreamCodecThread ioCodec);
	public void netconfOnDisconnect();
	public void networkElementOnConnect();
	public void networkElementOnDisconnect();

}
