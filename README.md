# retrofit-coroutines

## DEPRECATED

This project is discontinued and I recommend you use [Jake Wharton's version of an adapter](https://github.com/JakeWharton/retrofit2-kotlin-coroutines-adapter) that basically does the same thing, in a better way:

https://github.com/JakeWharton/retrofit2-kotlin-coroutines-adapter

## Introduction

A [Retrofit][1] Call adapter for [Kotlin][2] [Coroutines][3]

This library allows you to specify your service interface as:
```kotlin
interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Deferred<List<Repo>>
}
```

And then call your service with:
```kotlin
launch(YourCoroutineContext) {
    try {
        val request = gitHubService.listRepos("tinsukE")
        val repos = request.await()
        doSomethingWithRepos(repos)
    } catch (httpException: HttpException) {
        // a non-2XX response was received
    } catch (exception: IOEXception) {
        // a networking or data conversion error
    }
}
```

## But, why?
The beauty of using coroutines for your requests is to improve code readability in cases when you need to combine data from different sources in parallel:
```kotlin
launch(YourCoroutineContext) {
    try {
        val requestA = myService.getA()
        val requestB = myService.getB()
        
        // By awaiting only after creating both requests, you're running them in parallel
        
        val a = requestA.await()
        val b = requestB.await()
        
        val c = doSomethingWithAAndB(a, b)
    } ...
```

Or sequentially:
```kotlin
launch(YourCoroutineContext) {
    try {
        val list = myService.getList().await()
        val secondaryRequests = list.map { myService.getSpecificInformation(element.id) }
        val secondaryInformationList = secondaryRequests.map { it.await() }
        doSomethingWith(secondaryInformationList)
    } ...
```

In the last two examples you get the benefits of not having callbacks all around your code and having a single `try-catch` block handling the errors for any of the requests of the whole operation.

## Configuration

To use the provided adapter, you need to register the adapter factory when creating your `Retrofit` instance:
```kotlin
Retrofit.Builder()
    .baseUrl("https://your.api.com/")
    .addCallAdapterFactory(CoroutinesCallAdapterFactory.create())
    ...
    .build()
```

If you'd like to have more control over the `CoroutineContext` used by the adapter, just create one yourself and pass it to `CoroutinesCallAdapterFactory.create()`. The default value uses a pool with 5 threads.

You can also use `Deferred<Response<YourModel>>` as the return type for the Retrofit interface functions if you'd rather deal with a `Response<>` instance instead of cathing for `HttpException`.

If you want to have more control over how the `Call` gets executed, you can provide a `CoroutinesInterceptor` when creating the adapter factory. Bear in mind that, for most use cases, having an OkHttp `Interceptor` instead is a better solution.

Working examples can be found in the sample project [app][5].

## Setup

Currently available via [JitPack][4].

To use it, add the jitpack maven repository to your `build.gradle` file:
```gradle
repositories {
  ...
  maven { url "https://jitpack.io" }
  ...
}
```
and add the dependency:
```gradle
dependencies {
  ...
  compile 'com.github.tinsukE:retrofit-coroutines:0.5'
  ...
}
```

## License

    Copyright 2017 Angelo Suzuki

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: http://square.github.io/retrofit/
[2]: https://kotlinlang.org/
[3]: https://kotlinlang.org/docs/reference/coroutines.html
[4]: https://jitpack.io
[5]: https://github.com/tinsukE/retrofit-coroutines/tree/master/app
