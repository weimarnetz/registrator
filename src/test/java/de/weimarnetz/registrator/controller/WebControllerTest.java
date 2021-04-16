package de.weimarnetz.registrator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.servlet.ModelAndView;

import de.weimarnetz.MockitoTest;
import de.weimarnetz.registrator.configuration.NetworkInformation;
import de.weimarnetz.registrator.configuration.NetworksConfiguration;
import de.weimarnetz.registrator.exceptions.ResourceNotFoundException;
import de.weimarnetz.registrator.repository.RegistratorRepository;

class WebControllerTest extends MockitoTest {

    private static final String TESTNET = "testnet";
    private static final String FFWEIMAR = "ffweimar";

    @InjectMocks
    private WebController webController;
    @Mock
    private RegistratorRepository registratorRepository;
    @Mock
    private NetworksConfiguration networksConfiguration;

    @BeforeEach
    public void setUp() {
        Map<String, NetworkInformation> map = Map.of(FFWEIMAR, new NetworkInformation(), TESTNET, new NetworkInformation());
        when(networksConfiguration.getMap()).thenReturn(map);
    }

    @Test
    public void displayNodesByNetwork() {
        // given
        when(registratorRepository.findAllByNetwork(TESTNET)).thenReturn(Lists.emptyList());
        // when
        ModelAndView modelAndView = webController.displayNodesByNetwork(TESTNET);

        // then
        assertThat(modelAndView.getViewName()).isEqualTo("allNodes");
        assertThat(modelAndView.getModel()).containsEntry("network", TESTNET);
    }

    @Test
    public void displayNodes() {
        // given
        when(registratorRepository.findAllByNetwork(FFWEIMAR)).thenReturn(Lists.emptyList());
        // when
        ModelAndView modelAndView = webController.displayNodes();

        // then
        assertThat(modelAndView.getViewName()).isEqualTo("allNodes");
        assertThat(modelAndView.getModel()).containsEntry("network", FFWEIMAR);

    }

    @Test
    public void networkNotFound() {
        // when
        assertThrows(ResourceNotFoundException.class, () -> webController.displayNodesByNetwork("nonexistingnetwork"));
    }

}