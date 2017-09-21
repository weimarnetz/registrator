package de.weimarnetz.registrator.services;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = SomeClassTestsConfig.class)
public class NodeNumberServiceTest {

    @Mock
    private RegistratorRepository registratorRepository;

    @InjectMocks
    private NodeNumberService nodeNumberService;

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
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(0, 1, 2, 3, 4, 6)));

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


}