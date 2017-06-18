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
除`RdbListener` 以及 `CommandListener` 以外,还 ]

 
  dff
