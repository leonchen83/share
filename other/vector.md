# Vector API

```java
VectorSpecies<Integer> species = IntVector.SPECIES_128;

int n = 4;
int[] i1 = new int[]{1, 2, 3, 4};
int[] i2 = new int[]{4, 3, 2, 1};
int[] result = new int[n];

var v1 = IntVector.fromArray(species, i1, 0);
var v2 = IntVector.fromArray(species, i2, 0);

var v = v1.add(v2);
v.intoArray(result, 0);
```


```java

VectorSpecies<Integer> species = IntVector.SPECIES_128;

int n = 8;
int[] i1 = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
int[] i2 = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
int[] result = new int[n];

for (int index = 0; index < n; index += species.length()) {
    var v1 = IntVector.fromArray(species, i1, index);
    var v2 = IntVector.fromArray(species, i2, index);
    var v = v1.add(v2);
    v.intoArray(result, index);
}

```

```java

VectorSpecies<Integer> species = IntVector.SPECIES_128;

int n = 9;
int[] i1 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
int[] i2 = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1};
int[] result = new int[n];

for (int index = 0; index < n; index += species.length()) {
    var mask = species.indexInRange(index, n);
    var v1 = IntVector.fromArray(species, i1, index, mask);
    var v2 = IntVector.fromArray(species, i2, index, mask);
    var v = v1.add(v2, mask);
    v.intoArray(result, index, mask);
}

```


```java

int n = 9;
int[] i1 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
int[] i2 = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1};
int[] result = new int[n];

int index = 0;
for (; index < species.loopBound(n); index += species.length()) {
    var v1 = IntVector.fromArray(species, i1, index);
    var v2 = IntVector.fromArray(species, i2, index);
    var v = v1.add(v2);
    v.intoArray(result, index);
}

for (; index < n; index++) {
    result[index] = i1[index] + i2[index];
}
```

```
Lane-wise operations: operate on a given lane for two vectors

ADD, SUB, etc... are lane-wise operations

Cross-lane operations: operate on the different lanes of a vector

MAX, MIN, SORT are cross-lanes operations
```
$`norm=\sqrt{x^2+y^2+z^2+...}`$

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