package de.weimarnetz.registrator.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import de.weimarnetz.registrator.configuration.NetworksConfiguration;
import de.weimarnetz.registrator.exceptions.NetworkNotFoundException;
import de.weimarnetz.registrator.exceptions.NoMoreNodesException;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class NodeNumberService {

    private final RegistratorRepository registratorRepository;
    private final NetworksConfiguration networksConfiguration;

    public int getNextAvailableNodeNumber(String network) throws NoMoreNodesException, NetworkNotFoundException {
        Pair<Integer, Integer> nodeNumberBoundaries = getNodeNumberBoundaries(network);
        List<Node> nodeList = registratorRepository.findAllByNetwork(network);
        List<Integer> nodeNumbersList = nodeList.stream()
                .map(Node::getNumber)
                .sorted()
                .collect(Collectors.toList());
        if (nodeNumbersList.size() > nodeNumberBoundaries.getRight() - nodeNumberBoundaries.getLeft()) {
            throw new NoMoreNodesException();
        }
        return findFirstMissing(nodeNumbersList, 0, nodeNumbersList.size() - 1, network);

    }

    public boolean isNodeNumberValid(int nodeNumber, final String network) throws NetworkNotFoundException {
        return nodeNumber >= getNodeNumberBoundaries(network).getLeft() && nodeNumber <= getNodeNumberBoundaries(network).getRight();
    }

    private int findFirstMissing(List<Integer> list, int start, int end, String network) throws NetworkNotFoundException {
        int minNodeNumber = getNodeNumberBoundaries(network).getLeft();
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
        if (list.get(mid) == mid + minNodeNumber) {
            return findFirstMissing(list, mid + 1, end, network);
        }

        return findFirstMissing(list, start, mid, network);
    }

    private Pair<Integer, Integer> getNodeNumberBoundaries(String network) throws NetworkNotFoundException {
        if (networksConfiguration.getMap().containsKey(network)) {
            return Pair.of(networksConfiguration.getMap().get(network).getMinNodeNumber(),
                    networksConfiguration.getMap().get(network).getMaxNodeNumber());
        } else {
            throw new NetworkNotFoundException("Network " + network + " doesn't exist");
        }
    }

}
