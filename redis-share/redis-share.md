首先自我介绍一下，我是来自[攻城狮朋友圈](http://www.moilioncircle.com/)的陈宝仪。很荣幸能加入Redis技术交流群；并在Redis开发的各位同行之中做此次分享。我今天分享的主要内容围绕[Redis-replicator](https://github.com/leonchen83/redis-replicator)的设计与实现，提纲如下：  

1. Redis-replicator的设计动机
2. Redis replication的协议简析
3. Redis-repicator的功能结构
4. 设计可插拔式API以及开发中的取舍

### 1. Redis-replicator的设计动机

在之前的开发中，经常有如下的需求  
* Redis数据的跨机房同步
* 异构数据的迁移；比如Redis到SSDB,pika,mysql,MQ

Redis跨机房同步传统的方式通常采取双写的方式，这样会生产一种非常难weih

### 2. Redis replication的协议简析

### 3. Redis-repicator的功能结构

### 4. 设计可插拔式API以及开发中的取舍
