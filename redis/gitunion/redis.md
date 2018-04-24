<!-- page_number: true -->
<!-- $size: 16:9 -->
<!-- $theme: gaia -->
<!-- *template: invert -->
# Redis同步协议与实践
- Redis简介
- Redis同步协议介绍
- Redis-replicator介绍
- 使用Redis需要避免的问题
- 开发中的取舍与开源

---
<!-- *template: invert -->
# 1. Redis简介
Redis是开源的, NoSQL数据库. 支持多种数据结构, 例如string, hash, list, set, zset, module. 通过Sentinel和Cluster提高可用性. 并支持主备同步与持久化到硬盘

---
<!-- *template: invert -->
### 简单使用

```
> telnet 127.0.0.1 6379
> set string value
> hmset map key1 value1 key2 value2
> lpush list v1 v2 v3
> get string
> hmget map key1
> lpop list
```

---
<!-- *template: invert -->
### Redis的部署方式

主从
```
   +---------+          +---------+
   |         |          |         |
   |  MASTER |--------->|  SLAVE  |
   |         |          |         |
   +---------+          +---------+
```

---
<!-- *template: invert -->
哨兵
```
   +----------+           +---------+          +---------+
   |          |<--------->|  MASTER |--------->|  SLAVE  |
   |          |           +---------+          +---------+
   |          |           
   |   Multi  |           +---------+          +---------+
   | Sentinel |<--------->|  MASTER |--------->|  SLAVE  |
   |   Nodes  |           +---------+          +---------+
   |          |
   |          |           +---------+          +---------+
   |          |<--------->|  MASTER |--------->|  SLAVE  |
   +----------+           +---------+          +---------+
```

---
<!-- *template: invert -->
集群
```
       +---------+          +---------+
  +--->|  MASTER |--------->|  SLAVE  |
  |    +----+----+          +---------+
  |         |
  |    +----+----+          +---------+
  |    |  MASTER |--------->|  SLAVE  |
  |    +----+----+          +---------+
  |         |
  |    +----+----+          +---------+
  +--->|  MASTER |--------->|  SLAVE  |
       +---------+          +---------+
```

---
<!-- *template: invert -->
# 2. Redis同步协议介绍

### 全量同步

```
           +----------+            PSYNC(1)            +----------+
           |          |<-------------------------------|          |
           |          |                                |          |
           |          |          FULLRESYNC(2)         |          |
           |          |------------------------------->|          |
           |          |                                |          |
           |  MASTER  |              RDB(3)            |   SLAVE  |
           |          |------------------------------->|          |
           |          |                                |          |
           |          |        AOF, AOF, AOF(4)        |          |
           |          |------------------------------->|          |
           +----------+                                +----------+

```
---
<!-- *template: invert -->
#### (1). PSYNC

Slave发送`PSYNC ? -1`到Master

#### (2). FULLRESYNC

Master执行bgsave, 保存一份RDB文件, 并给Slave回复`FULLRESYNC repl-id offset`, Slave要把这个`repl-id`和`repl-offset`保存在内存中, 以便下次增量同步时使用  

#### (3). RDB

之后Master会把这个RDB文件以Socket流的形式发送给Slave

---
<!-- *template: invert -->
#### (4). AOF

如果在发送RDB的时间段Master有新的命令被==写入==, 会以AOF的形式写到Master的backlog, 并在RDB同步完之后再发给Slave, Slave会计算每条命令的大小, 并累加到repl-offset上

#### (5). 心跳

在RDB同步完之后Master会定期给Slave发送`PING`命令以维持长链接, Slave也会定期给Master发送`REPLCONF ACK repl-offset`告诉Master自己同步的位置

---
<!-- *template: invert -->
### 增量同步

```

           +----------+            PSYNC(1)            +----------+
           |          |<-------------------------------|          |
           |          |                                |          |
           |          |           CONTINUE(2)          |          |
           |  MASTER  |------------------------------->|   SLAVE  |
           |          |                                |          |
           |          |        AOF, AOF, AOF(3)        |          |
           |          |------------------------------->|          |
           +----------+                                +----------+

```
---
<!-- *template: invert -->
#### (1). PSYNC

Slave发送`PSYNC repl-id repl-offset`到Master, 这里的`repl-id`, `repl-offset`就是之前全量同步记录的值

#### (2). CONTINUE

Master回复`CONTINUE`或者`CONTINUE repl-id`(Redis 4.0)

---
<!-- *template: invert -->
#### (3). AOF

如果在断线期间内Master有新的命令被写入, 会以AOF的形式写到Master的backlog, 并在累积的AOF同步完之后再发给Slave, Slave会计算每条命令的大小, 并累加到repl-offset上

