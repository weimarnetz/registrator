package de.weimarnetz.registrator.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LinkServiceTest {
    @InjectMocks
    private LinkService linkService;

    @Before
    public void init() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    public void getNodeLocation() throws Exception {
        // when
        String location = linkService.getNodeLocation("ffweimar", 52);

        // then
        assertThat(location).isEqualTo("/ffweimar/knoten/52");
    }

    @Test
    public void getNodeLocationUri() throws Exception {
        // when
        URI locationUri = linkService.getNodeLocationUri("ffweimar", 52);

        // then
        assertThat(locationUri.toString()).isEqualTo("http://localhost/ffweimar/knoten/52");
    }

}