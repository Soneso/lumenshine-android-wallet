package com.soneso.lumenshine.util

open class LsException(message: String?, cause: Throwable? = null) : Throwable(message, cause) {

    constructor(message: String) : this(message, null)

    constructor(throwable: Throwable?) : this(null, throwable)
}