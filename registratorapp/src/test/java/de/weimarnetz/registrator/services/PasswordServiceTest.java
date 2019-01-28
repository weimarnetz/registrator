package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import de.weimarnetz.MockitoTest;

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

}