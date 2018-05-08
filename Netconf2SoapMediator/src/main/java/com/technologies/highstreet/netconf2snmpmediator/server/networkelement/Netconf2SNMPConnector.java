package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import com.technologies.highstreet.deviceslib.data.DataTable;
import com.technologies.highstreet.deviceslib.data.SNMPKeyValuePair;
import com.technologies.highstreet.deviceslib.devices.BaseSNMPDevice;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.UserCommand;
import com.technologies.highstreet.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.netconf2snmpmediator.server.Config;
import com.technologies.highstreet.netconf2snmpmediator.server.basetypes.SnmpTrap;
import com.technologies.highstreet.netconf2snmpmediator.server.basetypes.SnmpTrapList;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.w3c.dom.Document;

public class Netconf2SNMPConnector implements CommandResponder, ResponseListener {

	public interface SNMPErrorListener {
		void OnError(final String message);
	}

	private static final Log LOG = LogFactory.getLog(Netconf2SNMPConnector.class);

	private final int snmpTimeout;
	private final int trapPort;
	private final int snmpRetries;
	private final int snmpVersion;
	private final BaseSNMPDevice snmpDevice;
	private final Netconf2SNMPNetworkElement sne;
	private final Random rnd;

	private Snmp snmp;
	private int version;
	private final String remoteIP;
	private Target target;
	private int iterations;
	private boolean finished;
	private String lastOID = "";
	private SNMPErrorListener errorListener;
	private final Console console;

	// Constructor

	public Netconf2SNMPConnector(Netconf2SNMPNetworkElement sne, Console console) throws IOException {

		Config cfg=Config.getInstance();
		this.snmpTimeout = cfg.SNMPRequestLatency;// in ms
		this.snmpRetries = cfg.SNMPRequestRetries;
		this.snmpVersion = cfg.SNMPVersion;
		this.sne = sne;
		this.rnd = new Random();
		this.remoteIP = sne.getDeviceIp();
		this.trapPort = sne.getSNMPTrapPort();
		this.console = console;
		this.snmpDevice = BaseSNMPDevice.CREATOR.Create(sne.getDeviceClass());

		this.initSNMP();

	}