#### (4). 心跳

Master会定期给Slave发送`PING`命令以维持长链接, Slave也会定期给Master发送`REPLCONF ACK repl-offset`告诉Master自己同步的位置

---
<!-- *template: invert -->
### 格式详解

#### AOF
`PSYNC repl-id repl-offset`等命令都是以AOF格式发送, AOF格式可以参考[https://redis.io/topics/protocol](https://redis.io/topics/protocol)

比如`PSYNC ? -1`

AOF形式是`*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n`
`*3\r\n`
`$5\r\nPSYNC\r\n`
`$1\r\n?\r\n`
`$2\r\n-1\r\n`

---
<!-- *template: invert -->
#### RDB的E-BNF形式
[https://github.com/leonchen83/redis-replicator/wiki/RDB-dump-data-format](https://github.com/leonchen83/redis-replicator/wiki/RDB-dump-data-format)
`$payload\r\nRDB`
```
RDB      =  'REDIS',$ver,[AUX],{SELECT,[RESIZE],{RECORD}},'0xFF',[$checksum];
RECORD   =  [EXPIRED], KEY, VALUE;
SELECT   =  '0xFE', $length;
AUX      =  {'0xFA', $string, $string};
RESIZE   =  '0xFB', $length, $length;
EXPIRED  =  ('0xFD', $second) | ('0xFC', $millisecond);
KEY      =  $string;
VALUE    =  $value-type, ( $string | $list | $set | $zset
                         | $hash | $zset2 | $module | $module2 
                         | $hashzipmap | $listziplist | $setintset
                         | $zsetziplist | $hashziplist | $listquicklist
                         );
```

---
<!-- *template: invert -->
# 3. Redis-replicator介绍

### 项目地址

[https://github.com/leonchen83/redis-replicator](https://github.com/leonchen83/redis-replicator)
### 依赖
```xml
    <dependency>
        <groupId>com.moilioncircle</groupId>
        <artifactId>redis-replicator</artifactId>
        <version>2.5.0</version>
    </dependency>
```
---
<!-- *template: invert -->
### 设计的动机
```
     +----------+                                           +----------+
     |          |                异构数据同步                 |          |
     |   Redis  |------------------------------------------>|   Mysql  |
     |          |                                           |          |
     +----------+                                           +----------+
     
```

### 解决的问题
```
     +----------+                                           +----------+
     |          |  pretend as Slave to accept Master data   |          |
     |  MASTER  |------------------------------------------>|REPLICATOR|
     |          |                                           |          |
     +----------+                                           +----------+
```
---
<!-- *template: invert -->
### 架构
```

     +----------+                 PSYNC                     +----------+
     |          |<------------------------------------------|          |
     |  MASTER  |         +------------+                    |          |
     |          |-------->|            |                    |          |
     +----------+         |            |                    |          |
                          |   ASYNC    |     RDB event      |REPLICATOR|
                          | RINGBUFFER |------------------->|          |
                          |            |     AOF event      |          |
                          |            |------------------->|          |
                          +------------+                    |          |
                                                            +----------+
```
---
<!-- *template: invert -->
### 代码示例
```java
        // redis:///path/to/dump.rdb
        // redis:///path/to/appendonly.aof
        final Replicator r = new RedisReplicator("redis://127.0.0.1:6379");
        r.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                // RDB event, your business code goes here.
            }
        });
        r.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                // AOF event, your business code goes here.
            }
        });
        r.open();
```
---
<!-- *template: invert -->
### Redis-replicator的应用场景
* 解析RDB, AOF文件, 伪装Slave接收数据
* 将RDB格式转化成dump格式并同步给另一个Redis
* 异构数据的同步, 比如把Redis数据同步到Mysql
* 将巨大的key进行拆分解析, 同步
---
<!-- *template: invert -->
# 4. 使用Redis需要避免的问题
### 一些通用规则
* 避免过大的KV
* 运行Redis保持至少双核
* 不要有太大的实例, 单实例16G以内, 并保留50%内存空间做bgsave
* 谨慎使用Redis cluster
---
<!-- *template: invert -->
### 无限同步问题

```
      New command
           |
           |
     +----------+ 
     |          | 
     |  BACKLOG |
     |          | 
     +----------+ 
           |
     +----------+                                 +----------+
     |          |          RDB synchronize        |          |
     |  MASTER  |-------------------------------->|   SLAVE  |
     |          |                                 |          |
     +----------+                                 +----------+
```
要传输的RDB过大, 或者Slave写入事件过慢导致backlog溢出

---
<!-- *template: invert -->
### 单个KV过大导致查询慢
```
      REQ big key
           |
           |
           |
     +----------+ 
     |          |
     |  MASTER  | Redis本身是单线程的, 在请求大kv,
     |          | 并且复杂度是o(n)的话, 会使得查询时间不可控, 堵塞之后的查询
     +----------+
```

---
<!-- *template: invert -->
### Migrate命令无法迁移大KV

```
     +----------+                                 +----------+
     |          |         Migrate key1, key2      |          |
     |  NODE 1  |-------------------------------->|  NODE 2  |
     |          |                                 |          |
     +----------+                                 +----------+
```

在做上述迁移时, `NODE 1`会将key转成dump格式, 然后在`NODE 2`中用`RESTORE`命令导入. 但是Redis的命令有一个限制是单条命令不能大于512MB, 所以如果很大的KV不能成功导入

---
<!-- *template: invert -->

# 大KV与大实例是万恶之源

### 在设计之初就要考虑KV的规模与实例的规模

---
<!-- *template: invert -->
### 在Redis-replicator中处理大KV
```java
Replicator r = new RedisReplicator("redis:///path/to/dump.rdb");
r.setRdbVisitor(new ValueIterableRdbVisitor(r));
r.addRdbListener(new ValueIterableRdbListener(128) {
    @Override
    public void handleString(KeyValuePair<byte[]> kv, int batch, boolean last) {
        // your business code goes here.
    }
    @Override
    public void handleMap(KeyValuePair<Map<byte[], byte[]>> kv, int batch, boolean last) {
        // your business code goes here.
    }
    ......
});
r.open();
```

---
<!-- *template: invert -->
### Redis-replicator中处理大KV的原理
```

     +-----------+   SMALL MAP KV 1(batch size = 128)  +----------+
     |           |------------------------------------>|          |
     |HUGE MAP KV|   SMALL MAP KV 2(batch size = 128)  |  TARGET  |
     |           |------------------------------------>|          |
     +-----------+            ....                     +----------+
     
```

---
<!-- *template: invert -->
# 5. 开发中的取舍
在<软件框架设计的艺术>这本书中提到了几个概念
1. 无绪
所谓`无绪`，就是指某些事情并不需要对背后的原理、规则有深刻的理解，就可以使用。典型的，不懂得汽车的原理，但我们照样开车，而且开得还不错
2. 兼容
API就如同恒星，一旦出现，便与我们永恒共存
3. 依赖
开发基础库依赖的选择更要谨慎, 因为有可能和用户本身的版本冲突

---
<!-- *template: invert -->
### 接口还是抽象类
Redis-replicator-1.0.x
```java
public interface RdbVisitor {
    Event applyString(RedisInputStream in, DB db, int version) throws IOException
}
```
假如用户实现了这个接口, 用户使用的Redis版本是2.8
```
public class MyRdbVisitor implements RdbVisitor {
    Event applyString(RedisInputStream in, DB db, int version) throws IOException {
        // do something
    }
}
```

---
<!-- *template: invert -->
Redis-replicator-1.1.x
```java
public interface RdbVisitor {
    Event applyString(RedisInputStream in, DB db, int version) throws IOException
    // 3.2增加了新的数据类型quick list
    Event applyListQuickList(RedisInputStream in, DB db, int version) throws IOException
}
```

那么用户升级依赖为`Redis-replicator-1.1.x`, 自己的实现`MyRdbVisitor`就会有编译错误, 但自己的Redis版本是2.8, 为什么要给我编译错误呢?

---
<!-- *template: invert -->
Redis-replicator-2.x.x
```java
public abstract class RdbVisitor {
    public Event applyString(RedisInputStream in, DB db, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }
}
```

这样保证用户升级Redis-replicator不会影响自己的`MyRdbVisitor`, 只有同时把Redis升级到3.2的时候才会抛出`UnsupportedOperationException`

---
<!-- *template: invert -->
### 依赖相关的反面例子
```xml
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.11</version>
        </dependency>
```
这个jar包在curator-client中被依赖, 但是这个jar包本身依赖了很低版本的log4j实现库, 在引入curator-client时我们还需要手动exclude掉log4j, 在设计基础工具时应仅依赖log的API而不是实现库

---
<!-- *template: invert -->
### 开源现状

* 绝大多数的个人项目， 包括著名的个人项目，都缺乏人手维护。比如slf4j项目，由Ceki Gulcu一个人主力维护，由于缺乏维护者，导致slf4j现在还不能完全支持JDK9。Netty也由Norman Maurer主力维护，进展缓慢。

### 参与开源
- 提交有价值的issue或bug
- 提交更多测试用例
- 提交大的patch之前要与作者充分沟通
- 修改bug, 改善文档与注释也是贡献

---
<!-- *template: invert -->
# THANK YOU!
