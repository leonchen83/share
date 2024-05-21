# Sentry

# 1. 安装与启动

```
export VERSION="24.1.0"
git clone https://github.com/getsentry/self-hosted.git
cd self-hosted
git checkout ${VERSION}
sudo ./install.sh


```

# 2. 各模块简介

```
sentry-self-hosted-nginx-1

sentry-self-hosted-relay-1
sentry-self-hosted-web-1
sentry-self-hosted-worker-1-1

# kafka相关

sentry-self-hosted-subscription-consumer-events-1
sentry-self-hosted-subscription-consumer-metrics-1
sentry-self-hosted-subscription-consumer-transactions-1
sentry-self-hosted-subscription-consumer-generic-metrics-1
sentry-self-hosted-ingest-monitors-1
sentry-self-hosted-ingest-profiles-1
sentry-self-hosted-ingest-occurrences-1
sentry-self-hosted-ingest-replay-recordings-1
sentry-self-hosted-metrics-consumer-1
sentry-self-hosted-events-consumer-1-1
sentry-self-hosted-transactions-consumer-1
sentry-self-hosted-attachments-consumer-1
sentry-self-hosted-generic-metrics-consumer-1
sentry-self-hosted-billing-metrics-consumer-1

# 事件后处理

sentry-self-hosted-post-process-forwarder-errors-1
sentry-self-hosted-post-process-forwarder-issue-platform-1
sentry-self-hosted-post-process-forwarder-transactions-1

# snuba 相关

sentry-self-hosted-snuba-generic-metrics-distributions-consumer-1
sentry-self-hosted-snuba-group-attributes-consumer-1
sentry-self-hosted-snuba-replays-consumer-1
sentry-self-hosted-snuba-api-1
sentry-self-hosted-snuba-replacer-1
sentry-self-hosted-snuba-issue-occurrence-consumer-1
sentry-self-hosted-snuba-generic-metrics-sets-consumer-1
sentry-self-hosted-snuba-errors-consumer-1
sentry-self-hosted-snuba-metrics-consumer-1
sentry-self-hosted-snuba-outcomes-billing-consumer-1
sentry-self-hosted-snuba-spans-consumer-1
sentry-self-hosted-snuba-transactions-consumer-1
sentry-self-hosted-snuba-subscription-consumer-metrics-1
sentry-self-hosted-snuba-outcomes-consumer-1
sentry-self-hosted-snuba-profiling-profiles-consumer-1
sentry-self-hosted-snuba-profiling-functions-consumer-1
sentry-self-hosted-snuba-generic-metrics-counters-consumer-1
sentry-self-hosted-snuba-subscription-consumer-transactions-1
sentry-self-hosted-snuba-subscription-consumer-events-1

# 存储层

sentry-self-hosted-kafka-1
sentry-self-hosted-postgres-1
sentry-self-hosted-clickhouse-1
sentry-self-hosted-zookeeper-1
sentry-self-hosted-symbolicator-1
sentry-self-hosted-redis-1
sentry-self-hosted-memcached-1
sentry-self-hosted-vroom-1

# 定期清理数据

sentry-self-hosted-cron-1
sentry-self-hosted-symbolicator-cleanup-1
sentry-self-hosted-vroom-cleanup-1
sentry-self-hosted-sentry-cleanup-1

# 其他

sentry-self-hosted-smtp-1
graphite
rabbitmq
```

# 3. 事件处理流程

# 4. 配置项以及监控

# 5. 调优

# 6. 扩容