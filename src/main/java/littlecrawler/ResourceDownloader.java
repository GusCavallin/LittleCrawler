package littlecrawler;

import utils.Log;

import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: gcavalli
 * Date: Aug 6, 2009
 * Time: 2:24:13 PM
 */
public class ResourceDownloader
    extends ContentProcessor
{
    // private URL urlBeingSearched;

    public ResourceDownloader(URL resourceUrl, SearchThread searchThread, Log log) // URL urlBeingSearched,
    {
        super(resourceUrl, searchThread, log);
        // this.urlBeingSearched = urlBeingSearched;
    }

    public void runProcess()
            throws IOException
    {
        // log.add(this, "--- Downloading resource: " + url);
        readWriteContent(url, true, false);
    }

    /**
     * Hack for a Neuroanatomy site
     *
    private void findAdditionalImage()
    {
        int indexOfGif = resourceUrl.getPath().toLowerCase().indexOf(".gif");
        if (indexOfGif != -1) {
            String strUrl = urlBeingSearched.toString();
            if (strUrl.indexOf(PAGEMASTER_ATLAS) != -1 && strUrl.indexOf("+") != -1) {
                String additionalImgStrUrl = strUrl.substring(0, strUrl.indexOf(PAGEMASTER_ATLAS));
                additionalImgStrUrl += DRAWSTUFF;
                additionalImgStrUrl += strUrl.substring(strUrl.indexOf(PAGEMASTER_ATLAS) + PAGEMASTER_ATLAS.length(), strUrl.lastIndexOf(FPATH));
                additionalImgStrUrl += strUrl.substring(strUrl.indexOf(FPATH) + FPATH.length(), strUrl.lastIndexOf("+") + 1);
                additionalImgStrUrl += "-";
                String baseImgName = resourceUrl.getPath().substring(0, indexOfGif);

                downloadAdditionaImage(urlBeingSearched, additionalImgStrUrl, baseImgName, "o");
                downloadAdditionaImage(urlBeingSearched, additionalImgStrUrl, baseImgName, "ol");
                downloadAdditionaImage(urlBeingSearched, additionalImgStrUrl, baseImgName, "l");
            }
        }
    } /* */

    /**
     * HACK for a Neuroanatomy site
     *
    private void downloadAdditionaImage(URL urlBeingSearched, String additionalImgStrUrl, String baseImgName, String option)
    {
        // Image with outline and labels
        URL newUrlLink;
        try {
            newUrlLink = new URL(urlBeingSearched, additionalImgStrUrl + option);
            log.add(this, "+++ IMAGE Link: " + additionalImgStrUrl + option);
        } catch (MalformedURLException e) {
            log.add(this, "ERROR: Bad URL: " + additionalImgStrUrl + option);
            return;
        }

        String optionPostfix = "";
        if (option.equals("o")) {
            optionPostfix = "out";
        } else if (option.equals("ol")) {
            optionPostfix = "outlbls";
        } else if (option.equals("l")) {
            optionPostfix = "lbls";
        }

        try {
            readWriteContent(newUrlLink, true, false, baseImgName + "_" + optionPostfix + ".gif");
        } catch (IOException e) {
            log.add(this, "ERROR: Couldn't download Image URL: " + newUrlLink, e);
        }
    } /* */

    public String toString()
    {
        return getClass().getSimpleName() + "_" + url;  // " - resourceURL: "
    }

//    private static final boolean FIND_ADDITIONAL_IMG = false;
//    private static final String PAGEMASTER_ATLAS = "cgi-bin/DA/PageMaster?atlas:"; // Neuroanatomy+ffpathIndex";
//    private static final String DRAWSTUFF        = "cgi-bin/DA/drawStuff?"; // Neuroanatomy+Index";
//    private static final String FPATH            = "ffpath";
}
