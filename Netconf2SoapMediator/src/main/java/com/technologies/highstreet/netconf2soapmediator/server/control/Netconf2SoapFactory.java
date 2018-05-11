/**
 *
 */
package com.technologies.highstreet.netconf2soapmediator.server.control;

import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyOriginator;
import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;
import com.technologies.highstreet.netconf2soapmediator.server.streamProcessing.MediatorConnectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;

/**
 * @author herbert
 *
 */
public class Netconf2SoapFactory implements NamedFactory<Command> {
    private static final Log log2  = LogFactory.getLog(Netconf2SoapFactory.class);

    private MessageStore        messageStore        = null;
    private BehaviourContainer  behaviourContainer    = null;
    private Netconf2SoapNetworkElement      ne = null;
    private NetconfNotifyOriginator  notifyFunction = null;
    private final Console console;
    private final MediatorConnectionListener mConnectionListener;
    
    private Netconf2SoapFactory(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator notifyFunction, Netconf2SoapNetworkElement ne,MediatorConnectionListener connectionListener, Console console) {
        this.messageStore = messageStore;
        this.behaviourContainer = behaviourContainer;
        this.ne = ne;
        this.notifyFunction = notifyFunction;
        this.console = console;
        this.mConnectionListener=connectionListener;
    }

    public static Netconf2SoapFactory createFactory(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator notifyFunction, Netconf2SoapNetworkElement ne,MediatorConnectionListener connectionListener, Console console) {
        return new Netconf2SoapFactory(messageStore, behaviourContainer, notifyFunction, ne,connectionListener, console);
    }

    @Override
    public Command create() {
        return new Netconf2SNMPSubsystem(messageStore, behaviourContainer, notifyFunction, ne,this.mConnectionListener, console);
    }

    @Override
    public String getName() {
        return "netconf";
    }

}

