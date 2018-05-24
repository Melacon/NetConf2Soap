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
import com.technologies.highstreet.netconf2soapmediator.server.basetypes.SnmpTrapList;
import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;

import java.io.IOException;
import net.i2cat.netconf.messageQueue.MessageQueue;
import net.i2cat.netconf.rpc.RPCElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    		//cc=true;
    		// fill list
    	}
    	super.doMessageProcessing(receivedMessage);
    }

}
