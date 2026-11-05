package littlecrawler;

import utils.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Jul 25, 2009
 * Time: 2:46:29 PM
 */
public abstract class ContentProcessor
    implements Runnable
{
    protected final URL url;
    protected final SearchThread searchThread;
    protected final Log log;
    private File tempFile; // Temporary file sometimes is null. In that case it must be deleted.
    private File targetFile;
    private List<URL> resourcesList;
//    private Map<String, Closeable> threadResources;

    public ContentProcessor(final URL url, final SearchThread searchThread, final Log log)
    {
        this.url = url;
        this.searchThread = searchThread;
        this.log = log;
        resourcesList = new ArrayList<URL>();
//        threadResources = new HashMap<String, Closeable>();
    }

    public abstract void runProcess() throws IOException;

    public void run()
    {
        Date start = new Date();
        try {
            runProcess();
        } catch (IOException e) {
            long elapsed = ((new Date()).getTime() - start.getTime()) / 1000;
            log.add(this, "ERROR: Couldn't open URL " + url + ". Elapsed time: " + elapsed, e);
        } finally {
            jobFinished();
        }
    }

    protected void jobFinished()
    {
        synchronized (searchThread) {
            searchThread.notify();
        }
    }

    protected void processContent(String content, URL urlBeingSearched)
    {
        if (content == null) {
            log.add(this, "WARNING: Null content.");
            return;
        }

        if (searchThread.isStopping()) return;

        String lowerCaseContent = content.toLowerCase();

        findLinksAndResources(lowerCaseContent, content, urlBeingSearched);
        downloadResources();
    }

    private void findLinksAndResources(String lowerCaseContent, String content, URL urlBeingSearched)
    {
        int index = 0;

        while ((index = SearchUtils.findFirstOfMultipleItems(lowerCaseContent, new String[] {"<a", "<frame", "<img", "<script", "<input", "<link"}, index)) != -1) {  // Matches "<a\n" and "<a " ("<area" will also match...)
            if ((index = SearchUtils.findFirstOfMultipleItems(lowerCaseContent, new String[] {"href", "src", ">"}, index)) == -1)
                break;
            else
                if (lowerCaseContent.charAt(index) == '>') continue;   // A tag starting with "a" or "frame" might be a frameset or other thing. "script" tags might not have a "src" element.
            if ((index = SearchUtils.findFirstOfMultipleItems(lowerCaseContent, new String[] {"=", ">"}, index))  == -1)
                break;
            else
                if (lowerCaseContent.charAt(index) == '>') continue;   // There could be an irrelevant "src" string inside a tag.
            if (searchThread.isStopping()) break;

            index++;
            URL urlLink = getNextUrlLink(content.substring(index), urlBeingSearched);  // Uses Upper & Lower case content.
            if (urlLink == null) continue;
            String strLink = urlLink.toString();

            if (searchThread.isStopping()) break;

            if (SearchUtils.isAResourceLink(urlLink)) { // Simple check to detect resources (like images, pdfs, etc) that should not be navigated (only downloaded).
                // Downloads resources inside and outside relevant domains
                addResourceURL(urlLink);
            } else {
                if (searchThread.webCrawlerProperties.isSearchWithinStartingLocationOnly()) {
                    if (!searchThread.webCrawlerProperties.getStartingLocation().equals(urlBeingSearched.toString())) { // URL is not starting location
                        log.add(this, ">>> Ignoring page not referenced in the starting location: " + urlBeingSearched + " [Switch searchWithinStartingLocationOnly to false to ignore this filter]");
                        continue;
                    }
                } else if (searchThread.ignorePage(strLink)) {
                    log.addDebug(this, ">>> Ignoring page outside of relevant domains: " + urlLink);
                    continue;
                }

                searchThread.addElementToSearch(strLink);
            }
        }

        log.add(this, "INFO: No more links found in current page.");  //DEBUG:  + urlBeingSearched
    }

    private void downloadResources()
    {
        for (URL resourceURL : resourcesList) {
            executeResourceDownloaderThread(resourceURL);
        }
    }

    public void addResourceURL(URL resourceUrl)
    {
//        if (resourceUrl.getFile().contains("sml")) {  // TODO: Ignores resources that contain 'sml'
//            return;
//        }
        resourcesList.add(resourceUrl);
    }

    /**
     * TODO: Improve. For example, create another thread pool for downloading resources.
     */
    protected void executeResourceDownloaderThread(final URL resourceUrl)
    {
        // searchThread.executeThread(new ResourceDownloader(resourceUrl, urlBeingSearched, searchThread, log)); // CANNOT DO THIS!!! This will block this Worker, when no other threads are available.
        Runnable r = new ResourceDownloader(resourceUrl, searchThread, log);
        r.run(); // This does not create a new thread. Therefore, this thread remains waiting here. TODO: Improve with a JOB QUEUE...
    }

    private URL getNextUrlLink(String remainingContent, URL urlBeingSearched)
    {
        if (remainingContent.startsWith("\"\"")) {
            return null; // Empty href section
        }
        StringTokenizer st = new StringTokenizer(remainingContent, "\t\n\r\">'");
//      StringTokenizer st = new StringTokenizer(remainingContent, "\t\n\r\">#");
        String strLink = st.nextToken();
        if (strLink.trim().equals("") || strLink.startsWith("3D")) {
            strLink = st.nextToken().trim();
        }

        if (strLink.startsWith("#")) {
            return null; // Ignores local anchors
        } else {
            int index = strLink.indexOf("#"); // Removes #references in full URLs
            if (index > 0) {
                strLink = strLink.substring(0, index);
            }
        }
        URL urlLink;
        try {
            urlLink = new URL(urlBeingSearched, strLink);  // This resolves Absolute or Relative strLink.
        } catch (MalformedURLException e) {
            if (!strLink.trim().startsWith("javascript:")) { // IGNORES Warning for javascript links
                log.add(this, "ERROR: Unaccepted URL: " + strLink);
            }
            return null;
        }

        if (!SearchUtils.isHttpOrHttpsURL(urlLink)) {
            if (!urlLink.getProtocol().equalsIgnoreCase("mailto")) { // IGNORES Warning for mailto links.
                log.add(this, "WARNING: This is not an http or an https link: " + urlLink);
            }
            return null;
        }

        return urlLink;
    }

    protected String readWriteContent(URL url, boolean skipAlreadyDownloaded, boolean getStringContent)
            throws IOException
    {
        return readWriteContent(url, skipAlreadyDownloaded, getStringContent, null);
    }

    protected String readWriteContent(URL url, boolean skipAlreadyDownloaded, boolean getStringContent, String targetPath)
            throws IOException
    {
        // 1. Check if file exists
        // 2. Try to open the link
        // 3. Open the target file
        // 4. read and write...
        // 5. close everything

        FileOutputStream fos;
        if (skipAlreadyDownloaded) {
            fos = openTargetStream(url, targetPath);
            if (fos == null) {
                if (!searchThread.alreadyDownloadedFiles.contains(targetPath)) {  // Reduce repetitions of log messages.
                    log.add(this, ">>> Ignoring already downloaded file.");  // DEBUG:  + url.getFile()
                    searchThread.alreadyDownloadedFiles.add(targetPath);
                }
                return null; // Skip processing already downloaded file.
            }
        } else {
            fos = openTargetStream(url, targetPath);
        }

        String content = null;
        InputStream urlStream = null;
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setReadTimeout(searchThread.webCrawlerProperties.getWorkerTimeout() * 1000); // TODO: It should be smaller that the worker timeout (but I am not sure how much).
            urlStream = urlConnection.getInputStream(); // Not url.openStream(): that opens a second connection, without the read timeout.
//            addResource(url.toString(), urlStream);

            content = readWriteStream(urlStream, fos, getStringContent);
        } finally {
            if (urlStream != null) {
                urlStream.close();
            }
            if (fos != null) {
                fos.close();
            }
//            if (tempFile != null && tempFile.length() == 0) { // TODO: tempFile should not be != null if fos is == null...
//                tempFile.delete();
//                log.add(this, "++> Deleting empty temp file: " + tempFile);
//                tempFile = null;
//            }
            if (targetFile != null && targetFile.length() == 0) { // TODO: tempFile should not be != null if fos is == null...
                targetFile.delete();
                log.add(this, "INFO: Empty target file deleted: " + targetFile);
                targetFile = null;
            }
        }
        return content;
    }

    private FileOutputStream openTargetStream(URL url, String targetPath)
            throws IOException
    {
        if (targetPath == null) {
            targetPath = url.getPath();
        }
        targetPath = targetPath.replaceAll("%20", " ");
        targetPath = searchThread.webCrawlerProperties.getDestinationFolderString() + targetPath;
        if (!isInsideDestinationFolder(new File(targetPath))) { // A URL path can contain "/../" (or "\..\" on Windows).
            throw new IOException("Target file '" + targetPath + "' is outside of the destination folder. Download skipped");
        }
        String filename = null;
        int index = targetPath.lastIndexOf('/');
        if (index >= 0 && targetPath.length() > index + 1) {
            filename = targetPath.substring(index + 1);
        }

        File target;
        if (filename == null || filename.length() == 0 || !filename.contains(".")) {
            // With this code, folder links are stored as an index file within a folder.
            File targetDir = new File(targetPath);
            targetDir.mkdirs(); // TODO: This leaves empty directories (when the temp file is empty and is removed)
            tempFile = createAlternativeTargetFile(targetDir, url);
            target = tempFile;
            // log.add(this, "WARNING: Target file '" + targetPath + "' changed to: " + target); // DEBUG.
        } else {
            target = new File(targetPath);
        }

        if (target.exists() && tempFile == null) { // temp files are created as empty files.
            if (target.length() == 0) {
                log.add(this, "WARNING: Target file '" + targetPath + "' exists but is empty. Retrying download...");
            } else {
                return null;
            }
        }

        // log.addDebug(this, "--> Downloading file: " + url + " to: " + target); // DEBUG.
        log.add(this, "Downloading '" + target.getName() + "'...");
        target.getParentFile().mkdirs();
        targetFile = target;
        FileOutputStream fos = new FileOutputStream(target);
//        addResource(target.getAbsolutePath(), fos);
        return fos;
    }

    /**
     * Nothing may be written outside of the destination folder, whatever the URL says.
     */
    private boolean isInsideDestinationFolder(File target)
            throws IOException
    {
        String destination = searchThread.webCrawlerProperties.getDestinationFolder().getCanonicalPath();
        String canonicalTarget = target.getCanonicalPath();
        if (canonicalTarget.equals(destination)) {
            return true;
        }
        if (!destination.endsWith(File.separator)) { // A root folder (like "C:\") already ends with the separator.
            destination += File.separator;
        }
        return canonicalTarget.startsWith(destination);
    }

    protected File createAlternativeTargetFile(File targetDir, URL url)
            throws IOException
    {
        return File.createTempFile("index_file", ".html", targetDir);
    }

    protected static String readWriteStream(InputStream stream, FileOutputStream fos, boolean getStringContent)
            throws IOException
    {
        byte b[] = new byte[1024];
        int numRead;
        StringBuilder content = new StringBuilder();

        while((numRead = stream.read(b)) != -1) {
            if (getStringContent) content.append(new String(b, 0, numRead));
            if (fos != null) fos.write(b, 0, numRead);
        }

        if (getStringContent && content.equals("")) {
            return null;
        }
        return content.toString();
    }

//    protected void addResource(String fileName, Closeable resource)
//    {
//        threadResources.put(fileName, resource);
//    }

//    protected void closeAllResources()
//    {
//        for (Map.Entry c: threadResources.entrySet()) {
//            log.add(this, "WARNING: Closing resource: '" + c.getKey() + "'");
//            try {
//                ((Closeable)c.getValue()).close();
//            } catch (IOException exc) {
//                log.add(this, "Exception closing resource '" + c.getKey() + "'", exc);
//            }
//        }
//    }
}
