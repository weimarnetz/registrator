package de.weimarnetz.registrator.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.weimarnetz.registrator.exceptions.NoMoreNodesException;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

@Component
public class NodeNumberService {

    @Inject
    private RegistratorRepository registratorRepository;
    @Value("${nodenumber.max}")
    private int maxNodeNumber;
    @Value("${nodenumber.min}")
    private int minNodeNumber;

    public int getNextAvailableNodeNumber(String network) throws NoMoreNodesException {
        List<Node> nodeList = registratorRepository.findAllByNetwork(network);
        List<Integer> nodeNumbersList = nodeList.stream()
                .map(Node::getNumber)
                .sorted()
                .collect(Collectors.toList());
        if (nodeNumbersList.size() > maxNodeNumber - minNodeNumber) {
            throw new NoMoreNodesException();
        }
        return findFirstMissing(nodeNumbersList, 0, nodeNumbersList.size() - 1);

    }

    public boolean isNodeNumberValid(int nodeNumber) {
        return nodeNumber >= minNodeNumber && nodeNumber <= maxNodeNumber;
    }

    private int findFirstMissing(List<Integer> list, int start, int end) {
        if (list.isEmpty()) {
            return minNodeNumber;
        }

        if (start > end) {
            return end + 1 + minNodeNumber;
        }

        if (start + minNodeNumber != list.get(start)) {
            return start + minNodeNumber;
        }

        int mid = (start + end) / 2;

        // Left half has all elements from 0 to mid
        if (list.get(mid) == mid + minNodeNumber)
            return findFirstMissing(list, mid + 1, end);

        return findFirstMissing(list, start, mid);
    }

}
