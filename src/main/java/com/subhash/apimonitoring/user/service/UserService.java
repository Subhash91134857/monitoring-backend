package com.subhash.apimonitoring.user.service;



import com.subhash.apimonitoring.common.pagination.PaginationResponse;
import com.subhash.apimonitoring.exception.BusinessException;
import com.subhash.apimonitoring.exception.ResourceNotFoundException;
import com.subhash.apimonitoring.role.repository.RoleRepository;
import com.subhash.apimonitoring.user.dto.UserDTO;
import com.subhash.apimonitoring.user.entity.User;
import com.subhash.apimonitoring.user.mapper.UserMapper;
import com.subhash.apimonitoring.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    @Transactional(readOnly = true)
    public PaginationResponse<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return PaginationResponse.fromPage(users.map(userMapper::toDto));
    }
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toDto(user);
    }
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email));
        return userMapper.toDto(user);
    }
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setDepartment(userDTO.getDepartment());
        if (userDTO.getRole() != null) {
            roleRepository.findByName(userDTO.getRole())
                    .ifPresent(user::setRole);
        }
        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());
        return userMapper.toDto(updatedUser);
    }
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect", "INVALID_PASSWORD");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(true);
        user.resetFailedAttempts();
        userRepository.save(user);
        log.info("User activated: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public PaginationResponse<UserDTO> searchUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(search, pageable);
        return PaginationResponse.fromPage(users.map(userMapper::toDto));
    }
}
