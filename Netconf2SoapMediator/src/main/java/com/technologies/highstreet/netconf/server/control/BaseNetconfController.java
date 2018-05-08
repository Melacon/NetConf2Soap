package com.technologies.highstreet.netconf.server.control;

import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.networkelement.NetworkElement;

public interface BaseNetconfController extends NetconfNotifyExecutor {

    public abstract <T extends NetworkElement> void start(MessageStore messageStore, T ne, Console console);
    public abstract void destroy();

}
