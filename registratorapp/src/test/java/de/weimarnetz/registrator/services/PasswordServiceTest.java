package de.weimarnetz.registrator.services;

import org.junit.Test;
import org.mockito.InjectMocks;

import de.weimarnetz.MockitoTest;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordServiceTest extends MockitoTest {

    @InjectMocks
    private PasswordService passwordService;

    @Test
    public void encryptPassword() throws Exception {
        // when
        String encryptPassword = passwordService.encryptPassword("test");

        // then
        assertThat(encryptPassword).matches("^\\$2a\\$.{56}$");
    }

    @Test
    public void validPasswordTest() throws Exception {
        // given
        String encryptPassword = passwordService.encryptPassword("test");

        // when
        boolean passwordValid = passwordService.isPasswordValid("test", encryptPassword);

        // then
        assertThat(passwordValid).isTrue();
    }

    @Test
    public void invalidPasswordTest() throws Exception {
        // given
        String encryptPassword = passwordService.encryptPassword("test");

        // when
        boolean passwordValid = passwordService.isPasswordValid("test123", encryptPassword);

        // then
        assertThat(passwordValid).isTrue();
    }


}