Change Log
==========

Version 2.7.2 *(2020-12-23)*
----------------------------

Fixes:
* Crash on read caused by R8 optimization

Improvements:
* Add nullability annotations

Thanks Keita Watanabe, Nabil Mosharraf, Ahmet Türk

Version 2.7.1 *(2020-05-11)*
----------------------------

Fixes:
* Ignore backup files in getAllKeys
* Fix exception on simultaneous write and destroy

Thanks Alexandre Boucey, Mohamed Wael and other contributors to make this happen!


Version 2.6 *(2017-10-21)*
----------------------------

New API:
* `Paper.bookOn(path)` to set custom storage location;
* `book.getPath()` or `book.getPath(key)` to get path for content of book or key.
    
Improvements:
* simultaneous read/write for different keys, up to 97% performance gain per thread.
* name change: use `book.contains(key)` instead of deprecated `book.exist(key)`
    
Thanks [@hiperioncn](https://github.com/hiperioncn) and [@cezar-carneiro](https://github.com/cezar-carneiro) for your contribution!


Version 2.5 *(2017-09-21)*
----------------------------

* (!) Fixed crash on data migration when switching lib from 1.x to 2.x
* (!) Fixed possible data loss on failed read attempt. 

Version 2.1 *(2017-06-01)*
----------------------------

* Get timestamp of last update using `book.lastModified(key)`;
* Set log level for internal Kryo serializer using `Paper.setLogLevel()` or `book.setLogLevel()` ;
* (!) Fixed exception on read data on Android N+;


Thanks @aaronpoweruser and @fiskurgit for contrib!

Version 2.0 *(2016-10-24)*
----------------------------

* Update internal Kryo serializer to 4.0. The data format is changed, but Paper supports backward data compatibility automatically;
* Now 58% less methods count : [4037](http://www.methodscount.com/?lib=io.paperdb%3Apaperdb%3A2.0);
* Depends on data structure you may experience faster reading but slower writing.


Version 1.5 *(2016-04-28)*
----------------------------

 * Save all the things! No more restriction to use classes only having no-arg constructor.
 * Custom serializers can be added using `Paper.addSerializer()`.
 * Kotlin is fully supported now, including saving `data class`es. Saving lambdas is not supported.


Version 1.1 *(2015-11-27)*
----------------------------

 * New ```Paper.book().getAllKeys()``` api
 * Proguard config for lib itself is included in aar.


Version 1.0 *(2015-09-15)*
----------------------------

 * New multi-book API.
 * 0.9 API is still supported and marked as deprecated.
 * Unsafe possibility to write null values is disabled.

 *NOTE:* Data storage format is unchanged. You can easily use files created within version 0.9.
