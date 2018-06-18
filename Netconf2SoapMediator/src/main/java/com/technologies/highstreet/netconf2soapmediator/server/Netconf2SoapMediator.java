package com.technologies.highstreet.netconf2soapmediator.server;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.boot.SpringApplication;

import com.technologies.highstreet.deviceslib.data.SNMPDeviceType;
import com.technologies.highstreet.netconf.server.basetypes.Behaviour;
import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.basetypes.UserCommand;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyExecutor;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyOriginator;
import com.technologies.highstreet.netconf.server.exceptions.ServerException;
import com.technologies.highstreet.netconf.server.ssh.AlwaysTruePasswordAuthenticator;
import com.technologies.highstreet.netconf.server.streamprocessing.NetconfStreamCodecThread;
import com.technologies.highstreet.netconf2soapmediator.server.control.Netconf2SoapFactory;
import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;
import com.technologies.highstreet.netconf2soapmediator.server.streamProcessing.MediatorConnectionListener;
import com.technologies.highstreet.netconf2soapmediator.server.streamProcessing.SNMPDevicePollingThread;

import net.i2cat.netconf.rpc.RPCElement;

public class Netconf2SoapMediator implements MessageStore, BehaviourContainer, NetconfNotifyOriginator, Console {

	private static Log LOG;
	private static final String VERSION = "1.1 - ONF 4th PoC";
	private static final String MEDIATORSERVER_CONFIGFILENAME = "/etc/mediatorserver.conf";
	private static boolean CLIMODE = false;

	private SshServer sshd;

	// stored messages
	//private boolean storeMessages = false;
	private List<RPCElement> messages;

	// behaviours
	private List<Behaviour> behaviours;

	protected NetconfNotifyExecutor netconfNotifyExecutor = null;

	private DeviceConnectionMonitor deviceConnectionMonitor=null;
	private NMSTrapPortMapper nms = null;
	private Timer nmsWatchdog = null;

	private MediatorConfig configFile;

	// hide default constructor, forcing using factory method
	private Netconf2SoapMediator() {

	}

	/**
	 * Server factory creates a server
	 */
	public static Netconf2SoapMediator createServer() {
		Netconf2SoapMediator server = new Netconf2SoapMediator();
		server.messages = new ArrayList<>();
//		server.storeMessages = false;
		return server;
	}

	private final MediatorConnectionListener mediatorConnectionListener = new MediatorConnectionListener() {

		@Override
		public void netconfOnDisconnect() {
			LOG.debug("netconf disconnected");
			if (Netconf2SoapMediator.this.configFile != null) {
				Netconf2SoapMediator.this.configFile.setIsNetconfConnected(false);
				try {
					Netconf2SoapMediator.this.configFile.save();
				} catch (Exception e) {
					LOG.error("error saving netconf status to config file");
				}
			}
			if(Netconf2SoapMediator.this.deviceConnectionMonitor!=null)
				Netconf2SoapMediator.this.deviceConnectionMonitor.setIOStream(null);

		}

		@Override
		public void netconfOnConnect(NetconfStreamCodecThread ioCodec) {
			LOG.debug("netconf connected");
			if (Netconf2SoapMediator.this.configFile != null) {
				Netconf2SoapMediator.this.configFile.setIsNetconfConnected(true);
				try {
					Netconf2SoapMediator.this.configFile.save();
				} catch (Exception e) {
					LOG.error("error saving netconf status to config file");
				}
			}
			if(Netconf2SoapMediator.this.deviceConnectionMonitor!=null)
				Netconf2SoapMediator.this.deviceConnectionMonitor.setIOStream(ioCodec);
		}

		@Override
		public void networkElementOnConnect() {
			if (Netconf2SoapMediator.this.configFile != null) {
				Netconf2SoapMediator.this.configFile.setIsNeConnected(true);
				try {
					Netconf2SoapMediator.this.configFile.save();
				} catch (Exception e) {
					LOG.error("error saving ne connection status to config file");
				}
			}
		}

		@Override
		public void networkElementOnDisconnect() {
			if (Netconf2SoapMediator.this.configFile != null) {
				Netconf2SoapMediator.this.configFile.setIsNeConnected(false);
				try {
					Netconf2SoapMediator.this.configFile.save();
				} catch (Exception e) {
					LOG.error("error saving ne connection status to config file");
				}
			}
		}
	};

