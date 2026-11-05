package littlecrawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the URL helpers in SearchUtils.
 *
 * The first two tests replace the main method that SearchUtils had since 2007. It printed the
 * URL path and the slash count of twelve URLs for a person to read. The same twelve cases are
 * checked here, with example.com in place of the original host. No test opens a connection.
 *
 * Date: Nov 5, 2026
 */
class SearchUtilsTest
{
    private static URL url(String spec)
            throws MalformedURLException
    {
        return URI.create(spec).toURL();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "https://www.example.com/index.htm,             https://www.example.com",
            "https://www.example.com/,                      https://www.example.com",
            "https://www.example.com,                       https://www.example.com",
            "https://www.example.com/~user/index.htm,       https://www.example.com/~user",
            "https://www.example.com/~user/,                https://www.example.com/~user",
            "https://www.example.com/~user,                 https://www.example.com/~user",
            "https://www.example.com:4040/index.htm,        https://www.example.com:4040",
            "https://www.example.com:4040/,                 https://www.example.com:4040",
            "https://www.example.com:4040,                  https://www.example.com:4040",
            "https://www.example.com:4040/~user/index.htm,  https://www.example.com:4040/~user",
            "https://www.example.com:4040/~user/,           https://www.example.com:4040/~user",
            "https://www.example.com:4040/~user,            https://www.example.com:4040/~user"
    })
    void urlPathIsTheFolderWithoutTheFileOrTheLastSlash(String location, String expectedPath)
            throws MalformedURLException
    {
        assertEquals(expectedPath, SearchUtils.getURLPath(location));
    }

    @ParameterizedTest(name = "{0} has {1}")
    @CsvSource({
            "https://www.example.com/index.htm,             1",
            "https://www.example.com/,                      0",
            "https://www.example.com,                       0",
            "https://www.example.com/~user/index.htm,       2",
            "https://www.example.com/~user/,                1",
            "https://www.example.com/~user,                 1",
            "https://www.example.com:4040/index.htm,        1",
            "https://www.example.com:4040/,                 0",
            "https://www.example.com:4040,                  0",
            "https://www.example.com:4040/~user/index.htm,  2",
            "https://www.example.com:4040/~user/,           1",
            "https://www.example.com:4040/~user,            1"
    })
    void slashesAreCountedInThePathIgnoringTheLastOne(String location, int expectedCount)
            throws MalformedURLException
    {
        assertEquals(expectedCount, SearchUtils.countSlashes(url(location)));
    }

    @Test
    void urlPathKeepsALastSegmentWithoutADotBecauseItLooksLikeAFolder()
            throws MalformedURLException
    {
        assertEquals("https://www.example.com/docs/guide", SearchUtils.getURLPath("https://www.example.com/docs/guide"));
        assertEquals("https://www.example.com/docs", SearchUtils.getURLPath("https://www.example.com/docs/guide.html"));
    }

    @Test
    void urlPathRejectsSomethingThatIsNotAUrl()
    {
        assertThrows(MalformedURLException.class, () -> SearchUtils.getURLPath("C:/downloads/start.html"));
        assertThrows(MalformedURLException.class, () -> SearchUtils.getURLPath("not a url"));
    }

    @Test
    void removeLastSeparatorTrimsSpacesAndOneTrailingSlash()
    {
        assertEquals("https://www.example.com/docs", SearchUtils.removeLastSeparator("https://www.example.com/docs/"));
        assertEquals("https://www.example.com/docs", SearchUtils.removeLastSeparator("  https://www.example.com/docs/  "));
        assertEquals("https://www.example.com/docs", SearchUtils.removeLastSeparator("https://www.example.com/docs"));
        assertEquals("https://www.example.com/docs/", SearchUtils.removeLastSeparator("https://www.example.com/docs//"));
        assertEquals("", SearchUtils.removeLastSeparator("/"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example.com/files/report.pdf",
            "https://www.example.com/files/PHOTO.JPG",
            "https://www.example.com/files/archive.zip",
            "https://www.example.com/files/video.mp4",
            "https://www.example.com/files/data.dta",
            "https://www.example.com/cgi-bin/download.cgi"
    })
    void linksWithAKnownExtensionAreFilesToDownload(String location)
            throws MalformedURLException
    {
        assertTrue(SearchUtils.isAResourceLink(url(location)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example.com/index.html",
            "https://www.example.com/docs/",
            "https://www.example.com/docs/guide",
            "https://www.example.com/style.css",
            "https://www.example.com/page.php?id=7"
    })
    void otherLinksArePagesToFollow(String location)
            throws MalformedURLException
    {
        assertFalse(SearchUtils.isAResourceLink(url(location)));
    }

    @Test
    void onlyHttpAndHttpsLinksAreAccepted()
            throws MalformedURLException
    {
        assertTrue(SearchUtils.isHttpOrHttpsURL(url("http://www.example.com/")));
        assertTrue(SearchUtils.isHttpOrHttpsURL(url("https://www.example.com/")));
        assertTrue(SearchUtils.isHttpOrHttpsURL(url("HTTPS://www.example.com/")));
        assertFalse(SearchUtils.isHttpOrHttpsURL(url("ftp://ftp.example.com/file.zip")));
        assertFalse(SearchUtils.isHttpOrHttpsURL(url("file:///tmp/start.html")));
    }

    @Test
    void findFirstOfMultipleItemsReturnsTheEarliestMatchFromTheStartingIndex()
    {
        String content = "<p><img src=a.png><a href=b.html><img src=c.png>";
        String[] tags = {"<a", "<img"};

        assertEquals(3, SearchUtils.findFirstOfMultipleItems(content, tags, 0));   // the first <img
        assertEquals(18, SearchUtils.findFirstOfMultipleItems(content, tags, 4));  // the <a
        assertEquals(33, SearchUtils.findFirstOfMultipleItems(content, tags, 19)); // the second <img
        assertEquals(-1, SearchUtils.findFirstOfMultipleItems(content, tags, 34));
        assertEquals(-1, SearchUtils.findFirstOfMultipleItems(content, new String[] {"<frame"}, 0));
    }
}
