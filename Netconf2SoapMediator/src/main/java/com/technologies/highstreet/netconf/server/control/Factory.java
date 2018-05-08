/**
 *
 */
package com.technologies.highstreet.netconf.server.control;

import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.networkelement.NetworkElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;

/**
 * Netconf Subsystem Factory
 *
 * @author Julio Carlos Barrera
 *
 */
public class Factory implements NamedFactory<Command> {
    private static final Log log2  = LogFactory.getLog(Factory.class);

    private MessageStore        messageStore        = null;
    private BehaviourContainer  behaviourContainer    = null;
    private NetworkElement      ne = null;
    private NetconfNotifyOriginator  notifyFunction = null;
    private final Console console;

    private Factory(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator notifyFunction, NetworkElement ne, Console console) {
        this.messageStore = messageStore;
        this.behaviourContainer = behaviourContainer;
        this.ne = ne;
        this.notifyFunction = notifyFunction;
        this.console = console;
    }

    public static Factory createFactory(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator notifyFunction, NetworkElement ne, Console console) {
        return new Factory(messageStore, behaviourContainer, notifyFunction, ne, console);
    }

    @Override
    public Command create() {
        log2.info("Creating Netconf Subsystem Factory");
        return new NetconfSubsystem(messageStore, behaviourContainer, notifyFunction, ne, console);
    }

    @Override
    public String getName() {
        return "netconf";
    }

}
