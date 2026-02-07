package edu.Loopi.interfaces;

import edu.Loopi.entities.User;

public interface IAuthService {
    User login(String email, String password);
    boolean register(User user);
    boolean resetPassword(String email, String newPassword);
    boolean verifyEmail(String email);
    String generateResetToken(String email);
    boolean verifyResetToken(String email, String token);
    boolean updatePassword(String email, String newPassword);
    boolean checkEmailExists(String email);
    boolean checkPhoneExists(String phone);
}