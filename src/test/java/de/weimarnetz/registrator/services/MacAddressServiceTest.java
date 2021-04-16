package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;

import de.weimarnetz.MockitoTest;
import de.weimarnetz.registrator.exceptions.InvalidMacAddressException;

class MacAddressServiceTest extends MockitoTest {

    @InjectMocks
    private MacAddressService macAddressService;

    private static Stream<Arguments> provideMacAddressesForValidation() {
        return Stream.of(
                Arguments.of("02:ca:ff:ee:ba:be", true),
                Arguments.of("02:CA:FF:EE:BA:BE", true),
                Arguments.of("02caffeebabe", true),
                Arguments.of("02caffeebabee", false),
                Arguments.of("02,ca:ff:ee:ba:be", false),
                Arguments.of("02:ka:ff:ee:ba:be", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMacAddressesForValidation")
    void validateValidMacAddress(String mac, boolean expectedResult) {
        // when
        boolean validMacAddress = macAddressService.isValidMacAddress(mac);

        // then
        assertThat(validMacAddress).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = { "02:ca:ff:ee:ba:be", "02-ca-ff-ee-ba-be", "02-CA-FF-EE-BA-BE", "02caffeebabe" })
    void normalizeValidMacAddress(String mac) {
        // when
        String normalizeMacAddress = macAddressService.normalizeMacAddress(mac);

        // then
        assertThat(normalizeMacAddress).isEqualTo("02caffeebabe");
    }

    @Test
    void normalizeInvalidMacAddress() {
        // when
        assertThrows(InvalidMacAddressException.class, () -> macAddressService.normalizeMacAddress("02-ka-ff-ee-ba-be"));

    }

}