/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server.control;

import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyOriginator;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;
import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.MediatorConnectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;

/**
 * @author herbert
 *
 */
public class Netconf2SNMPFactory implements NamedFactory<Command> {
    private static final Log log2  = LogFactory.getLog(Netconf2SNMPFactory.class);

    private MessageStore        messageStore        = null;
    private BehaviourContainer  behaviourContainer    = null;
    private Netconf2SNMPNetworkElement      ne = null;
    private NetconfNotifyOriginator  notifyFunction = null;
    private final Console console;
    private final MediatorConnectionListener mConnectionListener;
    
    private Netconf2SNMPFactory(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator notifyFunction, Netconf2SNMPNetworkElement ne,MediatorConnectionListener connectionListener, Console console) {
        this.messageStore = messageStore;
        this.behaviourContainer = behaviourContainer;
        this.ne = ne;
        this.notifyFunction = notifyFunction;
        this.console = console;
        this.mConnectionListener=connectionListener;
    }

    public static Netconf2SNMPFactory createFactory(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator notifyFunction, Netconf2SNMPNetworkElement ne,MediatorConnectionListener connectionListener, Console console) {
        return new Netconf2SNMPFactory(messageStore, behaviourContainer, notifyFunction, ne,connectionListener, console);
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

