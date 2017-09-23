package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class NetworkVerificationServiceTest {

    @InjectMocks
    private NetworkVerificationService networkVerificationService;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(networkVerificationService, "validNetworks", Lists.newArrayList("ffweimar"));
    }

    @Test
    public void invalidNetworkTest() {
        // when
        boolean networkValid = networkVerificationService.isNetworkValid("testnet");

        // then
        assertThat(networkValid).isFalse();
    }

    @Test
    public void validNetworkTest() {
        // when
        boolean networkValid = networkVerificationService.isNetworkValid("ffweimar");

        // then
        assertThat(networkValid).isTrue();
    }
}