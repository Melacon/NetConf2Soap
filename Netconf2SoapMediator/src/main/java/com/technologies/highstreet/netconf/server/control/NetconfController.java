package com.technologies.highstreet.netconf.server.control;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.ExitCallback;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.basetypes.UserCommand;
import com.technologies.highstreet.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.netconf.server.streamprocessing.NetconfMessageProcessorThread;
import com.technologies.highstreet.netconf.server.streamprocessing.NetconfStreamCodecThread;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatus;
import com.technologies.highstreet.netconf.server.types.NetconfSessionStatusHolder;
import net.i2cat.netconf.messageQueue.MessageQueue;

/**
 * Netconf controller. Startup the netconf subsystem. Establish input an output processing.
 *
 * @author Julio Carlos Barrera
 *
 */
public class NetconfController implements BaseNetconfController {


    private static final Log log  = LogFactory.getLog(NetconfController.class);

    // status fields
    protected final NetconfSessionStatusHolder status;
    // XML parser & handler
    protected final MessageQueue                messageQueue;

    // message processor thread
    protected Thread                          messageProcessorThread;


    protected final NetconfStreamCodecThread    ioCodec;
    protected Thread ioThread = null;

    public NetconfController(InputStream in, OutputStream out, OutputStream err, ExitCallback callback) {

        this.messageQueue = new MessageQueue();
        this.status = new NetconfSessionStatusHolder();
        this.status.change(NetconfSessionStatus.INIT);
        this.ioCodec = new NetconfStreamCodecThread(in, out, new MyExitCallback(callback), status, messageQueue);

    }


    /*--------------------------------
     * Interface functions
     */


    @Override
    public void start(MessageStore messageStore, NetworkElement ne, Console console) {
        // start message processor
        startMessageProcessor(messageStore, ne, console);
        // wait for message processor to continue
        ioThread = ioCodec.start();
    }

    @Override
    public void destroy() {
        waitAndInterruptThreads();
        try {
            ioThread.join(2000);
        } catch (InterruptedException e) {
            log.warn("Error joining Client thread" + e.getMessage());
        }
        ioThread.interrupt();
        log.info("Netconf Subsystem destroyed");
    }

    @Override
    public void notify(String command) {
        log.info("Notification");
        UserCommand notification = new UserCommand(command);
        messageQueue.put(notification);
    }

    /*--------------------------------
     * Private functions and classes
     */

    public void startMessageProcessor(MessageStore messageStore, NetworkElement ne, Console console) {
        log.info("Creating new message processor...");
        messageProcessorThread = new NetconfMessageProcessorThread("Message processor", status, ioCodec, messageQueue, messageStore, ne, console);
        messageProcessorThread.start();
        log.info("Message processor started.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.warn("Error waiting for message processor thread.", e);
        }
    }

    private void waitAndInterruptThreads() {
        // wait for thread
        try {
            messageProcessorThread.join(2000);
        } catch (InterruptedException e) {
            log.error("Error waiting for thread end", e);
        }

        // kill thread if it don't finish naturally
        if (messageProcessorThread != null && messageProcessorThread.isAlive()) {
            log.info("Killing message processor thread");
            messageProcessorThread.interrupt();
        }
    }

    private class MyExitCallback implements ExitCallback {

        private final ExitCallback callback;
        MyExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onExit(int exitValue) {
            waitAndInterruptThreads();
            callback.onExit(exitValue);
        }

        @Override
        public void onExit(int exitValue, String exitMessage) {
            waitAndInterruptThreads();
            callback.onExit(exitValue, exitMessage);
        }

    }

}
