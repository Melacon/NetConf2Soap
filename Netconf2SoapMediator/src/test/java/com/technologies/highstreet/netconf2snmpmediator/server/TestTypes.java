/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server;

import com.technologies.highstreet.netconf2soapmediator.server.networkelement.NodeEditConfig;

/**
 * @author herbert
 *
 */
public class TestTypes {

    private static void test(String converter) {

        NodeEditConfig node = new NodeEditConfig(null, null, "1.2.3", converter, "read-write","");
        System.out.println("Test pattern: "+converter);
        String value;
        value = "0";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "1";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "2";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "3";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
        value = "133";
        System.out.println("In: "+value+" Out:"+node.convertValueSnmp2Netconf(value));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {


        test("int-to-boolean");

        test("int-to-boolean-2,3-true");

        test("int-to-boolean-3-true");

        test("int-to-boolean-3-false");

        test("int-to-boolean.dsa");

        test("map-1-d1-d2");

        test("divide-10");

    }

}
