package de.weimarnetz.registrator.services;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class NodeNumberServiceTest {

    @Mock
    private RegistratorRepository registratorRepository;

    @InjectMocks
    private NodeNumberService nodeNumberService;

    @Inject
    private PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

    @Test
    public void nextNodeNumberOnEmptyList() {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.emptyList());

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(2);
    }

    @Test
    public void nextNodeNumberOnFilledList() {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(2, 3, 4, 6)));

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(5);
    }

    private List<Node> getNodeList(int... nodeNumbers) {
        List<Node> nodeList = Lists.newArrayList();
        for (int nodeNumber : nodeNumbers) {
            nodeList.add(Node.builder().number(nodeNumber).build());
        }
        return nodeList;
    }

    @Configuration
    static class ContextConfiguration {

        @Bean
        public PropertySourcesPlaceholderConfigurer properties() throws Exception {
            final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
            Properties properties = new Properties();

            properties.setProperty("nodenumber.min", "2");
            properties.setProperty("nodenumber.max", "10");

            pspc.setProperties(properties);
            return pspc;
        }

    }

}