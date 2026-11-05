package littlecrawler;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * User: gcavalli
 * Date: Dec 6, 2007
 * Time: 12:39:04 PM
 * Last Edition Date: Jan 6, 2009
 */
public class SearchUtils
{
/*    static String getHostURL(String strURL)
    {
        String host = strURL.toLowerCase();
        int index;
        if ((index = strURL.indexOf(HTTP_)) != -1) {
            host = strURL.substring(index + HTTP_.length());
            if (host.startsWith("/")) {
                host = host.substring(1);
                if (host.startsWith("/")) {
                    host = host.substring(1);
                }
            }
        }

        if ((index = host.indexOf('/')) != -1) {
            host = host.substring(0, index);
        }

        return host;
    } /* */

    /**
     *
     *
    static int findFirstOfMultipleItems(String content, final String[] items)
    {
        return findFirstOfMultipleItems(content, items, 0);
    } /* */

    static int findFirstOfMultipleItems(String content, final String[] items, final int startingAt)
    {
        int firstOccurence = -1;
        for (String item : items) {
            int index = content.indexOf(item, startingAt);

            if (index != -1) {
                firstOccurence = index;
                content = content.substring(0, index); // This reduces the search scope on the other items.
            }
        }
        return firstOccurence;
    }

    static boolean isAResourceLink(URL urlLink)
    {
        String fileStr = urlLink.getFile();
        int idx = fileStr.lastIndexOf('.');
        if (idx < 0) {
            return false;
        }
        // NOTE: *.php?bla=bla  are not considered resources, for example.
        String extLower = fileStr.substring(idx + 1).toLowerCase();
        return extLower.equals("gif") || extLower.equals("pdf") || extLower.equals("jpg") ||
               extLower.equals("png") || extLower.equals("bmp") || extLower.equals("mht") ||
               extLower.equals("mpg") || extLower.equals("wmf") || extLower.equals("zip") ||
               extLower.equals("wmv") || extLower.equals("mov") || extLower.equals("avi") ||
               extLower.equals("m4v") || extLower.equals("mp4") || extLower.equals("cgi") ||
               extLower.equals("doc") || extLower.equals("xls") || extLower.equals("dta");
    }

    /**
    static boolean isHttpOrHttpsLocation(String location)
    {
        return location.startsWith(HTTP_) || location.startsWith(HTTPS_);
    } /* */

    public static boolean isHttpOrHttpsURL(URL url)
    {
//        return url.getProtocol().compareTo(HTTP) == 0 || url.getProtocol().compareTo(HTTPS) == 0;
        return url.getProtocol().equalsIgnoreCase(HTTP) || url.getProtocol().equalsIgnoreCase(HTTPS);
    }

    /**
     * Returns the URL path without any file:
     *   if it is a file, it returns the parent folder
     *   if it is a folder, it returns the folder
     *
     * @param urlString
     * @return
     * @throws MalformedURLException
     */
    static String getURLPath(String urlString)
            throws MalformedURLException
    {
        urlString = removeLastSeparator(urlString);
        URL url = new URL(urlString);
        if (url.getPort() == -1) {
            if (urlString.endsWith(url.getHost())) {
                return urlString;
            }
        } else {
            if (urlString.endsWith(url.getHost() + ":" + url.getPort())) {
                return urlString;
            }
        }
        int slashIndex = urlString.lastIndexOf(WEB_SEPARATOR);
        String postfixUrl = urlString.substring(urlString.lastIndexOf(WEB_SEPARATOR) + 1);

        if (!postfixUrl.equals("")) {
            if(postfixUrl.contains(".")) {
                urlString = urlString.substring(0, slashIndex);
            }
        }

        return urlString;
    }

    public static String removeLastSeparator(final String urlString)
    {
        String str = urlString.trim();
        if (str.endsWith(WEB_SEPARATOR)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

//    static int getNextLinkIndex(String lowerCaseContent, int index)
//    {
//        int indexA = lowerCaseContent.indexOf("<a", index);
//        int indexFrame = lowerCaseContent.indexOf("<frame", index);
//        // int indexImg = lowerCaseContent.indexOf("<img", index);
//        if (indexA == -1) {
//            return indexFrame;
//        } else if (indexFrame == -1) {
//            return indexA;
//        }
//        return (indexA < indexFrame)? indexA:indexFrame;
//    }

//    static int getNextResourceIndex(String lowerCaseContent, int index)
//    {
//        int indexImg = lowerCaseContent.indexOf("<img", index);
//        int indexScript = lowerCaseContent.indexOf("<script", index);
//        int indexInput = lowerCaseContent.indexOf("<input", index);
//
//        if (indexImg == -1) {
//            // indexImg DOESN't COUNT
//            if (indexScript == -1) {
//                return indexInput;
//            } else {
//                if (indexInput == -1) {
//                    return indexScript;
//                } else {
//                    return (indexInput < indexScript)? indexInput:indexScript;
//                }
//            }
//        } else if (indexScript == -1) {
//            // indexScript DOESN't COUNT  (indexImg DOES)
//            if (indexInput == -1) {
//                return indexImg;
//            } else {
//                return (indexInput < indexImg)? indexInput:indexImg;
//            }
//        } else if (indexInput == -1) {
//            // indexInput DOESN't COUNT  (indexImg and indexScirpt DO)
//            return (indexImg < indexScript)? indexImg:indexScript;
//        }
//
//        if (indexImg < indexScript) {
//            return (indexInput < indexImg)? indexInput:indexImg;
//        } else {
//            return (indexInput < indexScript)? indexInput:indexScript;
//        }
//    }

    private static String arrayToString(String[] array)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    public static int countSlashes(URL urlBeingSearched)
    {
        int i = 0;
        String strUrl = removeLastSeparator(urlBeingSearched.getPath());
        for (int idx = 0; (idx = strUrl.indexOf(WEB_SEPARATOR, idx)) != -1; idx++) {
            i++;
        }

        return i;
    }

    public static void main(String[] args)
    {
        try {
            System.out.println("*** Search Utils Test ***");
            String[] list = {
            "http://www.utdallas.edu/index.htm",
            "http://www.utdallas.edu/",
            "http://www.utdallas.edu",
            "http://www.utdallas.edu/~undrwd/index.htm",
            "http://www.utdallas.edu/~undrwd/",
            "http://www.utdallas.edu/~undrwd",
            "http://www.utdallas.edu:4040/index.htm",
            "http://www.utdallas.edu:4040/",
            "http://www.utdallas.edu:4040",
            "http://www.utdallas.edu:4040/~undrwd/index.htm",
            "http://www.utdallas.edu:4040/~undrwd/",
            "http://www.utdallas.edu:4040/~undrwd"};

            System.out.println("List: " + arrayToString(list));

            for(String element: list) {
                System.out.print("Element: " + element);
                System.out.print("\t  URL Path: " + SearchUtils.getURLPath(element));
                System.out.print("\t  / count: " + countSlashes(new URL(element)));
                System.out.println();
            }

            System.out.println("*** Search Utils Test COMPLETE ***");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

//    static final String HTTP_ = "http:";
//    static final String HTTPS_ = "https:";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    protected final static String WEB_SEPARATOR = "/";
}
