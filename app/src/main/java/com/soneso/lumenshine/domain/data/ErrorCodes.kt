package com.soneso.lumenshine.domain.data

object ErrorCodes {

    const val UNKNOWN = 0
    const val LOGIN_EMAIL_NOT_EXIST = 1
    const val LOGIN_INVALID_2FA = 1000
    const val MISSING_TFA = 1009
    const val LOGIN_WRONG_PASSWORD = 99
    const val SIGNUP_EMAIL_ALREADY_EXIST = 1001
}