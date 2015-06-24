# Paper
Data class storage for Android with focus on speed, simplicity and auto adaptation to class changes.

Paper based on a following ideas:
- Data on Android are relatively small. Prefer full read/write instead to partial select.
- Flash storage is fast. Keep one item/collection per file.
- Data class changes like new/removed fields can be handled automatically.

###Add dependency
```groovy
compile 'io.paperdb:paperdb:0.9'
```

#### Initialize Paper
#### Save
#### Read
#### Delete
#### Handle data class changes
#### Excluded fields
#### Benchmark results
##### Credits

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

