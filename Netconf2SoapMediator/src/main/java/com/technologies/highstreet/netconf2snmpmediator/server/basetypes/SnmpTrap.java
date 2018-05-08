/**
 * Message representation of a user CLI command that is forwarded to the Message Processor for further processing.
 *
 * @author herbert.eiselt@highstreet-technologies.com
 */

package com.technologies.highstreet.netconf2snmpmediator.server.basetypes;

public class SnmpTrap {

    private static final long serialVersionUID = 7469960234549882489L;

    private static int counter = 0;
    private final String oid;
    private final String value;
    private final int myNumber;

    public SnmpTrap( String oid, String value ) {
        this.oid = oid;
        this.value = value;
        this.myNumber = counter++;
    }

    /**
     * @return the oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the myNumber
     */
    public int getMyNumber() {
        return myNumber;
    }

    @Override
    public String toString() {
        return "SnmpTrap [oid=" + oid + ", value=" + value + ", myNumber=" + myNumber + "]";
    }



}
