# java11 升级到 java17

## step 1 替换不支持的API

```java  
jdk.internal.misc.SharedSecrets 替换为 jdk.internal.access.SharedSecrets
jdk.internal.misc.JavaLangAccess 替换为 jdk.internal.access.JavaLangAccess

Unsafe.putObjectRelease 替换为 Unsafe.putReferenceRelease
Unsafe.getObjectAcquire 替换为 Unsafe.getReferenceAcquire
Unsafe.compareAndSetObject 替换为 Unsafe.compareAndSetReference
```

## step 2: 检查maven版本和java版本

`mvn -version`

```xml  
Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
Maven home: C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2022.2.1\plugins\maven\lib\maven3
Java version: 17.0.2, vendor: Oracle Corporation, runtime: C:\Java\jdk-17.0.2
Default locale: zh_CN, platform encoding: UTF-8
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

## step 3: 升级maven的compiler插件

* 把一些不支持的api导出成ALL-UNNAMED, 把source和target标签替改成17, 并加入-parameters参数

```xml  

    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
                <compilerArgs>
                    <arg>-parameters</arg>
                    <arg>--add-exports=java.base/sun.nio.ch=ALL-UNNAMED</arg>
                    <arg>--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED</arg>
                    <arg>--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED</arg>
                    <arg>--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED</arg>
                    <arg>--add-exports=java.base/jdk.internal.access=ALL-UNNAMED</arg>
                </compilerArgs>
                <source>17</source>
                <target>17</target>
            </configuration>
        </plugin>
    </plugins>
```

## step 4: 升级相关的第三方版本

```
spring upgrade to 6.0.3
spring-boot upgrade to 3.0.0
spring-batch upgrade to 5.0.0
mybatis-spring upgrade to 3.0.1
jetty upgrade to 11.0.13
slf4j-api upgrade to 2.0.5
logback upgrade to 1.4.5
```

```xml  
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-api</artifactId>
                <version>2.0.6</version>
            </dependency>
            <dependency>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-core</artifactId>
                <version>1.4.5</version>
            </dependency>
            <dependency>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-classic</artifactId>
                <version>1.4.5</version>
            </dependency>
            <dependency>
                <groupId>org.mybatis</groupId>
                <artifactId>mybatis-spring</artifactId>
                <version>3.0.1</version>
            </dependency>
            <dependency>
                <groupId>org.springframework.batch</groupId>
                <artifactId>spring-batch-core</artifactId>
                <version>5.0.0</version>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter</artifactId>
                <version>3.0.0</version>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
                <version>3.0.0</version>
            </dependency>
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-framework-bom</artifactId>
                <version>6.0.3</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
            <dependency>
                <groupId>org.eclipse.jetty</groupId>
                <artifactId>jetty-bom</artifactId>
                <version>11.0.13</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
```

## step 5: 字节码工具升级

```xml  
            <dependency>
                <groupId>cglib</groupId>
                <artifactId>cglib</artifactId>
                <version>3.3.0</version>
            </dependency>
            <dependency>
                <artifactId>asm</artifactId>
                <groupId>org.ow2.asm</groupId>
                <version>9.4</version>
            </dependency>
            <dependency>
                <groupId>org.ow2.asm</groupId>
                <artifactId>asm-tree</artifactId>
                <version>9.4</version>
            </dependency>
            <dependency>
                <groupId>org.ow2.asm</groupId>
                <artifactId>asm-commons</artifactId>
                <version>9.4</version>
            </dependency>
            <dependency>
                <groupId>org.ow2.asm</groupId>
                <artifactId>asm-analysis</artifactId>
                <version>9.4</version>
            </dependency>
            <dependency>
                <groupId>org.objenesis</groupId>
                <artifactId>objenesis</artifactId>
                <version>3.3</version>
            </dependency>
            <dependency>
                <groupId>org.javassist</groupId>
                <artifactId>javassist</artifactId>
                <version>3.29.2-GA</version>
            </dependency>
            <dependency>
                <groupId>org.aspectj</groupId>
                <artifactId>aspectjrt</artifactId>
                <version>1.9.9.1</version>
            </dependency>
            <dependency>
                <groupId>org.aspectj</groupId>
                <artifactId>aspectjweaver</artifactId>
                <version>1.9.9.1</version>
            </dependency>
```

## step 6: 升级jakarta版本

```xml  
            <!-- jakarta -->
            <dependency>
                <groupId>com.sun.xml.ws</groupId>
                <artifactId>jaxws-rt</artifactId>
                <version>4.0.0</version>
            </dependency>
            <dependency>
                <groupId>jakarta.xml.ws</groupId>
                <artifactId>jakarta.xml.ws-api</artifactId>
                <version>4.0.0</version>
            </dependency>
            <dependency>
                <groupId>jakarta.xml.bind</groupId>
                <artifactId>jakarta.xml.bind-api</artifactId>
                <version>4.0.0</version>
            </dependency>
            <dependency>
                <groupId>jakarta.annotation</groupId>
                <artifactId>jakarta.annotation-api</artifactId>
                <version>2.1.1</version>
            </dependency>
            <dependency>
                <groupId>jakarta.validation</groupId>
                <artifactId>jakarta.validation-api</artifactId>
                <version>3.0.2</version>
            </dependency>
            <dependency>
                <groupId>jakarta.transaction</groupId>
                <artifactId>jakarta.transaction-api</artifactId>
                <version>2.0.1</version>
            </dependency>
