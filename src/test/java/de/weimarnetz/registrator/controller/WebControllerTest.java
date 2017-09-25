package de.weimarnetz.registrator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import de.weimarnetz.registrator.repository.RegistratorRepository;

@RunWith(MockitoJUnitRunner.class)
public class WebControllerTest {

    private static final String TESTNET = "testnet";
    private static final String FFWEIMAR = "ffweimar";

    @InjectMocks
    private WebController webController;
    @Mock
    private RegistratorRepository registratorRepository;

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

}