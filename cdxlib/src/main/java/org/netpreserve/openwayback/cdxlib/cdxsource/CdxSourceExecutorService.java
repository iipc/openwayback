/*
 * Copyright 2016 IIPC.
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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ExecutorService for parallel operations in CdxIterators.
 * <p>
 * This executor is shared between all instances.
 */
public final class CdxSourceExecutorService extends ThreadPoolExecutor {

    /**
     * The single instance of this class. Must be volatile for double checked locking to work.
     */
    private static volatile CdxSourceExecutorService instance;

    private static final Object LOCK = new Object();

    /**
     * Constructor creating a ExecutorService.
     * <p>
     * Constructor is private to ensure only one instance for the application.
     * <p>
     * @param maxPoolSize Max size of thread pool
     * @param keepAliveSeconds Time to keep alive idle threads before killing them
     */
    private CdxSourceExecutorService(int maxPoolSize, int keepAliveSeconds) {
        super(maxPoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new CdxSourceThreadFactory());

        allowCoreThreadTimeOut(true);
    }

    /**
     * Get the single instance of the executor service.
     * <p>
     * @return the shared executor service
     */
    public static CdxSourceExecutorService getInstance() {
        // Cheap check for instance already beeing initialized
        if (instance == null) {
            synchronized (LOCK) {
                // Recheck after aquiring lock. This should work because the instance field
                // is volatile.
                if (instance == null) {
                    int maxPoolSize = (int) (Runtime.getRuntime().availableProcessors() * 1.5);
                    int keepAliveSeconds = 3 * 60;
                    instance = new CdxSourceExecutorService(maxPoolSize, keepAliveSeconds);
                }
            }
        }
        return instance;
    }

    /**
     * ThreadFactory used to create new threads for the pool.
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

}
