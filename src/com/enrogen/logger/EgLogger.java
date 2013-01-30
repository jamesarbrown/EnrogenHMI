//////////////////////////////////////////////////////////////////////////
//com.enrogen.logger.EgLogger
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////
package com.enrogen.logger;

import com.enrogen.logger.EgLogger;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class EgLogger {

    private FileHandler fh1;
    private Handler wh1;
    private Logger mainLogger;

    public EgLogger() {
    }

    public void setFileHandler(String filename, int logsize, int logfiles) {
        EgFileHandler egfh1 = new EgFileHandler(filename, logsize, logfiles);
        fh1 = egfh1.getFileHandler();
    }

    public void setWindowHandler(JTextArea jta) {
        wh1 = new WindowHandler(jta);
    }

    public Logger getLogger() {
        return mainLogger;
    }

    public void initEgLogger() {
        // initialize logging to go to rolling log file
        LogManager logManager = LogManager.getLogManager();
        logManager.reset();

        //Add the logger handler to all loggers
        Logger.getLogger("").addHandler(fh1);
        Logger.getLogger("").addHandler(wh1);

        //Preserve stdout and stderr
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;

        //Now bind to the logger
        LoggingOutputStream los;

        mainLogger = Logger.getLogger("stdout");
        los = new LoggingOutputStream(mainLogger, StdOutErrLevel.STDOUT);
        System.setOut(new PrintStream(los, true));

        mainLogger = Logger.getLogger("stderr");
        los = new LoggingOutputStream(mainLogger, StdOutErrLevel.STDERR);
        System.setErr(new PrintStream(los, true));
    }
}
