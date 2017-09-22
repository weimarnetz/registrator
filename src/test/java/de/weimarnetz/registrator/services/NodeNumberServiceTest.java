package de.weimarnetz.registrator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import de.weimarnetz.registrator.exceptions.NoMoreNodesException;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

@RunWith(MockitoJUnitRunner.class)
public class NodeNumberServiceTest {

    @Mock
    private RegistratorRepository registratorRepository;

    @InjectMocks
    private NodeNumberService nodeNumberService;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(nodeNumberService, "minNodeNumber", 2);
        ReflectionTestUtils.setField(nodeNumberService, "maxNodeNumber", 10);
    }

    @Test
    public void nextNodeNumberOnEmptyList() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.emptyList());

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(2);
    }

    @Test
    public void nextNodeNumberOnFilledList() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(2, 3, 4, 6)));

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(5);
    }

    @Test
    public void firstNodeNumberOnFilledList() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(3, 4, 6)));

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(2);
    }

    @Test
    public void nextNodeNumberOnFilledListLeftHalf() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(2, 4, 5, 6, 7, 8, 9, 10)));

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(3);
    }

    @Test
    public void nextNodeNumberOnFilledListMiddle() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(2, 3, 4, 5, 7, 8, 9, 10)));

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(6);
    }


    @Test
    public void lastNodeNumberOnFilledList() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(2, 3, 4, 5, 8, 6, 7, 9)));

        // when
        int nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet");

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(10);
    }

    @Test(expected = NoMoreNodesException.class)
    public void noMoreNewNodesOnFilledList() throws NoMoreNodesException {
        // given
        when(registratorRepository.findAllByNetwork("testnet")).thenReturn(Lists.newArrayList(getNodeList(2, 3, 4, 5, 8, 6, 7, 9, 10)));

        // when
        nodeNumberService.getNextAvailableNodeNumber("testnet");
    }

    private List<Node> getNodeList(int... nodeNumbers) {
        List<Node> nodeList = Lists.newArrayList();
        for (int nodeNumber : nodeNumbers) {
            nodeList.add(Node.builder().number(nodeNumber).build());
        }
        return nodeList;
    }

}