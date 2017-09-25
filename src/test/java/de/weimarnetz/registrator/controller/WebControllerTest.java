package de.weimarnetz.registrator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;

import de.weimarnetz.registrator.configuration.NetworkInformation;
import de.weimarnetz.registrator.configuration.NetworksConfiguration;
import de.weimarnetz.registrator.exceptions.ResourceNotFoundException;
import de.weimarnetz.registrator.repository.RegistratorRepository;

@RunWith(MockitoJUnitRunner.class)
public class WebControllerTest {

    private static final String TESTNET = "testnet";
    private static final String FFWEIMAR = "ffweimar";

    @InjectMocks
    private WebController webController;
    @Mock
    private RegistratorRepository registratorRepository;
    @Mock
    private NetworksConfiguration networksConfiguration;

    @Before
    public void setUp() {
        Map<String, NetworkInformation> map = Maps.newHashMap();
        map.put(FFWEIMAR, null);
        map.put(TESTNET, null);
        when(networksConfiguration.getMap()).thenReturn(map);
    }

    @Test
    public void displayNodesByNetwork() throws Exception {
        // given
        when(registratorRepository.findAllByNetwork(TESTNET)).thenReturn(Lists.emptyList());
        // when
        ModelAndView modelAndView = webController.displayNodesByNetwork(TESTNET);

        // then
        assertThat(modelAndView.getViewName()).isEqualTo("allNodes");
        assertThat(modelAndView.getModel().get("network")).isEqualTo(TESTNET);
    }

    @Test
    public void displayNodes() throws Exception {
        // given
        when(registratorRepository.findAllByNetwork(FFWEIMAR)).thenReturn(Lists.emptyList());
        // when
        ModelAndView modelAndView = webController.displayNodes();

        // then
        assertThat(modelAndView.getViewName()).isEqualTo("allNodes");
        assertThat(modelAndView.getModel().get("network")).isEqualTo(FFWEIMAR);

    }

    @Test(expected = ResourceNotFoundException.class)
    public void networkNotFound() {
        // when
        webController.displayNodesByNetwork("nonexistingnetwork");
    }

}