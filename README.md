# AEVI AppFlow Config Provider Library

This repo contains an Android library that can be used to build upon to implement a content provider for FPS.

## DEPRECATION NOTICE

> [!IMPORTANT]
> This repo is as of February 2024 deprecated and further development will take place in a private Aevi repository. Please contact Aevi for any questions or concerns.

## Including in your project

This library is as of `v2.1.5` published to the maven central packages repository.

In your root project `build.gradle` file, add

```
repositories {
    mavenCentral()
}
```

In the gradle dependencies section of your application

```
implementation "com.aevi.sdk.pos.flow.config:config-provider-library:${latestVersionNo}"
```

## Bugs and Feedback

For bugs, feature requests and discussion please use [GitHub Issues](https://github.com/Aevi-AppFlow/config-provider-library/issues)

## LICENSE

Copyright 2022 AEVI International GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
