# jdk8升级jdk11

## step 1: 替换不支持的api
  
```java  
sun.misc.SharedSecrets 替换为jdk.internal.misc.SharedSecrets
sun.misc.Cleaner 替换为jdk.internal.ref.Cleaner
...

```
  
## step 2: 检查maven版本, maven版本升级到3.5.0或以上, 要求maven依赖的jdk也升级到11
  
`mvn -version`  
  
```xml  
Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-04T03:39:06+08:00)
Maven home: C:\install\apache-maven-3.5.0
Java version: 11.0.1, vendor: Oracle Corporation
Java home: C:\Java\jdk-11.0.1
Default locale: zh_CN, platform encoding: GBK
OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
```
  
## step 3: 升级maven的compiler插件到3.8.1
  
## step 4: 把一些不支持的api导出成ALL-UNNAMED, 并把source和target改成11
  
```xml  

    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.7.0</version>
            <configuration>
                <compilerArgs>
                    <arg>--add-exports=java.base/sun.nio.ch=ALL-UNNAMED</arg>
                    <arg>--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED</arg>
                    <arg>--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED</arg>
                    <arg>--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED</arg>
                </compilerArgs>
                <source>11</source>
                <target>11</target>
            </configuration>
        </plugin>
    </plugins>
```
  
注意用到`javax.annotation.PostConstruct`, `javax.annotation.Resources`, `javax.annotation.Resource`, `javax.annotation.PreDestroy` 等注解需要单独依赖jakarta等相关Jar包
```java  

<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>1.3.5</version>
</dependency>

```

