package org.archive.wayback.resourceindex;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.resourceindex.cdx.CDXIndex;

/**
 * SearchResultSource that watches a single directory for new
 * SearchResultSources.
 * 
 * <pre>
 * {@code
 * <property name="source">
 *   <bean class="org.archive.wayback.resourceindex.WatchedCDXSource">
 *     <property name="recursive" value="false" />
 *     <property name="filters">
 *       <list>
 *         <value>^.+\.cdx$</value>
 *       </list>
 *     </property>
 *     <property name="path" value="/wayback/cdx-index/" />
 *   </bean>
 * </property>
 * }
 * </pre>
 * 
 * @author rcoram
 * 
 */

public class WatchedCDXSource extends CompositeSearchResultSource {
    private static final Logger LOGGER = Logger
            .getLogger(WatchedCDXSource.class.getName());
    private Thread watcherThread;
    private Path path;
    private boolean recursive = false;
    private List<String> filters;
    private ArrayList<Pattern> includePatterns = new ArrayList<Pattern>();
    private final Set<FileVisitOption> visitOptions = EnumSet
            .noneOf(FileVisitOption.class);

    public WatchedCDXSource() {
        visitOptions.add(FileVisitOption.FOLLOW_LINKS);
    }

    {
        setFilters(Arrays.asList("^.+\\.cdx$"));
    }

    public void setFilters(List<String> filters) {
        for (String filter : filters) {
            includePatterns.add(Pattern.compile(filter));
        }
        this.filters = filters;
    }

    public List<String> getFilters() {
        return this.filters;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean getRecursive() {
        return this.recursive;
    }

    public void setPath(String path) {
        this.path = Paths.get(path);
        if (watcherThread == null) {
            try {
                watcherThread = new WatcherThread(this.path, this.recursive);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "Could not watch CDX directory: " + e.getMessage(), e);
            }
            watcherThread.start();
        }
    }

    public String getPath() {
        return this.path.toString();
    }

    /**
     * removes a SearchResultSource upon from the list of sources.
     * 
     * @param deleted
     * @return
     */
    public boolean removeSource(CDXIndex deleted) {
        return sources.remove(deleted);
    }

    /**
     * Monitors a directory for ENTRY_CREATE/ENTRY_DELETE events, creating
     * SearchResultSources.
     * 
     * @author rcoram
     * 
     */
    private class WatcherThread extends Thread {
        private final WatchService watcher;
        private final HashMap<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
        private final int depth;
        private final FileVisitor<Path> visitor = new CDXFileVisitor();

        public WatcherThread(Path path, boolean recursive) throws IOException {
            if (recursive) {
                LOGGER.finest("Watching recursively.");
                this.depth = Integer.MAX_VALUE;
            } else {
                this.depth = 1;
            }
            this.watcher = FileSystems.getDefault().newWatchService();
            Files.walkFileTree(path, visitOptions, depth, visitor);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public void run() {
            while (true) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();
                    Path dir = keys.get(key);

                    if (kind == ENTRY_CREATE) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path path = dir.resolve(ev.context());
                        try {
                            Files.walkFileTree(path, visitOptions, depth,
                                    visitor);
                        } catch (IOException e) {
                            LOGGER.log(Level.WARNING, "Problem walking: "
                                    + path.toString(), e);
                        }
                    }

                    if (kind == ENTRY_DELETE) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path path = dir.resolve(ev.context());

                        CDXIndex index = new CDXIndex();
                        index.setPath(path.toString());
                        if (!removeSource(index)) {
                            LOGGER.info("CDX " + path
                                    + " not found in list of sources.");
                        } else {
                            LOGGER.info("Removed " + path);
                        }
                    }
                }

                // "If the key is no longer valid, the directory is inaccessible
                // so exit the loop."
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }

        /**
         * handles traversal of CDX (sub)directories.
         * 
         * @author rcoram
         * 
         */
        public class CDXFileVisitor extends SimpleFileVisitor<Path> {
            @Override
            public FileVisitResult visitFile(Path path,
                    BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    String spath = path.toString();
                    Matcher matcher;
                    for (Pattern pattern : includePatterns) {
                        matcher = pattern.matcher(spath);
                        if (matcher.matches()) {
                            CDXIndex index = new CDXIndex();
                            index.setPath(spath);
                            if (!sources.contains(index)) {
                                LOGGER.info("Adding CDX: " + index.getPath());
                                addSource(index);
                            }
                            break;
                        }
                    }
                }
                return CONTINUE;
            }

            @SuppressWarnings("restriction")
			@Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                if (keys.keySet().size() < depth) {
                    WatchKey key = dir.register(watcher, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE}, HIGH);
                    LOGGER.info("Watching: " + dir.toString());
                    keys.put(key, dir);
                    return CONTINUE;
                } else {
                    return SKIP_SUBTREE;
                }

            }
        }
    }
}
