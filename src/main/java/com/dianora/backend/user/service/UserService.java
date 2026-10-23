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

    public UserResponse createUser(UserRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user with this email already exists"
            );
        }

        User user = new User(
                request.fullName().trim(),
                email,
                request.phone() == null ? null : request.phone().trim()
        );

        return mapToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return mapToResponse(findUser(userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse updateUser(Long userId, UserRequest request) {

        User user = findUser(userId);
        String email = request.email().trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already used by another user"
                    );
                });

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(
                request.phone() == null ? null : request.phone().trim()
        );

        return mapToResponse(userRepository.save(user));
    }

    public void deactivateUser(Long userId) {
        User user = findUser(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + userId
                ));
    }

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