/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.lth.cs.palcom.logging;

import java.io.PrintStream;
import se.lth.cs.palcom.logging.AbstractSink;
import se.lth.cs.palcom.logging.LogSink;
import se.lth.cs.palcom.logging.Logger;

/**
 *
 * @author mattias
 */
public class DualFileLogger implements LogSink {
    protected PrintStream messages, verbose;
    protected int horizon;

    public DualFileLogger(PrintStream messages, PrintStream verbose, int horizon) {
        this.messages = messages;
        this.verbose = verbose;
        this.horizon = horizon;
    }

    public DualFileLogger(PrintStream messages, PrintStream verbose) {
        this(messages, verbose, Logger.LEVEL_DEBUG);
    }
    public void log(int component, Object componentID, int level,
    		Object[] messageParts) {
        PrintStream out = level < horizon ? verbose : messages;
        AbstractSink.formatStatic(out, component, componentID, level, messageParts);
        out.println();
    }

    public boolean accept(int level) {
        return true;
    }

    /** Log all messages on and above the horizon to messages file, and below
     * the horizon to verbose file
     * @param level Horizon
     */
    public void setHorizon(int level) {
        this.horizon = level;
    }

	public void setAccept(int level, boolean accept) {
	}

	public void changeLevel(int lowestLevel) {
	}

	public void close() {
		messages.close();
		verbose.close();
	}

}
