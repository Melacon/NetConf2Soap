/**
 *
 */
package com.technologies.highstreet.netconf.server.networkelement;

import com.technologies.highstreet.netconf.server.types.NetconfTimeStamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author herbert
 *
 */
public class NetconfMessageAssembler {

    private static final Log LOG = LogFactory.getLog(NetworkElement.class);

    private static TimeZone TIMEZONEUTC = TimeZone.getTimeZone("GMT");
   // private static SimpleDateFormat dateFormatNotification = doInit("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private static SimpleDateFormat dateFormatNotification = doInit("yyyy-MM-dd'T'HH:mm:ss.0'Z'");

    private static final String TAG_PTPID1 = "$PTPID(";
    private static final String TAG_PTPID2 = ")";
    private static final String TAG_TIME = "$TIME";
    private static final String TAG_COUNTER = "$COUNTER";

    private static int counter = 0;  //Corrosponds to TAG_COUNTER



    protected String replaceAndWash( String xmlMessage ) {
        xmlMessage = xmlMessage
                .replace(TAG_TIME, NetconfTimeStamp.getTimeStamp() )
                .replace(TAG_COUNTER, String.valueOf(counter++ & 0xfffffff) );

        xmlMessage = substitudePtpId(xmlMessage);

        return xmlMessage;
    }

    protected List<String> replaceAndWash( List<String> xmlMessages ) {
        List<String> res = new ArrayList<>();
        for (String xmlMsg : xmlMessages) {
            res.add(replaceAndWash(xmlMsg));
        }
        return res;
    }

    /**
     * Calculate BASE64 String from 8 chars
     * @param xml
     * @return Base64 string
     */
    private String substitudePtpId(String xml) {
        int idx, idx2;
        String substringCode, toExchange, base64Result;

        int protect = 20;
        while ((idx = xml.indexOf(TAG_PTPID1)) > 0 && protect-- > 0) {
            idx2 = xml.indexOf(TAG_PTPID2, idx + TAG_PTPID1.length());
            substringCode = xml.substring(idx + TAG_PTPID1.length(), idx2);
            base64Result = Base64.getEncoder().encodeToString(substringCode.getBytes());

            toExchange = TAG_PTPID1+substringCode+TAG_PTPID2;
            xml = xml.replace(toExchange, base64Result);
        }
        if (protect <= 0) {
            LOG.warn("Problem during conversion of "+TAG_PTPID1+TAG_PTPID2);
        }

        return xml;
    }

    public String assembleRpcNotification(String xmlSubTree ) {
        StringBuffer res = new StringBuffer();
        res.append("<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n");
        //res.append("<eventTime>2011-11-11T11:11:11Z</eventTime>\n");
        //res.append("<eventTime>"+dateFormatNotification.format(new Date())+"</eventTime>\n");
        res.append("<eventTime>"+NetconfTimeStamp.getEventTimeStamp()+"</eventTime>\n");
         res.append(xmlSubTree);
        res.append("</notification>\n");
        return replaceAndWash(res.toString());

    }

    /**
     * Indicating an error
     * @param errorMsg with error text
     * @return StringBuffer with error message
     */
    StringBuffer assembleRpcReplyError(String errorTag, String errorMsg) {
        StringBuffer res = new StringBuffer();
        res.append("   <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
        "       <rpc-error>\n" +
        "         <error-type>rpc</error-type>\n" +
        "         <error-tag>"+errorTag+"</error-tag>\n" +
        "         <error-severity>error</error-severity>\n" +
        "         <error-message xml:lang=\"en\">\n" +
        errorMsg + "\n" +
        "         </error-message>\n" +
        "       </rpc-error>\n" +
        "     </rpc-reply>\n");
        return res;

    }

    /*---------------------------------------------------
     * Base functions for message creation
     */

    protected StringBuffer appendXmlMessageRpcReplyOpen( StringBuffer res, String id ) {
        res.append("<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" last-modified=\"2017-03-07T19:32:31Z\" message-id=\"");
        res.append(id);
        res.append("\">\n");
        return res;
    }

    protected StringBuffer appendXmlMessageRpcReplyClose( StringBuffer res ) {
        res.append("</rpc-reply>");
        return res;
    }

    protected StringBuffer appendXmlTagOpen( StringBuffer res, String name, String nameSpace ) {
        res.append("<");
        res.append(name);
        res.append(" xmlns=\"");
        res.append(nameSpace);
        res.append("\">\n");
        return res;
    }

    protected StringBuffer appendXmlTagOpen( StringBuffer res, String name ) {
        res.append("<");
        res.append(name);
        res.append(">");
        return res;
    }

    protected StringBuffer appendXmlTagClose( StringBuffer res, String name ) {
        res.append("</");
        res.append(name);
        res.append(">\n");
        return res;
    }

    protected StringBuffer appendXml( StringBuffer res, String name, String value ) {
        appendXmlTagOpen(res, name);
        res.append(value);
        appendXmlTagClose(res, name);
        return res;
    }

