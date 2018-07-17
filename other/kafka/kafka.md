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

## Partition

1. 结构  

[!img](log_anatomy.png)

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
