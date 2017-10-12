package de.weimarnetz.registrator.services;

import org.springframework.stereotype.Component;

import de.weimarnetz.registrator.exceptions.InvalidMacAddressException;

@Component
public class MacAddressService {

    public boolean isValidMacAddress(String macAddress) {
        return macAddress.matches("^([a-fA-F0-9]{2})([a-f0-9A-F]{2})([a-f0-9A-F]{2})([a-f0-9A-F]{2})([a-f0-9A-F]{2})([a-f0-9A-F]{2})$|^([a-f0-9A-F]{2}):([a-f0-9A-F]{2}):([a-f0-9A-F]{2}):([a-f0-9A-F]{2}):([a-f0-9A-F]{2}):([a-f0-9A-F]{2})$|^([a-f0-9A-F]{2})-([a-f0-9A-F]{2})-([a-f0-9A-F]{2})-([a-f0-9A-F]{2})-([a-f0-9A-F]{2})-([a-f0-9A-F]{2})$|^([0-9A-Fa-f]{4})\\.([0-9A-Fa-f]{4})\\.([0-9A-Fa-f]{4})$");
    }

    public String normalizeMacAddress(String macAddress) throws InvalidMacAddressException {
        if (!isValidMacAddress(macAddress)) {
            throw new InvalidMacAddressException("Mac Address invalid: " + macAddress);
        }
        return macAddress.replace(":", "").replace("-", "").toLowerCase();
    }
}