```

## step 7: 将javax名空间替换成jakarta名空间

```
javax.validation 替换成jakarta.validation
javax.annotation 替换成jakarta.annotation
javax.persistence 替换成jakarta.persistence
javax.servlet 替换成jakarta.servlet
```

## step 8: 修改logback配置文件

```xml  
<!-- logback 1.4.5不允许在root, logger, appender标签内嵌套if 下面是错误示范 -->
<root level="INFO">
    <appender-ref ref="FILE" /><if condition=""><then><appender-ref ref = "SYSLOG" /></then></if>
</root>

<!-- 需要改成 -->
<root level="INFO">
    <appender-ref ref="FILE" />
</root>

<if condition="">
    <then>
        <root level="INFO"><appender-ref ref="SYSLOG" /></root>
    </then>
</if>
```

## step 9: 关于Spring Multipart配置的更改

```xml
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver"/>

<!-- CommonsMultipartResolver这个类已经在spring 6中被删除, 所以上述配置由如下几条配置替代，替换完之后删除commons-fileupload包  -->

<bean id="multipartResolver" class="org.springframework.web.multipart.support.StandardServletMultipartResolver"/>

<bean id="multipart.factory" class="org.springframework.boot.web.servlet.MultipartConfigFactory">
    <property name="location" value="${java.io.tmpdir}" />
</bean>

<bean id="multipart.element" factory-bean="multipart.factory" factory-method="createMultipartConfig" />


```

spring boot 3中使用`MultipartAutoConfiguration`开启Multipart，并在`application.properties`中配置`spring.servlet.multipart.location`等参数

## step 10: 关于spring batch的变更

* 表结构变更
```sql
-- migrate 4.3 to 5.0
ALTER TABLE BATCH_STEP_EXECUTION ADD CREATE_TIME DATETIME(6) NOT NULL DEFAULT '1970-01-01 00:00:00';
ALTER TABLE BATCH_STEP_EXECUTION MODIFY START_TIME DATETIME(6) NULL;

ALTER TABLE BATCH_JOB_EXECUTION_PARAMS DROP COLUMN DATE_VAL;
ALTER TABLE BATCH_JOB_EXECUTION_PARAMS DROP COLUMN LONG_VAL;
ALTER TABLE BATCH_JOB_EXECUTION_PARAMS DROP COLUMN DOUBLE_VAL;

ALTER TABLE BATCH_JOB_EXECUTION_PARAMS CHANGE COLUMN TYPE_CD PARAMETER_TYPE VARCHAR(100);
ALTER TABLE BATCH_JOB_EXECUTION_PARAMS CHANGE COLUMN KEY_NAME PARAMETER_NAME VARCHAR(100);
ALTER TABLE BATCH_JOB_EXECUTION_PARAMS CHANGE COLUMN STRING_VAL PARAMETER_VALUE VARCHAR(2500);
```

* spring配置变更
```xml
    <!-- spring batch 4.x 配置 -->
    <bean id="batch.job.explorer" class="org.springframework.batch.core.explore.support.JobExplorerFactoryBean">
        <property name="dataSource" ref="batch.datasource"/>
    </bean>

    <!-- spring batch 5.x 配置 依赖了transaction manager -->
    <bean id="batch.job.explorer" class="org.springframework.batch.core.explore.support.JobExplorerFactoryBean">
        <property name="dataSource" ref="batch.datasource"/>
        <property name="transactionManager" ref="batch.transaction.manager"/>
    </bean>
```

* SimpleJobOperator.start(String jobName, String parameters)启动参数变更
```
这个parameters之前的格式是参数逗号分割，key,value用等号分割
5.0之后变为参数空格分割，key,value等号分割，我认为是个bug并提了issue。
```

## step 11: 启动参数
```
如果是用了jdk.internal.misc.SharedSecrets等内部类，那么运行时需要额外添加
--add-opens=java.base/jdk.internal.access=ALL-UNNAMED, 其他参数参照migrate-to-java11.md

-XX:BiasedLockingStartupDelay=500被废弃，请从启动参数中删除

--illegal-access的默认级别改为了deny, 所以一定要把所有用的--add-opens和--add-exports添加到命令行
```

# References

* [springframework upgrade to 6.x](https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x)
* [spring batch upgrade to 5.x](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide)
* [jdk upgrade to 17](https://docs.oracle.com/en/java/javase/17/migrate/getting-started.html)
* [--illegal-access default set to deny](https://openjdk.org/jeps/261#Relaxed-strong-encapsulation)
* [logback nested if element](https://logback.qos.ch/codes.html#nested_if_element)