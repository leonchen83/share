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
  
## step 4: 把一些不支持的api导出成ALL-UNNAMED, 并把target改成11
  
```xml  

    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.7.0</version>
            <configuration>
                <compilerArgs>
                    <arg>--add-opens=java.base/sun.nio.ch=ALL-UNNAMED</arg>
                    <arg>--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED</arg>
                    <arg>--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED</arg>
                    <arg>--add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED</arg>
                </compilerArgs>
                <source>11</source>
                <target>11</target>
            </configuration>
        </plugin>
    </plugins>
```
  
注意用到`javax.annotation.PostConstruct`,`javax.annotation.Resources`,`javax.annotation.Resource`,`javax.annotation.PreDestroy`等注解需要单独依赖jakarta等相关Jar包
```java  

<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>1.3.5</version>
</dependency>

```

利用jdk12的jdeps检测依赖了哪些内部包， `$project`代表你自己工程编译好的jar包
```java  
jdeps --jdk-internals -R --class-path 'libs/*' $project
```
  
## step 5: 构建
  
`mvn clean install -Dmaven.test.skip=true`  
  
## step 6: 运行时
  
运行时也要添加如上命令行参数  
  
`java xxx --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED`
  
一些依赖包升级
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
# 一些有用的链接

* [All You Need To Know For Migrating To Java 11](https://blog.codefx.org/java/java-11-migration-guide/)
* [jigsaw cheat-sheet](https://zeroturnaround.com/rebellabs/java-9-modules-cheat-sheet)
* [maven-compiler-plugin/examples/module-info](https://maven.apache.org/plugins/maven-compiler-plugin/examples/module-info.html)
* [maven-jmod-plugin](https://maven.apache.org/plugins/maven-jmod-plugin/usage.html)
* [jlink](https://docs.oracle.com/javase/9/tools/jlink.htm#JSWOR-GUID-CECAC52B-CFEE-46CB-8166-F17A8E9280E9)
* [creating-a-modular-jar](https://www.packtpub.com/mapt/book/application_development/9781786461407/3/03lvl1sec29/creating-a-modular-jar)
* [jdeps](https://docs.oracle.com/javase/9/tools/jdeps.htm#JSWOR690)
* [how-to-build-java-9-dependencies-from-maven-dependencies](https://stackoverflow.com/questions/47080660/how-to-build-java-9-dependencies-from-maven-dependencies)