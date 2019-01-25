package de.weimarnetz.registrator.services;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {

    private static final int LOG_ROUNDS = 8;

    public String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    @Deprecated
    public boolean isPasswordValid(String password, String hashedPassword) {
        // we do not want to use the password feature anymore, so let's return true
        return true;
    }


}
