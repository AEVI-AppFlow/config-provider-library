# AEVI AppFlow Config Provider Library

This repo contains an Android library that can be used to build upon to implement a content provider for FPS.

## Including in your project

This library is as of `v2.1.1` published to the Github packages repository. This is in response to `bintray` and `jcenter` shutting down in May 2021.
Version `2.1.1` is functionally equivalent to the last published `jcenter()` version (`2.1.0`).

If you require access to an earlier version once `jcenter` has shut down, please contact AEVI for assistance.

Unfortunately Github enforces authentication for retrieving Github packages, even for public repositories. This means that you must
use a valid Github user and generate a personal access token with `read:packages` ticked and provide as credentials as per below.
If you do not have a Github user or require assistance with this, please contact AEVI for support.

In your root project `build.gradle` file, add

```
repositories {
    maven {
        name = "AEVI-repos"
        url = uri("https://maven.pkg.github.com/aevi-appflow/*")
        credentials {
            username = <your Github username>
            password = <your Github personal access token with `read:packages` enabled>
        }
    }
}
```

In the gradle dependencies section of your application

```
implementation "com.aevi.sdk.pos.flow.config:config-provider-library:${latestVersionNo}"
```

See [Github packages](https://github.com/orgs/AEVI-AppFlow/packages?repo_name=config-provider-library) for list of published artifacts.

## Android studio

The API and applications use gradle 4.8 for building. Due to a bug in this version building in Android Studio will cause the following
error:

```text
Configuration on demand is not supported by the current version of the Android Gradle plugin since you are using Gradle version 4.6 or above. Suggestion: disable configuration on demand by setting org.gradle.configureondemand=false in your gradle.properties file or use a Gradle version less than 4.6.
```

In order to disable configuration on demand in Android Studio it must be configured in the settings for Android Studio not the
`gradle.properties` file as described above. You can disable this setting by navigating to

```text
Settings - Build, Execution, Deployment - Compiler - Configure on demand
```

and deselecting the check box.


## Bugs and Feedback

For bugs, feature requests and discussion please use [GitHub Issues](https://github.com/Aevi-AppFlow/config-provider-library/issues)

## LICENSE

Copyright 2018 AEVI International GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.