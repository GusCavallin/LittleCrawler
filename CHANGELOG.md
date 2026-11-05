---
title: LittleCrawler Changelog
author: Gustavo Cavallin
co-authored-by: Claude Fable 5.1 <noreply@anthropic.com>
created: 2026-11-05
covers: 0.0 (2007-08-31) to 5.0 (2026-11-05)
sources: .Zips version archives, the VERSION constant and the author's version log
---

# Changelog

All notable changes to LittleCrawler (a multi-threaded site downloader with a Swing window).

This history was rebuilt in 2026 from the version archives in `.Zips`, the
`VERSION` constant in `SearchThread` and the author's version log in the same
file. Dates come from the date comment beside `VERSION` where it exists (2.0
and later), otherwise from the archive's file date. There are no archives for
4.0, 4.5 or 4.9: the author's notes for 4.0 and 4.5 are covered under 4.1 and
4.7, and 4.9 is taken from the code as it stood before the 2026 changes.
Changes made only to suit one particular website are left out. Items marked
*(likely)* are inferred rather than certain.
See [docs/CHANGELOG-NOTES.md](docs/CHANGELOG-NOTES.md) for sources, method
and judgement calls.

Format loosely follows [Keep a Changelog](https://keepachangelog.com/).

## [5.0] - 2026-11-05
Prepared for publication: builds with Maven, runs on current Java versions, and the configuration is read from a file at start-up. Requires Java 21 or later.

### Added
- Maven build (`pom.xml`). `mvn clean package` produces a runnable `target/littlecrawler-<version>.jar` (`java -jar`), built for Java 21. The project has no dependencies.
- `mvn compile exec:java` runs the program from the sources.
- `README.md`, `LICENSE` (PolyForm Noncommercial License 1.0.0), `.gitignore` and `.gitattributes`.
- An optional command-line argument: the path of the properties file to use.
- When the properties file is missing, the program prints a warning and starts with the built-in defaults.

### Changed
- The configuration moved from `src/littlecrawler/webCrawler.properties`, loaded from the classpath, to `config/webCrawler.properties`, read from disk each time the program starts. A change no longer needs a copy or a rebuild.
- Sources moved to Maven's layout: the program to `src/main/java`, and the manual test programs (`ThreadPoolTest`, `TimerTest`) to `src/test/java`.
- The `VERSION` constant, shown in the start message and the status dialog, is v5.0. It had stayed at v4.7 through 4.9.
- `WebCrawlerApplet` extends `JPanel` instead of `JApplet`, because the Applet API was removed in JDK 26. The class name and the window are unchanged.
- `bin/downloader.bat` became `LittleCrawler.bat` in the project folder. It starts the built jar, can be double-clicked or run from any folder, checks for Java 21+ and for the jar, creates the `out` folder, starts the program without a console window, and takes an optional properties file path.
- The shipped configuration is a neutral example: proxy off, `example.com` as the starting location.

### Fixed
- A link with `/../` or `\..\` in its path could make the crawler write a file outside the destination folder. Such downloads are now skipped and logged as errors.
- The progress log has a scroll bar and follows the newest line. Before, it grew over its own label after about 15 lines and the older lines could not be reached.
- The window reserves room for the status line, so the first status message no longer pushes the progress log up under the fields above it.
- Start-up crashed on JDK 24 and later when `webCrawler.includedHostsAndPathsFile` was not set, because an empty path now counts as an existing file.
- The read timeout added in 3.5 now takes effect: a download that stops sending data fails after `webCrawler.workerTimeoutInS` seconds instead of holding its worker until the server closes the connection. Before, the timeout was set on one connection and the download ran on another.
- Three Windows-1252 apostrophes in `threadpool` comments stopped the code from compiling where sources are read as UTF-8.

### Removed
- The `littlecrawler.marina` and `littlecrawler.livingsocial` packages, with the *\* Process Form \** button (already hidden) and the *\* Living Social \** button (which did nothing). Both were one-off experiments for services that no longer exist. The 4.7 archive keeps a copy.
- The `producerconsumer` textbook example is kept out of the repository.

## [4.9] - 2012-07-10
Search limit setting and quieter logs. *(No archive; taken from the code and the author's version log. The `VERSION` constant was left at v4.7.)*

### Added
- `webCrawler.enableSearchLimit` and `webCrawler.searchLimit` (default 1,000,000). The limit is off unless enabled; before, it was always on at 1,000,000 pages.

### Changed
- "Ignoring already downloaded file" is logged once per file, not every time the file is linked.
- The *\* Living Social \** button is enabled before a run instead of during one. It still does nothing.

### Fixed
- The settings were missing from the log at the start of a run (broken in 4.7).

## [4.7] - 2012-07-02
Link detection fixes. Includes the author's 4.5 changes.

### Added
- `.doc`, `.xls` and `.dta` links are downloaded as files.
- `littlecrawler.livingsocial` package and a *\* Living Social \** button: a copy of the `marina` classes as the start of a deals parser. Unfinished; the button does nothing.

### Changed
- `mailto:` links are no longer logged as warnings.
- The `http` / `https` check ignores case.
- An included-hosts file that cannot be read stops start-up with an error instead of being ignored.

### Fixed
- Tags followed by a line break instead of a space (`<a` on one line, `href` on the next) were missed (4.5).
- A trailing `/` is removed from each line of the included-hosts file, so an entry ending in `/docs/` also matches a link ending in `/docs` (4.7).

## [4.1] - 2012-05-17
No more deadlock when all workers are busy. Includes the author's version 4 changes.

### Added
- `webCrawler.numberOfThreads` (default 50) and `webCrawler.workerTimeoutInS` (default 300). Both were fixed in the code before, at 20 and 180.
- Threads have names (`SearchThread`, `Worker_N`), which appear in the log.
- The thread pool logs its number of workers and timeout when it starts.

### Changed
- The task classes lost their `Thread` suffix: `ContentProcessorThread` → `ContentProcessor`, and the same for `LinkProcessor`, `FileProcessor`, `ResourceDownloader`, `LinkParserProcessor` and `ZipProcessor`.
- `ObjectFIFO` became the generic `FIFOQueue<E>`.
- Shorter log lines. Pages skipped for being outside the included paths are no longer logged.
- The connection read timeout equals the worker timeout (it was 10% longer).

### Fixed
- A deadlock: a worker handed each file it found to another worker and waited for a free one, so once every worker was waiting nothing moved. A worker now downloads the files of its own page itself, after scanning the page.

## [3.5] - 2012-05-01
Included-hosts file and sturdier downloads.

### Added
- `webCrawler.includedHostsAndPathsFile`: a text file with one host and path per line that links may stay within. It replaces a list written in `SearchThread`.
- A read timeout for each download. It is set on a connection object that the download does not use, so it has no effect (fixed in 2026).
- `.cgi` links are downloaded as files.
- *Show Detailed Status* lists how many worker threads are in each state.

### Changed
- The path of the starting URL is always included, together with the *Search Within Path* value. Before, the second replaced the first.
- Worker threads 50 → 20. Worker timeout 120 → 180 seconds.
- `ContentProcessorThread.run()` wraps a new abstract `runProcess()`: it logs a failure with the elapsed time and always wakes the search thread. (The author's version log lists this under version 4.)
- An error while reading a starting file stops the run and shows in the window, instead of only being logged.
- Empty links (`href=""`) are skipped. Worker numbers start at 1.

### Fixed
- Any file left empty by a failed download is deleted. Before, only the generated index files were.

## [2.2] - 2011-06-21
Better logs.

### Added
- Log lines name the thread and the page or file being handled.
- The settings in use are logged at the start of a run.
- *Show Detailed Status* writes pending log lines to disk first.

### Changed
- The *Search* button is now *Download*, and "Search results:" is "Progress Log:".
- Worker timeout 60 → 120 seconds.

### Fixed
- A file left empty by an earlier failed download is downloaded again instead of being skipped as already downloaded.

## [2.0] - 2010-10-12
Settings in a properties file.

### Added
- `webCrawler.properties` and `WebCrawlerProperties`: starting location, default host and path, destination folder and HTTP proxy. Before, these were edited in the source.
- `webCrawler.searchWithinStartingLocationOnly`: follow only the links found on the starting page.
- The start message lists every included path.
- `SearchAbortedException` for errors that stop a run before it starts.

### Changed
- The form results (`ZipsAndInfo_*`, `SegmentMap_*`) are written to the destination folder, not the working directory.
- The *\* Process Form \** button is hidden.
- `ThreadPoolWorker` writes its log lines through one helper and resets its interrupt flag again between tasks *(likely a fix for workers left interrupted after a timeout)*.

## [1.2] - 2009-08-09
Fixes for the ZIP code list.

### Changed
- ZIP codes whose result page is already saved are skipped.

### Fixed
- Reading the ZIP code file crashed at the end of the file.

## [1.1] - 2009-08-08
One pass over each page, and files downloaded from any host.

### Added
- `ZipProcessorThread`: reads a file of ZIP codes and queues one form lookup per code.

### Changed
- Links and files are found in one pass over the page instead of two.
- A link with a known file extension is downloaded from any host. The included path now limits only the pages that are followed.
- `<img>`, `<script>`, `<input>` and `<link>` targets are downloaded as files only when their extension is known. Others are treated as pages.
- The progress list is a read-only text area.
- The start message shows the version. Worker timeout 180 → 60 seconds.

### Fixed
- A task that timed out took its worker out of the pool for good. The task is now interrupted and the worker carries on.

## [1.0] - 2009-08-07
Rewritten as a multi-threaded Swing program, after almost two years without an archive.

### Added
- A thread pool (`threadpool` package): 50 workers, each task with a 3-minute timeout.
- `SearchThread` runs the crawl and hands out tasks: `LinkProcessorThread` downloads and scans a page, `ResourceDownloaderThread` downloads a file, and `FileProcessorThread` scans a local HTML file used as the starting point.
- Window fields for *Starting Location* (a URL or a local file), *Search Within Path* and *Destination Folder*.
- *Show Detailed Status* button. Buttons are enabled according to whether a run is in progress.
- `https` links are followed.
- Log files in the destination folder: `crawler_*.log` and `ThreadPool_*.log` (`utils.Log`).
- Links in `<input>` and `<link>` tags. Links with a known extension (PDF, images, ZIP, video) are downloaded but not scanned.
- A URL without a file name is saved as a generated `index_file*.html` in its folder.
- `littlecrawler.marina` package and a *\* Process Form \** button: a one-off job that looks up ZIP codes on a web form and writes the median household income and market segments of each to text files.
- `SearchUtils`, and two scratch programs: `threadtimer.TimerTest` and a textbook producer-consumer example.

### Changed
- `WebCrawler` (an AWT `Applet`) became `WebCrawlerApplet` (a Swing `JApplet`).
- A page is followed when its URL contains the included path. Before, its host had to equal the starting host.
- Links to anchors on the same page are ignored, and `#…` is cut from other links. `javascript:` links are skipped without an error.
- Page limit 50,000 → 1,000,000.
- The window opens centred with a fixed size.

## [0.2] - 2007-09-17
Runs as a small desktop program.

### Added
- `bin/downloader.bat` runs the crawler from an `out` folder, where the downloads land.
- Closing the window ends the program.

### Changed
- Window title "Site Downloader" (was "WebFrame").
- Generics removed from the `Vector` fields *(likely to compile for Java 1.4)*.

## [0.1] - 2007-09-12
Any starting site, and scripts downloaded too.

### Added
- Scripts (`<script src>`) are downloaded along with images.
- Every link found and every status message is added to the list in the window.

### Changed
- The crawl stays on the host of the starting URL. In 0.0 the host was written in the code.
- `javascript:void(0)` links are no longer reported as errors.

### Removed
- The `robots.txt` check and the content-type chooser, both already commented out.

## [0.0] - 2007-08-31
First version: one AWT applet class.

### Added
- `WebCrawler`, started from the WebCrawler applet in Sun's 1998 article "Writing a Web Crawler in the Java Programming Language". The class header is dated 2007-03-08.
- Follows `<a>` and `<frame>` links over `http`, within one host written in the code.
- Saves each page and its `<img>` images under the working directory, mirroring the URL path. Files already there are not written again.
- *Search* and *Stop* buttons and a status line. The crawl stops after 50,000 pages or links.
- The original's `robots.txt` check is switched off.
