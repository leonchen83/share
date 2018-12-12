# Rabbitmq简介



# Install
```java  
sudo vi /etc/yum.repos.d/rabbitmq_erlang.repo

[rabbitmq-erlang]
name=rabbitmq-erlang
baseurl=https://dl.bintray.com/rabbitmq-erlang/rpm/erlang/21/el/7
gpgcheck=1
gpgkey=https://dl.bintray.com/rabbitmq/Keys/rabbitmq-release-signing-key.asc
repo_gpgcheck=0
enabled=1

```

```java  
sudo vi /etc/yum.repos.d/rabbitmq.repo

[bintray-rabbitmq-server]
name=bintray-rabbitmq-rpm
baseurl=https://dl.bintray.com/rabbitmq/rpm/rabbitmq-server/v3.7.x/el/7/
gpgcheck=0
repo_gpgcheck=0
enabled=1

```

```java  
rpm --import https://github.com/rabbitmq/signing-keys/releases/download/2.0/rabbitmq-release-signing-key.asc

sudo yum install erlang
sudo yum install rabbitmq-server


```

# 开启管理画面

```java  

cd /usr/lib/rabbitmq/bin
sudo ./rabbitmq-plugins enable rabbitmq_management
# 修改参数/etc/rabbitmq/rabbitmq.conf
loopback_users.guest = none

http://192.168.1.242:15672/#/
guest
guest
```

# 启动
```java  

sudo service rabbitmq-server start

```

# 集群配置

```java  

/etc/hosts
192.168.1.241 btc-dev-1
192.168.1.242 btc-dev-2

# copy /var/lib/rabbitmq/.erlang.cookie to other nodes

sudo chmod 400 /var/lib/rabbitmq/.erlang.cookie
sudo chown rabbitmq:rabbitmq /var/lib/rabbitmq/*

# 在主节点之外的其他节点执行以下命令
sudo rabbitmqctl stop_app
sudo rabbitmqctl reset
sudo rabbitmqctl join_cluster rabbit@btc-dev-1
sudo rabbitmqctl start_app

# 在任意节点执行
sudo rabbitmqctl cluster_status
可以看到如下信息

Cluster status of node rabbit@btc-dev-2 ...
[{nodes,[{disc,['rabbit@btc-dev-1','rabbit@btc-dev-2']}]},
 {running_nodes,['rabbit@btc-dev-1','rabbit@btc-dev-2']},
 {cluster_name,<<"rabbit@btc-dev-1">>},
 {partitions,[]},
 {alarms,[{'rabbit@btc-dev-1',[]},{'rabbit@btc-dev-2',[]}]}]

```

```java

```

# 客户端编程

## 生产者

```java

```

## 消费者

```java

```

## 流控

```java  

ConnectionFactory factory = new ConnectionFactory();
Connection connection = factory.newConnection();
connection.addBlockedListener(new BlockedListener() {
    public void handleBlocked(String reason) throws IOException {
        // Connection is now blocked
    }

    public void handleUnblocked() throws IOException {
        // Connection is now unblocked
    }
});

```

## Nio & Oio

## 并发控制

```java  


```

## Failover

```java

```

## Monitor

```java

```

# 服务端细节

## Exchange & Queue

## 内存 & 持久化

## 服务端流控参数

## Producer confirms & Consumer ack

## 性能调优

## 关键配置

## 官方性能测试 & 自测试
