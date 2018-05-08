/**
 * Message representation of a user CLI command that is forwarded to the Message Processor for further processing.
 *
 * @author herbert.eiselt@highstreet-technologies.com
 */

package com.technologies.highstreet.netconf.server.basetypes;

import net.i2cat.netconf.rpc.RPCElement;

public class UserCommand extends RPCElement {

    private static final long serialVersionUID = 7469960234549882489L;

    public static final String SNMP_ALERTMESSAGE_REMOTEDEVICENOTFOUND="";
    public static final String SNMP_ALERTMESSAGE_PORTMAPPERNOTFOUND="";
    public static final String SNMP_ALERTMESSAGE_INITIALCONFIG_FAILED="";

    public static final String SNMP_ALERTMESSAGE_SNMPREQUESTFAILED = "";
	public static final String SNMP_ALERTMESSAGE_SNMPSETREQUESTFAILED = "";

    private final String command;

    public UserCommand( String command ) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "NetconfNotification [command=" + command + "]";
    }

}
