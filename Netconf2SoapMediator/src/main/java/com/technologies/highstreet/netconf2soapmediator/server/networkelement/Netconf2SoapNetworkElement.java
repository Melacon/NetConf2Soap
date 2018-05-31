package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.technologies.highstreet.deviceslib.data.SNMPDeviceType;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.netconf2soapmediator.server.Config;
import com.technologies.highstreet.netconf2soapmediator.server.basetypes.SnmpTrap;
import com.technologies.highstreet.netconf2soapmediator.server.basetypes.SnmpTrapList;
import com.technologies.highstreet.netconf2soapmediator.server.basetypes.SnmpTrapNotification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.digester.plugins.strategies.FinderSetProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extend NetworkElement with function to act as Mediator for SNMP Devices
 *
 * @author Micha
 */
public class Netconf2SoapNetworkElement extends NetworkElement {

	private static final Log LOG = LogFactory.getLog(Netconf2SoapNetworkElement.class);
	/**Start Pier variables
	 * 
	*/
	private Document tr069Document = null;

	/**Start Pier variables
	 * 
	*/
	private static String OIDPARAMETERNAMESTART = "$OIDVALUE=";
	private static String OIDPARAMETERNAMEEND = "<";

	// private final BaseSNMPDevice mSNMPDevice;
	private String snmpIp = null;
	private String mName = "";
	private Integer snmpTrapPort = null;
	@SuppressWarnings("unused")
	private final boolean isPortMapMaster = false;
	private final NodeEditConfigCollection mSNMPNodes;
	private final Netconf2SoapConnector snmpConnector;
	private final Node mMWProblemList;
	private final Node mNEProblemList;
	private final Node mETHProblemList;
	private final HashMap<String, SnmpTrapNotification> mAvailableTraps;

	private SNMPDeviceType mDeviceType;

	/*
	 * Constructor
	 */
	public Netconf2SoapNetworkElement(String filename, String schemaPath, String uuid, SNMPDeviceType type,
			String remoteSNMPIp, int trapport, Console console) throws SAXException, IOException,
			ParserConfigurationException, TransformerConfigurationException, XPathExpressionException {
		super(filename, schemaPath, uuid, console);

		this.snmpIp = remoteSNMPIp;
		this.snmpTrapPort = trapport;
		this.mDeviceType = type;
		if (this.snmpIp == null || this.snmpTrapPort == null) {
			throw new IllegalArgumentException("Can not find ip and trap port.");
		}

		// this.mSNMPDevice = BaseSNMPDevice.CREATOR.Create(type);
		this.mSNMPNodes = searchForOID("", NetworkElement.getXmlSubTree(getDocument(), "//data"), "oid",
				new NodeEditConfigCollection());
		this.mMWProblemList = NetworkElement.getNode(getDocument(),
				"//data/mw-air-interface-pac/air-interface-current-problems");
		if(this.mMWProblemList==null)
			LOG.warn("inconsistant xml file. air-interaface-current-problems path is missing");
		this.mNEProblemList = NetworkElement.getNode(getDocument(),	"//data/network-element-pac/network-element-current-problems");
		//this.mNEProblemList = NetworkElement.getNode(getDocument(),	"//data/NetworkElementCurrentProblems");
		if(this.mNEProblemList==null)
			LOG.warn("inconsistant xml file. network-element-current-problems path is missing");
		this.mETHProblemList = NetworkElement.getNode(getDocument(), "//data/mw-ethernet-container-pac/ethernet-container-current-problems");
		if(this.mETHProblemList==null)
			LOG.warn("inconsistant xml file. ethernet-container-current-problems path is missing");
		this.mAvailableTraps = new HashMap<>();
		this.snmpConnector = new Netconf2SoapConnector(this, getConsole());

		this.fillAvailableTraps(NetworkElement.getNode(getDocument(), "//snmptrap-notifications"));
		this.addNotifications(new String[] { "snmpTrapOid", "snmpTrapValue" },
				"//snmptrap-notifications/problem-notification",
				"//snmptrap-notifications/attribute-value-changed-notification");

		LOG.info(String.format("Current SNMP NetworkElement ip:%s Trapport:%d has %d SNMP values and %d possible traps",
				this.snmpIp, this.snmpTrapPort, this.mSNMPNodes.size(), this.mAvailableTraps.size()));
	}
	/*
	 * Constructor
	 *
	 * @Deprecated public Netconf2SNMPNetworkElement(String filename, String
	 * schemaPath, SNMPDeviceType type, String remoteIp, int snmpPort, Console
	 * console) throws SAXException, IOException, ParserConfigurationException,
	 * TransformerConfigurationException, XPathExpressionException { this(filename,
	 * schemaPath, null, type, console); }
	 */

