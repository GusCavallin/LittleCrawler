package littlecrawler;

import utils.Log;

import java.net.URL;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Jul 25, 2009
 * Time: 12:45:42 PM
 */
public class LinkProcessor
    extends ContentProcessor
{
    public LinkProcessor(final URL url, final SearchThread searchThread, final Log log)
    {
        super(url, searchThread, log);
    }

    public void runProcess()
            throws IOException
    {
        processContent(readWriteContent(url, false, true), url);
    }

    public String toString()
    {
        return getClass().getSimpleName() + "_" + url;   // " - URL: "
    }
}
