package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import de.weimarnetz.MockitoTest;
import de.weimarnetz.registrator.exceptions.InvalidMacAddressException;

public class MacAddressServiceTest extends MockitoTest {

    @InjectMocks
    private MacAddressService macAddressService;

    @Test
    public void validateValidMacAddress() throws Exception {
        // when
        boolean validMacAddress = macAddressService.isValidMacAddress("02:ca:ff:ee:ba:be");

        // then
        assertThat(validMacAddress).isTrue();
    }

    @Test
    public void validateValidUpperCaseMacAddress() throws Exception {
        // when
        boolean validMacAddress = macAddressService.isValidMacAddress("02:CA:FF:EE:BA:BE");

        // then
        assertThat(validMacAddress).isTrue();
    }

    @Test
    public void validatePureString() throws Exception {
        // when
        boolean validMacAddress = macAddressService.isValidMacAddress("02caffeebabe");

        // then
        assertThat(validMacAddress).isTrue();
    }

    @Test
    public void validateWrongSizeString() throws Exception {
        // when
        boolean validMacAddress = macAddressService.isValidMacAddress("02caffeebabee");

        // then
        assertThat(validMacAddress).isFalse();
    }

    @Test
    public void validateInvalidMacAddress() throws Exception {
        // when
        boolean validMacAddress = macAddressService.isValidMacAddress("02:ka:ff:ee:ba:be");

        // then
        assertThat(validMacAddress).isFalse();
    }

    @Test
    public void normalizeValidMacAddress() throws Exception {
        // when
        String normalizeMacAddress = macAddressService.normalizeMacAddress("02:ca:ff:ee:ba:be");

        // then
        assertThat(normalizeMacAddress).isEqualTo("02caffeebabe");
    }

    @Test
    public void normalizeValidMacAddressWithHyphens() throws Exception {
        // when
        String normalizeMacAddress = macAddressService.normalizeMacAddress("02-ca-ff-ee-ba-be");

        // then
        assertThat(normalizeMacAddress).isEqualTo("02caffeebabe");
    }

    @Test
    public void normalizeValidMacAddressWithHyphensAndUppercase() throws Exception {
        // when
        String normalizeMacAddress = macAddressService.normalizeMacAddress("02-CA-FF-EE-BA-BE");

        // then
        assertThat(normalizeMacAddress).isEqualTo("02caffeebabe");
    }

    @Test(expected = InvalidMacAddressException.class)
    public void normalizeInvalidMacAddress() throws Exception {
        // when
        macAddressService.normalizeMacAddress("02-ka-ff-ee-ba-be");

    }

}