# Swiss Table

## 1.HashMap的问题

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

## 2.Swiss table 的结构



### 2.1 matchH2 函数

```
func int[] matchH2(int g, byte h2)
    result = []
    byte[] meta = meta(g)
    
    for i = 0; i < meta.length; i++
        if meta[i] == h2
            result.append(i)

    return result
```

### 2.2 matchEmpty 函数

```
byte EMPTY = -128

func int[] matchEmpty(int g)
    result = []
    byte[] meta = meta(g)
    
    for i = 0; i < meta.length; i++
        if meta[i] == EMPTY
            result.append(i)

    return result
```

## 3.Swiss table 的实现

### 3.1 put方法

```
func void put(Object key, Object val)
    long hash = hash(key)
    
    long h1 = hi57(hash)
    byte h2 = lo07(hash)
    
    int g = h1 % groups.length
    
    for i = g; i < groups.length;
        
        int[] matches = matchH2(g, h2)
        
        for p in matches
            if keyGroup(g)[p] equals key
                // replace
                valGroup(g)[p] = value
                return
        
        matches = matchEmpty(g)
        for p in matches
            // add
            meta(g)[p] = h2
            keyGroup(g)[p] = key
            valGroup(g)[p] = val
            return
        
        i++
        if i >= groups.length
            i = 0
```

### 3.2 get方法

```
func void get(Object key)
    long hash = hash(key)
    
    long h1 = hi57(hash)
    byte h2 = lo07(hash)
    
    int g = h1 % groups.length
    
    for i = g; i < groups.length;
        
        int[] matches = matchH2(g, h2)
        
        for p in matches
            if keyGroup(g)[p] equals key
                Object val = valGroup(g)[p]
                return val
        
        matches = matchEmpty(g)
        
        // fast path
        if len(matches) > 0
            return nil
        
        i++
        if i >= groups.length
            i = 0
```

### 3.3 remove方法

```
func void remove(Object key)
    long hash = hash(key)
    
    long h1 = hi57(hash)
    byte h2 = lo07(hash)
    
    int g = h1 % groups.length
    
    for i = g; i < groups.length;
    
        int[] matches = matchH2(g, h2)
        
        for p in matches
            if keyGroup(g)[p] equals key
            
                keyGroup(g)[p] = nil
                keyGroup(g)[p] = nil
                
                if len(matchEmpty(g)) > 0
                    // deleted
                    meta(g)[p] = EMPTY
                else
                    // mark deleted
                    meta(g)[p] = TOMBSTONE
                return
                    
        matches = matchEmpty(g)
        
        if len(matchEmpty(g)) > 0
            // not found
            return
        
        i++
        if i >= groups.length
            i = 0
```

### 3.4 resize

### 3.5 初始化

## 4. SIMD简介



## 5. 优化

### 5.1 SIMD优化

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

### 5.2 位运算模拟SIMD

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

## 6. References

1. [Swiss Tables Design Notes](https://abseil.io/about/design/swisstables)
2. [CppCon 2017: Matt Kulukundis “Designing a Fast, Efficient, Cache-friendly Hash Table, Step by Step”](https://www.youtube.com/watch?v=ncHmEUmJZf4&t=2496s)
3. [Java SIMD](https://vksegfault.github.io/posts/java-simd/)
4. [how-to-see-jit-compiled-code-in-jvm](https://stackoverflow.com/questions/1503479/how-to-see-jit-compiled-code-in-jvm#15146962)
5. [hsdis download](https://chriswhocodes.com/hsdis/)