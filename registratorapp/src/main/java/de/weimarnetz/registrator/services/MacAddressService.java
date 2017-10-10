package de.weimarnetz.registrator.services;

import org.springframework.stereotype.Component;

import de.weimarnetz.registrator.exceptions.InvalidMacAddressException;

@Component
public class MacAddressService {

    public boolean isValidMacAddress(String macAddress) {
        return macAddress.matches("^(?:[0-9a-fA-F]{2}([-:]))(?:[0-9a-fA-F]{2}\\1){4}[0-9a-fA-F]{2}$");
    }

    public String normalizeMacAddress(String macAddress) throws InvalidMacAddressException {
        if (!isValidMacAddress(macAddress)) {
            throw new InvalidMacAddressException("Mac Address invalid: " + macAddress);
        }
        return macAddress.replace(":", "").replace("-", "").toLowerCase();
    }
}
