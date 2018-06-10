# Bitty

A small HTTP(S) framework atop [Netty](https://netty.io/), primarily intended for building simple API services.

## Overview

Bitty is intended to be used for handling HTTP(S) requests. For example, you could build a stand-alone API server, or integrate an API service within another server application.

The primary benefit of using Bitty is that it provides an HTTP(S) stack, passing `ClientRequest` and `ServerResponse` objects to a `ServerLogic` implementation for handling. Provide your own `ServerLogic` implementation to respond to messages.

## Caveats

> This is very much a work-in-progress development release!

Bitty itself does not (yet?) include its own _routing_ logic, but a `ServerLogic` implementation could be built to interpret the `ClientRequest` path and dispatch to other registered logic instances, for example.

Presently, `ClientRequest` and `ServerResponse` messages are expected to be strings. Bitty might handle binary data in the future.

Bitty's threading model is currently backed by Netty's _boss_ and _worker_ threads. If `ServerResponse` objects are not handled properly, worker threads might be consumed, and the service might degrade/stall. Bitty may provide its own threading layer in the future, to decouple server logic from network layer processing.

See notes in `BittyExample` and `HttpService` regarding asynchronous startup. Presently, starting a service is asynchronous and provides no Future on which the caller can await. `BittyExample` can fail because it attempts to contact the service after calling start, but before the service is actually listening.

_... and many more_
