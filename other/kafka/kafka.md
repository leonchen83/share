# Kafka

## 简介
Apache Kafka 是 一个分布式流处理平台  

1. 消息队列， 可以发布和订阅消息
2. 消息磁盘持久化， 可以订阅其他消费者已消费的消息
3. Connector组件与Stream组件可以将其他第三方应用集成到Kafka中

## 架构

1. 单broker

```$xslt  

                           +-----------+
                           | Zookeeper |
                           +-----------+      
                                 |
                    +---------------------------+
+--------+  topic1  | +---------------------- + |  1...N   +--------+
|Producer|--------->| |  Topic1 Partition 1   | |--------->|Consumer|
+--------+          | |-----------------------| |          +--------+
                    | |  Topic1 Partition 2   | |
                    | +-----------------------+ |
                    |         broker            |
                    | +---------------------- + |
                    | |  Topic2 Partition 1   | |
+--------+  topic2  | |-----------------------| |  1...N   +--------+
|Producer|--------->| |  Topic2 Partition 2   | |--------->|Consumer|
+--------+          | +-----------------------+ |          +--------+
                    +---------------------------+
                                |
                          +------------+
                          |File System |
                          +------------+
                   
```

2. 多broker cluster

```$xslt  
                        +--------------------+
                        |      Zookeeper     |
                        +--------------------+
                           |              |
                    +---------------------------+
+--------+  topic1  | +--------+     +--------+ |  1...N   +--------+
|Producer|--------->| |        |     |        | |--------->|Consumer|
+--------+          | |        |  C  |        | |          +--------+
                    | |        |  l  |        | |
+--------+  topic2  | |Broker1 |  u  |Broker2 | |  1...N   +--------+
|Producer|--------->| |        |  s  |        | |--------->|Consumer|
+--------+          | |        |  t  |        | |          +--------+
                    | |        |  e  |        | |
+--------+  topic3  | |        |  r  |        | |  1...N   +--------+
|Producer|--------->| |        |     |        | |--------->|Consumer|
+--------+          | +--------+     +--------+ |          +--------+
                    +---------------------------+
                          |                 |
                   +------------+     +------------+
                   |File System |     |File System |
                   +------------+     +------------+
                   
```

## 简单使用

## Broker 集群

## Partition

1. 结构  

![img](log_anatomy.png)

2. Partition 和 replica

当一个Topic有多个partition 并且topic创建时设置了`replication-factor` 时， 如下所示  
`> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 2 --partitions 10 --topic demo`  
那么我们查看这个topic时会如下所示
```java  

> bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic demo
Topic:demo      PartitionCount:10       ReplicationFactor:2     Configs:
        Topic: demo     Partition: 0    Leader: 1       Replicas: 1,0   Isr: 1,0
        Topic: demo     Partition: 1    Leader: 2       Replicas: 2,1   Isr: 2,1
        Topic: demo     Partition: 2    Leader: 0       Replicas: 0,2   Isr: 0,2
        Topic: demo     Partition: 3    Leader: 1       Replicas: 1,2   Isr: 1,2
        Topic: demo     Partition: 4    Leader: 2       Replicas: 2,0   Isr: 2,0
        Topic: demo     Partition: 5    Leader: 0       Replicas: 0,1   Isr: 0,1
        Topic: demo     Partition: 6    Leader: 1       Replicas: 1,0   Isr: 1,0
        Topic: demo     Partition: 7    Leader: 2       Replicas: 2,1   Isr: 2,1
        Topic: demo     Partition: 8    Leader: 0       Replicas: 0,2   Isr: 0,2
        Topic: demo     Partition: 9    Leader: 1       Replicas: 1,2   Isr: 1,2
```

* “leader”是负责给定分区所有读写操作的节点。每个节点都是随机选择的部分分区的领导者。
* “replicas”是复制分区日志的节点列表。
* “isr”是一组“同步”replicas（in-sync replicas），是replicas列表的子集。
  
ISR集合中的节点都是和 leader 保持高度一致的，只有这个集合的成员才有资格被选举为 leader，一条消息必须被这个集合所有节点读取并追加到日志中，这条消息才能视为提交。这个ISR集合发生变化会在ZooKeeper持久化，因此这个集合中的任何一个节点都有资格被选为leader。

## 示例代码

1. 依赖
```xml    

<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>1.0.0</version>
</dependency>

```

2. Producer

```java  

Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.100.125:9092");
props.put(ProducerConfig.CLIENT_ID_CONFIG, "demo_producer");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerSerializer");
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
try (Producer<Integer, String> producer = new KafkaProducer<>(props)) {
    for (int i = 0; i < 1000; i++) {
        producer.send(new ProducerRecord<>("demo", i, "value" + i)).get();
    }
}

```

3. Consumer

```java    

Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.100.125:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "demo_group");
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000"); // 自动提交offset
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerDeserializer");
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
try (Consumer<Integer, String> consumer = new KafkaConsumer<>(props)) {
    consumer.subscribe(Arrays.asList("demo"));
    while (true) {
        ConsumerRecords<Integer, String> records = consumer.poll(200);
        if (records == null) continue;
        for (ConsumerRecord<Integer, String> record : records) {
            System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
        }
        // consumer.commitSync(); // 手动同步提交offset
        // consumer.commitAsync(); // 手动异步提交offset
    }
}

```

4. 更精细的控制Producer

实现`Partitioner`接口， 并注册到`KafkaProducer` 可以定制Producer的数据发送到哪些partition。
默认的实现是`DefaultPartitioner`如果这个topic有多个partition， 那么采取轮询的方式写每个partition。

```java  

props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "package.your.Partitioner");

```

5. 更精细的控制Consumer

```java  

// consumer.subscribe(Arrays.asList("demo"));
List<PartitionInfo> list = consumer.partitionsFor("demo");
consumer.assign(list.stream().map(e -> new TopicPartition(e.topic(), e.partition())).collect(toList()));
consumer.seekToBeginning(consumer.assignment()); // 从头开始订阅
while (true) {
    ConsumerRecords<Integer, String> records = consumer.poll(200);
    ...
}

```

## Producer 和 Consumer的重要配置

## 容错

