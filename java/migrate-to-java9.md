# jdk8升级jdk9

## step 1: 替换不支持的api
  
```java  
sun.misc.SharedSecrets 替换为jdk.internal.misc.SharedSecrets
sun.misc.Cleaner 替换为jdk.internal.ref.Cleaner
...

```
  
## step 2: 检查maven版本, maven版本升级到3.5.0或以上, 要求maven依赖的jdk也升级到9
  
`mvn -version`  
  
```xml  
Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-04T03:39:06+08:00)
Maven home: C:\install\apache-maven-3.5.0
Java version: 9.0.4, vendor: Oracle Corporation
Java home: C:\Java\jdk-9.0.4
Default locale: zh_CN, platform encoding: GBK
OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
```
  
## step 3: 升级maven的compiler插件到3.7.0以上
  
## step 4: 把一些不支持的api导出成ALL-UNNAMED, 并把target改成9
  
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
                    <arg>--add-modules=java.xml.ws.annotation</arg>
                </compilerArgs>
                <source>8</source>
                <target>9</target>
            </configuration>
        </plugin>
    </plugins>
```
  
注意用到`javax.annotation.PostConstruct`,`javax.annotation.Resources`,`javax.annotation.Resource`,`javax.annotation.PreDestroy`等注解需要在pom文件添加`--add-modules=java.xml.ws.annotation`  
  
## step 5: 构建
  
`mvn clean install -Dmaven.test.skip=true`  
  
## step 6: 运行时
  
运行时也要添加如上命令行参数  
  
`java xxx --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-modules=java.xml.ws.annotation`
  
## step 7: 编写新代码时
  
编写新代码最好也按照jdk8的语法等编写, 因为一些字节码工具还没升级, 会导致一些问题(即使按照jdk8的语法写, 由于编译出来的字节码也不保证没问题), 比较好的方式是尽快升级依赖的jar包, 并升级到automatic module或者更严格的module模式.  
  
# 一些有用的链接

* [jigsaw cheat-sheet](https://zeroturnaround.com/rebellabs/java-9-modules-cheat-sheet)
* [maven-compiler-plugin/examples/module-info](https://maven.apache.org/plugins/maven-compiler-plugin/examples/module-info.html)
* [maven-jmod-plugin](https://maven.apache.org/plugins/maven-jmod-plugin/usage.html)
* [jlink](https://docs.oracle.com/javase/9/tools/jlink.htm#JSWOR-GUID-CECAC52B-CFEE-46CB-8166-F17A8E9280E9)
* [creating-a-modular-jar](https://www.packtpub.com/mapt/book/application_development/9781786461407/3/03lvl1sec29/creating-a-modular-jar)
* [jdeps](https://docs.oracle.com/javase/9/tools/jdeps.htm#JSWOR690)
* [how-to-build-java-9-dependencies-from-maven-dependencies](https://stackoverflow.com/questions/47080660/how-to-build-java-9-dependencies-from-maven-dependencies)