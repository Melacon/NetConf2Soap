/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server.control;

import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.control.NetconfNotifyOriginator;
import com.technologies.highstreet.netconf.server.control.NetconfSubsystem;
import com.technologies.highstreet.netconf2snmpmediator.server.Netconf2SNMPMediator;
import com.technologies.highstreet.netconf2snmpmediator.server.networkelement.Netconf2SNMPNetworkElement;
import com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing.MediatorConnectionListener;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.Environment;

/**
 * @author herbert
 *
 */
public class Netconf2SNMPSubsystem extends NetconfSubsystem {
    private static final Log LOG = LogFactory.getLog(Netconf2SNMPMediator.class);

    private final MediatorConnectionListener mConnectionListener;

    public Netconf2SNMPSubsystem(MessageStore messageStore, BehaviourContainer behaviourContainer,
            NetconfNotifyOriginator netconfNotifyExecutor, Netconf2SNMPNetworkElement ne,MediatorConnectionListener connectionListener, Console console) {
        super(messageStore, behaviourContainer, netconfNotifyExecutor, ne, console);
        this.mConnectionListener=connectionListener;
    }

    @Override
    public void start(Environment envParam) throws IOException {
        this.env = envParam;

        // initialize Netconf processor
        netconfProcessor = new Netconf2SNMPController(in,out,err,callback,this.mConnectionListener);

        LOG.info("Starting new client thread...");
        netconfProcessor.start(messageStore, ne, console);
        netconfNotifyExecutor.setNetconfNotifyExecutor(netconfProcessor);

    }


}
