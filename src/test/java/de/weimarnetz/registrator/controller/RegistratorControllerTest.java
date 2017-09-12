package de.weimarnetz.registrator.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RegistratorControllerTest {

    @InjectMocks
    private RegistratorController registratorController;

    @Test
    public void getTime() throws Exception {
        // when
        ResponseEntity<Map<String, Long>> time = registratorController.getTime();

        // then
        assertThat(time.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    @Test
    public void getNode() throws Exception {
    }

}