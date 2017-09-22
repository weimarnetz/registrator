package de.weimarnetz.registrator.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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

    public int getNextAvailableNodeNumber(String network) {
        List<Node> nodeList = registratorRepository.findAllByNetwork(network);
        List<Integer> nodeNumbersList = nodeList.stream()
                .map(Node::getNumber)
                .sorted()
                .collect(Collectors.toList());
        return findFirstMissing(nodeNumbersList, 0, nodeNumbersList.size() - 1);

    }

    private int findFirstMissing(List<Integer> list, int start, int end) {
        if (list.isEmpty()) {
            return minNodeNumber;
        }

        if (start > end)
            return end + 1;

        if (minNodeNumber - start != list.get(start))
            return start;

        int mid = (start + end) / 2;

        // Left half has all elements from 0 to mid
        if (list.get(mid) == mid)
            return findFirstMissing(list, mid + 1, end);

        return findFirstMissing(list, start, mid);
    }

}
