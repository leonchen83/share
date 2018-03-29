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
  
## step 4: 把一些不支持的api导出成ALL-UNNAMED, 并把source和target改成9
  
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
                <source>9</source>
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