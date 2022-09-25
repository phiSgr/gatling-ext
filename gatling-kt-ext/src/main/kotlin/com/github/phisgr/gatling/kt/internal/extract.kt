package com.github.phisgr.gatling.kt.internal

import com.github.phisgr.gatling.generic.check.ResponseExtract
import io.gatling.commons.validation.Validation
import io.gatling.javaapi.core.CheckBuilder
import scala.Function1
import scala.Option
import scala.collection.immutable.Seq


/**
 * Helps the [Res] type from the action builder flow into the extraction function `f`.
 * Allows writing `.check({ extract { it.field }.shouldBe("stuff") })`, Kotlin only.
 */
class From<out Res>(
    @PublishedApi internal val name: String,
    @PublishedApi internal val checkType: CheckBuilder.CheckType,
) {
    inline fun <reified X> extract(crossinline f: (Res) -> X?): CheckBuilder.Find<X> =
        extract(checkType, name, X::class.java, toScalaOptionF(f))

    inline fun <reified X> extractMultiple(crossinline f: (Res) -> List<X>?): CheckBuilder.MultipleFind<X> =
        extractMultiple(checkType, name, X::class.java, toScalaSeqOptionF(f))
}

fun <Res, X> extract(
    checkType: CheckBuilder.CheckType,
    name: String,
    clazz: Class<X>,
    f: Function1<Res, Validation<Option<X>>>,
): CheckBuilder.Find<X> =
    CheckBuilder.Find.Default(
        ResponseExtract.extract(f, name),
        checkType,
        clazz,
        null
    )

fun <Res, X> extractMultiple(
    checkType: CheckBuilder.CheckType,
    name: String,
    clazz: Class<X>,
    f: Function1<Res, Validation<Option<Seq<X>>>>,
): CheckBuilder.MultipleFind<X> =
    CheckBuilder.MultipleFind.Default<ResponseExtract, Res, X, X>(
        ResponseExtract.extractMultiple(f, name),
        checkType,
        clazz,
        null
    )
