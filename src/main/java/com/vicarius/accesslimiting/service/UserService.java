package com.vicarius.accesslimiting.service;

import com.vicarius.accesslimiting.model.User;
import com.vicarius.accesslimiting.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.vicarius.accesslimiting.utils.Constants.DAYTIME_END_HOUR;
import static com.vicarius.accesslimiting.utils.Constants.DAYTIME_START_HOUR;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final List<User> elasticUsers;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.elasticUsers = getUsersFromElasticSearch();
    }

    public User createUser(User user) {
        log.info("Creating user: {}", user);
        return userRepository.save(user);
    }

    public User getUser(String userId) {
        log.info("Retrieving user with ID: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }

    public User updateUser(String userId, User updatedUser) {
        log.info("Updating user with ID: {}", userId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            return userRepository.save(user);
        } else {
            log.warn("User with ID {} not found for update", userId);
            return null;
        }
    }

    public void deleteUser(String userId) {
        log.info("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
    }

    private List<User> getUsersFromElasticSearch() {
        log.info("Retrieving users from ElasticSearch");
        List<User> usersFromElasticSearch = List.of(
                new User("John", "Doe"),
                new User("Jane", "Smith")
        );
        usersFromElasticSearch.forEach(user -> log.info("Retrieved user from ElasticSearch: {}", user));
        return usersFromElasticSearch;
    }

    public User consumeQuota(String userId) {
        log.info("Consuming quota for user with ID: {}", userId);
        Optional<User> user;
        if (isDaytime()) {
            user = userRepository.findById(userId);
        } else {
            user = elasticUsers.stream().filter(user1 -> user1.getId().equals(userId)).findFirst();
        }

        return user.map(this::decrementQuota).orElse(null);
    }

    private boolean isDaytime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(DAYTIME_START_HOUR, 0))
                && now.isBefore(LocalTime.of(DAYTIME_END_HOUR, 0));
    }

    private User decrementQuota(User user) {
        log.info("Decrementing quota for user: {}", user.getId());
        User savedUser;
        Optional<User> userFromMySQL = userRepository.findById(user.getId());
        if (userFromMySQL.isPresent()) {
            if (user.getQuota() > 0) {
                user.setQuota(user.getQuota() - 1);
                log.info("Quota consumed for user {}. Remaining quota: {}", user.getId(), user.getQuota());
                savedUser = userRepository.save(user);
            } else {
                log.info("User {} has reached quota limit. User is locked.", user.getId());
                savedUser = null;
            }
        } else {
            if (user.getQuota() > 0) {
                user.setQuota(user.getQuota() - 1);
                log.info("Quota consumed for user {}. Remaining quota: {}", user.getId(), user.getQuota());
                savedUser = user;
            } else {
                log.info("User {} has reached quota limit. User is locked.", user.getId());
                savedUser = null;
            }
        }
        return savedUser;
    }

    public Map<String, Integer> getUsersQuota() {
        log.info("Retrieving quota status for all users");
        Map<String, Integer> userQuotaMap = new HashMap<>();
        userRepository.findAll().forEach(user -> userQuotaMap.put(user.getId(), user.getQuota()));
        elasticUsers.forEach(user -> userQuotaMap.putIfAbsent(user.getId(), user.getQuota()));

        log.info("Quota status for all users:");
        userQuotaMap.forEach((userId, quota) -> log.info("User: {}, Quota: {}", userId, quota));

        return userQuotaMap;
    }
}