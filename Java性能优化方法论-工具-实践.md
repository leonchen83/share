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

#### 1. junit & mockito
--------------

#### 2. jmh
--------------

#### 3. async-profiler
--------------

#### 4. Monitor系统
--------------

#### 5. 压力测试 & 回归测试
--------------

## 实践

#### 1. 对第三方依赖进行封装
--------------

#### 2. 构建monitor系统
--------------
#### 3. 编写jmh性能测试
--------------
#### 4. GC对性能的影响
--------------
#### 5. 如何避免创建过多的临时对象
--------------
#### 6. 设计lock free架构与异步编程
--------------
