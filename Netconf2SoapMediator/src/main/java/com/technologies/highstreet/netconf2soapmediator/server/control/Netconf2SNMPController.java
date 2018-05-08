package com.technologies.highstreet.netconf2soapmediator.server.control;

import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.control.NetconfController;
import com.technologies.highstreet.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SNMPNetworkElement;
import com.technologies.highstreet.netconf2soapmediator.server.streamProcessing.MediatorConnectionListener;
import com.technologies.highstreet.netconf2soapmediator.server.streamProcessing.Netconf2SNMPMessageProcessorThread;
import com.technologies.highstreet.netconf2soapmediator.server.streamProcessing.SNMPDevicePollingThread;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.ExitCallback;

public class Netconf2SNMPController extends NetconfController {

	private static final Log log = LogFactory.getLog(Netconf2SNMPController.class);
	private static final boolean pollAsThread = false;

	private final MediatorConnectionListener mConnectionListener;

		public Netconf2SNMPController(InputStream in, OutputStream out, OutputStream err, ExitCallback callback,
			MediatorConnectionListener connectionListener) {
		super(in, out, err, callback);
		this.mConnectionListener = connectionListener;
	}

	// -->> Should be improved to avoid cast
	@Override
	public void start(MessageStore messageStore, NetworkElement ne, Console console) {
		// start message processor
		startMessageProcessor(messageStore, (Netconf2SNMPNetworkElement) ne, console);
		// wait for message processor to continue
		ioThread = ioCodec.start();
		if (this.mConnectionListener != null) {
			this.mConnectionListener.netconfOnConnect(this.ioCodec);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		//waitAndInterruptThreads();
		if (this.mConnectionListener != null)
			this.mConnectionListener.netconfOnDisconnect();

	}

	protected void startMessageProcessor(MessageStore messageStore, Netconf2SNMPNetworkElement ne, Console console) {
		boolean startPolling = false;
		log.info("Creating new message processor...");
		messageProcessorThread = new Netconf2SNMPMessageProcessorThread("Message processor", status, ioCodec,
				messageQueue, messageStore, ne, console);

		messageProcessorThread.start();
		log.info("Message processor started.");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.warn("Error waiting for message processor thread.", e);
		}
	}



}
