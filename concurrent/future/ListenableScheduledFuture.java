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

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
public class ListenableScheduledFuture<T> extends ListenableRunnableFuture<T> implements RunnableScheduledFuture<T> {

    protected final long sequenceNumber;
    protected final RunnableScheduledFuture<T> future;
    protected static final AtomicLong sequencer = new AtomicLong();

    public ListenableScheduledFuture(Runnable runnable, RunnableScheduledFuture<T> future) {
        super(runnable, null);
        this.future = future;
        this.sequenceNumber = sequencer.getAndIncrement();
    }

    public ListenableScheduledFuture(Callable<T> callable, RunnableScheduledFuture<T> future) {
        super(callable);
        this.future = future;
        this.sequenceNumber = sequencer.getAndIncrement();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public void run() {
        future.run();
    }

    @Override
    public boolean isPeriodic() {
        return future.isPeriodic();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return future.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed o) {
        int c = future.compareTo(o);
        if (c == 0 && o instanceof ListenableScheduledFuture) {
            ListenableScheduledFuture<?> x = (ListenableScheduledFuture<?>) o;
            return sequenceNumber < x.sequenceNumber ? -1 : 1;
        }
        return c;
    }

}
