package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import de.weimarnetz.MockitoTest;
import de.weimarnetz.registrator.configuration.NetworkInformation;
import de.weimarnetz.registrator.configuration.NetworksConfiguration;

public class NetworkVerificationServiceTest extends MockitoTest {

    @InjectMocks
    private NetworkVerificationService networkVerificationService;
    @Mock
    private NetworksConfiguration networksConfiguration;

    @BeforeEach
    public void setup() {
        Map<String, NetworkInformation> networkInformationMap = Maps.newHashMap("testnet", null);
        when(networksConfiguration.getMap()).thenReturn(networkInformationMap);
    }

    @Test
    public void invalidNetworkTest() {
        // when
        boolean networkValid = networkVerificationService.isNetworkValid("not_our_netz");

        // then
        assertThat(networkValid).isFalse();
    }

    @Test
    public void validNetworkTest() {
        // when
        boolean networkValid = networkVerificationService.isNetworkValid("testnet");

        // then
        assertThat(networkValid).isTrue();
    }
}