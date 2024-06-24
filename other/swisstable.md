## Swiss Table

## HashMap的问题

Node节点占用的内存空间过大
```
java.util.HashMap$Node object internals:
OFF  SZ                         TYPE DESCRIPTION               VALUE
  0   8                              (object header: mark)     N/A
  8   4                              (object header: class)    N/A
 12   4                          int Node.hash                 N/A
 16   4             java.lang.Object Node.key                  N/A
 20   4             java.lang.Object Node.value                N/A
 24   4       java.util.HashMap$Node Node.next                 N/A
 28   4                              (object alignment gap)    
Instance size: 32 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```

Node使用链表指向下一节点，导致内存局部性不够好

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K, V> next;
    
    Node(int hash, K key, V value, Node<K, V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}
```

## 

## 实现

## SIMD简介



## 优化

使用SIMD优化matchH2与matchEmpty, 首先加入参数`--add-modules=jdk.incubator.vector` 与`--enable-preview` 开启Jdk的预览功能Vector

```java
public long matchH2(int offset, byte h2) {
    ByteVector v = ByteVector.fromArray(SPECIES_PREFERRED, data, offset);
    return v.eq(h2).toLong();
}

public long matchEmpty(int offset) {
    ByteVector v = ByteVector.fromArray(SPECIES_PREFERRED, data, offset);
    return v.eq(EMPTY).toLong();
}
```

安装`hsdis`并加入启动参数`-XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*SwissMap.matchH2` 查看生成的汇编代码

```
vmovdqu 0x10(%r10,%r8,1),%ymm0
vpbroadcastb %xmm1,%ymm1
vpcmpeqb %ymm1,%ymm0,%ymm0
```

用位运算模拟SIMD

```java
public static long LO_BITS = 0x0101010101010101L;
public static long HI_BITS = 0x8080808080808080L;

public long matchH2(int offset, byte h2) {
    long v1 = Unsafes.getLong(data, offset, ByteOrder.nativeOrder());
    long v2 = v1 ^ (LO_BITS * h2);
    return (v2 - LO_BITS) & ~v2 & HI_BITS;
}

public long matchEmpty(int offset) {
    long v1 = Unsafes.getLong(data, offset, ByteOrder.nativeOrder());
    long v2 = v1 ^ HI_BITS;
    return (v2 - LO_BITS) & ~v2 & HI_BITS;
}
```

## References

1. [Swiss Tables Design Notes](https://abseil.io/about/design/swisstables)
2. [CppCon 2017: Matt Kulukundis “Designing a Fast, Efficient, Cache-friendly Hash Table, Step by Step”](https://www.youtube.com/watch?v=ncHmEUmJZf4&t=2496s)
3. [Java SIMD](https://vksegfault.github.io/posts/java-simd/)
4. [how-to-see-jit-compiled-code-in-jvm](https://stackoverflow.com/questions/1503479/how-to-see-jit-compiled-code-in-jvm#15146962)
5. [hsdis download](https://chriswhocodes.com/hsdis/)