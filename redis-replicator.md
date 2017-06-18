## redis-replicator的简介
redis-replicator是实现了redis的replication协议,将自己伪装成slave,接受master的数据.  
redis-replicator也可以解析redis的rdb以及aof文件.  

## redis replication协议简介
slave发送`PSYNC ? -1`(在redis2.8以前是发送`SYNC`) 之后开启与master的同步.
master接受协议之后,会发送给slave `FULLRESYNC repl_id repl_offset`之后会发送master的数据给slave.  
master发送给slave的数据格式如下  
`rdb格式数据的payload` `rdb格式的数据` `实时命令数据`  
`rdb格式的数据`同步完之后slave端要记录offset,并定期发送给master(heartbeat),以备重连时使用这个offset  
如果同步完`rdb格式的数据`之后主从有断线(没同步完`rdb格式的数据`断线重连之后还要全量同步上述数据一遍)  
那么slave会发送`PSYNC repl_id 记录的offset` 发送给master, master会根据情况决定全量同步还是部分同步.  

## redis-replicator的应用

```java  
        final Replicator replicator = new RedisReplicator(
                "127.0.0.1", 6379,
                Configuration.defaultSetting());

        replicator.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                System.out.println(kv); //解析rdb格式的数据
            }
        });

        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                System.out.println(command); //解析实时命令的数据
            }
        });

        replicator.open();
```

## 其他实用的回调函数
除`RdbListener` 以及 `CommandListener` 以外,还有其他实用的回调函数,但首先要多讲一下`RdbListener`  
`RdbListener`里还有两个方法`preFullSync`,`postFullSync` 在同步rdb之前和之后分别触发这两个事件.  
`CloseListener` 在调用replicator.close之后触发, 或者在异常退出时会触发此事件(仅触发一次)  
`ExceptionListener` 在用户处理`rdb事件`或者`command事件`时发生异常会触发此事件  
`AuxFieldListener` 在(redis 3.0以上) 同步真正的rdb数据之前,master会发送给slave一些类似于版本信息.通过监听此事件可以处理这些信息  

## 单机版调试

### 安装redis
```java  
$wget https://github.com/antirez/redis/archive/4.0-rc3.tar.gz
$tar -xvzf 4.0-rc3.tar.gz
$cd redis-4.0-rc3
$make
$cd src
$nohup ./redis-server ../redis.conf &

```

### 写入一些命令以便检测rdb格式的数据同步

```java
$cd redis-4.0-rc3/src
$./redis-cli
127.0.0.1:6379> set key value
127.0.0.1:6379> lpush queue v1 v2 v3
127.0.0.1:6379> hmset mkey mfield mvalue
```

### 开启redis-replicator监听
```java
        final Replicator replicator = new RedisReplicator(
                "127.0.0.1", 6379,
                Configuration.defaultSetting());

        replicator.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                System.out.println(kv); //解析rdb格式的数据
            }
        });

        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                System.out.println(command); //解析实时命令的数据
            }
        });

        replicator.open();
```

### 在控制台会观察到刚才写入的三条数据(rdb数据同步)

```java  
KeyValuePair{db=DB{dbNumber=0, dbsize=7, expires=0}, valueRdbType=0, expiredType=NONE, expiredValue=null, key='key', value=value}
KeyValuePair{db=DB{dbNumber=0, dbsize=7, expires=0}, valueRdbType=13, expiredType=NONE, expiredValue=null, key='mkey', value={mfield=mvalue}}
KeyValuePair{db=DB{dbNumber=0, dbsize=7, expires=0}, valueRdbType=14, expiredType=NONE, expiredValue=null, key='queue', value=[v3, v2, v1]}
```

### 不要关闭redis-replicator,再往redis写入数据
```java  
$cd redis-4.0-rc3/src
$./redis-cli
127.0.0.1:6379> set key newvalue
127.0.0.1:6379> lpush queue v4 v5 v6
127.0.0.1:6379> hmset mkey mfield1 mvalue1
```
### 在控制台会观察到写入的实时数据(command实时同步)
```java  
SetCommand{name='key', value='newvalue', ex=null, px=null, existType=NONE}
LPushCommand{key='queue', values=[v4, v5, v6]}
HMSetCommand{key='mkey', fields={mfield1=mvalue1}}
```

