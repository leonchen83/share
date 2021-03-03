---
marp: true
title: Java性能优化方法论、工具与实践
description: 
theme: uncover
paginate: true
_paginate: false
---
# <!--fit--> Java性能优化方法论、工具与实践
--------------
## 方法论
#### 1. 为什么要做性能优化

* 双11电商促销, 直播平台, 交易系统, 12306春运购票
* 性能优化就是用最小的人力成本和硬件成本满足系统不断变化的性能指标
--------------

#### 2. 过早优化是万恶之源？

```txet
The real problem is that programmers have spent far too much time worrying 
about efficiency in the wrong places and at the wrong times; 
premature optimization is the root of all evil (or at least most of it) 
in programming. 
                                                        ------Donald Knuth
```
--------------
#### 可以不过早优化，但要预先设计出性能优化友好的架构
--------------

#### 3. 性能优化的边际效益递减原则
![w:700 h:500](https://github.com/leonchen83/share/blob/master/java/optimize-profit.png?raw=true)  

--------------

#### 4. 设计性能优化友好的架构

1. 估算
2. 原型
3. 扩展
4. 封装
--------------
# 工具

#### 2. JMH
```xml  
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.25.2</version>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.25.2</version>
</dependency>
```
--------------
```java  
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class EnumMapBenchmark {
    Map<Validation, Boolean> map = new HashMap<>();
    Map<Validation, Boolean> enumMap = new EnumMap<>(Validation.class);
    @Setup
    public void setup() {
        map.put(Validation.INVALID, TRUE);
        map.put(Validation.EXPIRED, TRUE);
        enumMap.put(Validation.INVALID, TRUE);
        enumMap.put(Validation.EXPIRED, TRUE);
    }
    @Benchmark
    public Boolean measureEnumMap() { return enumMap.get(INVALID); }
    @Benchmark
    public Boolean measureMap() { return map.get(INVALID); }
}
```
---------------
```
 Benchmark        Mode  Cnt         Score      Error  Units
 measureEnumMap  thrpt    5  19824783.306 ± 3662.429  ops/s
 measureMap      thrpt    5  14834630.039 ± 1054.444  ops/s
```
---------------
#### 3. async-profiler
```
# https://github.com/jvm-profiling-tools/async-profiler/releases
> tar -xvf async-profiler-1.8.4-linux-x64.tar.gz
> cd async-profiler-1.8.4
> $JAVA_HOME/bin/jps
  9234 Jps
  8983 your-java-program
> ./profiler.sh -d 30 -e cpu -f /tmp/your-java-program-cpu.svg 8983
> ./profiler.sh -d 30 -e alloc -f /tmp/your-java-program-alloc.svg 8983
```
--------------

![w:800 h:500](https://github.com/leonchen83/share/blob/master/java/profile-alloc.png?raw=true)  

--------------

#### 4. Monitor系统
![w:800 h:300](https://github.com/leonchen83/share/blob/master/java/monitor1.png?raw=true)  

--------------
![w:800 h:300](https://github.com/leonchen83/share/blob/master/java/monitor.png?raw=true)  

--------------

## 实践

#### 1. 对第三方依赖进行封装
--------------
```
String json = JSON.toJSONString(person);
Person person = JSON.parseObject(json, Person.class);
```
---------------
```
public interface JsonMarshaller {
    <T> T read(String json, Class<T> clazz);
    <T> T read(InputStream json, Class<T> clazz);
    String write(Object value);
    void write(OutputStream out, Object value);
}
```
--------------

##### 优点
1. 可以让第三方依赖的类缩小在某一个范围内
2. 可以针对某些特性对第三方类库进行性能优化
3. 可以加入特定的埋点进行监控
--------------
##### 缺点
1. 考验研发人员的水平，封装的不好还不如不封装
2. 需要一定的工作量来封装，不是开箱即用
3. 需要深刻理解业务，根据业务要求封装
--------------

#### 2. 构建Monitor系统
```java  
public void timeConsumingMethod() {
    long st = System.nanoTime();
    // do something
    ....
    long ed = System.nanoTime();
    logger.debug("{}, {}, {}", name, method, (ed - st));
}

```
--------------
```java  
public void timeConsumingMethod() {
    long st = System.nanoTime();
    // do something
    ....
    long ed = System.nanoTime();
    monitor.add(metric, COUNTER, (ed - st));
    // monitor.add(metric, GAUGE, value);
}

```
--------------
```
+---------+  metric  +---------+           +---------+ 
| MODULE1 |--------->|         |           |         |
+---------+          |         |           |         |
                     |         |           |         |
+---------+  metric  |         |   query   |         |
| MODULE2 |--------->|INFLUXDB |---------->| GRAFANA |
+---------+          |         |           |         |
                     |         |           |         |
+---------+  metric  |         |           |         |
| MODULE3 |--------->|         |           |         |
+---------+          +---------+           +---------+ 
```
--------------
```
services:
  influxdb:
    image: influxdb:1.7.9
    container_name: influxdb
    ports:
      - "8086:8086"
    volumes:
      - ./influxdb/:/docker-entrypoint-initdb.d
    restart: on-failure
  grafana:
    image: grafana/grafana:5.3.2
    container_name: grafana
    ports:
      - "3000:3000"
    volumes:
      - ./grafana/provisioning/:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
    links:
      - influxdb
    restart: on-failure
```
--------------
#### 3. 编写JMH性能测试注意的问题
##### 避免编译优化
```
    @Benchmark
    @Fork(1)
    public void benchThreadLocal() {
        raw.set(value); raw.get();
    }

    @Benchmark
    @Fork(1)
    public void benchThreadLocal(Blackhole bh) {
        raw.set(value); bh.consume(raw.get());
    }
```
--------------
##### 避免常量折叠
```
    private double x = Math.PI;
    private final double wrongX = Math.PI;

    @Benchmark
    public double measureWrong_1() {
        return Math.log(Math.PI);
    }

    @Benchmark
    public double measureWrong_2() {
        return Math.log(wrongX);
    }

    @Benchmark
    public double measureRight() {
        return Math.log(x);
    }
```
--------------
##### 避免循环优化
```
    @Benchmark
    public int measureRight() {
        return (x + y);
    }

    private int reps(int reps) {
        int s = 0;
        for (int i = 0; i < reps; i++) {
            s += (x + y);
        }
        return s;
    }

    @Benchmark
    public int measureWrong_100() {
        return reps(100);
    }
```
--------------
#### 4. GC对性能的影响
```
Total time for which application threads were stopped: 0.3110979 seconds, 
Stopping threads took: 0.0000309 seconds
Application time: 1.0001194 seconds
```

Etcd分布式锁由于gc时间过长导致续期失败，进而HA切换

--------------
1. 升级Jdk版本
2. 吞吐优先还是延迟优先(-XX:MaxGCPauseMillis)
3. 避免创建过多临时对象
--------------

#### 5. 如何避免创建过多的临时对象
##### ThreadLocal与池化

```java  
ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(
       () -> new byte[8192]);

public void marshal(Object object, DataOutput out) {
    int length = serializedSize(object);
    byte[] buffer = length > 8192 ? new byte[length] : BUFFER.get();
    serialized(object, buffer);
    out.write(buffer, 0, length);
}
```
--------------
针对 `BUFFER.get();` 的性能损耗，扩展ThreadFactory与Thread, 实现FastThreadLocal

```
FastThreadLocal<byte[]> BUFFER = FastThreadLocal.withInitial(
       () -> new byte[8192]);
```
--------------

##### 利用堆外内存
```java  
sun.misc.Unsafe.allocateMemory(size);
sun.misc.Unsafe.freeMemory(addr);
sun.misc.Unsafe.reallocateMemory(addr, size);
sun.misc.Unsafe.putByte(object, addr, value);
sun.misc.Unsafe.getByte(addr);
```
--------------

##### 通过JNI实现一套分配回收内存碎片更小的堆外内存库

````java  
public class Jemalloc {
    static {
        LibraryLoader.load("jemalloc");
    }
    public static native Stats je_stats_info();
    public static native void je_free(long ptr);
    public static native long je_malloc(long size);
    public static native long je_calloc(long num, long size);
    public static native long je_realloc(long ptr, long size);
    public static native long je_aligned_alloc(long align, long size);
    public static class Stats {
        public long allocated, resident, metadata;
        public long retained, mapped, active, epoch;
    }
}

````
--------------

##### zero-copy

```java  
ByteBuffer.allocate(1024);
ByteBuffer.allocateDirect(1024);

ByteBuffer.slice();
ByteBuffer.flip();
ByteBuffer.compact();
ByteBuffer.rewind();
ByteBuffer.duplicate();
```

--------------

#### 6. 设计lock free架构与异步编程

```

+---------+   shard  +---------------+           +----------+ 
|         |--------->|thread process |---------->|          |
|         |          +---------------+           |          |
|         |                                      |          |
|         |   shard  +---------------+           |          |
| Request |--------->|thread process |---------->| RESPONSE |
|         |          +---------------+           |          |
|         |                                      |          |
|         |   shard  +---------------+           |          |
|         |--------->|thread process |---------->|          |
+---------+          +---------------+           +----------+ 

```
--------------
##### 灵活使用CountDownLatch处理不可shard的请求

```java  
CountDownLatch latch = new CountDownLatch(shard);
Request request = new Request(latch);
submitToThreadPool(request);
latch.await();
//do some other process
```
--------------
##### 灵活使用AtomicInteger处理不可shard的请求

```java  
AtomicInteger count = new AtomicInteger(shard);
Request request = new Request(count);
submitToThreadPool(request);

public void process(Request request) {
    Response res = ...
    request.addResponse(res);
    if(request.count.decrementAndGet() == 0) {
        reply(request.getResponses());
    }
}
```
--------------