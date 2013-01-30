//////////////////////////////////////////////////////////////////////////
//com.enrogen.logger
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////

package com.enrogen.logger;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import javax.swing.JTextArea;

public class WindowHandler extends StreamHandler {
    final JTextArea textArea;

    public WindowHandler(final JTextArea jta) {
        textArea = jta;

        this.setFormatter(new Formatter() {
                public String format(LogRecord rec) {
                    StringBuffer buf = new StringBuffer(1000);
                    buf.append(new java.util.Date());
                    buf.append(' ');
                    buf.append(rec.getLevel());
                    buf.append(' ');
                    buf.append(formatMessage(rec));
                    buf.append('\n');
                    return buf.toString();
                }
            }) ;


        setOutputStream(new OutputStream() {
            public void write(int b) {
            } // not called

            public void write(byte[] b, int off, int len) {
                textArea.append(new String(b, off, len));
            }
        });
    }

    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

}
