/**
 * Class handling time stamps in NETCONF format.
 *
 * @author herbert.eiselt@highstreet-technologies.com
 */

package com.technologies.highstreet.netconf.server.types;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfTimeStamp {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfTimeStamp.class);
    private static TimeZone TIMEZONEUTC = TimeZone.getTimeZone("GMT");
    private static SimpleDateFormat dateFormatResult = doInit("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private static SimpleDateFormat dateFormatResultEventTime = doInit("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Static initialization
     * 2017-03-28T15:11:12Z
     */
    private static SimpleDateFormat doInit(String format) {
        LOG.debug("Init begin");
        SimpleDateFormat res;
        //dateFormatResult =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        res =new SimpleDateFormat(format); //Netconf
        res.setTimeZone(TIMEZONEUTC);
        LOG.debug("Init end");
        return res;
    }
    //------------------------------------
    //No public constructor
    private NetconfTimeStamp() {
    }
    /**
     * Get actual timestamp in
     * @return DateAndTime Date in this YANG Format
     */
    public static String getTimeStamp() {
        //Time in GMT
        return getRightFormattedDate(dateFormatResult,new Date().getTime());
    }
    public static String getEventTimeStamp() {
        //Time in GMT
        return getRightFormattedDate(dateFormatResultEventTime,new Date().getTime());
    }

     /**
     * Deliver format in a way that milliseconds are correct.
     * @param dateMillis Date as milliseconds in Java definition
     * @return String
     */
    private static String getRightFormattedDate(SimpleDateFormat format, long dateMillis ) {
        long tenthOfSeconds = dateMillis % 1000/100L; //Extract 100 milliseconds
        long base = dateMillis / 1000L * 1000L; //Cut milliseconds to 000
        Date newDate = new Date( base + tenthOfSeconds);
        return format.format(newDate);
    }
}