	/*----------------------------------------------------------------------------------------
	 * Get/Set
	 */

	public String getDeviceIp() {
		return this.snmpIp;
	}

	public void setDeviceIp(String ip) {
		this.snmpIp = ip;
	}

	public int getSNMPTrapPort() {
		return this.snmpTrapPort;
	}

	public SNMPDeviceType getDeviceClass() {
		return this.mDeviceType;
	}

	public String getDeviceName() {
		return this.mName;
	}

	public void setDeviceName(String name) {
		this.mName = name;
	}

	private void fillAvailableTraps(Node root) {
		if (root == null)
			return;
		NodeList notifications = root.getChildNodes();
		if (notifications == null || notifications.getLength() <= 0)
			return;
		int i, j;
		String snmpTrapOID = "", snmpTrapValue = "";
		NamedNodeMap attrs;
		for (i = 0; i < notifications.getLength(); i++) {
			if (notifications.item(i).getNodeType() == Node.ELEMENT_NODE
					&& notifications.item(i).getNodeName().equals("problem-notification")) {
				attrs = notifications.item(i).getAttributes();
				if (attrs != null) {
					for (j = 0; j < attrs.getLength(); j++) {
						if (attrs.item(j) != null && attrs.item(j).getNodeName().equals("snmpTrapOid"))
							snmpTrapOID = attrs.item(j).getTextContent();
						else if (attrs.item(j) != null && attrs.item(j).getNodeName().equals("snmpTrapValue"))
							snmpTrapValue = attrs.item(j).getTextContent();
					}

					String key = String.format("%s%s", snmpTrapOID, snmpTrapValue);
					String name = findNodeValue(notifications.item(i).getChildNodes(), "problem");
					String ref = findNodeValue(notifications.item(i).getChildNodes(), "object-id-ref");
					String sev = findNodeValue(notifications.item(i).getChildNodes(), "severity");
					if (name != null && ref != null && sev != null)
						this.mAvailableTraps.put(key, new SnmpTrapNotification(name, ref, sev));

				}
			}
		}

	}

	private static String findNodeValue(NodeList items, String nodeName) {
		String value = null;
		if (items != null) {
			for (int i = 0; i < items.getLength(); i++) {
				if (items.item(i) != null && items.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if (items.item(i).getNodeName().equals(nodeName)) {
						value = items.item(i).getTextContent();
						break;
					}

				}
			}
		}
		return value;
	}

	/*----------------------------------------------------------------------------------------
	 * Modify xml-model as preparation for answer back to controller
	 */
	/**
	 * First analyze message, call information from SNMP device and update the
	 * related Document parameter and send SNMP commands Secondly do the original
	 * function and create reply message.
	 */
	@Override
	public synchronized List<String> assembleEditConfigElementReplyList(String sessionId, NetconfTagList tags,
			String xml) {
		try {
			// consoleMessage("------------------------------------");
			// consoleMessage("Start editConfig processing for message "+sessionId);
			Document inDoc = loadXMLFromString(xml);
			// consoleMessage("Doc loaded");
			snmpConnector.onPreEditConfigTarget(sessionId, tags, inDoc);
		} catch (Exception e) {
			LOG.warn("Can not do SNMP processing: ", e);
		}
		// return super.assembleEditConfigElementReplyList(sessionId, tags, xml);
		List<String> res = new ArrayList<>();
		res.add(assembleRpcReplyOk(sessionId));
		return res;
	}