    /**
     * Static initialization
     * 2017-03-28T15:11:12Z
     */
    private static SimpleDateFormat doInit(String format) {
        SimpleDateFormat res;
        res =new SimpleDateFormat(format);
        res.setTimeZone(TIMEZONEUTC);
        return res;
    }

    /*---------------------------------------------------
     * Message Examples
     */

    /* ------------------
     * DEBUG Help from ODL/OPENYUMA log as example for problem notification
        <notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
        <eventTime>2017-03-28T15:11:12Z</eventTime>
        <ProblemNotification xmlns="uri:onf:MicrowaveModel-Notifications">
          <counter>9648</counter>
          <timeStamp>20170328171112.1Z</timeStamp>
          <objectIdRef>LP-MWPS-ifIndex1</objectIdRef>
          <problem>signalIsLost</problem>
          <severity>critical</severity>
        </ProblemNotification>
      </notification>
    */

    /*-----------------------------------------------------------------------
     * Functions to create WRITE message content to deliver answers back to the SDN controller.
     */

    /* DEBUG Help with example of ODL netconf write message to mountpoint sim34tdm, creating something new.
     * Tag fingerprint: rpc edit-config target running default-operation config
        2017-04-24 12:56:37,199 | TRACE | qtp87320730-9024 | NetconfDeviceCommunicator        | 214 - org.opendaylight.netconf.sal-netconf-connector - 1.4.1.Boron-SR1 |
        RemoteDevice{sim34tdm}: Sending message
        <rpc message-id="m-56" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
        <edit-config>
        <target>
        <running/>
        </target>
        <default-operation>none</default-operation>
        <config>
            <forwarding-construct xmlns="urn:onf:params:xml:ns:yang:core-model" xmlns:a="urn:ietf:params:xml:ns:netconf:base:1.0" a:operation="replace">
            <uuid>FC1</uuid>
            <forwarding-direction>bidirectional</forwarding-direction>
            <fc-port>
            <uuid>FCPort2</uuid>
            <fc-port-direction>bidirectional</fc-port-direction>
            </fc-port>
            <fc-port>
            <uuid>FCPort1</uuid>
            <fc-port-direction>bidirectional</fc-port-direction>
            </fc-port>
            <layer-protocol-name>ETH</layer-protocol-name>
            <is-protection-lock-out>true</is-protection-lock-out>
            </forwarding-construct>
        </config>
        </edit-config>
        </rpc>

    * Queued RPC Message:NetconfIncommingMessageRepresentation RpcIn [messageType=rpc,
    * tags=NetconfTagList [tags=[
    *     NetconfTag [namespace=urn:ietf:params:xml:ns:netconf:base:1.0, name=edit-config, values=[]],
    *     NetconfTag [namespace=urn:ietf:params:xml:ns:netconf:base:1.0, name=target, values=[]],
    *     NetconfTag [namespace=urn:ietf:params:xml:ns:netconf:base:1.0, name=running, values=[]],
    *     NetconfTag [namespace=urn:ietf:params:xml:ns:netconf:base:1.0, name=default-operation, values=[none]],
    *     NetconfTag [namespace=urn:ietf:params:xml:ns:netconf:base:1.0, name=config, values=[]],
    *     NetconfTag [namespace=urn:onf:params:xml:ns:yang:microwave-model, name=mw-ethernet-container-pac, values=[]],
    *     NetconfTag [namespace=urn:onf:params:xml:ns:yang:microwave-model, name=layer-protocol, values=[ETHC1]],
    *     NetconfTag [namespace=urn:onf:params:xml:ns:yang:microwave-model, name=ethernet-container-configuration, values=[]],
    *     NetconfTag [namespace=urn:onf:params:xml:ns:yang:microwave-model, name=container-id, values=[ID17]]]],
    *
    * messageUri=urn:ietf:params:xml:ns:netconf:base:1.0,
    * content={edit-config.target.running.default-operation=none, edit-config.target.running.default-operation.config.mw-ethernet-container-pac.layer-protocol.ethernet-container-configuration.container-id=ID17, edit-config.target.running.default-operation.config.mw-ethernet-container-pac.layer-protocol=ETHC1},
    *
    *     getMessageId()=m-65, getCtx()=null, xmlSourceMessage='<?xml version="1.0" encoding="UTF-8" standalone="no"?>
    */

    /*------------------
     * DEBUG Help from ODL/OPENYUMA log as example for attribute change notification
       <notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
          <eventTime>2017-03-28T15:11:12Z</eventTime>
          <AttributeValueChangedNotification xmlns="uri:onf:MicrowaveModel-Notifications">
            <counter>9648</counter>
            <timeStamp>20170328171112.1Z</timeStamp>
            <objectIdRef>LP-MWPS-ifIndex1</objectIdRef>
            <attributeName>airInterfaceStatus/modulationCur</attributeName>
            <newValue>128</newValue>
          </AttributeValueChangedNotification>
        </notification>
     */

}
