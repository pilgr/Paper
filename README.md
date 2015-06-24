# Paper
Fast and simple data storage for Android. Supports data upgrade for changed classes automatically.

###Add dependency
```groovy
compile 'io.paperdb:paperdb:0.9'
```

#### Initialize Paper
Should be called in Application's or Activity's onCreate().

```java
Paper.init(context);
```

It's an UI friendly call. All other methods should be used in background thread.

#### Save
Save data object. Your custom classes must have no-arg constructor.
Paper creates separate data file for each key.

```java
Paper.put("city", "Lund"); // Primitive
Paper.put("task-queue", queue); // LinkedList
Paper.put("countries", countryCodeMap); // HashMap
```

#### Read
Read data objects. Instantiates exactly the classes which has been used in saved data. Support limited backward and forward compatibility. See section "Handle data class changes".

```java
String city = Paper.get("city");
LinkedList queue = Paper.get("task-queue");
HashMap countryCodeMap = Paper.get("countries");
```

Use default values if object doesn't exist in the storage.

```java
String city = Paper.get("city", "Kyiv");
LinkedList queue = Paper.get("task-queue", new LinkedList());
HashMap countryCodeMap = Paper.get("countries", new HashMap());
```

#### Delete
Delete data for one key.

```java
Paper.delete("countries");
```

Completely clear Paper storage. Can be executed regardless init() call.

```java
Paper.clear(context);
```

#### Handle data class changes
Removed class's fields are ignored on read and new fields have their default values on create class instance.

#### Excluded fields
Use <i>transient</i> modifier for class fields you don't want to save.

```java
public transient String tempId = "default";
```

#### How it works
Paper based on the following assumptions:
- Saved data on mobile are relatively small;
- Random file access on flash storage is fast.

So each data object is saved in separate file and put/get operations write/read whole file.

The Kryo is used for object graph serialization and to provide data compatibility support.

#### Benchmark results

#### Apps using Paper
AppDialer. Dramatically reduce start up time using Paper.

###License
    Copyright 2015 Aleksey Masny

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

