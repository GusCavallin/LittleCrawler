package utils;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Oct 3, 2008
 * Time: 5:45:20 PM
 * Last Edition Date: Jan 6, 2009
 */
public class Log 
{
    private static final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
    private FileWriter logWriter;
    private boolean paranoid;

    public Log(String filePrefix)
    {
        this(filePrefix, null);
    }

    public Log(String filePrefix, boolean paranoid)
    {
        this(filePrefix, null, paranoid);
    }

    public Log(String filePrefix, File destinationFolder)
    {
        this(filePrefix, destinationFolder, false);
    }

    public Log(String filePrefix, File destinationFolder, boolean paranoid)
    {
        this.paranoid = paranoid;
        if (filePrefix == null) {
            add("WARNING: Log file prefix is null. Logging to standar output.");
        } else {
            if (destinationFolder == null) {
                destinationFolder = new File(System.getProperty("user.dir"));
            }
            File logFile = null;
            try {
                destinationFolder.mkdirs(); // First we create the destination folder and then the temp file (if the folder does not exist, createTempFile fails)
                logFile = File.createTempFile(filePrefix,".log", destinationFolder);

                logWriter = new FileWriter(logFile);
            } catch (IOException e) {
                add("WARNING: Error initializing log file ('" + logFile + "'). Logging to standar output. Exception: " + e);
            }
        }
    }

    public synchronized void flush()
    {
        if (logWriter != null) {
            try {
                logWriter.flush();
            } catch (IOException e) {
                System.out.println("WARNING: Error flushing the log file.");
            }
        }
    }

    public synchronized void add(String str, Throwable t)
    {
        add(null, str, t);
    }

    public synchronized void add(Runnable thread, String str, Throwable t)
    {
        String s = str + ".\n" + getStackTrace(t);
        add(thread, s);
    }

    private String getStackTrace(Throwable t)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Exception: ");
        sb.append(t.toString());
        for (StackTraceElement trace : t.getStackTrace()) {
            sb.append("\n\tat ");
            sb.append(trace);
        }
        return sb.toString();
    }

    public synchronized void addDebug(Runnable thread, String str)
    {
        // TODO: Not implemented.
        // add(thread, str);
    }

    public synchronized void add(String str)
    {
        add(null, str);
    }

    public synchronized void add(Runnable thread, String str)
    {
        StringBuilder sb = new StringBuilder();
        String threadStr = "";
        if (thread != null) {
            threadStr = thread.toString();
//            threadStr = Thread.currentThread().getName();
        }
        sb.append(sdFormat.format(new Date()));
        sb.append(SEPARATOR);
        sb.append(Thread.currentThread().getName());
        if (!threadStr.isEmpty()) {
            sb.append(':');
            sb.append(threadStr);
        }
        sb.append(SEPARATOR);
        sb.append(str);

        if (logWriter == null) {
            System.out.println(sb.toString());
        } else {
            try {
                sb.append('\n');
                logWriter.write(sb.toString());
                if (paranoid) {
                    logWriter.flush();
                }
            } catch (IOException e) {
                System.out.println("WARNING: Error writing to the log file. Logging to standar output. Exception: " + e);
                System.out.println(str);
            }
        }
    }

    public synchronized void close()
    {
        if (logWriter != null) {
            try {
                logWriter.flush();
                logWriter.close();
                logWriter = null;
            } catch (IOException e) {
                System.out.println("WARNING: Error closing the log file. Exception: " + e);
            }
        }
    }

    private static final char SEPARATOR = '|';
}
