
package com.dianora.backend.user.service;

import com.dianora.backend.user.dto.UserRequest;
import com.dianora.backend.user.dto.UserResponse;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE USER
    public UserResponse createUser(UserRequest request) {

        String fullName = request.fullName() == null
                ? null
                : request.fullName().trim();

        String phone = request.phone() == null
                ? null
                : request.phone().trim();

        String email = normalizeEmail(request.email());

        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Full name is required"
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number is required"
            );
        }

        if (email != null &&
                userRepository.existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user with this email already exists"
            );
        }

        User user = new User(fullName, email, phone);

        return mapToResponse(userRepository.save(user));
    }

    // GET USER BY ID
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {

        return mapToResponse(findUser(userId));
    }

    // GET ALL USERS
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // UPDATE USER PROFILE
    public UserResponse updateUser(Long userId, UserRequest request) {

        User user = findUser(userId);

        String fullName = request.fullName() == null
                ? null
                : request.fullName().trim();

        String email = normalizeEmail(request.email());

        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Full name is required"
            );
        }

        // Check duplicate email only when email is provided
        if (email != null) {

            userRepository.findByEmailIgnoreCase(email)
                    .filter(existingUser ->
                            !existingUser.getId().equals(userId))
                    .ifPresent(existingUser -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Email is already used by another customer"
                        );
                    });
        }

        // Update profile fields
        user.setFullName(fullName);
        user.setEmail(email);

        // IMPORTANT:
        // Do not update phone number.
        // Phone is the OTP login identity.

        return mapToResponse(userRepository.save(user));
    }

    // DEACTIVATE USER
    public void deactivateUser(Long userId) {

        User user = findUser(userId);

        user.setActive(false);

        userRepository.save(user);
    }

    // FIND USER
    private User findUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found: " + userId
                        )
                );
    }

    // NORMALIZE EMAIL
    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    // RESPONSE MAPPING
    private UserResponse mapToResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
