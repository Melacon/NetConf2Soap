/**
 *
 */
package com.technologies.highstreet.netconf.server.streamprocessing;

import java.io.IOException;

/**
 * @author herbert
 *
 */
public interface ProcessMessage {

    public void doMessageProcessing(NetconfIncommingMessageRepresentation receivedMessage) throws IOException;

}
