# Redis降级迁移

## 1. 问题的来源

Redis与此相关的issue与讨论
* [Downgrade support in Redis](https://github.com/redis/redis/discussions/11089)
* [How to restore redis 3.0.6 with redis 4.0's dump.rdb](https://github.com/redis/redis/issues/8128)
* [Sync data between KeyDB v6 - Redis v7](https://github.com/leonchen83/redis-rdb-cli/issues/38)
* [How to migrate RDB to new format supported by Redis 7?](https://github.com/redis/redis/issues/11172)

## 2. 降级迁移目前普遍的解决方案

### 方案1： 转成AOF迁移, 伪码如下

```
let kvs = parse(RDB)
for (key, val) in kvs
    if val is map 
        for (field, value) in val
            // execute migration
            redis.HSET(key, field, vlaue)
    if val is string
        // execute migration
        redis.SET(key, val)
    ...
    ...
```

此方案的优点: 

1. 实现简单
2. 可以实现大Key降级迁移

此方案的缺点:

1. 使用非紧凑的AOF协议迁移, 占用带宽
2. 不能保证迁移的原子性, 实际上需要保证迁移要么成功， 要么失败

### 方案2： 转成低版本的DUMP格式, 伪码如下

```
let kvs = parse(RDB)
for (key, val) in kvs
    let dump-val = convertToDump(val)
    let lower-dump-val = convertToLowerDump(dump-val)
    // execute migration
    redis.RESTORE(key, lower-dump-val)
```

此方案的优点:

1. 实现原子性迁移 (在多数迁移工具中这条相当重要)
2. 迁移的格式紧凑, 减少迁移带宽

此方案的缺点:

1. 实现复杂
2. 大Key迁移需要保证迁移工具的内存足够，以及调整源以及目标端redis的相应参数


** 注: Redis-replicator与Redis-rdb-CLI都是采用方案2进行迁移**

## 3. Redis-replicator如何做降级迁移

代码示例：

```java
// 从redis-7.0.0 降级迁移到 redis-6.0.0
Replicator replicator = new RedisReplicator("redis://127.0.0.1:6379");
replicator.setRdbVisitor(new DumpRdbVisitor(replicator, 9));
replicator.addEventListener(new EventListener() {
    @Override
    public void onEvent(Replicator replicator, Event event) {
        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair dkv = (DumpKeyValuePair) event;
            byte[] serialized = dkv.getValue();
            // use redis RESTORE command
            // to migrate serialized data to target redis.
        }
    }
});
replicator.open();
```

示例解析:

```java  
// 创建一个DumpRdbVisitor的Rdb解析器, 将rdbv10格式解析成rdbv9的格式
replicator.setRdbVisitor(new DumpRdbVisitor(replicator, 9));
```

```java  
// 注册事件监听器, 这里得到的serialized数据就是降级成rdbv9的数组
// 可以直接使用RESTORE命令迁移到目标库
replicator.addEventListener(new EventListener() {
    @Override
    public void onEvent(Replicator replicator, Event event) {
        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair dkv = (DumpKeyValuePair) event;
            byte[] serialized = dkv.getValue();
        }
    }
});
```

## 4. Redis-rdb-CLI如何做降级迁移

```shell
# Redis-rdb-CLI内部实现是依赖Redis-replicator的
# 迁移的第一步即使改配置文件/path/to/redis-rdb-cli/conf/redis-rdb-cli.conf
# 将dump_rdb_version从-1改到9
$ sed -i 's/dump_rdb_version=-1/dump_rdb_version=9/g' /redis-rdb-cli/conf/redis-rdb-cli.conf

# 执行迁移命令,  此时迁移的数据会自动降级成低版本的DUMP格式
$ rmt -s redis://com.redis7:6379 -m redis://com.redis6:6379 -r
```

## 5. 已知问题与限制

**不能降级迁移低版本redis不存在的数据类型**. 比如不能将MODULE格式迁移到redis4.0版本以下, 将STREAM格式迁移到redis5.0版本以下. 除此之外无其他限制