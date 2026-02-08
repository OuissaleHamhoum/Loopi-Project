package edu.Loopi.interfaces;

import edu.Loopi.entities.User;
import java.util.List;

public interface IUserService {
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    List<User> getAllUsers();
    User getUserById(int id);
    User authenticate(String email, String password);
    List<User> getUsersByRole(String role);
    boolean emailExists(String email);
    int countUsers();

    // Statistics
    int[] getUserStatistics();
}