	/** Do SNMP specific construct actions */
	private void initSNMP() throws IOException {
		TransportMapping<?> transport = null;
		// try {
		LOG.debug("init snmp: TRAPPORT: " + this.trapPort + " RemoteIP" + this.remoteIP);
		transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + this.trapPort));
		this.snmp = new Snmp(transport);
		if (this.version == SnmpConstants.version3) {
			byte[] localEngineID = MPv3.createLocalEngineID();
			// byte[] localEngineID = ((MPv3)
			// snmp.getMessageProcessingModel(MessageProcessingModel.MPv3)).createLocalEngineID();
			USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(localEngineID), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			snmp.setLocalEngine(localEngineID, 0, 0);

		}
		this.target = new CommunityTarget(new UdpAddress(this.remoteIP + "/161"), new OctetString("public"));
		this.target.setRetries(this.snmpRetries);
		this.target.setTimeout(this.snmpTimeout);
		this.target.setVersion(this.snmpVersion);
		this.finished = true;
		snmp.addCommandResponder(this);
		snmp.listen();

		// } catch (IOException e) {
		// e.printStackTrace();
		// log.error("unable to start snmplistener on Port"+this.trapPort);
		// }
	}

	/*
	 * ----------------------------------- Functions Get/Set/Console/Listener
	 */

	private void onSNMPError(SNMPErrorListener listener) {
		this.errorListener = listener;
	}

	private void onError(final String message) {
		if (this.errorListener != null) {
			this.errorListener.OnError(message);
		}
	}

	/**
	 * Message to console
	 *
	 * @param msg
	 *            content
	 * @return again the msg
	 */
	protected String consoleMessage(String msg) {
		return console.cliOutput("NE:" + msg);
	}

	/*
	 * --------------------------- Functions Message handling
	 */

	/**
	 * Replace specified OID parameters of actual request within the DOM
	 *
	 * @param messageId
	 *            of the message
	 * @param tags
	 *            of the message
	 */
	void onPreReplyMessage(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes) {

		LOG.debug("running snmp requests for " + messageId);
		// check if snmp request is necessary for this request
		if (nodes != null && nodes.size() > 0) {
			for (NodeEditConfig node : nodes) {
				try {
					if (node != null) {
						String value;
						if (node.isTestNode()) {
							LOG.debug(consoleMessage("Get SNMP Testpattern"));
							// Should be compliant with the parameter
							value = "1";

						} else if (node.getOid().endsWith("*")) {
							// do a sync snmp get next request
							LOG.debug("snmp getnext request for " + node.getOid() + "...");
							SNMPKeyValuePair x = this.snmpGetNextSync(node.getOid(), node.getDefaultValue());
							value = x != null ? x.Value : "";
						} else { // do a sync snmp get request
							LOG.debug("snmp get request for " + node.getOid() + "...");
							value = this.snmpGetSync(node.getOid(), node.getDefaultValue());
						}
						LOG.debug("succeeded with result=\"" + value + "\"");
						// modify the ne
						if(node.isInternalConversionNeeded())
							value=this.snmpDevice.ConvertValueSnmp2Netconf(node.getOid(),value);
						sne.setSNMPValueInDocumentNode(node, value);
					}
				} catch (IOException e) {
					this.onError(String.format(UserCommand.SNMP_ALERTMESSAGE_SNMPREQUESTFAILED));
					LOG.error("snmp request failed");
				}
			}
		} else {
			LOG.debug("no oids to request for");
		}
	}

	/**
	 * Do changes in the NE
	 *
	 * @param messageId
	 * @param xmlSourceMessage
	 */
	void onPreEditConfigTarget(String messageId, NetconfTagList tags, Document sourceMessage) {

		// consoleMessage("onPreEditConfigTarget");
		// do a sync snmp set request
		LOG.debug("Start pre edit-config request message " + messageId);
		// check if snmp request is necessary for this request
		NodeEditConfigCollection nodes = this.sne.getOIDsForEditRequest(messageId, tags);
		LOG.debug("onPreEditConfigTarget nodes: " + nodes.size());

		if (nodes != null && nodes.size() > 0) {
			for (NodeEditConfig oidNode : nodes) {
				String value = oidNode.getValueFromNetconfMessageForOid(sourceMessage);
				LOG.debug("EditReply: Parameters for oid: " + oidNode.getOid() + " docValue:'"
						+ oidNode.getTextContent() + "' MessageValue:'" + value + "' differ="
						+ value.compareTo(oidNode.getTextContent()) + " rw=" + oidNode.getReadWrite()
						+ (oidNode.getReadWrite() ? " Converted2snmp:" + oidNode.convertValueNetconf2SnmpString(value)
								: " No conversion"));

				//if not writeable-configured =>ignore changes
				if(!oidNode.getReadWrite())
					continue;

				Variable snmpSetValue = oidNode.convertValueNetconf2SnmpString(value);
				String snmpGetValue = null;
				try {
					LOG.debug("onPreEditConfigTarget oid: " + oidNode.getOid());
					if (oidNode.getOid().endsWith("*")) {


					} else {
						// write with snmp-set
						this.snmpSetSync(oidNode.getOid(), snmpSetValue);
						// read value with snmp-get-request
						snmpGetValue = this.snmpGetSync(oidNode.getOid(), null);
						// check change and write to xmlnode of sne
						if (!snmpSetValue.equals(snmpGetValue))
							this.sne.setSNMPValueInDocumentNode(oidNode, value);
					}
				} catch (Exception err) {
					this.onError(String.format(UserCommand.SNMP_ALERTMESSAGE_SNMPSETREQUESTFAILED));
				}


			}

		} else {
			LOG.debug("no oids to request for");
		}

	}

	private boolean snmpSetSync(String oid, Variable value) throws IOException {

		boolean r=false;
		PDU pdu = new PDU();
		OID o = new OID(oid);

		VariableBinding varBind = new VariableBinding(o, value);
		pdu.add(varBind);

		pdu.setType(PDU.SET);
		pdu.setRequestID(new Integer32(this.rnd.nextInt()));
		ResponseEvent response = this.snmp.set(pdu, this.target);

		// Process Agent Response
		if (response != null) {
			LOG.debug("got snmp set response from agent");
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {
					LOG.debug("snmp set response = " + responsePDU.getVariableBindings());
					r=true;
				} else {
					LOG.debug("set-request failed");
					LOG.debug("error status = " + errorStatus);
					LOG.debug("error index = " + errorIndex);
					LOG.debug("error status Text = " + errorStatusText);
				}
			} else {
				LOG.error("set-response PDU is null");
			}
		} else {
			LOG.error("set-request timed out ");
		}
		return r;
	}
	public void fillDataTable(DataTable dt) throws IOException
	{
		//dt.
	}
	public DataTable snmpGetTable(String baseOID,String[] colOIDs,String[] rows) throws IOException
	{
		DataTable dt=new DataTable(baseOID,colOIDs,rows);

		String oidTable = baseOID;
		int rowIdx=0;
		for(String colOID : colOIDs)
		{
			rowIdx=0;
			for(String row:rows)
			{
				String oid=oidTable+colOID+row;
				dt.setValueAt(colOID, rowIdx, this.snmpGetSync(oid,null));
				rowIdx++;
			}
		}
		return dt;
	}
	private String snmpGetSync(String oid, String defaultValue) throws IOException {

		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		ResponseEvent evt = this.snmp.get(pdu, this.target);
		if (evt != null) {
			PDU evtResponsePdu = evt.getResponse();
			if (evtResponsePdu != null) {
				// int errorStatus = evtResponsePdu.getErrorStatus();
				// String error = evtResponsePdu.getErrorStatusText();
				if (evtResponsePdu.size() > 0) {
					defaultValue = evtResponsePdu.get(0).toValueString();
				}
			}
		}

		return defaultValue;
	}

	private SNMPKeyValuePair snmpGetNextSync(String oid, String defaultValue) throws IOException {
		PDU pdu = new PDU();
		if (oid.endsWith(".*")) {
			oid = oid.substring(0, oid.length() - 2);
		}
		pdu.add(new VariableBinding(new OID(oid)));
		ResponseEvent evt = this.snmp.getNext(pdu, this.target);
		if (evt != null) {
			PDU evtResponsePdu = evt.getResponse();
			if (evtResponsePdu != null) {
				// int errorStatus = evtResponsePdu.getErrorStatus();
				// String error = evtResponsePdu.getErrorStatusText();
				if (evtResponsePdu.size() > 0) {
					oid = evtResponsePdu.get(0).getOid().toString();
					defaultValue = evtResponsePdu.get(0).toValueString();
				}
			}

		}
		return new SNMPKeyValuePair(oid, defaultValue);
	}

	/*
	 * private boolean walkthroughAsync(String startOID, int max_requests) {
	 * if(!this.finished) { return false; } this.finished=false;
	 * this.iterations=max_requests; final PDU pdu_req = new PDU(); PDU
	 * pdu_resp; pdu_req.add(new VariableBinding(new OID(startOID)));
	 *
	 * try { snmp.getNext(pdu_req, this.target, null, this); } catch
	 * (IOException e) {
	 *
	 * e.printStackTrace(); return false; }
	 *
	 * return true; }
	 */

	/*
	 * UPD Trap Event(non-Javadoc)
	 *
	 * @see
	 * org.snmp4j.CommandResponder#processPdu(org.snmp4j.CommandResponderEvent)
	 */
	@Override
	public void processPdu(CommandResponderEvent evt) {
		PDU command = evt.getPDU();
		if (command != null && command.size() > 0) {
			SnmpTrapList traps = new SnmpTrapList();
			for (int i = 0; i < command.size(); i++) {
				VariableBinding item = command.get(i);
				if (item != null) {
					traps.add(new SnmpTrap(item.getOid().toDottedString(), item.toValueString()));
				}
			}
			this.onTrapReceived(traps);

		}
		// printPDU(command);
	}

	private synchronized void onTrapReceived(SnmpTrapList traps) {

		LOG.debug("TRAPs received (" + traps+")");
		sne.onTrapReceived(traps);
		if (sne.getMessageQueue() != null) {
			// Put message into queue for Netconf2SNMPMessageProcessorThread and
			// message processing
			LOG.debug("put trap into message queue");
			sne.getMessageQueue().put(traps); // Seems to block
		} else {
			LOG.warn(consoleMessage("TRAPTRAP No Queue. Connected?"));
		}
		/*
		 * if(this.snmpDevice!=null)
		 * {
		 * SNMPAlert a=this.snmpDevice.getAvailableAlert(oid);
		 * if(a!=null)//alert is valid for this device {
		 * this.snmpDevice.onTrapReceived(a); //push to netconf
		 * this.onError(a.ShortName);
		 * }
		 * else //unknown snmp trap
		 * {
		 * //TODO
		 * }
		 * }
		 */
	}

	/*
	 * Async Request Response(non-Javadoc)
	 *
	 * @see org.snmp4j.event.ResponseListener#onResponse(org.snmp4j.event.
	 * ResponseEvent)
	 */
	@Override
	public void onResponse(ResponseEvent evt) {

		((Snmp) evt.getSource()).cancel(evt.getRequest(), this);
		PDU pdu = evt.getResponse();
		if (pdu != null && this.iterations > 0) {
			printPDU(pdu);
			try {
				String currentOID = pdu.get(0).getOid().toDottedString();
				// if oid is the same as the last one
				if (this.lastOID != null && this.lastOID.length() > 0 && this.lastOID.equals(currentOID)) {
					this.finished = true;
				} else // new oid to request
				{
					this.lastOID = currentOID;
					PDU pdu_req = new PDU();
					pdu_req.add(new VariableBinding(pdu.get(0).getOid()));
					this.iterations--;
					this.snmp.getNext(pdu_req, this.target, null, this);
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.finished = true;
			}

		} else {
			this.finished = true;
		}
	}

	private void printPDU(PDU pdu) {
		if (pdu != null) {
			this.printPDU(pdu.get(0).getOid(), pdu.get(0).toValueString());
		} else {
			LOG.debug("pdu is null");
		}
	}

	private void printPDU(OID oid, String value) {
		LOG.debug("" + this.iterations + ": " + oid + ":" + value);
	}

}
