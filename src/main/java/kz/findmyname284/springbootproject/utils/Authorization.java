package kz.findmyname284.springbootproject.utils;

import java.util.Arrays;

import org.springframework.security.core.userdetails.UserDetails;

import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AuthorizationException;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.UserService;

public class Authorization {
    public static void checkAuthorization(UserService userService, UserDetails userDetails, UserRole... allowedRoles) {
        if (userDetails == null) {
            throw new AuthorizationException("Not authenticated");
        }

        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (currentUser == null) {
            throw new AuthorizationException("User not found");
        }

        if (!Arrays.asList(allowedRoles).contains(currentUser.getRole())) {
            throw new AuthorizationException("Insufficient privileges");
        }
    }

    public static void checkRole(User user, UserRole... allowedRoles) {
        if (!Arrays.asList(allowedRoles).contains(user.getRole())) {
            throw new AuthorizationException("Insufficient privileges");
        }
    }

    public static User getAuthorizationUser(UserService userService, UserDetails userDetails) throws AuthorizationException {
        if (userDetails == null) {
            throw new AuthorizationException("Not authenticated");
        }

        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (currentUser == null) {
            throw new AuthorizationException("User not found");
        }

        return currentUser;
    }
}
