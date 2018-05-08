/**
 * Interface for a class that can send out Strings with Netconf/XML content.
 *
 * @author herbert.eiselt@highstreet-technologies.com
 */

package com.technologies.highstreet.netconf.server.types;

import java.io.IOException;

public interface NetconfSender {

    /**
     * Send xml Message like example below
     *  <rpc message-id="m-56" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
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
     * @param xmlMessage (notNull) with Message content to send.
     * @throws IOException on problems with output stream
     */
    public void send(String xmlMessage) throws IOException;

}
