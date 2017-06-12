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

package concurrent.executor;

import concurrent.future.ExecutorListener;
import concurrent.future.ListenableRunnableFuture;
import concurrent.future.ListenableScheduledFuture;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
public class ListenableScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private final List<ExecutorListener> listeners = new CopyOnWriteArrayList<>();

    public void addExecutorListener(ExecutorListener listener) {
        this.listeners.add(listener);
    }

    public void removeExecutorListener(ExecutorListener listener) {
        this.listeners.remove(listener);
    }

    public ListenableScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public ListenableScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public ListenableScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public ListenableScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return new ListenableScheduledFuture<>(callable, task);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return new ListenableScheduledFuture<>(runnable, task);
    }

    @Override
    public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return (ListenableScheduledFuture<?>) super.schedule(command, delay, unit);
    }

    @Override
    public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return (ListenableScheduledFuture<V>) super.schedule(callable, delay, unit);
    }

    @Override
    public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return (ListenableScheduledFuture<?>) super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return (ListenableScheduledFuture<?>) super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public ListenableRunnableFuture<?> submit(Runnable task) {
        return (ListenableRunnableFuture<?>) super.submit(task);
    }

    @Override
    public <T> ListenableRunnableFuture<T> submit(Runnable task, T result) {
        return (ListenableRunnableFuture<T>) super.submit(task, result);
    }

    @Override
    public <T> ListenableRunnableFuture<T> submit(Callable<T> task) {
        return (ListenableRunnableFuture<T>) super.submit(task);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        for (ExecutorListener listener : listeners) {
            listener.beforeExecute(this, (ListenableRunnableFuture<?>) r);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        for (ExecutorListener listener : listeners) {
            listener.afterExecute(this, (ListenableRunnableFuture<?>) r, t);
        }
    }

    @Override
    protected void terminated() {
        super.terminated();
        for (ExecutorListener listener : listeners) {
            listener.onTerminated(this);
        }
    }
}