	/**
	 * Initializes the server
	 *
	 * @param host
	 *            host name (use null to listen in all interfaces)
	 * @param listeningPort
	 *            where the server will listen for SSH connections
	 * @param ne
	 *            with NetworkElement model
	 * @param devNum
	 *            number of interface card
	 *
	 */
	private void initializeServer(String host, int listeningPort, Netconf2SoapNetworkElement sne, MediatorConfig cfg,
			int devNum) {
		LOG.info(staticCliOutputNewLine("Configuring mediator ..."));
		configFile = cfg;
		sshd = SshServer.setUpDefaultServer();
		sshd.setHost(host);
		sshd.setPort(listeningPort);

		LOG.info(staticCliOutputNewLine("Host: '" + host + "', listenig port: " + listeningPort));

		sshd.setPasswordAuthenticator(new AlwaysTruePasswordAuthenticator());
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

		List<NamedFactory<Command>> subsystemFactories = new ArrayList<>();
		subsystemFactories
				.add(Netconf2SoapFactory.createFactory(this, this, this, sne, mediatorConnectionListener, this));
		sshd.setSubsystemFactories(subsystemFactories);

		if (Config.IsPortMapperNeeded()) {
			String myip = "";
			try {
				myip = getNetworkIp(devNum);

			} catch (SocketException e) {
				LOG.error("failed to detect ip for " + devNum);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			sne.setDeviceIp(myip);
			LOG.info(staticCliOutputNewLine("registering Mediator on TrapPortMapper: '" + sne.getDeviceIp()
					+ "', for port: " + sne.getSNMPTrapPort() + " with ip=" + myip));
			nms = new NMSTrapPortMapper();
			if (!nms.registerSync(myip, sne.getDeviceIp(), sne.getSNMPTrapPort())) {
				LOG.warn(staticCliOutputNewLine("failed to register on trap portmapper"));
			} else {
				int response = nms.checkMaster();
				switch (response) {
				case NMSTrapPortMapper.RET_VALUE_CHECKMASTER_YOUARE: // this
																		// mediator
																		// is
																		// the
																		// watchdog
																		// for
																		// PortMapper
					Config.getInstance().PortMapperMaster = true;
					this.startPortMapperWatchdog();
					break;
				case NMSTrapPortMapper.RET_VALUE_CHECKMASTER_YOUARENOT: // everything
																		// is
																		// okay
					Config.getInstance().PortMapperMaster = false;
					break;
				case NMSTrapPortMapper.RET_VALUE_INVALID_RESPONSE:
					LOG.warn(String.format("received invalid response (%d) from TrapPortMapper", response));
					break;
				case NMSTrapPortMapper.RET_VALUE_NORESPONSE:
					LOG.warn("TrapPortMapper is not responding on request");
					break;
				}
			}
		}
		this.deviceConnectionMonitor=new DeviceConnectionMonitor(sne,null,this.mediatorConnectionListener);
		LOG.info(staticCliOutputNewLine("Mediator configured."));
	}

	private void startPortMapperWatchdog() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (!nms.ping()) // ping failed
				{
					// send alert notfication via netconf
					Netconf2SoapMediator.this.notify(UserCommand.SNMP_ALERTMESSAGE_PORTMAPPERNOTFOUND);
				}
			}
		};

		nmsWatchdog = new Timer();
		nmsWatchdog.schedule(task, Config.getInstance().PortMapperWatchdogInterval);

	}

	private void stopPortMapperWatchdog() {
		if (nmsWatchdog != null) {
			nmsWatchdog.cancel();
		}
	}

	@Override
	public void defineBehaviour(Behaviour behaviour) {
		if (behaviours == null) {
			behaviours = new ArrayList<>();
		}
		synchronized (behaviours) {
			behaviours.add(behaviour);
		}
	}

	@Override
	public List<Behaviour> getBehaviours() {
		if (behaviours == null) {
			return null;
		}
		synchronized (behaviours) {
			return behaviours;
		}
	}

	public void startServer() throws ServerException {
		LOG.info(staticCliOutputNewLine("Starting server..."));
		try {
			sshd.start();
		} catch (IOException e) {
			LOG.error(staticCliOutputNewLine("Error starting server!" + e.getMessage()));
			throw new ServerException("Error starting server", e);
		}
		LOG.info(staticCliOutputNewLine("Server started."));
	}

	public void stopServer() throws ServerException {
		LOG.info(staticCliOutputNewLine("Stopping server..."));
		try {
			sshd.stop();
			if (nms != null && nms.isRegistered()) {
				nms.unregisterSync();
			}
			if(deviceConnectionMonitor!=null)
				deviceConnectionMonitor.waitAndInterruptThreads();
			stopPortMapperWatchdog();
			stopSNMPThreads();
		} catch (IOException e) {
			LOG.error(staticCliOutputNewLine("Error stopping server!" + e));
			throw new ServerException("Error stopping server", e);
		}
		LOG.info(staticCliOutputNewLine("Server stopped."));
	}

	private void stopSNMPThreads() {
		if(SNMPDevicePollingThread.isRunning())
			SNMPDevicePollingThread.stopThread();
	}

	@Override
	public void storeMessage(RPCElement message) {
		if (messages != null) {
			synchronized (messages) {
				LOG.info("Storing message " + message.getMessageId());
				messages.add(message);
			}
		}
	}

	@Override
	public List<RPCElement> getStoredMessages() {
		synchronized (messages) {
			return Collections.unmodifiableList(messages);
		}
	}

	@Override
	public void setNetconfNotifyExecutor(NetconfNotifyExecutor executor) {
		this.netconfNotifyExecutor = executor;
	}

	private void notify(String command) {
		if (netconfNotifyExecutor != null) {
			netconfNotifyExecutor.notify(command);
		} else {
			System.out.println("No notifier registered.");
		}

	}

	private static void initDebug(String debugFilename) {
		BasicConfigurator.configure();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		LOG = LogFactory.getLog(Netconf2SoapMediator.class);

		if(CLIMODE)
		{
			ConsoleAppender console = new ConsoleAppender(); // create appender
			// configure the appender
			// String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			String PATTERN = "%d [%p|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			console.setThreshold(Config.getInstance().LogLevel);
			console.activateOptions();
			// add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(console);
		}

		RollingFileAppender fa = new RollingFileAppender();
		fa.setName("FileLogger");
		fa.setFile(debugFilename);
		fa.setLayout(new PatternLayout("%d %-5p [%c] %m%n"));
		fa.setThreshold(Config.getInstance().LogLevel);
		fa.setMaximumFileSize(10000000);
		fa.setAppend(true);
		fa.setMaxBackupIndex(5);
		fa.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(fa);
		// repeat with all other desired appenders

	}

	/*
	 * get IPv4 Address of LAN for ETHERNET Device <devNum>
	 */
	private static String getNetworkIp(int devNum) throws SocketException, Exception {
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			if (devNum-- >= 0) {
				continue;
			}
			NetworkInterface n = e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = ee.nextElement();
				if (!(i instanceof Inet6Address)) {
					return i.getHostAddress();
				}
			}
		}
		throw new Exception("no ip address found for selected network interface");
	}
	

	public static void main(String[] args) {
		String title = "Netconf NE Soap Mediator\n";
		int optIdx=0;
		if (args.length < 1) {
			System.err.println("Too less parameters. Command: Server configFilename [pathToYang]");
			return;
		}
		if(args[0].equals("--cli"))
		{
			CLIMODE=true;
			optIdx++;
		}
		String jsonFilename = args[optIdx++];
		MediatorConfig cfg = null;
		try {
			cfg = new MediatorConfig(jsonFilename);
			staticCliOutput("loaded config file successfully");
			cfg.writePIDFile();
		} catch (Exception e) {
			staticCliOutputNewLine("Error loading config file " + jsonFilename);
			return;
		}
		Config.getInstance().tryLoad(MEDIATORSERVER_CONFIGFILENAME);
		String debugFile = cfg.getLogFilename();
		String yangPath = args.length >= optIdx+1 ? args[optIdx++] : "yang/yangNeModel";
		String uuid = args.length >= optIdx+1 ? args[optIdx] : "";
		String xmlFilename = cfg.getNeXMLFilename();
		String rootPath = "";
		final int port = cfg.getNetconfPort();

		/*
		 * if (Config.DEBUG) { rootPath = "build/"; } else { rootPath = ""; }
		 */
		xmlFilename = rootPath + xmlFilename;
		yangPath = rootPath + yangPath;

		staticCliOutputNewLine(title);
		staticCliOutputNewLine("Version: " + VERSION);
		staticCliOutputNewLine("Start parameters are:");
		staticCliOutputNewLine("\tFilename: " + xmlFilename);
		staticCliOutputNewLine("\tPort: " + port);
		staticCliOutputNewLine("\tDebuginfo and communication is in file: " + debugFile);
		staticCliOutputNewLine("\tYang files in directory: " + yangPath);
		staticCliOutputNewLine("\tUuid: " + uuid);

		initDebug(debugFile);
		LOG.info(title);
		
		// start HTTP server, processing TR-069 SOAP messages
		LOG.info("Starting HTTP server");
		SpringApplication.run(HTTPServer.class);
		LOG.info("HTTP server started");
		
		try {
			Netconf2SoapNetworkElement ne;

			Netconf2SoapMediator server = Netconf2SoapMediator.createServer();
			ne = new Netconf2SoapNetworkElement(xmlFilename, yangPath, uuid, SNMPDeviceType.FromInt(cfg.mDeviceType),
					cfg.getDeviceIp(), cfg.getTrapPort(), server);
			ne.setDeviceName(cfg.getName());
			HTTPServlet.setNetworkElement(ne);
			server.initializeServer("0.0.0.0", port, ne, cfg, Config.getInstance().MediatorDefaultNetworkInterfaceNum);
			server.startServer();

			// here a HTTP client is create that send connection request to the CWMP device
			HTTPClient httpclient = new HTTPClient();
			LOG.info("start sendOpenConnectionToDevice("+ cfg.getCpeUrl()+","+ cfg.getCpeUsername()+"," + cfg.getCpePassword()+")");
			httpclient.sendOpenConnectionToDevice(cfg.getCpeUrl(), cfg.getCpeUsername(), cfg.getCpePassword());
			HTTPServlet.setConnActive(true);
			LOG.info("finished sendOpenConnectionToDevice("+ cfg.getCpeUrl()+","+ cfg.getCpeUsername()+"," + cfg.getCpePassword()+")");
			
			int sleep = 1000*60*60;
			LOG.info("sleeping for " +  sleep + "ms");
			Thread.sleep(sleep); // milliseconds
			LOG.info("finished sleeping");

			for (int i = 0; i < 999999; i++) {
				System.out.println("connActive=" + HTTPServlet.getConnActive());
				if (HTTPServlet.getConnActive() == false) {
					Thread.sleep(10*1000); // milliseconds
					// periodically send connection request to the device
					LOG.info("start sendOpenConnectionToDevice("+ cfg.getCpeUrl()+","+ cfg.getCpeUsername()+"," + cfg.getCpePassword()+")");
					httpclient.sendOpenConnectionToDevice(cfg.getCpeUrl(), cfg.getCpeUsername(), cfg.getCpePassword());
					HTTPServlet.setConnActive(true);
					LOG.info("finished sendOpenConnectionToDevice("+ cfg.getCpeUrl()+","+ cfg.getCpeUsername()+"," + cfg.getCpePassword()+")");
				}
				Thread.sleep(1000); // milliseconds
			}

		} catch (Exception e) {
			LOG.error("(..something..) failed", e);
		}

//		Netconf2SoapNetworkElement ne;
//		try {
//			Netconf2SoapMediator server = Netconf2SoapMediator.createServer();
//			ne = new Netconf2SoapNetworkElement(xmlFilename, yangPath, uuid, SNMPDeviceType.FromInt(cfg.mDeviceType),
//					cfg.getDeviceIp(), cfg.getTrapPort(), server);
//			ne.setDeviceName(cfg.getName());
//			HTTPServlet.setNetworkElement(ne);
//			server.initializeServer("0.0.0.0", port, ne, cfg, Config.getInstance().MediatorDefaultNetworkInterfaceNum);
//			server.startServer();
//			
//			if (CLIMODE == true) {
//				LOG.info("connActive=" + HTTPServlet.getConnActive() );
//				
////				for (int i = 0; i < 3000; i++) {
////					System.out.println("connActive=" + HTTPServlet.getConnActive());
////					if (HTTPServlet.getSetParam() == true) {
////						// request connection from CWMP device
////						httpclient = new HTTPClient();
////				        LOG.info("start sendOpenConnectionToDevice("+ cfg.getCpeUrl()+","+ cfg.getCpeUsername()+"," + cfg.getCpePassword()+")");
////						httpclient.sendOpenConnectionToDevice(cfg.getCpeUrl(), cfg.getCpeUsername(), cfg.getCpePassword());
////						LOG.info("finished sendOpenConnectionToDevice("+ cfg.getCpeUrl()+","+ cfg.getCpeUsername()+"," + cfg.getCpePassword()+")");
////					}
////					Thread.sleep(10000); // milliseconds
////				}
//				
//				// read lines form input
//				BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
//				String command;
//					while (true) {
//					staticCliOutput(port + ":" + xmlFilename + "> ");
//					command = buffer.readLine();
//					if (command != null) {
//						command = command.toLowerCase();
//					} else {
//						command = "<null>";
//					}
//
//					if (command.equals("list")) {
//						staticCliOutputNewLine("Messages received(" + server.getStoredMessages().size() + "):");
//						for (RPCElement rpcElement : server.getStoredMessages()) {
//							staticCliOutputNewLine("#####  BEGIN message #####\n" + rpcElement.toXML() + '\n'
//									+ "#####   END message  #####");
//						}
//					} else if (command.equals("size")) {
//						staticCliOutputNewLine("Messages received(" + server.getStoredMessages().size() + "):");
//
//					} else if (command.equals("quit")) {
//						staticCliOutputNewLine("Stop server");
//						server.stopServer();
//						break;
//					} else if (command.equals("info")) {
//						staticCliOutputNewLine("Version: " + VERSION + " Port: " + port + " File: " + xmlFilename);
//					} else if (command.equals("status")) {
//						staticCliOutputNewLine("Status: not implemented");
//					} else if (command.startsWith("n")) {
//						String notifyCommand = command.substring(1);
//						staticCliOutputNewLine("Notification: " + notifyCommand);
//						server.notify(notifyCommand);
//					} else if (command.length() == 0) {
//					} else {
//						staticCliOutputNewLine("NETCONF Simulator V3.0");
//						staticCliOutputNewLine("Available commands: status, quit, info, list, size, nZZ, nl, nx");
//						staticCliOutputNewLine("\tnx: list internal XML doc tree");
//						staticCliOutputNewLine("\tnl: list available notifications");
//						staticCliOutputNewLine("\tnZZ: send notification with number ZZ");
//					}
//				}
//			} else {
//				while (true)
//					Thread.sleep(1000);
//
//			}
//		} catch (SAXException e) {
//			LOG.error(staticCliOutputNewLine("(..something..) failed" + e.getMessage()));
//		} catch (ParserConfigurationException e) {
//			LOG.error(staticCliOutputNewLine("(..something..) failed" + e.getMessage()));
//		} catch (TransformerConfigurationException e) {
//			LOG.error("(..something..) failed", e);
//		} catch (ServerException e) {
//			LOG.error("(..something..) failed", e);
//		} catch (XPathExpressionException e) {
//			LOG.error("(..something..) failed", e);
//		} catch (IOException e) {
//			LOG.error("(..something..) failed", e);
//		} catch (InterruptedException e) {
//			LOG.error("(..something..) failed", e);
//		}
		cfg.deletePIDFile();
		staticCliOutputNewLine("Exiting");
		System.exit(0);
	}

	@Override
	public String cliOutput(String msg) {
		return staticCliOutputNewLine(msg);
	}

	static String staticCliOutputNewLine(String msg) {
		if(CLIMODE || LOG == null)
			System.out.println(msg);
		else
			LOG.info(msg);
		return msg;
	}

	static String staticCliOutput(String msg) {
		if(CLIMODE || LOG == null)
			System.out.print(msg);
		else
			LOG.info(msg);
		return msg;
	}

}
