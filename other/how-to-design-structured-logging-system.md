# 如何设计结构化日志系统

1. 系统定位与设计总则
2. 选择一款日志实现库和一款API
3. 设计结构化日志的一些常见技巧
4. 总结

## 1. 系统定位与设计总则

### 1.1. Library与Project

定位一个工程是Library和Project可以根据此工程是否被其他工程以maven dependency的方式依赖，
如果被另一个工程依赖，那么此工程可基本确定为Library。 如果不被另一个工程依赖，一般会独立部署到一台服务器上；那么此工程可确定为Project。
针对Library和Project构建的结构化日志系统是不同的。

### 1.2. 总则1： 避免喋喋不休

在\<Classic Shell Scripting\>这本书中，提到过UNIX软件工具的几个设计原则。其中有一条对结构化日志系统的设计有很好的指导意义：  
  
**避免喋喋不休**  
  
\[ 软件工具的执行过程不该像在“聊天”（chatty）。不要将“开始处理”（start processing），“即将完成”（almost done）
或是“处理完成”（finished processing）这类信息放进程序的标准输出（至少这不该是默认状态）。
若每个工具都将自己的信息送至标准错误输出，那么整个屏幕就会布满一堆无用的过程信息。**在工具程序的世界里，没有消息就是好消息** \]  
  
设计结构化日志系统与UNIX软件工具的设计哲学类似。避免无用的过程日志占据硬盘空间和混淆错误定位是非常必要的。
我们需要记录日志主要是为了排查将来可能遇到的问题与bug，那么设计一个避免冗余的结构化日志系统是非常必要的。

### 1.3. 总则2： 要携带足够的信息

这句总则看上去和第一条总则是矛盾的，并且像一句废话，但我所见满足此条标准的项目并不多；
实际上在有限的日志中携带足够的信息是必要的，因为出问题要从有限的日志信息中挖掘问题的原因。
因此更要精细的设计呈现出的日志格式，使其避免冗余又包含足够的信息（我知道这很难）。

### 1.4. 适用于Library的规则

1. Library中，异常尽量抛出去交给应用层处理不要自己记录错误日志。因为Library自己记录日志的话，有一定概率不能和应用层日志记录方式保持一致。
2. 可以保留一定的info级别的日志，但是最好提供verbose开关，以便用户动态调整，类似于`if (verbose()) logger.info("info")`。
3. 在Library中，不可以依赖日志实现库只可以依赖API，比如不能依赖`log4j-core`以及桥接库`log4j-jcl`等，因为容易在应用层与用户自己的日志实现库冲突。
4. 清楚自己的Library使用的目标用户和使用环境，根据这些条件选择一款合适的API比如流行的`slf4j-api`或者`commons-logging`，选择的标准是尽量和用户其他依赖的Library的API保持一致，
防止一个工程出现多个API。第二个选择标准是API稳定，长期保持一个版本，不会造成依赖多个版本的API的问题（`slf4j-api`更新略频繁，大项目中很容易出现多个版本）。

### 1.5. 适用于Project的规则

1. Project中，要选择一款适用于你们项目或者公司传统的日志实现库，比如你司在`logback`上有一定积累和开发了一定的周边工具，那么不要另类选择`log4j2`。不一致也会带给软件开发一定伤害。
2. 要对异常日志进行包装，如果你们已经非常了解某一类异常产生的原因，可以不用记录异常堆栈，只记录自己包装的error message，更好的让日志满足总则1和2。
实际上日志应该只记录Unexpected Exception的堆栈信息。
3. 对不同的日志分文件显示， 比如访问日志分为（xx-system.access）， session日志为(xx-system.session)跟踪日志分为(xx-system.trace)，事件日志分为(xx-system.event)，其他日志分为(xx-system.log)。
4. 和Library一样，Project日志对单个功能也要提供可配置的verbose开关，并且这些verbose配置是可推送或可动态配置的。以便追踪一些难以发现的问题。

## 2. 选择一款日志实现库和一款API

### 2.1. 常用日志API和日志实现库

在Java开发中，有一些常见的日志API和日志实现库，相对著名的API库有`commons-logging`，`slf4j-api`以及`log4j-api`。
相对著名的API实现库有`log4j2`以及`logback`。当你选择`slf4j-api`和`logback`组合或者`log4j-api`和`log4j2`组合时，
不需要引入额外的bridge库。否则的话，还需要引入类似于`log4j-jcl`或者`log4j-slf4j-impl`等桥接库。
在一些较新语言的使用者(比如go)看来，Java的日志有诸多的不合理之处， 比如为什么有多套的库来实现日志， 
我们可以深入探讨一下这个历史问题。  
  
历史上Java是没有默认的日志库的，Ceki Gülcü实现了第一版的`log4j`，并在Java开发届变得流行。
在游说jdk团队把`log4j`加入jdk失败后，jdk团队开发了自己的日志实现库`java.util.logging`，
遗憾的是这个自带的日志库并没有获得绝大多数开发的认可，日志实现库在这个时候就走向了分裂。
随后`log4j`也出现了一些问题。Ceki Gülcü转而开发了流行到现在的`slf4j-api`和`logback`，日志的实现库在这个时候更加分裂。
事实上采用`log4j`和`logback`的开源软件日益增多，导致了不可避免的兼容问题。
而apache团队近年来开发的`log4j2`实际上是一款更先进的日志实现库，有优秀的插件系统和更快的性能，但也有自己的API库`log4j-api`，
在未来会一步一步蚕食`logback`的市场份额， 到那时日志实现库和API会更加分裂， 对每个开发者都有影响。
  
