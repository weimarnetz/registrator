package de.weimarnetz.registrator.configuration;

import lombok.Data;

@Data
public class NetworkInformation {

    private int maxNodeNumber;
    private int minNodeNumber;
    private int leaseDays;
}