	/**
	 * First analyze message, call information from SNMP device and update the
	 * related Document parameter. Secondly create reply message.
	 */
	@Override
	public synchronized String assembleRpcReplyFromFilterMessage(String id, NetconfTagList tags) {
		// do a sync snmp set request
		NodeEditConfigCollection oidNodes = getOIDsForRequest(id, tags);
		snmpConnector.onPreReplyMessage(id, tags, oidNodes);
		String message = super.assembleRpcReplyFromFilterMessage(id, tags);
		return this.replaceConfigValues(message);
	}

	/**
	 * Delete from outgoing message SNMP related elements
	 */
	@Override
	protected String replaceAndWash(String xmlMessage) {
		xmlMessage = super.replaceAndWash(xmlMessage);
		xmlMessage = this.replaceConfigValues(xmlMessage);
		xmlMessage = removeCommentsAndAttributes(xmlMessage);
		return xmlMessage;
	}

	/*----------------------------------------------------------------------------------------
	* Functions
	*/

	private String replaceConfigValues(String xmlMessage) {
		return xmlMessage.replace("$NEIPADDRESS", this.snmpIp).replace("$MOUNTPOINTNAME", this.mName);
	}

	private static String removeSnmpCommentsAndAttributes(String xmlMessage) {
		return removeCommentsAndAttributes(xmlMessage, "oid", "snmpTrapOid", "snmpTrapValue", "conversion", "access");
	}

	/**
	 * Remove specified part of message. Example1: oid="dsfsdf"
	 * removeFromString(xml, "oid", "\"", "\"") Example2: <!-- -->
	 * removeFromString(xml, "<!--", null, "-->")
	 *
	 * @param xml
	 *            input/output with message
	 * @param start
	 *            indicating string (could be null)
	 * @param intermediate
	 *            intermediate
	 * @param end
	 *            end indicating string
	 * @return changed xml as StringBuffer
	 */
	private static StringBuffer removeFromString(StringBuffer xml, String start, String intermediate, String end) {

		int protect = 100;
		int idx1a = 0;
		int idx1b, idx2;

		while ((idx1a = xml.indexOf(start, idx1a)) > -1 && protect-- > 0) {
			if (intermediate == null || intermediate.isEmpty()) {
				idx1b = idx1a + start.length();
			} else {
				idx1b = xml.indexOf(intermediate, idx1a + start.length());
				if (idx1b == -1) {
					idx1a++;
					continue;
				}
				idx1b += intermediate.length();
			}
			idx2 = xml.indexOf(end, idx1b);
			if (idx2 == -1) {
				idx1a++;
				continue;
			}
			if (idx2 > idx1a) {
				xml.replace(idx1a, idx2 + end.length(), "");
			}
		}
		return xml;
	}

	/**
	 * Remove specific strings and all comments from message
	 *
	 * @param xmlMessage
	 *            to process
	 * @param names
	 *            with attributes to be removed
	 * @return xml as string
	 */
	private static String removeCommentsAndAttributes(String xmlMessage, String... names) {
		StringBuffer xml = new StringBuffer(xmlMessage);

		xml = removeFromString(xml, "<!--", null, "-->");

		for (String name : names) {
			xml = removeFromString(xml, name, "\"", "\"");
		}

		return xml.toString();
	}

