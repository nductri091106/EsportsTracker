package com.example.esportstracker.data

import java.security.MessageDigest
import java.util.UUID

object PasswordUtil {
    fun newSalt(): String = UUID.randomUUID().toString()

    fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray())
            .joinToString("") { "%02x".format(it) }
}