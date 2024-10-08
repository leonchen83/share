# Vector API

```java
private static final Species species = IntVector.SPECIES_PREFERRED;

int n = ...;
int[] i1 = ....; int[] i2 = ....;
int[] result = new int[n];

var v1 = IntVector.fromArray(species, i1, 0);
var v2 = IntVector.fromArray(species, i2, 0);

var v = v1.add(v2);
v.intoArray(result, 0);
```


```java

for(int index = 0; index < i1.length; index += species.length()) {
    var v1 = IntVector.fromArray(species, i1, index);
    var v2 = IntVector.fromArray(species, i2, index);
    var v = v1.add(v2);
    v.intoArray(result, index);
}

```

```java

for(int index = 0; index < i1.length; index += species.length()) {

    var mask = species.indexInRange(index, i1.length);
    var v1 = IntVector.fromArray(species, i1, index, mask);
    var v2 = IntVector.fromArray(species, i2, index, mask);
    var v = v1.add(v2, mask);
    v.intoArray(result, index, mask);
}

```


```java

int index = 0;
for(; index < species.loopBound(i1.length); index += species.length()) {

    var v1 = IntVector.fromArray(species, i1, index);
    var v2 = IntVector.fromArray(species, i2, index);
    var v = v1.add(v2);
    v.intoArray(result, index);
}

for(; index < i1.length; index++) {
    result[index] = i1[index] + i2[index];
}
```

```
Lane-wise operations: operate on a given lane for two vectors

ADD, SUB, etc... are lane-wise operations

Cross-lane operations: operate on the different lanes of a vector

MAX, MIN, SORT are cross-lanes operations
```

```java

float sum = 0f;

for() {
    var v1 = FloatVector.fromArray(...);
    var v2 = v1.multi(v1);
    sum += v2.reduceLanes(VectorOperators.ADD);
}

float norm = Math.sqrt(sum);
```

```java

var sum = FloatVector.zero(species);

for() {
    var v1 = FloatVector.fromArray(...);
    var v2 = v1.multi(v1);
    sum = sum.add(v2);
}

float norm = Math.sqrt(sum.reduceLanes(VectorOperators.ADD));

```

```java

int maxIndex = 0;

for() {
    var mask = v.comare(VectorOperators.GT, 5);
    v.compress(mask).intoArray(result, maxIndex);
    maxIndex += mask.trueCount();
}

```