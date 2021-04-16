package de.weimarnetz.registrator.services;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {

    private static final int LOG_ROUNDS = 8;

    public String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

}
