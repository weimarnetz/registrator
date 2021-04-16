package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.weimarnetz.MockitoTest;

public class LinkServiceTest extends MockitoTest {
    @InjectMocks
    private LinkService linkService;

    @BeforeEach
    public void init() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    public void getNodeLocation() {
        // when
        String location = linkService.getNodeLocation("ffweimar", 52);

        // then
        assertThat(location).isEqualTo("/ffweimar/knoten/52");
    }

    @Test
    public void getNodeLocationUri() {
        // when
        URI locationUri = linkService.getNodeLocationUri("ffweimar", 52);

        // then
        assertThat(locationUri).hasToString("http://localhost/ffweimar/knoten/52");
    }

}