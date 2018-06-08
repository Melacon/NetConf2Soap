/**
 * Netconf Message processor.
 *
 * Reads the message queue and executes related actions within a Thread.
 * Owns the network element class which simulates the NETCONF NE behavior.
 * Processes also other messages, like user commands.
 *
 * @author Herbert (herbert.eiselt@highstreet-technologies.com)
 *
 */

package com.technologies.highstreet.netconf2soapmediator.server.streamProcessing;

import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.streamprocessing.NetconfIncommingMessageRepresentation;
import com.technologies.highstreet.netconf.server.streamprocessing.NetconfMessageProcessorThread;
import com.technologies.highstreet.netconf.server.types.NetconfSender;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatusHolder;
import com.technologies.highstreet.netconf2soapmediator.server.HTTPServlet;
import com.technologies.highstreet.netconf2soapmediator.server.basetypes.SnmpTrapList;
import com.technologies.highstreet.netconf2soapmediator.server.networkelement.BBFTRModelMapping;
import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.i2cat.netconf.messageQueue.MessageQueue;
import net.i2cat.netconf.rpc.RPCElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Netconf2SNMPMessageProcessorThread extends NetconfMessageProcessorThread  {

    private static final Log log  = LogFactory.getLog(Netconf2SNMPMessageProcessorThread.class);

    private final Netconf2SoapNetworkElement sne;

    public Netconf2SNMPMessageProcessorThread(String name, NetconfSessionStatusHolder status, NetconfSender sender,
            MessageQueue messageQueue, MessageStore messageStore, Netconf2SoapNetworkElement ne, Console console) {

        super(name, status, sender,  messageQueue,  messageStore,  ne,  console);
        sne = ne;
        this.consoleMessage("SNMP Thread created");
        log.info("SNMP Thread created");

    }
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
	}
    @Override
    public boolean doMessageProcessingForSpecificMessageClass(RPCElement message) throws IOException {

        boolean handled = super.doMessageProcessingForSpecificMessageClass(message);

        if (! handled) {
            if (message instanceof SnmpTrapList) {
                doNotificationProcessing((SnmpTrapList)message);
                handled = true;
            }
        }
        return handled;
    }

    /*
    // The SnmpTrap is added here
    @Override
    public void run() {
        while (status.less(NetconfSessionStatus.SESSION_CLOSED)) {

            RPCElement message = messageQueue.blockingConsume();

            if (message == null) {
                log.debug("Received received: null");
            } else {
                log.debug("Message received: " + message.getClass().getSimpleName()+" "+message.toString());
            }

            // store message if necessary
            if (messageStore != null) {
                messageStore.storeMessage(message);
            }

            // avoid message processing when session is already closed
            if (status.equals(NetconfSessionStatus.SESSION_CLOSED)) {
                log.warn("Session is closing or is already closed, message will not be processed");
                return;
            }

            // process message
            try {
                if (message instanceof NetconfIncommingMessageRepresentation) {
                    doMessageProcessing((NetconfIncommingMessageRepresentation)message);
                } else if (message instanceof UserCommand) {
                    doNotificationProcessing((UserCommand)message);
                } else if (message instanceof SnmpTrap) {
                    doNotificationProcessing((SnmpTrap)message);
                } else {
                    log.warn("Unknown message: " + message.getClass().getSimpleName() + " " + message.toString());
                }
            } catch (IOException e) {
                log.error("Error sending reply", e);
                break;
            }
        }
        log.trace("Message processor ended");
    }
    */

    /*********************************************************************
     * Private message processing
     */


    private void doNotificationProcessing(SnmpTrapList receivedMessage) throws IOException {

        String msg = sne.doProcessSnmpTrapAction(receivedMessage.get());
        if (msg != null) {
            send( msg );
        }
    }
    
    @Override
    protected void doMessageProcessing(NetconfIncommingMessageRepresentation receivedMessage) throws IOException {
    	if (receivedMessage.isRpcEditConfigTargetRunningDefaultOperationConfig()) {
    		
//    		ArrayList<String> list1 = new ArrayList<String>();
//    		list1.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.ULBandwidth");
//    		list1.add("20");
//    		HTTPServlet.setParamMap.put(0, list1);
//
//    		ArrayList<String> list2 = new ArrayList<String>();
//    		list2.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.DLBandwidth");
//    		list2.add("20");
//    		HTTPServlet.setParamMap.put(1, list2);
//    		System.out.println(receivedMessage.getXmlSourceMessage());
//    		System.out.println(receivedMessage.getFilterTags().getSubTreePath());
    		
    		
    		// fill list of parameters that you want to set
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            Document inDoc = null;
			try {
				builder = factory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(receivedMessage.getXmlSourceMessage()));
	    		inDoc = builder.parse(is);
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		Set<String> yangKeys = BBFTRModelMapping.getYangKeys();
    		XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String xpathKeyString = "//fap-service/alias";
			Object result;
			String fap_id = "Device.Services.FAPService.";
			try {
				
				XPathExpression expr = xpath.compile(xpathKeyString);
				result = expr.evaluate(inDoc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				for (int i = 0; i < nodes.getLength(); i++) {
					// System.out.println(nodes.item(i).getLocalName());		
					fap_id = fap_id + nodes.item(i).getTextContent()+".";
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			
			//*[@*[starts-with(., 'h')]]
			xpathKeyString = "//*[@operation]";
			String first_tag = null;
			try {
				
				XPathExpression expr = xpath.compile(xpathKeyString);
				result = expr.evaluate(inDoc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				for (int i = 0; i < nodes.getLength(); i++) {	
					first_tag = nodes.item(i).getNodeName();
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			
			
			int map_index = 0;
    		for(String xpathString: yangKeys) {
    			if(xpathString.contains(first_tag)) {
	    			String xpathStringFixed = "//fap-service/"+xpathString.substring(xpathString.indexOf(first_tag));
	    			   			
	    			try {
	    				
	    				XPathExpression expr = xpath.compile(xpathStringFixed);
	    				result = expr.evaluate(inDoc, XPathConstants.NODESET);
	    				NodeList nodes = (NodeList) result;
	    				String value = null;
	    				for (int i = 0; i < nodes.getLength(); i++) {
	    					// System.out.println(nodes.item(i).getLocalName());
	    					value = nodes.item(i).getTextContent();
	    					
	    				}
	    				if(value !=null && !value.equals("")) {
	    					ArrayList<String> list = new ArrayList<String>();
	        				list.add(fap_id + BBFTRModelMapping.getTR069fromYang(xpathString));
	        	    		list.add(value);
	        	    		HTTPServlet.setParamMap.put(map_index, list);		
	        	    		map_index++;
	    				}
	    				
	    			} catch (XPathExpressionException e) {
	    				e.printStackTrace();
	    			}
	    		}
    		}
    		HTTPServlet.setSetParam(true);
    	}
    	super.doMessageProcessing(receivedMessage);
    }

}
