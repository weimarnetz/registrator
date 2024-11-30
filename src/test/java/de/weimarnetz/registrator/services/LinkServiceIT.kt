package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.IntegrationTestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class LinkServiceIT : IntegrationTestConfig() {
    private val linkService: LinkService = LinkService()

    @BeforeEach
    fun init() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
    }

    @Test
    fun getNodeLocation() {
        // when
        val location = linkService.getNodeLocation("ffweimar", 52)

        // then
        assertThat(location).isEqualTo("/ffweimar/knoten/52")
    }

    @Test
    fun getNodeLocationUri() {
        // when
        val locationUri = linkService.getNodeLocationUri("ffweimar", 52)

        // then
        assertThat(locationUri).hasToString("http://localhost/ffweimar/knoten/52")
    }
}