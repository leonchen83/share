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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
public interface CompletableFuture<T> extends Future<T> {

    void complete(T t);

    void complete(Throwable t);

    FutureListener<T> setListener(FutureListener<T> listener);

    default <U> CompletableFuture<U> map(Function<T, U> function) {
        CompletableFuture<U> r = new ListenableFuture<>();
        this.setListener(f -> {
            try {
                r.complete(function.apply(f.get()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                r.complete(e);
            }
        });
        return r;
    }
}
