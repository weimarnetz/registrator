package de.weimarnetz.registrator.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.weimarnetz.registrator.model.Node;

public interface RegistratorRepository extends CrudRepository<Node, Long> {

    Node findByNumberAndNetwork(int number, String network);

    Node findByNetworkAndMac(String network, String mac);

    List<Node> findAllByNetwork(String network);

    List<Node> findAllByNetworkOrderByLastSeenDesc(String network);
}
