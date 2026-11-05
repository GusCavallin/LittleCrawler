package littlecrawler;

import utils.Log;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Jul 25, 2009
 * Time: 1:06:15 PM
 */
public class FileProcessor
    extends ContentProcessor
{
    private String startingLocation;

    public FileProcessor(final String startingLocation, final SearchThread searchThread, final Log log)
    {
        super(null, searchThread, log);
        this.startingLocation = startingLocation;
    }

    public void runProcess()
            throws IOException
    {
        // Do nothing. This class overwrites run()!!!
    }

    public void run()
    {
        File file = new File(startingLocation);
        log.add(this, "==> Reading from file: " + file);

        InputStream fileStream;
        try {
            fileStream = new FileInputStream(file);
//            addResource(startingLocation, fileStream); // Not necessary.
        } catch (FileNotFoundException exc) {
            searchThread.stopApp("ERROR: Couldn't open file: " + file);
            return;
        }

        URL url;
        String strURL = searchThread.webCrawlerProperties.getDefaultHostAndPath(); // WORKAROUND: I passing the host and port as urlBeingSearched, just to pass something...
        try {
            url = new URL(strURL);
        } catch (MalformedURLException e) {
            searchThread.stopApp("ERROR: Invalid Default Host and Path URL: " + strURL);
            return;
        }

        try {
            processContent(readStream(fileStream), url);
        } catch (IOException e) {
            searchThread.stopApp("ERROR: Couldn't read file: " + file);
        } finally {
            try {
                fileStream.close();
            } catch (IOException e) {
                searchThread.stopApp("ERROR closing file: " + file);
            }
            jobFinished(); // MUST Notify other threads!
        }
    }

    private String readStream(InputStream fileStream)
            throws IOException
    {
        return readWriteStream(fileStream, null, true);
    }

    public String toString()
    {
        return getClass().getSimpleName() + ": " + searchThread.webCrawlerProperties.getStartingLocation();
    }
}
