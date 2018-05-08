package com.technologies.highstreet.netconf.server.control;

/**
 * Netconf Subsystem
 *
 * @author Julio Carlos Barrera
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import com.technologies.highstreet.netconf.server.basetypes.BehaviourContainer;
import com.technologies.highstreet.netconf.server.basetypes.Console;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;
import com.technologies.highstreet.netconf.server.networkelement.NetworkElement;

public class NetconfSubsystem implements Command, SessionAware {

    private static final Log    log                    = LogFactory.getLog(NetconfSubsystem.class);

    // subsystem fields
    protected ExitCallback        callback;
    protected InputStream         in;
    protected OutputStream        out;
    protected OutputStream        err;

    protected Environment        env;
    @SuppressWarnings("unused")
    private ServerSession        session;
    @SuppressWarnings("unused")
    private final BehaviourContainer  behaviourContainer;

    protected final MessageStore    messageStore;
    protected final NetworkElement ne;

    protected BaseNetconfController    netconfProcessor;
    protected final NetconfNotifyOriginator netconfNotifyExecutor;
    protected final Console console;

    public NetconfSubsystem(MessageStore messageStore, BehaviourContainer behaviourContainer, NetconfNotifyOriginator netconfNotifyExecutor, NetworkElement ne, Console console) {
        this.messageStore = messageStore;
        this.behaviourContainer = behaviourContainer;
        this.ne = ne;
        this.netconfNotifyExecutor = netconfNotifyExecutor;
        this.console = console;
    }

    public InputStream getInputStream() {
        return in;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public OutputStream getErrorStream() {
        return err;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment envParam) throws IOException {
        this.env = envParam;
        log.error("Wron Wrong wrong");

        netconfProcessor = new NetconfController(in, out, err, callback);
        /*// initialize Netconf processor
        if(this.ne instanceof Netconf2SNMPNetworkElement) {
            netconfProcessor = new Netconf2SNMPController(in,out,err,callback);
        } else {
            netconfProcessor = new NetconfController(in, out, err, callback);
        }*/

        log.info("Starting new client thread...");
        netconfProcessor.start(messageStore, ne, console);
        netconfNotifyExecutor.setNetconfNotifyExecutor(netconfProcessor);

    }

    @Override
    public void destroy() {
        netconfProcessor.destroy();
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = session;
    }

}
