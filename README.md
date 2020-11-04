# Gatling-Ext

Extensions to the powerful [Gatling](https://gatling.io/) DSL.

This is a **third party** library.
Please don't annoy the Gatling team with problems in this library.

This library is code-heavy,
and probably goes against the philosophy of Gatling.

## Call Any Java/Scala code

Let's say you are given an SDK which calls a server.
You need to measure the backend performance,
but you don't want to write the HTTP calls again in Gatling
(or implement [a whole new protocol](https://github.com/phiSgr/gatling-grpc)).

With `callbackAction`, `futureAction` and `blockingAction`,
you can measure the time taken by code that
takes callbacks, returns futures, or straight up blocks threads.

## Async-await

My take on [Gatling#3783](https://github.com/gatling/gatling/issues/3783).

## Polling

Repeatedly run some actions.
Allows combining the data saved in the forked virtual user.

# Disclaimer

Actions not running sequentially for a virtual user may break some Gatling code.
In that case I am sorry, but nothing can be done.
**It is not reasonable to ask Gatling to change its code
just because there exists some third party hack.**
