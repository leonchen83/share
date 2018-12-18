# etcd 与 分布式锁

## etcd 简介
  
etcd是高可用的分布式键值(key-value)数据库, 由go语言实现, etcd实现了Raft分布式一致性协议, 大多被用来作为Kubernetes以及微服务的服务发现组件. 与之类似的是雅虎开源的zookeeper. 用java编写, 实现了Paxos分布式一致性协议.
同样的Tidb的Tikv组件, 也用rust语言实现了一个更为高效的Raft协议. 被当作Tidb的基础设施.  
  
### CAP
* 强一致性(Consistency)
* 可用性(Availability)
* 分区容错性(Partition Tolerance)
  
etcd 与 zookeeper 都满足其中的CP  

### 基本操作

```java  

export ETCDCTL_API=3

etcdctl --endpoints=192.168.1.124:2379 member list
etcdctl --endpoints=192.168.1.124:2379 put foo "Hello World!"
etcdctl --endpoints=192.168.1.124:2379 get foo
etcdctl --endpoints=192.168.1.124:2379 del key
etcdctl --endpoints=192.168.1.124:2379 get web --prefix
etcdctl --endpoints=192.168.1.124:2379 lock resource --ttl=5

```

### gRPC协议

* [gRPC API](https://coreos.com/etcd/docs/latest/learning/api.html)
* [gRPC concurrent API](https://coreos.com/etcd/docs/latest/dev-guide/api_concurrency_reference_v3.html)

#### gRPC API

* LeaseGrantRequest -> LeaseGrantResponse
* LeaseRevokeRequest -> LeaseRevokeResponse
* LeaseKeepAliveRequest -> LeaseKeepAliveResponse

#### gRPC concurrent API

* LockRequest -> LockResponse
* UnlockRequest -> UnlockResponse

### rest协议

etcd支持grpc-gateway, 提供一层rest服务, 接受到rest请求之后, 翻译成gRPC请求, 发送到etcd-server, 再将gRPC返回值翻译成rest response返回给客户端  
  
[Swagger](https://coreos.com/etcd/docs/latest/dev-guide/apispec/swagger/rpc.swagger.json)  

```java  

<<COMMENT
https://www.base64encode.org/
foo is 'Zm9v' in Base64
bar is 'YmFy'
COMMENT

curl -L http://localhost:2379/v3alpha/kv/put \
	-X POST -d '{"key": "Zm9v", "value": "YmFy"}'
# {"header":{"cluster_id":"12585971608760269493","member_id":"13847567121247652255","revision":"2","raft_term":"3"}}

curl -L http://localhost:2379/v3alpha/kv/range \
	-X POST -d '{"key": "Zm9v"}'
# {"header":{"cluster_id":"12585971608760269493","member_id":"13847567121247652255","revision":"2","raft_term":"3"},"kvs":[{"key":"Zm9v","create_revision":"2","mod_revision":"2","version":"1","value":"YmFy"}],"count":"1"}

# get all keys prefixed with "foo"
curl -L http://localhost:2379/v3alpha/kv/range \
	-X POST -d '{"key": "Zm9v", "range_end": "Zm9w"}'
# {"header":{"cluster_id":"12585971608760269493","member_id":"13847567121247652255","revision":"2","raft_term":"3"},"kvs":[{"key":"Zm9v","create_revision":"2","mod_revision":"2","version":"1","value":"YmFy"}],"count":"1"}

```

## raft 简介

 * [raft animation](http://thesecretlivesofdata.com/raft/)  

## jetcd 简介

jetcd是etcd的官方java客户端, 实现了etcd的gRPC的API, 与etcd-server进行通信. 结合etcd-server端DNS SRV discovery, 可以进行etcd的服务发现, 及failover设置.

```xml  

    <dependency>
        <groupId>com.coreos</groupId>
        <artifactId>jetcd-core</artifactId>
        <version>0.0.2</version>
    </dependency>

```

## 应用 jetcd 实现分布式锁

### Lock步骤

1. 发送LeaseGrantRequest 申请一个持续ttl时间的 leaseId. LeaseGrantRequest(ttl) -> LeaseGrantResponse(leaseId)
2. 发送LockRequest 并设置第一步申请的leaseId以及要lock的资源, 向服务器发送lock请求. LockRequest(resource, leaseId) -> LockResponse(key)
3. 如果第二步成功, 那么返回一个key, 说明锁住了相应的资源,跳转到步骤4 如果第二步超时, 说明这个资源正在被其他的锁锁住, 跳转到步骤5
4. 如果已经获得了锁, 那么定期发送LeaseKeepAliveRequest(leaseId), 维持这个锁的获得时间, 如果这个请求发送失败, 那么在ttl的时间后释放锁, 跳转到步骤1
5. 如果这个资源被其他的资源锁住, 那么发送LeaseRevokeRequest(leaseId) 请求, 撤销掉申请的租期, 然后定期 间隔固定时间跳转到步骤1

### Unlock步骤

1. 锁住资源的时候会返回一个key, 通过这个key发送UnlockRequest(key)

### 实现的一些细节

* 一个实现 gondor工程下的EtcdNamedLocker
* ttl
* retries
* heartbeat interval

```java  
ttl, retries, heartbeat interval 的一个错误配置  
ttl = 8000
retries = 3
heartbeat interval = 2000

get lock |-----------ttl-------------|
         |--2s--|retry1|--2s--|retry2|--2s--|retry3| release lock
         |---------------------------|其他模块在此时获得了锁
         
一个正确的配置
ttl = 8000
retries = 3
heartbeat interval = 1000

get lock |-------------------------ttl---------------------------|
         |--1s--|retry1|--1s--|retry2|--1s--|retry3|release lock
         |-------------------------------------------------------|其他模块在此时获得了锁
```

## etcd 集群服务发现

etcd服务端设置DNS+SRV  
  
```java  
# nslookup                     
> set type=srv
> etcd.nextop.cn

etcd.nextop.cn  service = 10 10 2379 etcd02.nextop.cn.
etcd.nextop.cn  service = 10 10 2379 etcd03.nextop.cn.
etcd.nextop.cn  service = 10 10 2379 etcd01.nextop.cn.
```

实现jetcd的URIResolverLoader接口以及URIResolver接口, 在URIResolver接口的实现中, 应用jndi的dns discovery发现服务端的多台etcd-server

```java  

Hashtable<String, String> env = new Hashtable<>();
env.put("java.naming.provider.url", "dns:");
env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
final DirContext context = new InitialDirContext(env);
Attributes attributes = context.getAttributes(authority, new String[]{"SRV"});
NamingEnumeration<?> it = attributes.get("srv").getAll();

while (it.hasMore()) {
    // 10 10 2379 etcd01.nextop.cn.
    String record = (String)it.next();
    
}
				
```

## jetcd gRPC failover的原理

1. 选取上面解析的多个host, port中的一个连接发送请求
2. 当发送请求的连接失效后, 选取下一个host port创建连接发送请求
3. 当所有连接失效后, 调用NameResolver.refresh, 重新解析dns的地址获得有效host port, 重新执行步骤1

## references
 * [etcd](https://github.com/etcd-io/etcd/blob/master/Documentation/docs.md)
 * [jetcd](https://github.com/etcd-io/jetcd)  
 * [raft raw paper](https://ramcloud.atlassian.net/wiki/download/attachments/6586375/raft.pdf)  
 * [raft paper chinese version](https://github.com/maemual/raft-zh_cn/blob/master/raft-zh_cn.md)  
 * [raft resources](https://raft.github.io/)  
 * [raft animation](http://thesecretlivesofdata.com/raft/)  
 * [raft paper shorter version](https://www.usenix.org/conference/atc14/technical-sessions/presentation/ongaro)  
 * [raft blog](http://www.thinkingyu.com/articles/Raft/)  