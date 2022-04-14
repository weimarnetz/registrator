package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.exceptions.InvalidMacAddressException
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class MacAddressService {
    fun isValidMacAddress(macAddress: String?): Boolean {
        return macAddress?.matches("^([a-fA-F\\d]{2})([a-f\\dA-F]{2})([a-f\\dA-F]{2})([a-f\\dA-F]{2})([a-f\\dA-F]{2})([a-f\\dA-F]{2})$|^([a-f\\dA-F]{2}):([a-f\\dA-F]{2}):([a-f\\dA-F]{2}):([a-f\\dA-F]{2}):([a-f\\dA-F]{2}):([a-f\\dA-F]{2})$|^([a-f\\dA-F]{2})-([a-f\\dA-F]{2})-([a-f\\dA-F]{2})-([a-f\\dA-F]{2})-([a-f\\dA-F]{2})-([a-f\\dA-F]{2})$|^([\\dA-Fa-f]{4})\\.([\\dA-Fa-f]{4})\\.([\\dA-Fa-f]{4})$".toRegex())
            ?: false
    }

    @Throws(InvalidMacAddressException::class)
    fun normalizeMacAddress(macAddress: String?): String {
        if (!isValidMacAddress(macAddress)) {
            throw InvalidMacAddressException()
        }
        return macAddress!!.replace(":", "").replace("-", "").lowercase(Locale.getDefault())
    }
}