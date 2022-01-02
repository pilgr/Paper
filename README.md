# Paper
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Paper-blue.svg?style=flat)](http://android-arsenal.com/details/1/2080)   [![Build Status](https://travis-ci.org/pilgr/Paper.svg?branch=master)](https://travis-ci.org/pilgr/Paper)

Paper's aim is to provide a simple yet [fast](#benchmark-results) object storage option for Android. It allows to use Java/Kotlin classes as is: without annotations, factory methods, mandatory class extensions etc. Moreover adding or removing fields to data classes is no longer a pain – all data structure changes are handled automatically.

![Paper icon](/paper_icon.png)

### Migration to Maven Central
**Library has been moved to Maven Central since service ends for JCenter. Note that group id
has been changed to `io.github.pilgr`. See the updated section below.**  

### Add dependency
```groovy
implementation 'io.github.pilgr:paperdb:2.7.2'
```

RxJava wrapper for Paper is available as a separate lib [RxPaper2](https://github.com/pakoito/RxPaper2). Thanks [@pakoito](https://github.com/pakoito) for it!

### Initialize Paper
Should be initialized once in `Application.onCreate()`:

```java
Paper.init(context);
```

### Threading
* `Paper.init()` should be called in UI thread; 
* All other APIs (`write`, `read` etc.) are thread-safe and obviously must be called outside of UI thread. Reading/writing for different `key`s can be done in parallel. 
 
### Save
Save any object, Map, List, HashMap etc. including all internal objects. Use your existing data classes as is. Note that key is used as file name to store the data and so *cannot* contain symbols like `/`.

```java
List<Person> contacts = ...
Paper.book().write("contacts", contacts);
```

### Read
Read data objects is as easy as

```java
List<Person> = Paper.book().read("contacts");
```
the instantiated class is exactly the one used to save data. Limited changes to the class structure are handled automatically. See [Handle data class changes](#handle-data-structure-changes).

Use default values if object doesn't exist in the storage.

```java
List<Person> = Paper.book().read("contacts", new ArrayList<>());
```

### Delete
Delete data for one key.

```java
Paper.book().delete("contacts");
```

Remove all keys for the given Book. ```Paper.init()``` must be called prior calling `destroy()`.

```java
Paper.book().destroy();
```

### Use custom book
You can create custom Book with separate storage using

```java
Paper.book("for-user-1").write("contacts", contacts);
Paper.book("for-user-2").write("contacts", contacts);
```
Each book is located in a separate file folder.

### Get all keys 
Returns all keys for objects in the book.

```java
List<String> allKeys = Paper.book().getAllKeys();
```

### Handle data structure changes
You can add or remove fields to the class. Then on next read attempt of a new class:
* Newly added fields will have their default values. 
* Removed field will be ignored. 

*Note:* field type changes are not supported.

For example, if you have following data class saved in Paper storage:

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

the _isActive_ field will be ignored on next read and new _location_ field will have its default value as _null_.

### Exclude fields
Use _transient_ keyword for fields which you want to exclude from saving process.

```java
public transient String tempId = "default"; // Won't be saved
```

### Set storage location for Book instances
By default, all the Paper data files are located with all files belonging to your app, at `../you-app-package-name/files`. To save data on SDCard or at any other location you can use new API:
* `Paper.bookOn("/path/to/the/new/location")`
* or `Paper.bookOn("path/to/the/new/location", "book-for-user-1")` to create custom book. 

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

| Benchmark                 | Paper    | [Hawk](https://github.com/orhanobut/hawk) |
|---------------------------|----------|----------|
| Read/write 500 contacts   | 187      | 447      |
| Write 500 contacts        | 108      | 221      |
| Read 500 contacts         | 79       | 155      |

### Limitations
* Circular references are not supported

### Apps using Paper
- [AppDialer](https://play.google.com/store/apps/details?id=name.pilgr.appdialer) – Paper initially has been developed as internal lib to reduce start up time for AppDialer. Currently AppDialer has the best start up time in its class. And simple no-sql-pain data storage layer like a bonus.
- [Busmap](https://play.google.com/store/apps/details?id=com.t7.busmap&hl=en) - This application provide all things you need for travelling by bus in Ho Chi Minh city, Vietnam. While the source code is not opened, it is found that the application use Paper internally to manange the bus stop data, route data, time data,... and more.

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

