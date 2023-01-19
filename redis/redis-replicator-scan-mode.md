### 1. 问题的由来

[#issue51](https://github.com/leonchen83/redis-replicator/issues/51)
```
Q : 有计划支持Tendis吗？
    Tendis内部主从同步没有使用sync,psync指令，请问有计划支持下Tendis的数据同步吗？
    
A : 暂时没有计划支持Tendis等内存数据库
```

在`redis-replicator`中，通过模拟`slave`来接收`master`数据并进行解析成一个一个`Event`。
但是在一些云`redis`服务中， `SYNC`与`PSYNC`一般是被禁用的。所以`redis-replicator`无法应对这种情况。

### 2. SCAN模式的设计构想

在`redis-replicator-3.7.0`这个版本，添加了一个`SCAN`模式，在上述命令被禁用的情况下通过`SCAN`命令来扫描全库解析成相对应的`Event`。其关键伪代码实现如下

```
for db in db0 - db16
    SELECT db
    
    while cursor not 0
        let keys = SCAN cursor COUNT 512
        for key in keys
            let ttl = PTTL key
            let val = DUMP key
            handle(ttl, value)
```

### 3. 设计遇到的问题

#### 3.1 兼容性

`redis-replicator`之前是基于同步协议传输的`rdb`流来解析成`Event`的。那么在用上述伪代码时，就不是基于`rdb`流而是`DUMP`和`TTL`返回结果来解析`Event`。这样会产生两个问题：
1. `RawByteListener`接收的数据格式会有所变化。
2. 用户自定义的`RdbVisitor`以及内置的`RdbVisitor`也会失效。

#### 3.2 性能

是否引入`Jedis`？ 在考虑实现时，首先要引入一个`redis`客户端。`Jedis` 是比较流行的客户端，但是在实现时并没有引入。基于如下两个考量：

1. 最小依赖原则，`redis-replicator`严格遵循最小依赖原则。不会引入多余的库
2. `Jedis`糟糕的`pipeline`实现。我们要用到`pipeline`来增加吞吐量，`Jedis`的实现是把所有的`pipeline`结果都取得之后再一一消费掉，这样很容易因为大`key`的问题`OOM`

#### 3.3 容错

由于我们伪代码中使用的都是只读命令，那么在任何情况下出现`IOException`时，我们可以在错误时点重放未执行的命令，严格保证一个`Event`只生成一次

### 4. 实现SCAN模式

#### 4.1 保证兼容性

由于上面提到的兼容性问题，我们在实现时采取了不直接解析`DUMP`和`TTL`流，而是将上述结果重新组装成一个`rdb`流，这样就完全保证了兼容性。

#### 4.2 使用pipeline增加吞吐量

```
    while cursor not 0
        let keys = SCAN cursor COUNT 512
        PIPELINE start
        for key in keys
            let ttl = PTTL key
            let val = DUMP key
            handle(ttl, value)
        PIPELINE end
```

#### 4.3 设计一个响应回调的简易客户端避免pipeline中OOM

```java  
    // pipeline
    RESP2.Response r = client.newCommand();
    r.post(pttl -> { /* handle pttl */ }, "pttl", key);
    r.post(dump -> { /* handle dump */ }, "dump", key);
    r.get();
```

#### 4.4 重放失败的请求保证容错

```java
    public RESP2.Response post(NodeConsumer handler, byte[]... command) throws IOException {
        this.resp2.emit(command);
        this.responses.offer(Tuples.of(handler, command));
        return this;
    }
```

`this.responses.offer(Tuples.of(handler, command));` 在发送`post`请求时同时保存`handler`与`command`，以便在异常时重放此命令

### 5. 已知问题

实现`SCAN`模式后，和`PSYNC`模式在`API`层面只有一处不一致：即`redis-replicator`会在`SCAN`完全库之后终止，`PSYNC`模式还会在后续接收增量命令。

### 6. 使用示例

示例1：使用`SCAN`模式接收数据
```java  
        Replicator r = new RedisReplicator("redis://127.0.0.1:6379?enableScan=yes&scanStep=128");
        r.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                System.out.println(event);
            }
        });
        
        r.open();
```

示例2：使用`SCAN`模式将数据保存成`rdb`格式
```java
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("./dump.rdb")));
        RawByteListener listener = new RawByteListener() {
            @Override
            public void handle(byte... rawBytes) {
                try {
                    out.write(rawBytes);
                } catch (IOException ignore) {
                }
            }
        };
    
        //save rdb from remote server
        Replicator r = new RedisReplicator("redis://127.0.0.1:6379?enableScan=yes&scanStep=512");
        r.setRdbVisitor(new SkipRdbVisitor(r));
        r.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof PreRdbSyncEvent) {
                    replicator.addRawByteListener(listener);
                }
                if (event instanceof PostRdbSyncEvent) {
                    replicator.removeRawByteListener(listener);
                    try {
                        out.close();
                        replicator.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        });
        
        r.open();
    
        //check rdb file
        r = new RedisRdbReplicator(new File("./dump.rdb"), Configuration.defaultSetting());
        r.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                System.out.println(event);
            }
        });
        r.open();
```

通过如上示例我们发现，`SCAN`模式与`PSYNC`模式的唯一区别就是`redis url`中增加了参数`enableScan=yes&scanStep=512`