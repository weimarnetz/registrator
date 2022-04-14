package de.weimarnetz.registrator.services

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

private const val LOG_ROUNDS = 8

@Component
class PasswordService {
    fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS))
    }
}