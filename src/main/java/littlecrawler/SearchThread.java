package littlecrawler;

import threadpool.ThreadPool;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import utils.Log;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Oct 3, 2008
 * Time: 5:56:54 PM
 * Last Edition Date: Jan 6, 2009
 */
public class SearchThread
    extends Thread
{
    protected WebCrawlerApplet webCrawlerApplet;
    protected WebCrawlerProperties webCrawlerProperties;
    protected Log log;
    protected Log threadPoolLog;
    protected ThreadPool threadPool;
    // protected ThreadPool resourcesThreadPool; // TODO

    protected Vector<String> vectorToSearch;
    protected Vector<String> vectorSearched;
    protected Vector<String> vectorMatches;
    protected HashSet<String> alreadyDownloadedFiles;
    private boolean startingLocationIsAFile = false;

    protected List<String> includedHostsAndPaths;

    public SearchThread(WebCrawlerApplet webCrawlerApplet, WebCrawlerProperties webCrawlerProperties, Log log)
    {
        this.webCrawlerApplet = webCrawlerApplet;
        this.webCrawlerProperties = webCrawlerProperties;

        // initialize search data structures
        vectorToSearch = new Vector<String>();
        vectorSearched = new Vector<String>();
        vectorMatches = new Vector<String>();
        alreadyDownloadedFiles = new HashSet<String>();
        setName(toString());
        this.log = log;
    }

    public int getNbrPendingItems()
    {
        return vectorToSearch.size();
    }

    protected void validateStartingLocation()
            throws SearchAbortedException
    {
        try {
            new URL(webCrawlerProperties.getStartingLocation());
        } catch (MalformedURLException exc) {
            if (new File(webCrawlerProperties.getStartingLocation()).exists()) {
                startingLocationIsAFile = true;
            } else {
                stopApp("ERROR: Invalid Starting URL or file.", true);
            }
        }
    }

    public void setSearchWithinPath()
            throws SearchAbortedException
    {
        includedHostsAndPaths = webCrawlerProperties.getIncludedHostsAndPaths(); // This initializes the List.

        try {
            if (!startingLocationIsAFile) {
                includedHostsAndPaths.add(SearchUtils.getURLPath(webCrawlerProperties.getStartingLocation())); // Always allow searches within the initial path.
            }
            if ((!startingLocationIsAFile || startingLocationIsAFile && includedHostsAndPaths.size() == 0) && !webCrawlerProperties.isDefaultHostAndPathEmpty()) {
                includedHostsAndPaths.add(SearchUtils.getURLPath(webCrawlerProperties.getDefaultHostAndPath()));
            }
        } catch (MalformedURLException exc) {
            if (startingLocationIsAFile) {
                if (includedHostsAndPaths.size() == 0) {
                    stopApp("ERROR: Invalid 'Search within Path' URL. When the Starting Location is a file 'Search within Path' cannot be empty.", true);
                }
            } else {
                stopApp("ERROR: Invalid 'Search within Path' URL.", true);
            }
        }
        log.add("--> includedHostsAndPaths: " + includedHostsAndPaths);
    }

    public void initVectors()
    {
        // initialize search data structures
        vectorToSearch.removeAllElements();
        vectorSearched.removeAllElements();
        vectorMatches.removeAllElements();

        // if (!SearchUtils.isHttpOrHttpsLocation(startingLocation)) {  // I use isAFile, instead
        if (startingLocationIsAFile) {
            executeFileProcessorThread(webCrawlerProperties.getStartingLocation());
        } else {
            vectorToSearch.addElement(webCrawlerProperties.getStartingLocation());
            vectorMatches.addElement(webCrawlerProperties.getStartingLocation());
        }
    }

    protected void init()
            throws SearchAbortedException
    {
        validateStartingLocation();
        setSearchWithinPath();

        threadPoolLog = new Log("ThreadPool_", webCrawlerProperties.getDestinationFolder(), true);
        threadPool = new ThreadPool(webCrawlerProperties.getNrThreads(), webCrawlerProperties.getWorkerTimeout(), threadPoolLog);

        initVectors();
    }

    protected void executeFileProcessorThread(final String startingLocation)
    {
        executeThreadWithoutTimeout(new FileProcessor(startingLocation, this, log));
    }

    public void run()
    {
        try {
            init();
        } catch (SearchAbortedException exc) {
            log.add(this, "ERROR: Initialization Failed. Search Aborted", exc);
            return;
        }

        String msg = "Web Download (" + VERSION + ") ";
        if (webCrawlerProperties.isSearchWithinStartingLocationOnly()) {
            msg += "withing location '" + webCrawlerProperties.getStartingLocation() + "'";
        } else {
            msg += "within path/s " + includedHostsAndPaths;
        }
        msg += " starting...";
        webCrawlerApplet.setStatus(msg);
        log.add(this, webCrawlerProperties.toString());

        waitForFirstLink();

        while (!webCrawlerApplet.isStopping() && !isSearchComplete()) {
            // get the first element from the list to be searched list
            URL url = getNextElement();

            // can only search "http:" and "https:" protocol URLs.
            if (url == null || !SearchUtils.isHttpOrHttpsURL(url)) break;

            executeLinkProcessorThread(url);

            waitForMoreLinks();
        }

        logPendingItems();
        stopApp("Done.");
    }

    private boolean isSearchComplete()
    {
        if (webCrawlerProperties.isSearchLimitEnabled() && vectorSearched.size() >= webCrawlerProperties.getSearchLimit()) {
            webCrawlerApplet.setStatus("Reached search limit of " + webCrawlerProperties.getSearchLimit());
            return true;
        }
        return vectorToSearch.size() == 0 && vectorMatches.size() == vectorSearched.size();
    }

    private void waitForFirstLink()
    {
        if ((vectorToSearch.size() == 0)) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void waitForMoreLinks()
    {
        while(vectorToSearch.size() == 0 && !isSearchComplete()) {
            synchronized (this) {
                try {
                    wait();
                    // System.out.println("Leaving wait: vectorToSearch.size()="+vectorToSearch.size() + ", vectorMatches.size()=" + vectorMatches.size() + ", vectorSearched.size()=" + vectorSearched.size() + ", isSearchComplete()=" + isSearchComplete()); // Debug
                } catch (InterruptedException e) {
                    log.add(this, "ERROR", e);
                }
            }
        }
        if (isSearchComplete()) {
            do {
                // give the workers a chance to finish (and probably find additional links)
                try {
                    Thread.sleep(250);
                    // System.out.println("Leaving sleep: vectorToSearch.size()="+vectorToSearch.size() + ", vectorMatches.size()=" + vectorMatches.size() + ", vectorSearched.size()=" + vectorSearched.size() + ", isSearchComplete()=" + isSearchComplete()); // Debug
                } catch ( InterruptedException x ) {
                    log.add(this, "ERROR", x);
                }
            } while (threadPool.isBusy());
        }
    }

    protected void executeLinkProcessorThread(final URL url)
    {
        executeThread(new LinkProcessor(url, this, log));
    }

    /**
     * Blocks until a Thread becomes available!
     */
    protected void executeThread(ContentProcessor thread)
    {
        try {
            threadPool.execute(thread);
        } catch ( InterruptedException ix ) {
            log.add(this, "WARNING: Excecute has been interrupted", ix);
        }
    }

    /**
     * Blocks until a Thread becomes available!
     */
    protected void executeThreadWithoutTimeout(ContentProcessor thread)
    {
        try {
            threadPool.execute(thread, NO_TIMEOUT);
        } catch ( InterruptedException ix ) {
            log.add(this, "WARNING: Excecute has been interrupted", ix);
        }
    }

    protected void stopApp(String status)
    {
        if (threadPool != null) threadPool.stopRequestAllWorkers();
        webCrawlerApplet.setStatus(status);
        webCrawlerApplet.setStopping();
        log.close();
        if (threadPoolLog != null) threadPoolLog.close();
    }

    protected void stopApp(String status, boolean error)
            throws SearchAbortedException
    {
        stopApp(status);
        if (error) {
            throw new SearchAbortedException(status);
        }
    }

    protected void logPendingItems()
    {
        if (!vectorToSearch.isEmpty()) {
            log.add(this, "List of pending search items:");
            for(String item:vectorToSearch) {
                log.add(this, "    " + item);
            }
            log.add(this, "End of list of pending search items.");
        }
    }

    public boolean ignorePage(String strLink)
    {
        for (String includedHostsAndPath : includedHostsAndPaths) {
            if (strLink.contains(includedHostsAndPath)) {
                return false; // If the link belongs to a includedHostsAndPaths, DO NOT ignore.
            }
        }
        return true;
    }

    protected URL getNextElement()
    {
        String strURL = vectorToSearch.elementAt(0);

        webCrawlerApplet.setStatus("Searching " + strURL);

        URL url;
        try {
            url = new URL(strURL);
        } catch (MalformedURLException e) {
            webCrawlerApplet.setStatus("ERROR: Invalid URL: " + strURL);
            return null;
        }

        // mark the URL as searched (we want this one way or the other)
        vectorToSearch.removeElementAt(0);
        vectorSearched.addElement(strURL);

        return url;
    }

    public synchronized void addElementToSearch(String strLink)
    {
        if (!vectorSearched.contains(strLink) && !vectorToSearch.contains(strLink)) {
            vectorToSearch.addElement(strLink);
        }

        // Add link to the results list
        // unless we have already seen it
        if (!vectorMatches.contains(strLink)) {
            log.add(this, "  + Found: " + strLink);
            webCrawlerApplet.addMatch(strLink);
            vectorMatches.addElement(strLink);
//                    numberFound++;
//                    if (numberFound >= FOUND_LIMIT) break;
        }

        notify(); // Notifies 'this' to process the element just added.
    }

    public String getStatus()
    {
        // Flush log
        log.flush();
        // Write Status String
        StringBuilder sb = new StringBuilder();
        sb.append("Search Thread " + VERSION + " Status: ");
        if (isStopping()) {
            sb.append("Stopping");
        } else {
            sb.append("Running");
        }
        sb.append('\n');
        sb.append('\n');
        sb.append("Found links: ");
        sb.append(vectorMatches.size());
        sb.append(", Searched links: ");
        sb.append(vectorSearched.size());
        sb.append(", Pending links: ");
        sb.append(vectorToSearch.size());
        sb.append('\n');
        sb.append("Is search complete: ");
        sb.append(isSearchComplete());
        sb.append('\n');
        sb.append('\n');
        sb.append(threadPool.getStatus());

        return sb.toString();
    }

    public boolean isStopping()
    {
        return webCrawlerApplet.isStopping();
    }

    public static class SearchAbortedException
        extends Exception
    {
        public SearchAbortedException(String msg)
        {
            super(msg);
        }
    }

    public String toString()
    {
        return getClass().getSimpleName();
    }

    private static final int NO_TIMEOUT = -1;
    protected static final String VERSION = "v5.0"; // Date: 2026/11/05
    /* VERSION LOG:
     * * Changes for Version 4:
     * In this version I addressed the issue with Workers trying to execute other workers (which was creating a deadlock).
     * Additionally, run() method was implemented in ContentProcessor and runProcess() was left as abstract.
     * This run() method ensures that jobFinished() is called to notify the waiting SearchThread.
     * Additionally, ObjectFIFO was made Generic and renamed to FIFOQueue.
     * * Changes for Version 4.1:
     * Thread names (Workers and SearchThread) and log changes.
     * * Changes for Version 4.5:
     * The tags to start a link do not contain spaces now. That is now we have: "<a" (it used to be "<a "). This had problems with <a\n"
     * * Changes for Version 4.7:
     * The elements of the list of IncludedHostsAndPaths must not end with '/' (so, I remove it when adding the elements to the list)
     * * Changes for Version 4.9:
     * Properties print fixed.
     * Search limit property added.
     * Already downloaded files print one log message only. Subsequent messages are ignored.
     * * Changes for Version 5.0:
     * Prepared for publication. From this version on, the changes are listed in CHANGELOG.md.
     */
}
