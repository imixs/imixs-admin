package org.imixs.application.admin;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Named;

/**
 * The LogController stores a message log
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class LogController implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int LOG_INFO = 1;
    public static final int LOG_WARNING = 2;

    private static Logger logger = Logger.getLogger(LogController.class.getName());
    String pattern = " HH:mm:ss.SSSZ";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    private List<String> logEntries;

    public void reset() {
        logEntries = new ArrayList<String>();
    }

    public void info(String message) {
        add(LOG_INFO, message);
    }

    public void warning(String message) {
        add(LOG_WARNING, message);
    }

    /**
     * Logs a new message to the message log
     *
     * @param message
     */
    private void add(int type, String message) {
        if (logEntries == null) {
            reset();
        }

        String entry = simpleDateFormat.format(new Date()) + " ";
        if (type == LOG_WARNING) {
            entry = entry + "[WARNING] ";
            logger.warning(message);
        } else {
            entry = entry + "[INFO]    ";
            logger.info(message);

        }
        entry = entry + message;
        logEntries.add(entry);
    }

    public List<String> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<String> logEntries) {
        this.logEntries = logEntries;
    }

}