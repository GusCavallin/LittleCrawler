---
title: How the LittleCrawler Changelog Was Built
author: Gustavo Cavallin
co-authored-by: Claude Fable 5.1 <noreply@anthropic.com>
created: 2026-11-05
related: ../CHANGELOG.md
---

# How the Changelog Was Built

[CHANGELOG.md](../CHANGELOG.md) was written in November 2026, long after the
code, from old archives and the code itself. These notes record the sources,
the method and the judgement calls, so the history can be checked or redone.

## Sources

1. **Version archives** in the author's `.Zips` folder, next to the project
   folder (not part of this repository): 11 `.zip` files from
   `LittleCrawler_0.0.zip` (2007-08-31) to `LittleCrawler_4.7.zip`
   (2012-07-04), and one `LittleCrawler.rar` (2007-09-18).
2. **The `VERSION` constant** in `SearchThread` (1.0 and later), with a date
   comment beside it from 2.0 on.
3. **The author's version log**, a comment under `VERSION` in
   `SearchThread.java`, with notes for versions 4, 4.1, 4.5, 4.7 and 4.9.
4. **The current code**, compared against the 4.7 archive.

## Method

1. Extracted each archive into its own temporary folder.
2. Diffed the sources of consecutive versions, ignoring whitespace and line
   endings. For 3.5 → 4.1 the renamed classes were paired by hand
   (`ContentProcessorThread` with `ContentProcessor`, `ObjectFIFO` with
   `FIFOQueue`, and so on), so the renames didn't show up as whole files
   removed and added.
3. Read 0.0 and 1.0 in full: 0.0 is the starting point, and 1.0 is a rewrite
   from one source file into seventeen, so a diff against 0.2 says little.
4. Wrote notes per version: Added / Changed / Fixed / Removed, focused on
   what the program could do and notable design changes, not small refactors.
5. Merged everything into `CHANGELOG.md`, newest first, loosely following
   [Keep a Changelog](https://keepachangelog.com/).

## Dates

- From 2.0 on, the date in the comment beside `VERSION` is used.
- Before that, the archive's file date is used (0.0 to 1.2).
- 4.7's code says 2012/07/02; some of its files were saved on 07-03 and the
  archive is dated 07-04.
- 4.9 has no archive. Its date is the file date of `SearchThread.java`, which
  holds the 4.9 note.

## Gaps and judgement calls

- **No archives for 4.0, 4.5 or 4.9.** The author's notes for version 4 are
  covered under 4.1 and those for 4.5 under 4.7. 4.9 is the difference
  between the 4.7 archive and the code as it stood before the 2026 changes.
- **The `VERSION` constant still reads v4.7**, although the version log
  below it describes 4.9. The changelog follows the log.
- **`LittleCrawler.rar`** holds the same `WebCrawler.java` as the 0.0
  archive. Only the IDE workspace file differs, so it isn't a version.
- **1.0 comes almost two years after 0.2.** Class headers in 1.0 are dated
  from December 2007 to August 2009, so the rewrite was gradual, but nothing
  in between was archived.
- **The `run()` / `runProcess()` split** is listed by the author under
  version 4 but is already in the 3.5 archive. The changelog puts it in 3.5.
- **Site-specific changes are left out.** Several versions carry tweaks
  written for one particular website, such as extra download rules, a
  property to switch them on, and commented-out lists of hosts. At the
  author's request they are not in the changelog.
- **Default values in `webCrawler.properties`** (starting URLs, folders,
  proxy) changed in almost every version. Only new or renamed property keys
  are recorded.
- **`ThreadPoolTest`** grew in 1.1 and 2.0. It is a manual test, so its
  changes are left out.
- **IDE files and compiled classes** in the archives were ignored. 4.7 is
  the first archive with an `.idea` folder.
- **The 2026 entry is labelled Unreleased** because no version number has
  been chosen for it.
- Items marked *(likely)* in the changelog are inferences, not certainties.

## Redoing it

To check an entry, extract the two archives around it and diff their `src`
folders, for example:

```bash
diff -ruN -w "LittleCrawler_2.0/LittleCrawler/src" "LittleCrawler_2.2/LittleCrawler/src"
```

For 3.5 → 4.1, diff the renamed classes pair by pair.
