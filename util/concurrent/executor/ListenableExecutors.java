/*
 * Copyright 2016 leon chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util.concurrent.executor;

import java.util.concurrent.*;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
public class ListenableExecutors {

    public static ListenableThreadPoolExecutor newThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ListenableThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public static ListenableThreadPoolExecutor newFixedThreadPool(int nThreads) {
        return new ListenableThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public static ListenableThreadPoolExecutor newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ListenableThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
    }

    public static ListenableThreadPoolExecutor newSingleThreadExecutor() {
        return new ListenableThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public static ListenableThreadPoolExecutor newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new ListenableThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
    }

    public static ListenableThreadPoolExecutor newCachedThreadPool() {
        return new ListenableThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public static ListenableThreadPoolExecutor newCachedThreadPool(ThreadFactory threadFactory) {
        return new ListenableThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
    }

    public static ListenableScheduledThreadPoolExecutor newSingleThreadScheduledExecutor() {
        return new ListenableScheduledThreadPoolExecutor(1);
    }

    public static ListenableScheduledThreadPoolExecutor newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new ListenableScheduledThreadPoolExecutor(1, threadFactory);
    }

    public static ListenableScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize) {
        return new ListenableScheduledThreadPoolExecutor(corePoolSize);
    }

    public static ListenableScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return new ListenableScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    public static ListenableScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ListenableScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
    }

}