## 测试断线重连
在上述的replicator开启过程中,在控制台可以观测到类似于如下的信息(redis版本不同信息也略有不同)  
```java
2017-06-18 22:22:03.762 [main] INFO c.m.r.r.RedisSocketReplicator:244 - REPLCONF listening-port 34450
2017-06-18 22:22:03.767 [main] INFO c.m.r.r.RedisSocketReplicator:248 - OK
2017-06-18 22:22:03.767 [main] INFO c.m.r.r.RedisSocketReplicator:258 - REPLCONF ip-address 127.0.0.1
2017-06-18 22:22:03.768 [main] INFO c.m.r.r.RedisSocketReplicator:262 - OK
2017-06-18 22:22:03.768 [main] INFO c.m.r.r.RedisSocketReplicator:273 - REPLCONF capa eof
2017-06-18 22:22:03.768 [main] INFO c.m.r.r.RedisSocketReplicator:277 - OK
2017-06-18 22:22:03.769 [main] INFO c.m.r.r.RedisSocketReplicator:273 - REPLCONF capa psync2
2017-06-18 22:22:03.769 [main] INFO c.m.r.r.RedisSocketReplicator:277 - OK
2017-06-18 22:22:03.769 [main] INFO c.m.r.r.RedisSocketReplicator:93 - PSYNC ? -1
2017-06-18 22:22:03.770 [main] INFO c.m.r.r.RedisSocketReplicator:168 - FULLRESYNC dd0334312c96a8054afc2143becb10ae5150ef13 1
2017-06-18 22:22:03.866 [main] INFO c.m.r.r.RedisSocketReplicator:200 - RDB dump file size:719
2017-06-18 22:22:03.868 [main] INFO c.m.r.r.r.RdbListener$Adaptor:40 - pre full sync
2017-06-18 22:22:03.872 [main] INFO c.m.r.r.r.DefaultRdbVisitor:152 - RDB redis-ver: 3.2.3
2017-06-18 22:22:03.874 [main] INFO c.m.r.r.r.DefaultRdbVisitor:152 - RDB redis-bits: 64
2017-06-18 22:22:03.874 [main] INFO c.m.r.r.r.DefaultRdbVisitor:152 - RDB ctime: 1497795723
2017-06-18 22:22:03.875 [main] INFO c.m.r.r.r.DefaultRdbVisitor:152 - RDB used-mem: 565520
```
我们可以观察到slave开启的端口是`34450`  
然后我们模拟一下断线  
```java  
iptables -I OUTPUT -p tcp --dport 34450 -j DROP
iptables -I INPUT -p tcp --dport 34450 -j DROP
```

在控制台会观察到如下日志  
```java
2017-06-18 22:37:14.078 [main] INFO c.m.r.r.RedisSocketReplicator:352 - heartbeat canceled.
2017-06-18 22:37:14.079 [main] INFO c.m.r.r.RedisSocketReplicator:374 - socket closed
2017-06-18 22:37:14.079 [main] INFO c.m.r.r.RedisSocketReplicator:156 - reconnect to redis-server. retry times:1
2017-06-18 22:37:15.080 [main] INFO c.m.r.r.RedisSocketReplicator:244 - REPLCONF listening-port 34472
2017-06-18 22:37:15.080 [main] INFO c.m.r.r.RedisSocketReplicator:248 - OK
2017-06-18 22:37:15.081 [main] INFO c.m.r.r.RedisSocketReplicator:258 - REPLCONF ip-address 127.0.0.1
2017-06-18 22:37:15.081 [main] INFO c.m.r.r.RedisSocketReplicator:262 - OK
2017-06-18 22:37:15.081 [main] INFO c.m.r.r.RedisSocketReplicator:273 - REPLCONF capa eof
2017-06-18 22:37:15.081 [main] INFO c.m.r.r.RedisSocketReplicator:277 - OK
2017-06-18 22:37:15.081 [main] INFO c.m.r.r.RedisSocketReplicator:273 - REPLCONF capa psync2
2017-06-18 22:37:15.082 [main] INFO c.m.r.r.RedisSocketReplicator:277 - OK
2017-06-18 22:37:15.082 [main] INFO c.m.r.r.RedisSocketReplicator:93 - PSYNC dd0334312c96a8054afc2143becb10ae5150ef13 225
2017-06-18 22:37:15.082 [main] INFO c.m.r.r.RedisSocketReplicator:168 - CONTINUE
2017-06-18 22:37:15.082 [main] INFO c.m.r.r.RedisSocketReplicator:297 - heartbeat thread started.
```

注意日志中的`CONTINUE`这表明部分同步起作用了,在此次重连同步中,不会再次同步rdb数据,而是直接从实时命令开始同步  

## 更多高级功能
参照[中文文档#高级主题](https://github.com/leonchen83/redis-replicator/blob/master/README.zh_CN.md#4-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98)  
