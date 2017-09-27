# Paper
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Paper-blue.svg?style=flat)](http://android-arsenal.com/details/1/2080)   [![Build Status](https://travis-ci.org/pilgr/Paper.svg?branch=master)](https://travis-ci.org/pilgr/Paper)

Paper is a [fast](#benchmark-results) NoSQL-like storage for Java/Kotlin objects on Android with automatic schema migration support.

![Paper icon](/paper_icon.png)

### What's [new](/CHANGELOG.md) in 2.5
* 
Thanks [@hiperioncn](https://github.com/hiperioncn) for your contribution!

### What's [new](/CHANGELOG.md) in 2.5
(!) This update contains critical fixes, please update your project ASAP!
* Fixed crash on reading data saved with Paper 1.x.
* Fixed potential data loss on failed read attempt. 

### Add dependency
```groovy
compile 'io.paperdb:paperdb:2.5'
```

### Initialize Paper
Should be initialized one time in onCreate() in Application or Activity.

```java
Paper.init(context);
```

### Threading
* `Paper.init()` should be called in UI thread; 
* All other APIs (`write`, `read` etc.) are thread-safe and obviously must be called outside of UI thread. Since of v2.6 simultaneous reading/writing for different `key`s significantly improves the performance (up to 97% per thread). 
 
### Save
Save any data objects, Map, List, HashMap etc. including all the internal data hierarchy. 
Paper creates separate data file for each key.

```java
Paper.book().write("city", "Lund"); // Object
Paper.book().write("task-queue", queue); // LinkedList
Paper.book().write("countries", countryCodeMap); // HashMap etc.
```

### Read
Read data objects, the instantiated class is exactly the one used to save data. Limited changes to the class structure are handled automatically. See [Handle data class changes](#handle-data-structure-changes).

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

### Delete
Delete data for one key.

```java
Paper.book().delete("countries");
```

Completely destroys Paper storage. Requires to call ```Paper.init()``` before usage.

```java
Paper.book().destroy();
```

### Use custom book
You can create custom Book with separate storage using

```java
Paper.book("custom-book")...;
```
Each book is located in separate file folder.

### Get all keys 
Returns all keys for objects in the book.

```
List<String> allKeys = Paper.book().getAllKeys();
```

### Handle data structure changes
Class fields which has been removed will be ignored on restore and new fields will have their default values. For example, if you have following data class saved in Paper storage:

```java
class Volcano {
    public String name;
    public boolean isActive;
}
```

And then you realized you need to change the class like:

```java
class Volcano {
    public String name;
    // public boolean isActive; removed field
    public Location location; // New field
}
```

Then on restore the _isActive_ field will be ignored and new _location_ field will have its default value _null_.

### Exclude fields
Use _transient_ keyword for fields which you want to exclude from saving process.

```java
public transient String tempId = "default"; // Won't be saved
```

### Export/Import
* Use `Paper.book().getPath()` to get path for a folder containing all *.pt files for a given book.
* Use `Paper.book().getPath(key)` to get path for a particular *.pt file containing saved object for a given key.
 Feel free to copy/rewrite those files for export/import purposes. It's your responsibility to finalize file's export/import operations prior accessing data over Paper API.

### Proguard config
* Keep your data classes from modification by Proguard:

```
-keep class your.app.data.model.** { *; }
```

also you can implement _Serializable_ for all your data classes and keep all of them using:

```
-keep class * implements java.io.Serializable { *; }
```

### How it works
Paper is based on the following assumptions:
- Datasets on mobile devices are small and usually don't have relations in between; 
- Random file access on flash storage is very fast;

Paper saves each object for given key in a separate file and every write/read operations write/read the whole file.

The [Kryo](https://github.com/EsotericSoftware/kryo) is used for object graph serialization and to provide data compatibility support.

### Benchmark results
Running [Benchmark](https://github.com/pilgr/Paper/blob/master/paperdb/src/androidTest/java/io/paperdb/benchmark/Benchmark.java) on Nexus 4, in ms:

| Benchmark                 | Paper    | [Hawk](https://github.com/orhanobut/hawk) | [sqlite](http://developer.android.com/reference/android/database/sqlite/package-summary.html) |
|---------------------------|----------|----------|----------|
| Read/write 500 contacts   | 187      | 447      |          |
| Write 500 contacts        | 108      | 221      |          |
| Read 500 contacts         | 79       | 155      |          |

### Limitations
* Circular references are not supported

### Apps using Paper
- [AppDialer](https://play.google.com/store/apps/details?id=name.pilgr.appdialer) – Paper initially has been developed as internal lib to reduce start up time for AppDialer. Currently AppDialer has the best start up time in its class. And simple no-sql-pain data storage layer like a bonus.

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

