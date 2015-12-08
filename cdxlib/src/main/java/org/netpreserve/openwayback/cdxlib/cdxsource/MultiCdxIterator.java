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

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.netpreserve.openwayback.cdxlib.CdxLine;

/**
 * A {@link CdxIterator} which iterates over a collection of iterators.
 */
public class MultiCdxIterator implements CdxIterator {

    private static final CdxSourceExecutorService EXECUTOR_SERVICE = new CdxSourceExecutorService();

    CdxIterator[] iterators;

    int count;

    int takeptr = 0;

    /**
     * Constructs a new MultiCdxIterator from an array of CdxIterators.
     * <p>
     * @param parallel true if source iterators should be running in separate threads to achieve
     * parallel processing.
     * @param iterators the CdxIterators to use as sources.
     */
    public MultiCdxIterator(boolean parallel, final CdxIterator... iterators) {
        int len = iterators.length;
        this.iterators = new CdxIterator[len];

        if (parallel) {
            synchronized (EXECUTOR_SERVICE) {
                for (int i = 0; i < len; i++) {
                    CdxIterator iter = new IteratorTask(iterators[i]);
                    if (iter.peek() != null) {
                        this.iterators[i] = iter;
                        count++;
                    } else {
                        iter.close();
                    }
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                CdxIterator iter = iterators[i];
                if (iter.peek() != null) {
                    this.iterators[i] = iter;
                    count++;
                } else {
                    iter.close();
                }
            }
        }
        Arrays.sort(this.iterators, new ForwardComparator());
    }

    @Override
    public CdxLine next() {
        if (count > 0) {
            CdxIterator current = iterators[takeptr];
            CdxLine result = current.next();
            if (current.peek() == null) {
                count--;
                takeptr++;
                current.close();
            } else {
                for (int i = takeptr + 1; i < count; i++) {
                    if (current.peek().compareTo(iterators[i].peek()) > 0) {
                        iterators[i - 1] = iterators[i];
                    } else {
                        iterators[i - 1] = current;
                        current = null;
                        break;
                    }
                }
                if (current != null) {
                    iterators[iterators.length - 1] = current;
                }
            }
            return result;
        }
        return null;

    }

    @Override
    public CdxLine peek() {
        if (count > 0) {
            return iterators[takeptr].peek();
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        for (CdxIterator iter : iterators) {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Override
    public boolean hasNext() {
        return peek() != null;
    }

    /**
     * Class for wrapping an iterator in its own thread.
     */
    private final class IteratorTask implements CdxIterator {

        private final ArrayBlockingQueue<CdxLine> queue;

        private final Future future;

        private final CdxIterator iterator;

        private CdxLine next;

        private boolean hasMore = true;

        /**
         * Constructs a new IteratorTask and submits it to the ExecutorService.
         * <p>
         * @param iterator the iterator to wrap
         */
        public IteratorTask(final CdxIterator iterator) {
            this.iterator = iterator;
            this.queue = new ArrayBlockingQueue<>(8);

            future = EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (iterator.hasNext()) {
                            queue.put(iterator.next());
                        }
                        hasMore = false;
                    } catch (InterruptedException ex) {
                        hasMore = false;
                    }
                }

            });
        }

        @Override
        public CdxLine next() {
            CdxLine result = peek();
            next = null;
            return result;
        }

        @Override
        public CdxLine peek() {
            if (next == null) {
                getNext();
            }
            return next;
        }

        @Override
        public boolean hasNext() {
            return peek() != null;
        }

        @Override
        public void close() {
            hasMore = false;
            future.cancel(true);
            iterator.close();
            queue.clear();
        }

        /**
         * Helper method for getting the next item from the queue. Waiting if necessary for one to
         * become available.
         */
        private void getNext() {
            while (next == null && (hasMore || !queue.isEmpty())) {
                try {
                    next = queue.poll(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

    }

    /**
     * Comparator for initial sorting of iterators.
     */
    static class ForwardComparator implements Comparator<CdxIterator> {

        @Override
        public int compare(CdxIterator cdx1, CdxIterator cdx2) {
            if (cdx1 == null && cdx2 == null) {
                return 0;
            }
            if (cdx1 == null) {
                return 1;
            }
            if (cdx2 == null) {
                return -1;
            }
            return cdx1.peek().compareTo(cdx2.peek());
        }

    };

    /**
     * ThreadFactory used by the shared ExecutorService of MultiCdxIterator.
     */
    static class CdxSourceThreadFactory implements ThreadFactory {

        private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        /**
         * Constructs a ThreadFactory with its own thread group.
         */
        CdxSourceThreadFactory() {
            group = new ThreadGroup("multicdxsource");
            namePrefix = "multicdxsource-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

    }

    /**
     * ExecutorService for MultiCdxIterator. This executor is shared between all instances of
     * MultiCdxIterator.
     */
    static class CdxSourceExecutorService extends ThreadPoolExecutor {

        private static final int MAX_POOL_SIZE
                = (int) (Runtime.getRuntime().availableProcessors() * 1.5);

        private static final int KEEP_ALIVE_TIME = 3;

        /**
         * Constructor creating a ExecutorService with reasonable default values.
         */
        public CdxSourceExecutorService() {
            super(MAX_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(), new CdxSourceThreadFactory());

            allowCoreThreadTimeOut(true);
        }

    }
}
