/*
 * Copyright 2015 IIPC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.openwayback.cdxlib.cdxsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.processor.Processor;
import org.netpreserve.openwayback.cdxlib.CdxSource;

/**
 *
 */
public class MultiCdxSource implements CdxSource {

    private final List<CdxSource> sources;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public MultiCdxSource() {
        this.sources = new ArrayList<>();
    }

    public MultiCdxSource(CdxSource... sources) {
        this.sources = Arrays.asList(sources);
    }

    public MultiCdxSource(List<CdxSource> sources) {
        this.sources = sources;
    }

    public void addSource(CdxSource source) {
        this.sources.add(source);
    }

    @Override
    public SearchResult search(String startUrl, String endUrl, CdxLineSchema outputFormat,
            List<Processor> processors) {
        CdxIterator[] sourceIterators = new CdxIterator[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            sourceIterators[i] = sources.get(i)
                    .search(startUrl, endUrl, outputFormat, processors).iterator();
        }
        return new ParallellCdxIterable(sourceIterators);
    }

    @Override
    public void close() throws IOException {
        for (CdxSource source : sources) {
            source.close();
        }
    }

    private class ParallellCdxIterable implements SearchResult {

        CdxIterator[] sourceIterators;

        public ParallellCdxIterable(CdxIterator[] sourceIterators) {
            this.sourceIterators = sourceIterators;
        }

        @Override
        public CdxIterator iterator() {
            return new ParallellCdxIterator(sourceIterators);
        }

    }

    private final static class ForwardComparator implements Comparator<IteratorTask> {

        public int compare(IteratorTask cdx1, IteratorTask cdx2) {
            return cdx1.peek().compareTo(cdx2.peek());
        }

    };

    private final class IteratorTask {

        final Lock lock = new ReentrantLock();

        final Condition notFull = lock.newCondition();

        final Condition notEmpty = lock.newCondition();

        final CdxLine[] cdxLines = new CdxLine[128];

        int putptr, takeptr, count;

        public IteratorTask(final CdxIterator iterator) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (iterator.hasNext()) {
                            put(iterator.next());
                        }
                        endOfInput();
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted");
                        cdxLines[putptr] = null;
                        if (++putptr == cdxLines.length) {
                            putptr = 0;
                        }
                        ++count;
                        notEmpty.signal();
                    }
                }

            });
        }

        public void put(CdxLine cdxLine) throws InterruptedException {
            lock.lock();
            try {
                while (count == cdxLines.length) {
                    notFull.await();
                }
                cdxLines[putptr] = cdxLine;
                if (++putptr == cdxLines.length) {
                    putptr = 0;
                }
                ++count;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public void endOfInput() throws InterruptedException {
            lock.lock();
            try {
                while (count == cdxLines.length) {
                    notFull.await();
                }
                cdxLines[putptr] = null;
                if (++putptr == cdxLines.length) {
                    putptr = 0;
                }
                ++count;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public CdxLine take() {
            lock.lock();
            try {
                while (count == 0) {
                    notEmpty.await();
                }
                CdxLine cdxLine = cdxLines[takeptr];
                if (++takeptr == cdxLines.length) {
                    takeptr = 0;
                }
                --count;
                notFull.signal();
                return cdxLine;
            } catch (InterruptedException ex) {
                return null;
            } finally {
                lock.unlock();
            }
        }

        public CdxLine peek() {
            lock.lock();
            try {
                while (count == 0) {
                    notEmpty.await();
                }
                CdxLine cdxLine = cdxLines[takeptr];
//                if (++takeptr == cdxLines.length) {
//                    takeptr = 0;
//                }
//                --count;
//                notFull.signal();
                return cdxLine;
            } catch (InterruptedException ex) {
                return null;
            } finally {
                lock.unlock();
            }
        }

    }

    private class SortedCdxIteratorQueue {

        IteratorTask[] iteratorTasks;

        int count;

        int takeptr = 0;

        public SortedCdxIteratorQueue(final CdxIterator... iterators) {
            int len = iterators.length;
            iteratorTasks = new IteratorTask[len];
            for (int i = 0; i < len; i++) {
                IteratorTask it = new IteratorTask(iterators[i]);
                if (it.peek() != null) {
                    iteratorTasks[i] = it;
                    count++;
                }
            }
            Arrays.sort(this.iteratorTasks, new ForwardComparator());
        }

        public CdxLine take() {
            if (count > 0) {
                IteratorTask current = iteratorTasks[takeptr];
                CdxLine result = current.take();

                if (current.peek() == null) {
                    count--;
                    takeptr++;
                } else {
                    for (int i = takeptr + 1; i < count; i++) {
                        if (current.peek().compareTo(iteratorTasks[i].peek()) > 0) {
                            iteratorTasks[i - 1] = iteratorTasks[i];
                        } else {
                            iteratorTasks[i - 1] = current;
                            current = null;
                            break;
                        }
                    }
                    if (current != null) {
                        iteratorTasks[iteratorTasks.length - 1] = current;
                    }
                }
                return result;
            }
            return null;
        }

        public CdxLine peek() {
            if (count > 0) {
                return iteratorTasks[takeptr].peek();
            } else {
                return null;
            }
        }

    }

    private class ParallellCdxIterator implements CdxIterator {

        SortedCdxIteratorQueue queue;

        public ParallellCdxIterator(CdxIterator[] sourceIterators) {
            queue = new SortedCdxIteratorQueue(sourceIterators);
        }

        @Override
        public boolean hasNext() {
            return (queue.peek() != null);
        }

        @Override
        public CdxLine next() {
            CdxLine cdxLine = queue.take();
            if (cdxLine == null) {
                throw new NoSuchElementException();
            }
            return cdxLine;
        }

        @Override
        public CdxLine peek() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
