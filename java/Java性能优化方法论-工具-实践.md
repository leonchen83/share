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
* 性能优化就是用最小的人力成本和硬件成本满足系统的性能指标
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
--------------

#### 4. 设计性能优化友好的架构

1. 估算
2. 原型
3. 扩展
4. 封装

# 工具

#### 1. 单元测试 & 回归测试
--------------

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

#### 4. Monitor系统

--------------


#### 5. 压力测试
--------------

## 实践

#### 1. 对第三方依赖进行封装
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
```
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
#### 3. 编写JMH性能测试注意的问题
--------------
#### 4. GC对性能的影响
--------------
#### 5. 如何避免创建过多的临时对象
--------------
#### 6. 设计lock free架构与异步编程
--------------
