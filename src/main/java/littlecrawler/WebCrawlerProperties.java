package littlecrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: gus
 * Date: Oct 12, 2010
 * Time: 6:39:08 PM
 */
public class WebCrawlerProperties
{
    private String startingLocation;
    private String defaultHostAndPath;
    private File destinationFolder;
    private boolean searchWithinStartingLocationOnly;
    private boolean enableSearchLimit;
    private int searchLimit;
    private boolean proxySet;
    private File includedHostsAndPathsFile;
    private List<String> includedHostsAndPaths;
    private int nrThreads;
    private int workerTimeout;

    public WebCrawlerProperties()
            throws IOException
    {
        this(new File(DEFAULT_CONFIG_FILE));
    }

    /**
     * Reads the properties from a file. A relative path is resolved against the working directory.
     * When the file does not exist, every property takes its default value.
     */
    public WebCrawlerProperties(File configFile)
            throws IOException
    {
        Properties properties = new Properties();
        if (configFile.isFile()) {
            FileInputStream in = new FileInputStream(configFile);
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        } else {
            System.out.println("WARNING: Configuration file '" + configFile.getAbsolutePath() + "' not found. Using the default values.");
        }

        // Default values are set here
        destinationFolder = new File(properties.getProperty(DESTINATION_FOLDER_PROP, System.getProperty("user.dir")));
        startingLocation = properties.getProperty(STARTING_LOCATION_PROP, "http://");
        defaultHostAndPath = properties.getProperty(DEFAULT_HOST_PATH_PROP, "");
        includedHostsAndPathsFile = new File(properties.getProperty(INCLUDED_HOSTS_AND_PATHS_FILE, ""));
        searchWithinStartingLocationOnly = Boolean.valueOf(properties.getProperty(WITHIN_STARTING_LOCATION_PROP, "false"));
        enableSearchLimit = Boolean.valueOf(properties.getProperty(ENABLE_SEARCH_LIMIT, "false"));
        searchLimit = Integer.parseInt(properties.getProperty(SEARCH_LIMIT, DEFAULT_SEARCH_LIMIT)); 
        nrThreads = Integer.parseInt(properties.getProperty(NR_THREADS, DEFAULT_NR_THREADS));
        workerTimeout = Integer.parseInt(properties.getProperty(WORKER_TIMEOUT, DEFAULT_WORKER_TIMEOUT));
        proxySet = Boolean.valueOf(properties.getProperty(PROXY_SET_PROP, "false"));

        initIncludedHostsAndPaths();

        if (proxySet) {
            // Sets proxy here
            Properties props = new Properties(System.getProperties());
            props.put(PROXY_SET_PROP, properties.getProperty(PROXY_SET_PROP));
            props.put(PROXY_HOST_PROP, properties.getProperty(PROXY_HOST_PROP));
            props.put(PROXY_PORT_PROP, properties.getProperty(PROXY_PORT_PROP));

            Properties newprops = new Properties(props);
            System.setProperties(newprops);
        }
    }

    public void initIncludedHostsAndPaths()
            throws IOException
    {
        includedHostsAndPaths = new ArrayList<String>();

        if (includedHostsAndPathsFile.isFile()) { // Not exists(): since JDK 24 the empty path (property not set) exists, as the working directory.
            BufferedReader br = new BufferedReader(new FileReader(includedHostsAndPathsFile));

            String line;
            while ((line = br.readLine()) != null) {
                line = SearchUtils.removeLastSeparator(line);
                includedHostsAndPaths.add(line);
            }
        }
    }

    public List<String> getIncludedHostsAndPaths()
    {
        return includedHostsAndPaths;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation.trim();
    }

    public boolean isDefaultHostAndPathEmpty()
    {
        return defaultHostAndPath.equals("");
    }

    public String getDefaultHostAndPath() {
        return defaultHostAndPath;
    }

    public void setDefaultHostAndPath(String defaultHostAndPath) {
        this.defaultHostAndPath = defaultHostAndPath.trim();
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    public String getDestinationFolderString() {
        return destinationFolder.getAbsolutePath();
    }

    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder = new File(destinationFolder.trim());
    }

    public boolean isSearchWithinStartingLocationOnly() {
        return searchWithinStartingLocationOnly;
    }

    public int getNrThreads() {
        return nrThreads;
    }

    public int getWorkerTimeout() {
        return workerTimeout;
    }

    public boolean isSearchLimitEnabled()
    {
        return enableSearchLimit;
    }

    public int getSearchLimit()
    {
        return searchLimit;
    }

/*
    public void setSearchWithinStartingLocationOnly(boolean searchWithinStartingLocationOnly) {
        this.searchWithinStartingLocationOnly = searchWithinStartingLocationOnly;
    } /* */

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(printProp(PROXY_SET_PROP, proxySet));
        sb.append(printProp(STARTING_LOCATION_PROP, startingLocation));
        sb.append(printProp(DEFAULT_HOST_PATH_PROP, defaultHostAndPath));
        sb.append(printProp(DESTINATION_FOLDER_PROP, destinationFolder));
        sb.append(printProp(WITHIN_STARTING_LOCATION_PROP, searchWithinStartingLocationOnly));
        sb.append(printProp(INCLUDED_HOSTS_AND_PATHS_FILE, includedHostsAndPathsFile));
        sb.append(printProp(NR_THREADS, nrThreads));
        sb.append(printProp(WORKER_TIMEOUT, workerTimeout));
        // sb.append('\n');

        return sb.toString();
    }

    public String printProp(String name, Object value)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(name);
        sb.append(" = ");
        sb.append(value);
        return sb.toString();
    }

    public static final String DEFAULT_CONFIG_FILE = "config/webCrawler.properties";

    private static final String DEFAULT_SEARCH_LIMIT = "1000000";
    private static final String DEFAULT_NR_THREADS = "50";
    private static final String DEFAULT_WORKER_TIMEOUT = "300";  // 5 minutes (note that downloading a link includes downloading all associated resources)

    private static final String PROXY_SET_PROP = "http.proxySet";
    private static final String PROXY_HOST_PROP = "http.proxyHost";
    private static final String PROXY_PORT_PROP = "http.proxyPort";
    private static final String STARTING_LOCATION_PROP = "webCrawler.startingLocation";
    private static final String DEFAULT_HOST_PATH_PROP = "webCrawler.defaultHostAndPath";
    private static final String DESTINATION_FOLDER_PROP = "webCrawler.destinationFolder";
    private static final String WITHIN_STARTING_LOCATION_PROP = "webCrawler.searchWithinStartingLocationOnly";
    private static final String INCLUDED_HOSTS_AND_PATHS_FILE = "webCrawler.includedHostsAndPathsFile";
    private static final String NR_THREADS = "webCrawler.numberOfThreads";
    private static final String WORKER_TIMEOUT = "webCrawler.workerTimeoutInS";
    private static final String ENABLE_SEARCH_LIMIT = "webCrawler.enableSearchLimit";
    private static final String SEARCH_LIMIT = "webCrawler.searchLimit";
}
