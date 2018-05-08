/**
 *
 */
package com.technologies.highstreet.netconf2soapmediator.server.basetypes;

import java.util.ArrayList;
import java.util.List;
import net.i2cat.netconf.rpc.RPCElement;

/**
 * @author herbert
 *
 */
public class SnmpTrapList  extends RPCElement {

     private static final long serialVersionUID = -1235647591803564718L;

    private final List<SnmpTrap> traps = new ArrayList<>();

    public void add(SnmpTrap trap) {
        traps.add(trap);
    }

    public List<SnmpTrap> get() {
        return traps;
    }

    @Override
    public String toString() {
        return "SnmpTrapList [traps=" + traps + "]";
    }



}
