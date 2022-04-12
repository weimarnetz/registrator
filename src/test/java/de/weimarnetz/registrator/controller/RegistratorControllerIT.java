package de.weimarnetz.registrator.controller;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.weimarnetz.registrator.RegistratorApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RegistratorApplication.class)
class RegistratorControllerIT {

    @Inject
    private RegistratorController registratorController;

    @Test
    void getTime() {
        // when
        ResponseEntity<Map<String, Long>> time = registratorController.getTime();

        // then
        assertThat(time.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

}