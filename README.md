# LittleCrawler

A small multi-threaded site downloader with a Swing interface, written in plain Java with no dependencies. It starts from a URL (or a local HTML file), follows the links that stay inside the paths you allow, and saves the pages and the files they reference to a local folder.

It was written between 2007 and 2012 as a personal tool and freshened up in 2026 for publication: it now builds with Maven, runs on current Java, and a handful of old bugs are fixed. It is not maintained. [CHANGELOG.md](CHANGELOG.md) has the version history.

## What it does

- Reads a page and looks for links in `<a>`, `<frame>`, `<img>`, `<script>`, `<input>` and `<link>` tags.
- Follows a page link only when its URL contains one of the included hosts and paths: the path of the starting location, the "Search Within Path" value, and any line of the optional included-hosts file.
- Downloads files it recognises by extension (images, PDF, ZIP, video, Office documents and a few others) from any host, without following them.
- Saves everything under the destination folder, mirroring the URL path. A URL with no file name is saved as a generated `index_file*.html` inside that folder. Files that already exist and are not empty are skipped. A link whose path would land outside the destination folder is skipped too.
- Runs the downloads on a fixed pool of worker threads, each with a timeout.
- Writes a `crawler_*.log` and a `ThreadPool_*.log` in the destination folder.

## Requirements

- Java 21 or later (last checked with 21 and 26)
- Maven 3.9 or later, to build

The program has no dependencies beyond the JDK. The main class is still called `WebCrawlerApplet` because it began as an applet. It is now a plain Swing panel shown in a window, since the Applet API was removed in JDK 26.

## Build and run

```bash
mvn clean package
```

```bash
java -jar target/littlecrawler-5.0.jar
```

Run it from the project folder, so it finds `config/webCrawler.properties`. To use another file, pass its path as the only argument:

```bash
java -jar target/littlecrawler-5.0.jar config/my-site.local.properties
```

On Windows, after building once, you can also double-click `LittleCrawler.bat`. It starts the program without a console window, from any folder, and explains what's wrong if Java 21+ or the jar is missing. It uses `JAVA_HOME` if set, otherwise the `java` on your PATH. It runs the program from an `out` folder, which it creates if needed, so the downloads and logs land there unless a destination folder is set. It takes the path of another properties file as an optional argument.

Or run it straight from the sources, without building the jar:

```bash
mvn compile exec:java
```

In the window, fill in the starting location, the path to stay within and the destination folder, then press **Download**. **Show Detailed Status** reports the link counts and the state of each worker, and **Stop** ends the run.

## Configuration

`config/webCrawler.properties` sets the values the window starts with and the ones it does not show. It is read each time the program starts, so a change needs no rebuild. When the file is missing, the program prints a warning and starts with the defaults below.

To keep one configuration per site, copy the file inside `config/` under a name ending in `.local.properties`. Git ignores those names.

| Property | Default | Meaning |
| --- | --- | --- |
| `webCrawler.startingLocation` | `http://` | URL, or path of a local HTML file, where the crawl starts. |
| `webCrawler.defaultHostAndPath` | empty | Extra host and path that links may stay within. Required when the starting location is a file. |
| `webCrawler.includedHostsAndPathsFile` | none | Text file with one more included host and path per line. |
| `webCrawler.destinationFolder` | working directory | Where pages, files and logs are saved. |
| `webCrawler.searchWithinStartingLocationOnly` | `false` | When `true`, only links found on the starting page are followed. |
| `webCrawler.numberOfThreads` | `50` | Number of worker threads. |
| `webCrawler.workerTimeoutInS` | `300` | Seconds a worker may spend on one page before it is interrupted. Also the longest a download waits for more data before it fails. |
| `webCrawler.enableSearchLimit` | `false` | When `true`, stop after `webCrawler.searchLimit` pages. |
| `webCrawler.searchLimit` | `1000000` | Page limit used when the limit is enabled. |
| `http.proxySet`, `http.proxyHost`, `http.proxyPort` | proxy off | HTTP proxy to use when `http.proxySet` is `true`. |

## Limitations

- It does not read `robots.txt` and does not pause between requests. With the default of 50 threads it can put real load on a site. Lower the thread count and crawl only sites you are allowed to copy.
- Links are found by scanning the text of the page, not by parsing HTML, so links built by JavaScript are missed and some odd markup is misread.
- There are no automated tests, so the build's test step reports zero tests. `threadpool.ThreadPoolTest` and `threadtimer.TimerTest`, under `src/test/java`, and `SearchUtils.main` are manual checks that you start yourself.

## Layout

| Path | What it holds |
| --- | --- |
| `pom.xml` | Maven build |
| `src/main/java` | The program |
| `src/test/java` | Manual test programs |
| `config/` | Settings, read from disk when the program starts |
| `LittleCrawler.bat` | Starts the built program on Windows |
| `start/` | A sample local starting page |
| `docs/` | Notes on how the changelog was built |

## Third-party material

Parts of this code started from published examples and were then changed:

- The crawler window and search loop (`WebCrawlerApplet`, `SearchThread`) began as the WebCrawler applet from the Sun Microsystems article "Writing a Web Crawler in the Java Programming Language" (1998).
- The `threadpool` package is adapted from the thread pool in Paul Hyde's book "Java Thread Programming" (Sams, 1999).

## License

LittleCrawler is licensed under the PolyForm Noncommercial License 1.0.0, with the Required Notice in the first line of [LICENSE](LICENSE). In short: any noncommercial use is allowed, and anyone who gets a copy from you must also get the license terms or their URL and the `Required Notice:` line. Other uses need a separate permission from the licensor. This summary does not replace the license text.

The license covers the rights the licensor holds in the material. Third-party material, such as the examples named above, keeps its own terms.
