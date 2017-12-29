# 如何设计结构化日志系统

1. 系统定位与设计总则
2. 选择一款日志实现类和一款API
3. 设计结构化日志的一些常见技巧
4. 总结

## 1. 系统定位与设计总则

### 1.1. Library与Project

定位一个工程是Library和Project可以根据此工程是否被其他工程依赖，依赖是指maven dependency的方式依赖，不是广义的依赖，
如果被另一个工程依赖，那么此工程可基本确定为Library。 如果不被另一个工程依赖，一般会独立部署到一台服务器上；那么此工程可确定为Project。
针对Library和Project构建的结构化日志系统是不同的。

### 1.2. 总则1: 避免喋喋不休

在\<Classic Shell Scripting\>这本书中，提到过UNIX软件工具的几个设计原则。其中有一条对结构化日志系统的设计有很好的指导意义：  
  
**避免喋喋不休**  
  
\[ 软件工具的执行过程不该像在“聊天”（chatty）。不要将“开始处理”（start processing）,“即将完成”（almost done）
或是“处理完成”（finished processing）这类信息放进程序的标准输出（至少这不该是默认状态）。
若每个工具都将自己的信息送至标准错误输出，那么整个屏幕就会布满一堆无用的过程信息。**在工具程序的世界里，没有消息就是好消息** \]  
  
设计结构化日志系统与UNIX软件工具的设计哲学类似。避免无用的过程日志占据硬盘空间和混淆错误定位是非常必要的。
我们需要记录日志主要是为了排查将来可能遇到的问题与bug，那么设计一个避免冗余的结构化日志系统是非常必要的。

### 1.3. 总则2: 要携带足够的信息

这句总则看上去和第一条总则是矛盾的，并且像一句废话，但我所见满足此条标准的项目并不多。
实际上在有限的日志中携带足够的信息是必要的，因为出问题要从有限的日志信息中挖掘问题的原因。
因此更要精细的设计呈现出的日志格式，使其避免冗余又包含足够的信息（我知道这很难）。

### 1.4. 适用于Library的规则

1. Library中，异常尽量抛出去交给应用层处理不要自己记录错误日志。因为Library自己记录日志的话，有一定概率不能和应用层日志记录方式保持一致。
2. 可以保留一定的info级别的日志，但是最好提供verbose开关，以便用户动态调整，类似于`if (verbose()) logger.info("info")`。
3. 在Library中，不可以依赖日志实现类只可以依赖API，比如不能依赖`log4j-core`以及桥接类`log4j-jcl`等，因为容易在应用层与用户自己的日志实现类冲突。
4. 清楚自己的Library使用的目标用户和使用环境，根据这些条件选择一款合适的API比如流行的`slf4j-api`或者`commons-logging`，选择的标准是尽量和用户其他依赖的Library的API保持一致，
防止一个工程出现多个API。第二个选择标准是API稳定，长期保持一个版本，不会造成依赖多个版本的API的问题（`slf4j-api`更新略频繁，大项目中很容易出现多个版本）。

### 1.5. 适用于Project的规则

1. Project中，要选择一款适用于你们项目或者公司传统的日志实现类，比如你司在logback上有一定积累和开发了一定的周边工具，那么不要另类选择log4j2。不一致也会带给软件开发一定伤害。
2. 要对异常日志进行包装，如果你们已经非常了解某一类异常产生的原因，可以不用记录异常堆栈，只记录自己包装的error message，更好的让日志满足总则1和2。
实际上日志应该只记录Unexpected Exception的堆栈信息。
3. 对不同的日志分文件显示， 比如访问日志分为（xxx.access）, 跟踪日志分为(xxx.trace)，事件日志分为(xxx.event)，其他日志分为(xxx.log)。
4. 和Library一样，Project日志对单个功能也要提供可配置的verbose开关，并且这些verbose配置是可推送或可动态配置的。以便追踪一些难以发现的问题。

## 2. 选择一款日志实现类和一款API

1. 常用日志API和日志实现类
2. 需要避免的误区
3. 清楚内部细节

### 2.1. 常用日志API和日志实现类

### 2.2. 需要避免的误区

### 2.3. 清楚内部细节

## 3. 设计结构化日志的一些常见技巧

1. 动态日志开关
2. 合理规划日志文件
3. 合理规划日志内容
4. 合理规划日志级别
5. 性能取舍与异步写入

### 3.1. 动态日志开关

### 3.2. 合理规划日志文件

### 3.3. 合理规划日志内容

### 3.4. 合理规划日志级别

### 3.5. 性能取舍与异步写入

## 4. 总结
