package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.RegistratorApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [RegistratorApplication::class])
class LinkServiceIT {
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