	/**
	 * Do processing for bundle of received traps
	 *
	 * @param traps
	 *            contains all consecutive taps
	 * @return processed message
	 */
	public synchronized String doProcessSnmpTrapAction(List<SnmpTrap> traps) {

		// start processing
		String key = null;
		String xmlSubTree = null;

		// Try to find for one key created by oid+value a definition for a notification
		for (SnmpTrap trap : traps) {
			key = trap.getOid() + trap.getValue();
			xmlSubTree = getNotification(key);
			if (xmlSubTree != null) {
				break;
			}
		}

		// No process the notification and take over some values from the traps
		if (xmlSubTree != null) {
			StringBuffer xmlMsg = new StringBuffer(assembleRpcNotification(xmlSubTree));

			int idxStart = 0, paramIdxStart, paramIdxEnd;
			String oidString;

			while ((idxStart = xmlMsg.indexOf(OIDPARAMETERNAMESTART, idxStart)) > -1) {
				paramIdxStart = idxStart + OIDPARAMETERNAMESTART.length();
				paramIdxEnd = xmlMsg.indexOf(OIDPARAMETERNAMEEND, paramIdxStart);
				if (idxStart < paramIdxStart && paramIdxStart < paramIdxEnd) {
					oidString = xmlMsg.substring(paramIdxStart, paramIdxEnd);
					for (SnmpTrap trap : traps) {
						if (oidString.contentEquals(trap.getOid())) {
							xmlMsg.replace(idxStart, paramIdxEnd, trap.getValue());
						}
					}
				}
				idxStart = paramIdxStart;
			}
			String xmlMsgString = removeSnmpCommentsAndAttributes(xmlMsg.toString());
			LOG.debug("Notification for key '" + key + "'\n" + xmlMsgString);
			return xmlMsgString;
		} else {
			LOG.warn("No instructions for:" + traps);
			return null;
		}
	}

	public NodeEditConfigCollection getOIDsForRequest(String messageId, NetconfTagList tags) {

		if (tags.isEmtpy()) {

			return NodeEditConfigCollection.EMPTY;

		} else {

			String xmlSubTreePath = tags.getSubTreePath();
			LOG.debug("Subtreepath=" + xmlSubTreePath);
			NodeList xmlSubTree = NetworkElement.getXmlSubTree(getDocument(), "//data/" + xmlSubTreePath); // Get nodes
																											// from
																											// Document
			LOG.debug("onPreEditConfigTarget odelist: " + xmlSubTree.getLength());
			NodeEditConfigCollection res = itOIDsForRequest(mSNMPNodes, xmlSubTree, new NodeEditConfigCollection());
			LOG.debug("onPreEditConfigTarget odelist: " + res.size());
			return res;

		}
	}

	public NodeEditConfigCollection getOIDsForEditRequest(String messageId, NetconfTagList tags) {

		if (tags.isEmtpy()) {

			return NodeEditConfigCollection.EMPTY;

		} else {

			String xmlSubTreePath = tags.getSubTreePath(5, 3);
			LOG.debug("onPreEditConfigTarget subtreepath=" + xmlSubTreePath);
			// consoleMessage("onPreEditConfigTarget subtree: "+xmlSubTreePath);
			NodeList xmlSubTree = NetworkElement.getXmlSubTree(getDocument(), "//data/" + xmlSubTreePath); // Get nodes
																											// from
																											// Document
			LOG.debug("onPreEditConfigTarget nodelist: " + xmlSubTree.getLength());
			NodeEditConfigCollection res = itOIDsForRequest(mSNMPNodes, xmlSubTree, new NodeEditConfigCollection());
			LOG.debug("onPreEditConfigTarget nodelist: " + res.size());
			return res;

		}
	}

	/**
	 * set Value From SNMP Response into internal XMLModel
	 *
	 * @param node
	 *            of document to be changed
	 * @param value
	 *            with new content
	 * @return true if there was a parameter change
	 */
	public boolean setSNMPValueInDocumentNode(NodeEditConfig node, String value) {
		boolean r = false;
		if (node != null) {
			return node.setConvertedSnmpValue2Xml(value);
		}
		return r;
	}

	/*----------------------------------------------------------------------------------------
	 * Private functions to process the XML model.
	 */

