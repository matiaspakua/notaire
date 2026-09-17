package com.licensis.notaire.adapter.out.persistence.user;

import com.licensis.notaire.application.port.out.user.UserRepositoryPort;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the user slice.
 */
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    public UserPersistenceAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User entity) {
        return userRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Override
    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public List<User> findByStatus(boolean status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public boolean existsByName(String name) {
        return userRepository.existsByName(name);
    }
}
