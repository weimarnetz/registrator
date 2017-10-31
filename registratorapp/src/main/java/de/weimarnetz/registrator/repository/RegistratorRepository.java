package de.weimarnetz.registrator.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

import de.weimarnetz.registrator.model.Node;

public interface RegistratorRepository extends CrudRepository<Node, Long> {

    Node findByNumberAndNetwork(int number, String network);

    Node findByNetworkAndMac(String network, String mac);

    List<Node> findAllByNetwork(String network);

    List<Node> findAllByNetworkOOrderByLastSeenDesc(String network);
}
