# Paper
[![Build Status](https://travis-ci.org/pilgr/Paper.svg?branch=master)](https://travis-ci.org/pilgr/Paper) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Paper-blue.svg?style=flat)](http://android-arsenal.com/details/1/2080)

Paper is a [fast](#benchmark-results) NoSQL data storage for Android that lets you save/restore Java/Kotlin objects using efficient Kryo serialization. Object structure changes handled automatically.

![Paper icon](/paper_icon.png)

#### What's [new](/CHANGELOG.md) in 1.5
* Save all the things! No more restriction to use classes only having no-arg constructor.
* Custom serializers can be added using `Paper.addSerializer()`.
* Kotlin is fully supported now, including saving `data class`es. Obviously saving lambdas is not supported.

#### Add dependency
```groovy
compile 'io.paperdb:paperdb:1.5'
```

#### Initialize Paper
Should be initialized one time in onCreate() in Application or Activity.

```java
Paper.init(context);
```

It's OK to call it in UI thread. All other methods should be used in background thread.

#### Save
Save data object.
Paper creates separate data file for each key.

```java
Paper.book().write("city", "Lund"); // Primitive
Paper.book().write("task-queue", queue); // LinkedList
Paper.book().write("countries", countryCodeMap); // HashMap
```

#### Read
Read data objects. Paper instantiates exactly the classes which has been used in saved data. The limited backward and forward compatibility is supported. See [Handle data class changes](#handle-data-structure-changes).

```java
String city = Paper.book().read("city");
LinkedList queue = Paper.book().read("task-queue");
HashMap countryCodeMap = Paper.book().read("countries");
```

Use default values if object doesn't exist in the storage.

```java
String city = Paper.book().read("city", "Kyiv");
LinkedList queue = Paper.book().read("task-queue", new LinkedList());
HashMap countryCodeMap = Paper.book().read("countries", new HashMap());
```

#### Delete
Delete data for one key.

```java
Paper.book().delete("countries");
```

Completely destroys Paper storage. Requires to call ```Paper.init()``` before usage.

```java
Paper.book().destroy();
```

#### Use custom book
You can create custom Book with separate storage using

```java
Paper.book("custom-book")...;
```
Each book is located in separate file folder.

#### Get all keys 
Returns all keys for objects in the book.

```
List<String> allKeys = Paper.book().getAllKeys();
```

#### Handle data structure changes
Class fields which has been removed will be ignored on restore and new fields will have their default values. For example, if you have following data class saved in Paper storage:

```java
class Volcano {
        public String name; // I like Eyjafjallajökull
        public boolean isActive;
    }
```

And then you realized you need to change the class like:

```java
class Volcano {
        public String name; // I like Eyjafjallajökull
        // public boolean isActive; removed field, who cares about volcano activity
        public Location location; // New field
    }
```

Then on restore the _isActive_ field will be ignored and new _location_ field will have its default value _null_.

#### Exclude fields
Use _transient_ keyword for fields which you want to exclude from saving process.

```java
public transient String tempId = "default"; // Won't be saved
```
#### Proguard config
* Keep data classes:

```
-keep class my.package.data.model.** { *; }
```

alternatively you can implement _Serializable_ for all your data classes and keep all of them using:

```
-keep class * implements java.io.Serializable { *; }
```

#### How it works
Paper is based on the following assumptions:
- Saved data on mobile are relatively small;
- Random file access on flash storage is very fast.

So each data object is saved in separate file and write/read operations write/read whole file.

The [Kryo](https://github.com/EsotericSoftware/kryo) is used for object graph serialization and to provide data compatibility support.

#### Benchmark results
Running [Benchmark](https://github.com/pilgr/Paper/blob/master/paperdb/src/androidTest/java/io/paperdb/benchmark/Benchmark.java) on Nexus 4, in ms:

| Benchmark                 | Paper    | [Hawk](https://github.com/orhanobut/hawk) | [sqlite](http://developer.android.com/reference/android/database/sqlite/package-summary.html) |
|---------------------------|----------|----------|----------|
| Read/write 500 contacts   | 187      | 447      |          |
| Write 500 contacts        | 108      | 221      |          |
| Read 500 contacts         | 79       | 155      |          |


#### Apps using Paper
- [AppDialer](https://play.google.com/store/apps/details?id=name.pilgr.appdialer) – Paper _initially_ has been developed to reduce start up time for AppDialer. Currently AppDialer has the best start up time in its class. And simple no-sql-pain data storage layer like a bonus.

### License
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

