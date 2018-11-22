package com.soneso.lumenshine.networking.dto.exceptions

import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.networking.dto.Parse
import com.soneso.lumenshine.util.LsException
import okhttp3.ResponseBody

class ServerException(private val errorBody: ResponseBody?, throwable: Throwable?) : LsException(throwable) {

    private var body: List<ValidationError>? = null
    var displayMessage: String = ""
        private set

    constructor(errorBody: ResponseBody?) : this(errorBody, null) {
        body = parseBody()
        code = body?.get(0)?.code ?: ErrorCodes.UNKNOWN
        displayMessage = body?.get(0)?.message ?: ""
    }

    constructor(throwable: Throwable?) : this(null, throwable)

    constructor(code: Int) : this(null, null) {
        this.code = code
    }

    var code: Int = 0
        private set

    private fun parseBody(): List<ValidationError>? {

        return try {
            val javaType = OBJECT_MAPPER.typeFactory.constructCollectionType(List::class.java, ValidationError::class.java)
            val list = OBJECT_MAPPER.readValue<List<ValidationError>>(errorBody?.string(), javaType)
            if (list.isEmpty()) {
                return null
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val TAG = "ServerException"
        val OBJECT_MAPPER = Parse.createMapper()
    }
}