利用jdk12+的jdeps检测依赖了哪些内部包（jdk11的jdeps有[bug](https://bugs.openjdk.java.net/browse/JDK-8207162)）, `$project`代表你自己工程编译好的jar包
```java  
jdeps --multi-release 11 --jdk-internals -R --class-path 'libs/*' $project
```

结果类似如下, 凡是用到`jdk8internals`的包都要检查运行时有没有被调用到; 如果没被调用, 可以安全升级（因为不兼容）

```java  
$ jdeps  --multi-release 11 --jdk-internals -R --class-path './dep/*' ./lib/*.jar

Warning: split package: javax.xml.parsers jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: javax.xml.transform jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: javax.xml.transform.dom jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: javax.xml.transform.sax jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: javax.xml.transform.stream jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.css jrt:/jdk.xml.dom ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.events jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.html jrt:/jdk.xml.dom ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.ranges jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.stylesheets jrt:/jdk.xml.dom ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.traversal jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.w3c.dom.views jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.xml.sax jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.xml.sax.ext jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
Warning: split package: org.xml.sax.helpers jrt:/java.xml ./dep/xml-apis-1.0.b2.jar
aeron-all-1.21.2.jar -> jdk.unsupported
   io.aeron.driver.PublicationImage                   -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.driver.buffer.MappedRawLog                -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.logbuffer.ExclusiveTermAppender           -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.logbuffer.HeaderWriter                    -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.logbuffer.NativeBigEndianHeaderWriter     -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.logbuffer.TermAppender                    -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.samples.EmbeddedBufferClaimIpcThroughput$Subscriber -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.samples.EmbeddedExclusiveBufferClaimIpcThroughput$Subscriber -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.samples.EmbeddedExclusiveIpcThroughput$Subscriber -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.samples.EmbeddedExclusiveVectoredIpcThroughput$Subscriber -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.samples.EmbeddedIpcThroughput$Subscriber  -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.aeron.status.ReadableCounter                    -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.BufferUtil                              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.ExpandableArrayBuffer                   -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.ExpandableDirectByteBuffer              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.UnsafeAccess                            -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.AbstractConcurrentArrayQueue -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ManyToManyConcurrentArrayQueue -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ManyToOneConcurrentArrayQueue -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ManyToOneConcurrentLinkedQueue -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ManyToOneConcurrentLinkedQueuePadding1 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ManyToOneConcurrentLinkedQueuePadding1$Node -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.MappedResizeableBuffer       -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.OneToOneConcurrentArrayQueue -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ShutdownSignalBarrier        -> sun.misc.Signal                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ShutdownSignalBarrier        -> sun.misc.SignalHandler                             JDK internal API (jdk.unsupported)
   org.agrona.concurrent.SigInt                       -> sun.misc.Signal                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.SigInt                       -> sun.misc.SignalHandler                             JDK internal API (jdk.unsupported)
   org.agrona.concurrent.SigIntBarrier                -> sun.misc.Signal                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.SigIntBarrier                -> sun.misc.SignalHandler                             JDK internal API (jdk.unsupported)
   org.agrona.concurrent.UnsafeBuffer                 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.broadcast.BroadcastReceiver  -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.broadcast.BroadcastTransmitter -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.status.AtomicCounter         -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.status.UnsafeBufferPosition  -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.agrona.concurrent.status.UnsafeBufferStatusIndicator -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
aspectjweaver-1.9.4.jar -> jdk.unsupported
   org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
concurrentlinkedhashmap-lru-1.4.2.jar -> jdk.unsupported
   com.googlecode.concurrentlinkedhashmap.ConcurrentHashMapV8 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.googlecode.concurrentlinkedhashmap.ConcurrentHashMapV8$1 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.googlecode.concurrentlinkedhashmap.ConcurrentHashMapV8$TreeBin -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
freemarker-2.3.29.jar -> java.xml
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xml.internal.utils.PrefixResolver JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.XPath            JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.XPathContext     JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.objects.XBoolean JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.objects.XNodeSet JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.objects.XNull    JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.objects.XNumber  JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.objects.XObject  JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport    -> com.sun.org.apache.xpath.internal.objects.XString  JDK internal API (java.xml)
   freemarker.ext.dom.SunInternalXalanXPathSupport$1  -> com.sun.org.apache.xml.internal.utils.PrefixResolver JDK internal API (java.xml)
guava-28.1-jre.jar -> jdk.unsupported
   com.google.common.cache.Striped64                  -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.cache.Striped64$1                -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.cache.Striped64$Cell             -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.LittleEndianByteArray$UnsafeByteArray -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.LittleEndianByteArray$UnsafeByteArray$1 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.LittleEndianByteArray$UnsafeByteArray$2 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.LittleEndianByteArray$UnsafeByteArray$3 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.Striped64                   -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.Striped64$1                 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.hash.Striped64$Cell              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.primitives.UnsignedBytes$LexicographicalComparatorHolder$UnsafeComparator -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.primitives.UnsignedBytes$LexicographicalComparatorHolder$UnsafeComparator$1 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper$1 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
kryo-4.0.2.jar -> jdk8internals
kryo-4.0.2.jar -> java.base
kryo-4.0.2.jar -> jdk.unsupported
   com.esotericsoftware.kryo.io.UnsafeInput           -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.io.UnsafeMemoryInput     -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.io.UnsafeMemoryInput     -> sun.nio.ch.DirectBuffer                            JDK internal API (java.base)
   com.esotericsoftware.kryo.io.UnsafeMemoryOutput    -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.io.UnsafeMemoryOutput    -> sun.nio.ch.DirectBuffer                            JDK internal API (java.base)
   com.esotericsoftware.kryo.io.UnsafeOutput          -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.FieldSerializerUnsafeUtilImpl -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeBooleanField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeByteField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeCharField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeDoubleField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeFloatField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeIntField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeLongField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeObjectField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeRegionField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeShortField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.serializers.UnsafeCacheFields$UnsafeStringField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.util.FastestStreamFactory -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.util.UnsafeUtil          -> sun.misc.Cleaner                                   JDK internal API (jdk8internals)
   com.esotericsoftware.kryo.util.UnsafeUtil          -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.esotericsoftware.kryo.util.UnsafeUtil          -> sun.nio.ch.DirectBuffer                            JDK internal API (java.base)
   com.esotericsoftware.kryo.util.UnsafeUtil$1        -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
logback-classic-1.2.3.jar -> jdk8internals
   ch.qos.logback.classic.spi.PackagingDataCalculator -> sun.reflect.Reflection                             JDK internal API (jdk8internals)
lz4-java-1.6.0.jar -> jdk.unsupported
   net.jpountz.util.UnsafeUtils                       -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
msgpack-core-0.8.20.jar -> jdk.unsupported
   org.msgpack.core.buffer.DirectBufferAccess         -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.msgpack.core.buffer.MessageBuffer              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.msgpack.core.buffer.MessageBufferBE            -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
netty-all-4.1.47.Final.jar -> java.base
netty-all-4.1.47.Final.jar -> jdk.unsupported
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.util.ObjectIdentifier                 JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.AlgorithmId                      JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateAlgorithmId           JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateIssuerName            JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateSerialNumber          JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateSubjectName           JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateValidity              JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateVersion               JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.CertificateX509Key               JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.X500Name                         JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.X509CertImpl                     JDK internal API (java.base)
   io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator -> sun.security.x509.X509CertInfo                     JDK internal API (java.base)
   io.netty.util.internal.CleanerJava9                -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.CleanerJava9$1              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.CleanerJava9$2              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.PlatformDependent$Mpsc$1    -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.PlatformDependent0          -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.PlatformDependent0$1        -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.PlatformDependent0$2        -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.PlatformDependent0$3        -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.PlatformDependent0$5        -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueueConsumerNodeRef -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueueProducerNodeRef -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueProducerFields -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.LinkedQueueNode -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerLimitField -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
objenesis-2.5.1.jar -> jdk.unsupported
   org.objenesis.instantiator.sun.UnsafeFactoryInstantiator -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
protobuf-java-3.5.1.jar -> jdk.unsupported
   com.google.protobuf.UnsafeUtil                     -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.protobuf.UnsafeUtil$1                   -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.protobuf.UnsafeUtil$JvmMemoryAccessor   -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   com.google.protobuf.UnsafeUtil$MemoryAccessor      -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
reactor-core-3.2.11.RELEASE.jar -> jdk8internals
reactor-core-3.2.11.RELEASE.jar -> jdk.unsupported
   reactor.core.publisher.MultiProducerRingBuffer     -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   reactor.core.publisher.RingBuffer                  -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   reactor.core.publisher.RingBufferFields            -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   reactor.core.publisher.Traces$SharedSecretsCallSiteSupplierFactory$TracingException -> sun.misc.JavaLangAccess                            JDK internal API (jdk8internals)
   reactor.core.publisher.Traces$SharedSecretsCallSiteSupplierFactory$TracingException -> sun.misc.SharedSecrets                             JDK internal API (jdk8internals)
   reactor.core.publisher.UnsafeSequence              -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   reactor.core.publisher.UnsafeSupport               -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
spring-core-5.2.8.RELEASE.jar -> jdk.unsupported
   org.springframework.objenesis.instantiator.sun.UnsafeFactoryInstantiator -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.springframework.objenesis.instantiator.util.DefineClassHelper$Java8 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
   org.springframework.objenesis.instantiator.util.UnsafeUtils -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
thorin-common-0.0.1-SNAPSHOT.jar -> java.base
thorin-common-0.0.1-SNAPSHOT.jar -> jdk.unsupported
   cn.nextop.thorin.common.util.Buffers               -> jdk.internal.ref.Cleaner                           JDK internal API (java.base)
   cn.nextop.thorin.common.util.Buffers               -> sun.nio.ch.DirectBuffer                            JDK internal API (java.base)
   cn.nextop.thorin.common.util.Unsafes               -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)

Warning: JDK internal APIs are unsupported and private to JDK implementation that are
subject to be removed or changed incompatibly and could break your application.
Please modify your code to eliminate dependence on any JDK internal APIs.
For the most recent update on JDK internal API replacements, please check:
https://wiki.openjdk.java.net/display/JDK8/Java+Dependency+Analysis+Tool

JDK Internal API                         Suggested Replacement
----------------                         ---------------------
jdk.internal.ref.Cleaner                 Use java.lang.ref.PhantomReference @since 1.2 or java.lang.ref.Cleaner @since 9
sun.misc.Cleaner                         Use java.lang.ref.PhantomReference @since 1.2 or java.lang.ref.Cleaner @since 9
sun.misc.JavaLangAccess                  Removed. See http://openjdk.java.net/jeps/260
sun.misc.SharedSecrets                   Removed. See http://openjdk.java.net/jeps/260
sun.misc.Signal                          See http://openjdk.java.net/jeps/260
sun.misc.SignalHandler                   See http://openjdk.java.net/jeps/260
sun.misc.Unsafe                          See http://openjdk.java.net/jeps/260
sun.reflect.Reflection                   Use java.lang.StackWalker @since 9
sun.security.x509.X500Name               Use javax.security.auth.x500.X500Principal @since 1.4

```

## step 5: 构建

一些关于字节码的依赖包升级
```java  

<dependency>
    <groupId>org.javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.23.1-GA</version>
</dependency>

<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.3.0</version>
</dependency>

```

升级完成之后maven打包  

`mvn clean install -Dmaven.test.skip=true`  
  

## step 6: 运行时

运行时可能会有如下cglib警告 
```java  
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by net.sf.cglib.core.ReflectUtils$1 
         (file:/C:/Users/chenby/.m2/repository/cglib/cglib/3.3.0/cglib-3.3.0.jar) 
         to method java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
WARNING: Please consider reporting this to the maintainers of net.sf.cglib.core.ReflectUtils$1
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```

处理这个警告需要在运行时添加`--add-opens=$module/$package=ALL-UNNAMED`, 根据警告上的`java.lang.ClassLoader.defineClass`关键字, 如上警告运行时需要添加`--add-opens=java.base/java.lang=ALL-UNNAMED`, 以便让cglib可以反射调用私有方法.  
具体参考[What's the difference between --add-exports and --add-opens in Java 9?](https://stackoverflow.com/questions/44056405/whats-the-difference-between-add-exports-and-add-opens-in-java-9)  
  
根据jdeps的依赖分析, 以及cglib需要添加的参数, 总结出实际运行时需要添加如下参数  

```java  
--add-opens=java.base/java.lang=ALL-UNNAMED 
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED 
--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED 
--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED 
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED 
--add-opens=java.base/sun.security.util=ALL-UNNAMED 
--add-opens=java.base/sun.security.x509=ALL-UNNAMED 
--add-opens=java.xml/com.sun.org.apache.xpath.internal=ALL-UNNAMED 
--add-opens=java.xml/com.sun.org.apache.xml.internal.utils=ALL-UNNAMED 
```

一些废弃的jvm options, 如下参数已经删除或者废弃. 其中`-XX:+AggressiveOpts` 标记为`deprecated`但仍然能够使用. `snmp`已经完全废弃. gc相关的参数可以另行替代  
```java  
-Dcom.sun.management.snmp.port=$port 
-Dcom.sun.management.snmp.acl=false 
-Dcom.sun.management.snmp.interface=0.0.0.0 
-XX:+AggressiveOpts 
-verbose:gc -XX:+PrintGCCause -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
```

替代如上gc相关参数, 可以把stdout改成`file=/path/to/gc.log` 把相关gc日志保存到指定文件, 例子只输出到stdout
```java  
-Xlog:gc,safepoint:stdout:time
```
替换-XX:+AggressiveOpts 为 `-XX:AutoBoxCacheMax=20000 -XX:BiasedLockingStartupDelay=500`

# 一些有用的链接

* [All You Need To Know For Migrating To Java 11](https://blog.codefx.org/java/java-11-migration-guide/)
* [jigsaw cheat-sheet](https://zeroturnaround.com/rebellabs/java-9-modules-cheat-sheet)
* [maven-compiler-plugin/examples/module-info](https://maven.apache.org/plugins/maven-compiler-plugin/examples/module-info.html)
* [maven-jmod-plugin](https://maven.apache.org/plugins/maven-jmod-plugin/usage.html)
* [jlink](https://docs.oracle.com/javase/9/tools/jlink.htm#JSWOR-GUID-CECAC52B-CFEE-46CB-8166-F17A8E9280E9)
* [creating-a-modular-jar](https://www.packtpub.com/mapt/book/application_development/9781786461407/3/03lvl1sec29/creating-a-modular-jar)
* [jdeps](https://docs.oracle.com/javase/9/tools/jdeps.htm#JSWOR690)
* [how-to-build-java-9-dependencies-from-maven-dependencies](https://stackoverflow.com/questions/47080660/how-to-build-java-9-dependencies-from-maven-dependencies)
