/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author herbert
 *
 */
public class TestPattern {


    public static void main(String[] args) {


    	Pattern p = Pattern.compile("if-(((\\d[0-9]*),?))+-(\\w+)-(\\w+)$");

    	Matcher m = p.matcher("if-1,2,3-true-false");

    	System.out.println("Matches: "+m.matches());
    	for (int t= 1; t <= m.groupCount(); t++) {
        	System.out.println("Matches: "+m.group(t));
    	}


    }

}
