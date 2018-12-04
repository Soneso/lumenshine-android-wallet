package com.soneso.lumenshine.model.entities

data class UserCredentialsEntity(
        val username: String,
        val tfaSecret: String,
        val password: String,
        val registrationCompleted: Boolean
)