这份历史引发了Java开发日志系统的乱象。而`slf4j-api`和`log4j-api`的版本管理更加剧了乱象，原因是由于采用maven的module来管理API和桥接库，即使API没有任何更新也会随着其他库的更新而增加版本。
在一个依赖稍微多一些的Project中，不难发现会依赖多个版本的`slf4j-api`或`log4j-api`； 希望未来`slf4j`团队和`log4j`团队能正视这个问题，将依赖广泛的这两个API库独立管理，使版本更稳定便于依赖。 
下图是`slf4j-api` 和 `log4j-api` 版本的更新状况  
![图1](./image-1.png)  
![图2](./image-2.png)  
    
在看这篇文章的开发者的你，我只希望不要随便造日志库的轮子，如果不小心你的库流行了之后，虽然会给你个人带来巨大声望，但是在开发届的影响有可能是负面的。
试着改善现有的日志系统而不是发明轮子，也是一种贡献。
  

## 3. 设计结构化日志的一些常见技巧

### 3.1. 动态配置

关于动态配置，主流的日志实现库都有定时读取配置的选项，例如在logback中，指定xml文件根节点的scan属性  

```xml  

<configuration scan="true" scanPeriod="30 seconds" > 
  ...
</configuration> 

```

以及log4j2中类似的配置
  
```xml  
  
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="30">
...
</Configuration>

```

但是在实际的开发中，上述动态扫描文件获得配置变更并不常用。在实际的开发中，使用verbose开关是更好的方法，因为可以细粒度的控制日志开关。
使用verbose动态开关，就需要实现配置中心的配置推送，比较简单的方法是使用zookeeper或者数据库。
比如通过zookeeper存储日志verbose配置项，监控zookeeper节点变化来确定verbose是否变更。并刷新到应用程序中。
或者通过数据库存储verbose配置，应用程序定时从数据库中取得verbose配置并刷新到应用程序中。当然，实现这个配置中心，不仅仅verbose配置可推送，其他程序配置也可以实现推送化。
引入zookeeper还是数据库还是其他开源的配置中心取决于你司现有的技术栈，但有一点需要注意，引入一个新工具，也增加了现有系统的复杂度和维护的复杂度，这是一个值得权衡的问题。

### 3.2. 合理规划日志内容

在前文中，已经根据日志类型对日志进行了相应的分类并输出到不同的文件中，文件的具体格式上，还可以做相应的细化。
比如定义相应的前缀来清晰化日志内容，session日志登录和退出登录可以分别加前缀`in ` 与 `out ` session异常日志加`! `，可以通过前缀就了解日志的整体内容。
如下所示  

```xml  

18-01-02 15:22:44.516 INFO  in  account: 118932192, role: admin, ip: 192.168.100.165
18-01-02 15:40:00.116 INFO  out account: 118932192, role: admin, ip: 192.168.100.165
18-01-02 15:40:00.116 ERROR !   account: 118932192, role: admin, ip: 192.168.100.165, cause: NotRegisterException
...

```

对于要打印的对象，统一Java里该对象`toString()`的格式也是一个较好的习惯(事实上我希望你所有的对象的`toString`方法都能统一格式，一致性在软件开发中非常重要)。  
  
如何实现多日志文件也非常简单：每个文件指定不同的appender，并指定不同的logger引用这个appender即可，logback中类似于：  

```xml  

<appender name="SESSION_APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>./log/admapi.%d{yyyy-MM-dd}.session</fileNamePattern>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yy-MM-dd HH:mm:ss.SSS} %msg%n</pattern>
        <outputPatternAsHeader>false</outputPatternAsHeader>
    </encoder>
</appender>

<logger name="SESSION_LOGGER" additivity="false" >
    <appender-ref ref="SESSION_APPENDER" />
</logger>

```

如果你的程序使用`slf4j-api`为API库的话，需要用如下语句引入SESSION_LOGGER  
`Logger session = LoggerFactory.getLogger("SESSION_LOGGER");`

### 3.3. 性能取舍与异步写入

如果你做应用程序的性能分析就会发现，日志是一个IO操作很重的地方，在某些重CPU轻IO的应用中，日志可能会是应用程序的瓶颈（一定要先profiler确定瓶颈）。
在`logback`中，没有默认的异步日志appender，有可能需要扩展RollingFileAppender实现一个异步的RollingFileAppender。当实现好之后再把这个自己实现的appender注册到配置文件中。
如下所示：  
```xml  

<appender name="SESSION_APPENDER" class="your.package.name.YourAsyncRollingFileAppender">
```
  
log4j2实现自定义appender的方法类似，但是log4j2的很多扩展是基于注解的，具体不再详述。  

### 3.4. 关于Library日志设计的技巧

前文在关于Library日志中，有一条规则是选取合适的API库，虽然遵循了这条规则，但还是有可能和客户现有的日志API有冲突怎么办？此节就来深入探讨这个问题。  
  
可以采用类似netty方式来避免API库的冲突，包装API库为[InternalLogger](https://github.com/netty/netty/blob/4.1/common/src/main/java/io/netty/util/internal/logging/InternalLogger.java)和[InternalLoggerFactory](https://github.com/netty/netty/blob/4.1/common/src/main/java/io/netty/util/internal/logging/InternalLoggerFactory.java)。
然后再把所有的主流API都包装一遍，用户可以自己指定用哪个API如`InternalLoggerFactory.setDefaultFactory(CommonsLoggerFactory.INSTANCE);`  

## 4. 总结

以上就是针对Library和Project的设计结构化日志系统的技巧。在实践中读者可能找到更适合自己公司的日志最佳实践。本文不是教条的要求读者完全遵守，仅仅起到一个指导意义。谢谢。