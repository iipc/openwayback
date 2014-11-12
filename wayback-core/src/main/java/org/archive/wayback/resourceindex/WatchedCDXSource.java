package org.archive.wayback.resourceindex;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.resourceindex.cdx.CDXIndex;

/**
 * SearchResultSource that watches a single directory for new
 * SearchResultSources.
 * 
 * @author rcoram
 * 
 */

public class WatchedCDXSource extends CompositeSearchResultSource {
    private static final Logger LOGGER = Logger
	    .getLogger(WatchedCDXSource.class.getName());
    private Thread watcherThread;
    private Path path;

    public void setPath(String path) {
	this.path = Paths.get(path);
	if (watcherThread == null) {
	    try {
		watcherThread = new WatcherThread(this.path);
	    } catch (IOException e) {
		LOGGER.log(Level.SEVERE,
			"Could not watch CDX directory: " + e.getMessage(), e);
	    }
	    watcherThread.start();
	}
	try {
	    addExistingSources(this.path);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE,
		    "Could not add existing CDXs: " + e.getMessage(), e);
	}
    }

    public String getPath(){
	return this.path.toString();
    }

    /**
     * adds already-existing SearchResultSource in the watched directory
     * 
     * @param path
     * @throws IOException
     */
    private void addExistingSources(Path path) throws IOException {
	DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
	for (Path file : directoryStream) {
	    CDXIndex index = new CDXIndex();
	    index.setPath(file.toString());
	    LOGGER.info("Adding CDX: " + index.getPath());
	    addSource(index);
	}
    }

    /**
     * removes a SearchResultSource upon from the list of sources.
     * 
     * @param deleted
     * @return
     */
    public boolean removeSource(SearchResultSource deleted) {
	return sources.remove(deleted);
    }

    /**
     * Monitors a directory for ENTRY_CREATE events, creating
     * SearchResultSources.
     * 
     * @author rcoram
     * 
     */
    private class WatcherThread extends Thread {
	private final WatchService watcher;
	private final Path dir;

	public WatcherThread(Path path) throws IOException {
	    this.dir = path;
	    this.watcher = FileSystems.getDefault().newWatchService();
	    dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
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

		    if (kind == ENTRY_CREATE) {
			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			Path cdx = dir.resolve(ev.context());
			LOGGER.info("Adding new CDX " + cdx);
			CDXIndex index = new CDXIndex();
			index.setPath(cdx.toString());
			LOGGER.info("Adding CDX: " + index.getPath());
			addSource(index);
		    }

		    if (kind == ENTRY_DELETE) {
			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			Path cdx = dir.resolve(ev.context());
			LOGGER.info("Removing CDX " + cdx);
			CDXIndex index = new CDXIndex();
			index.setPath(cdx.toString());
			if (!removeSource(index)) {
			    LOGGER.info("CDX " + cdx
				    + " not found in list of sources.");
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
    }
}
