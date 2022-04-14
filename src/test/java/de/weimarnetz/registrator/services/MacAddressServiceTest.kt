package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.exceptions.InvalidMacAddressException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

internal class MacAddressServiceTest {
    private val macAddressService: MacAddressService = MacAddressService()

    private fun provideMacAddressesForValidation(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of("02:ca:ff:ee:ba:be", true),
            Arguments.of("02:CA:FF:EE:BA:BE", true),
            Arguments.of("02caffeebabe", true),
            Arguments.of("02caffeebabee", false),
            Arguments.of("02,ca:ff:ee:ba:be", false),
            Arguments.of("02:ka:ff:ee:ba:be", false)
        )
    }

    @ParameterizedTest
    @MethodSource("provideMacAddressesForValidation")
    fun validateValidMacAddress(mac: String?, expectedResult: Boolean) {
        // when
        val validMacAddress = macAddressService.isValidMacAddress(mac)

        // then
        Assertions.assertThat(validMacAddress).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @ValueSource(strings = ["02:ca:ff:ee:ba:be", "02-ca-ff-ee-ba-be", "02-CA-FF-EE-BA-BE", "02caffeebabe"])
    fun normalizeValidMacAddress(mac: String?) {
        // when
        val normalizeMacAddress = macAddressService.normalizeMacAddress(mac)

        // then
        Assertions.assertThat(normalizeMacAddress).isEqualTo("02caffeebabe")
    }

    @Test
    fun normalizeInvalidMacAddress() {
        // when
        org.junit.jupiter.api.Assertions.assertThrows(InvalidMacAddressException::class.java) {
            macAddressService.normalizeMacAddress(
                "02-ka-ff-ee-ba-be"
            )
        }
    }

}