	private NodeEditConfigCollection searchForOID(String xPath, NodeList list, String attributeName,
			NodeEditConfigCollection result) {
		Element e = null;
		String xPathOfNode, oid;
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			xPathOfNode = xPath + "/" + n.getNodeName();
			String lp = getNodeAttribute(n, "layer-protocol");
			if (!lp.isEmpty()) {
				xPathOfNode = xPathOfNode + "[" + "layer-protocol=\"" + lp + "\"]"; // Add layerprotocol index
			}
			// consoleMessage("searchForOID: "+lp+" "+n.getNodeType()+" "+n.getNodeName()+"
			// "+n.getBaseURI()+" xPath:"+xPathOfNode);

			if (n.hasAttributes() && n instanceof Element) {
				e = (Element) n;
				if (e.hasAttribute(attributeName) && !e.getAttribute(attributeName).isEmpty()) {
					oid = e.getAttribute(attributeName);
					result.add(new NodeEditConfig(xPathOfNode.replaceFirst("data", ""), e, oid));
				}
			}
			if (n.hasChildNodes()) {
				searchForOID(xPathOfNode, n.getChildNodes(), attributeName, result);
			}
		}
		return result;
	}

	private static NodeEditConfigCollection itOIDsForRequest(NodeEditConfigCollection mSNMPNodes, NodeList list,
			NodeEditConfigCollection result) {
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			Element e = null;
			if (n.hasAttributes() && n instanceof Element) {
				e = (Element) n;
				if (e.hasAttribute("oid") && !e.getAttribute("oid").isEmpty()) {
					NodeEditConfig c = mSNMPNodes.find(e);
					if (c != null) {
						result.add(c);
					}
				}
			}
			if (n.hasChildNodes()) {
				itOIDsForRequest(mSNMPNodes, n.getChildNodes(), result);
			}
		}
		return result;
	}

	public boolean addToProblemListMW(String problemName, String problemSeverity, String timestamp, String objectRefId, String counter) {
		return this.addToProblemList12(this.mMWProblemList, problemName, problemSeverity, timestamp, objectRefId, counter);
	}

	public boolean addToProblemListNE(String problemName, String problemSeverity, String timestamp, String objectRefId, String counter) {
		return this.addToProblemList12(this.mNEProblemList, problemName, problemSeverity, timestamp, objectRefId, null);
	}

	public boolean addToProblemListETH(String problemName, String problemSeverity, String timestamp, String objectRefId, String counter) {
		return this.addToProblemList12(this.mETHProblemList, problemName, problemSeverity, timestamp, objectRefId, counter);
	}
	public boolean removeFromProblemListMW(String problemName) {
		return this.removeFromProblemListMW(problemName, false);
	}
	public boolean removeFromProblemListMW(String problemName, boolean withCleared) {
		return this.removeFromProblemList12(this.mMWProblemList, problemName, withCleared);
	}
	public boolean removeFromProblemListNE(String problemName) {
		return this.removeFromProblemListNE(problemName,false);
	}
	public boolean removeFromProblemListNE(String problemName, boolean withCleared) {
			return this.removeFromProblemList12(this.mNEProblemList, problemName, withCleared);
	}
	public boolean removeFromProblemListETH(String problemName) {
		return this.removeFromProblemListETH(problemName, false);
	}
	public boolean removeFromProblemListETH(String problemName, boolean withCleared) {
		return this.removeFromProblemList12(this.mETHProblemList, problemName, withCleared);
	}

	private static Element findProblemNode10(final NodeList list, String problemName) {
		return findProblemNode10(list, problemName, false);
	}
	private static Element findProblemNode10(final NodeList list, String problemName, boolean withCleared) {
		return findProblemNode(list, "currentProblemList","problemName",problemName, withCleared);
	}
	private static Element findProblemNode12(final NodeList list, String problemName) {
		return findProblemNode12(list, problemName, false);
	}
	private static Element findProblemNode12(final NodeList list, String problemName, boolean withCleared) {
				return findProblemNode(list, "current-problem-list","problem-name",problemName, withCleared);
	}

	/*
	 * returns <current-problem-list> Node in which <problem-name> value equals
	 * param 'problemName'
	 */
	private static Element findProblemNode(final NodeList list,final String cplName,final String pnName, String problemName, boolean withCleared) {
		int i;
		Element node = null;
		Element nodeName = null;
		if (problemName != null) {
			if (withCleared) {
				int idx = problemName.indexOf("Cleared");
				if (idx > 0)
					problemName = problemName.substring(0, idx);
			}
			for (i = 0; i < list.getLength(); i++) {
				if (list.item(i).getNodeType() == Node.ELEMENT_NODE
						&& list.item(i).getNodeName().equals(cplName)) {
					NodeList children = list.item(i).getChildNodes();
					nodeName = findChildElementByName(children, pnName);
					if (nodeName != null && nodeName.getTextContent().equals(problemName)) {
						node = (Element) list.item(i);
						break;
					}
				}
			}
		}
		return node;
	}

	private static Element findChildElementByName(final NodeList list, final String nodeName) {
		Element node = null;
		for (int j = 0; j < list.getLength(); j++) {
			if (list.item(j).getNodeType() == Node.ELEMENT_NODE && list.item(j).getNodeName().equals(nodeName)) {
				node = (Element) list.item(j);
				break;
			}
		}
		return node;
	}

	public boolean addToProblemList12(final Node root,String problemName, String problemSeverity,String timestamp,String objectRefId,String counter) {
		LOG.debug(String.format("try to add problem %s with sev=%s to node %s",problemName,problemSeverity,root==null?"null":root.getNodeName()));
		if(root==null)
			return false;
		NodeList list=root.getChildNodes();

		//search for <urrent-problem-list> Node with problemName
		Element node=findProblemNode12(list, problemName);
		if(problemSeverity.equals("non-alarmed"))
		{
			if(node!=null)
				root.removeChild(node);
		}
		else
		{
			Document doc=getDocument();
			if(node==null)
			{
				node=doc.createElement("current-problem-list");
				Node seq=doc.createElement("sequence-number");
				seq.setTextContent(String.format("%d", list.getLength()+1));
				Node name=doc.createElement("problem-name");
				name.setTextContent(problemName);
				Node sev=doc.createElement("problem-severity");
				sev.setTextContent(problemSeverity);
				Node ts=doc.createElement("time-stamp");
				ts.setTextContent(timestamp);
				Node objid=doc.createElement("object-reference");
				objid.setTextContent(objectRefId);
				node.appendChild(seq);
				node.appendChild(name);
				node.appendChild(sev);
				node.appendChild(ts);
				node.appendChild(objid);
				if(counter!=null)
				{
					Node c=doc.createElement("counter");
					c.setTextContent(counter);
					node.appendChild(c);
				}
				root.appendChild(node);
				LOG.debug("problem added");
			}
			else	//refresh data
			{
				LOG.debug("problem already exists");
				if(Config.getInstance().updateProblemTimestamps())
				{
					LOG.debug("time-stamp will be refreshed");
					Node ts=findChildElementByName(node.getChildNodes(), "time-stamp");
					if(ts!=null)
						ts.setTextContent(timestamp);
				}
			}

		}
		return true;
	}
	public boolean addToProblemList10(final Node root,String problemName, String problemSeverity,String timestamp,String objectRefId,String counter) {
		LOG.debug(String.format("try to add problem %s with sev=%s to node %s",problemName,problemSeverity,root==null?"null":root.getNodeName()));
		if(root==null)
			return false;
		NodeList list=root.getChildNodes();

		//search for <urrent-problem-list> Node with problemName
		Element node=findProblemNode10(list, problemName);
		if(problemSeverity.equals("non-alarmed"))
		{
			if(node!=null)
				root.removeChild(node);
		}
		else
		{
			Document doc=getDocument();
			if(node==null)
			{
				node=doc.createElement("currentProblemList");
				Node seq=doc.createElement("sequenceNumber");
				seq.setTextContent(String.format("%d", list.getLength()+1));
				Node name=doc.createElement("problemName");
				name.setTextContent(problemName);
				Node sev=doc.createElement("problemSeverity");
				sev.setTextContent(problemSeverity);
				Node ts=doc.createElement("timeStamp");
				ts.setTextContent(timestamp);
				Node objid=doc.createElement("objectIdRef");
				objid.setTextContent(objectRefId);
				node.appendChild(seq);
				node.appendChild(name);
				node.appendChild(sev);
				node.appendChild(ts);
				node.appendChild(objid);
				if(counter!=null)
				{
					Node c=doc.createElement("counter");
					c.setTextContent(counter);
					node.appendChild(c);
				}
				root.appendChild(node);
				LOG.debug("problem added");
			}
			else	//refresh data
			{
				LOG.debug("problem already exists");
				if(Config.getInstance().updateProblemTimestamps())
				{
					LOG.debug("time-stamp will be refreshed");
					Node ts=findChildElementByName(node.getChildNodes(), "timeStamp");
					if(ts!=null)
						ts.setTextContent(timestamp);
				}
			}

		}
		return true;
	}

	public boolean removeFromProblemList12(final Node root, String problemName, boolean withCleared) {

		if (root == null)
			return false;
		NodeList list = root.getChildNodes();
		Element node = findProblemNode12(list, problemName, withCleared);
		if (node != null)
		{
			root.removeChild(node);
			return true;
		}
		return false;
	}
	public boolean removeFromProblemList10(final Node root, String problemName, boolean withCleared) {

		if (root == null)
			return false;
		NodeList list = root.getChildNodes();
		Element node = findProblemNode10(list, problemName, withCleared);
		if (node != null)
		{
			root.removeChild(node);
			return true;
		}
		return false;
	}

	/*
	 * process function for filling or clearing problem list
	 */
	public boolean onTrapReceived(SnmpTrapList traps) {

		String key = null;
		boolean r=false;
		SnmpTrapNotification notification = null;
		// Try to find for one key created by oid+value a definition for a notification
		for (SnmpTrap trap : traps.get()) {
			key = trap.getOid() + trap.getValue();
			notification = this.mAvailableTraps.get(key);
			if (notification != null)
				break;
		}

		if (notification != null) {
			LOG.debug("found notification for trap:" + notification.toString());
			// set counter and timestamp
			notification.setCounter(null);
			String timeStamp = new Timestamp(System.currentTimeMillis()).toInstant().toString();
			notification.setTimeStamp(timeStamp);
			// add/remove to/from list
			if (notification.isNonAlarmed()) {
				LOG.debug("remove notification " + notification.getProblemName() + " from problem list");
				//remove from problem list // maybe with 'Cleared' as problemNameSuffix =>param=true
				if (notification.getType() == SnmpTrapNotification.TYPE_MICROWAVE)
					r=this.removeFromProblemListMW(notification.getProblemName());
				else if (notification.getType() == SnmpTrapNotification.TYPE_ETHERNET)
					r=this.removeFromProblemListETH(notification.getProblemName());
				else if (notification.getType() == SnmpTrapNotification.TYPE_NETWORKELEMENT)
					r=this.removeFromProblemListNE(notification.getProblemName());
			} else {
				LOG.debug("add notification " + notification.getProblemName() + " to problem list");
				if (notification.getType() == SnmpTrapNotification.TYPE_MICROWAVE)
					r=this.addToProblemListMW(notification.getProblemName(), notification.getSeverity(),
							notification.getTimestamp(),this.getDeviceName(), notification.getCounter());
				else if (notification.getType() == SnmpTrapNotification.TYPE_ETHERNET)
					r=this.addToProblemListETH(notification.getProblemName(), notification.getSeverity(),
							notification.getTimestamp(),this.getDeviceName(), notification.getCounter());
				else if (notification.getType() == SnmpTrapNotification.TYPE_NETWORKELEMENT)
					r=this.addToProblemListNE(notification.getProblemName(), notification.getSeverity(),
							notification.getTimestamp(),this.getDeviceName(), notification.getCounter());
			}
			LOG.debug("succeeded");
		} else
		{
			LOG.warn("no notification found for trap:" + traps.toString());
			r=false;
		}
		return r;
	}
	
	/**Start Pier functions
	 * 
	*/
	public Document getTr069Document() {
		return tr069Document;
	}

	public void setTr069Document(Document tr069Document) {
		this.tr069Document = tr069Document;
		updateDoc();
	}
	
	public void setTr069DocumentCFromString(String stringDoc) {
		DocumentBuilder db;
		System.out.println(stringDoc);
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(stringDoc));

			Document doc = db.parse(is);
			this.setTr069Document(doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/***
	 * This function take the values from this.tr069Document (coming from the device) and update this.doc
	 */
	public void updateDoc()  {
		//printDocument(tr069Document, System.out);
		try {
			System.out.println("BEFOREEEEE");
			printNode(NetworkElement.getNode(getDocument(), "//data/equipment"));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		NodeList informs = tr069Document.getElementsByTagName("ParameterValueStruct");
		if(informs != null) {
			System.out.println("informs lenght "+informs.getLength());
			for (int i = 0; i < informs.getLength(); i++) {
				NodeList child = informs.item(i).getChildNodes();
				String value = "";
				String key = "";			
				for (int j = 0; j < child.getLength(); j++) {
					
					if(child.item(j).getNodeName().equals("Name")) {
						key = child.item(j).getTextContent();
						
					}else if (child.item(j).getNodeName().equals("Value")) {
						value = child.item(j).getTextContent();
					}						
				}
				updateDocKeyValue(key, value);
			}
		}
		try {
			System.out.println("AFTERRRR");
			printNode(NetworkElement.getNode(getDocument(), "//data/equipment"));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateDocKeyValue(String tr069Key, String value) {

		String key = CoreModelMapping.getYangfromTR069(tr069Key);
		if (key != null) {
			// The key is in the coremodel so update the core model using the mapping
			this.updateCoreModel(key, value);
		}else {
			// Update bbf model
			this.updateBbfModel(tr069Key, value);
		}
	}
	
	private void updateBbfModel(String tr069Key, String value) {
		// The tr069Key is like this FAPService.{i}.CellConfig.LTE.RAN.RF.DLBandwidth
		// We need to find the correspondent xpath //data/fap-service/alias[text()=i]/cell-config/lte/lte-ran/lte-ran-rf/dl-bandwidth
		// I don't know if we can multiple fapservices,  otherwise the part alias[text()=i] is not needed
		
	}
	
	public void updateCoreModel(String key, String value) {
		String[] parts = key.split("%");
		for (String k : parts) {
			Object result;
			try {
				XPathFactory xPathfactory = XPathFactory.newInstance();
				XPath xpath = xPathfactory.newXPath();
				XPathExpression expr = xpath.compile(k);
				result = expr.evaluate(getDocument(), XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				for (int i = 0; i < nodes.getLength(); i++) {
					// System.out.println(nodes.item(i).getLocalName());
					nodes.item(i).setTextContent(value);
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// Call printDocument(doc, System.out)
	public static void printDocument(Document doc, OutputStream out){
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		    transformer.transform(new DOMSource(doc), 
		         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	private void printNode(Node node) {
		  StringWriter sw = new StringWriter();
		  try {
		    Transformer t = TransformerFactory.newInstance().newTransformer();
		    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    t.transform(new DOMSource(node), new StreamResult(sw));
		  } catch (TransformerException te) {
		    System.out.println("nodeToString Transformer Exception");
		  }
		  System.out.println(sw.toString());
		}
	
	    

	
	/**End Pier functions
	 * 
	*/
}
