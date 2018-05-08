/**
 *
 */
package com.technologies.highstreet.netconf.server.types;

/**
 * @author herbert
 *
 */
public class NetconfBoolean {

    public static String TRUE = "true";
    public static String FALSE = "false";

    public static String getNetconfBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

}
