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

package concurrent.future;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class ListenableFuture<T> implements CompletableFuture<T> {

    /**
     * NEW -> COMPLETING -> COMPLETED
     * NEW -> CANCELED
     */
    private static final int NEW = 0;
    private static final int COMPLETING = 1;
    private static final int COMPLETED = 2;
    private static final int CANCELED = 3;

    protected volatile Object object;

    protected volatile FutureListener<T> listener;

    protected final CountDownLatch latch = new CountDownLatch(1);

    protected final AtomicInteger status = new AtomicInteger(NEW);

    @Override
    public synchronized FutureListener<T> setListener(FutureListener<T> listener) {
        FutureListener<T> r = this.listener;
        this.listener = listener;
        if (this.isDone() && listener != null) listener.onComplete(this);
        return r;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean rs;
        if (rs = this.status.compareAndSet(NEW, CANCELED)) latch.countDown();
        return rs;
    }

    @Override
    public boolean isCancelled() {
        return this.status.get() == CANCELED;
    }

    @Override
    public boolean isDone() {
        return this.status.get() == COMPLETED;
    }

    /**
     * @return T value
     * @throws InterruptedException link to {@link Future#get}
     * @throws ExecutionException link to {@link Future#get}
     * @throws CancellationException link to {@link Future#get}
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (status.get() <= COMPLETING) latch.await();
        if (isCancelled()) throw new CancellationException();
        if (object instanceof ExecutionException) {
            throw (ExecutionException) object;
        } else if (object instanceof Throwable) {
            throw new ExecutionException((Throwable) object);
        }
        return (T) object;
    }

    /**
     * @return T value
     * @throws InterruptedException link to {@link Future#get(long,TimeUnit)}
     * @throws ExecutionException link to {@link Future#get(long,TimeUnit)}
     * @throws TimeoutException link to {@link Future#get(long,TimeUnit)}
     * @throws CancellationException link to {@link Future#get(long,TimeUnit)}
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (status.get() <= COMPLETING && !latch.await(timeout, unit) && status.get() <= COMPLETING)
            throw new TimeoutException();
        if (isCancelled()) throw new CancellationException();
        if (object instanceof ExecutionException) {
            throw (ExecutionException) object;
        } else if (object instanceof Throwable) {
            throw new ExecutionException((Throwable) object);
        }
        return (T) object;
    }

    @Override
    public void complete(T t) {
        if (!this.status.compareAndSet(NEW, COMPLETING)) return;
        this.object = t;
        latch.countDown();
        if (!this.status.compareAndSet(COMPLETING, COMPLETED)) return;
        FutureListener<T> listener = this.listener;
        if (listener != null) listener.onComplete(this);
    }

    @Override
    public void complete(Throwable t) {
        if (!this.status.compareAndSet(NEW, COMPLETING)) return;
        this.object = t;
        latch.countDown();
        if (!this.status.compareAndSet(COMPLETING, COMPLETED)) return;
        FutureListener<T> listener = this.listener;
        if (listener != null) listener.onComplete(this);
    }
}
