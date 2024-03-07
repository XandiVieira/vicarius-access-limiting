package com.vicarius.accesslimiting.service;

import com.vicarius.accesslimiting.model.User;
import com.vicarius.accesslimiting.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private List<User> mockUsers;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        // Create a user object for testing
        User user = new User("John", "Doe");
        user.setId("testId");

        when(userRepository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);

        verify(userRepository, times(1)).save(user);

        assertEquals(user, createdUser);
    }

    @Test
    void testGetUser() {
        // Mocked user object
        User user = new User("John", "Doe");
        user.setId("testId");

        // Mock the findById method of userRepository
        when(userRepository.findById("testId")).thenReturn(Optional.of(user));

        // Call the getUser method
        User retrievedUser = userService.getUser("testId");

        // Verify that the findById method of userRepository was called with the correct userId
        verify(userRepository, times(1)).findById("testId");

        // Verify that the getUser method returns the expected user object
        assertEquals(user, retrievedUser);
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Mock the findById method of userRepository to return empty Optional
        when(userRepository.findById("testId")).thenReturn(Optional.empty());

        // Call the updateUser method
        User modifiedUser = userService.updateUser("testId", new User());

        // Verify that the findById method of userRepository was called with the correct userId
        verify(userRepository, times(1)).findById("testId");

        // Verify that the updateUser method returns null when user is not found
        assertNull(modifiedUser, "Expected updateUser to return null when user is not found");
    }

    @Test
    void testDeleteUser() {
        // Mock the deleteUserById method of userRepository
        doNothing().when(userRepository).deleteById("testId");

        // Call the deleteUser method
        userService.deleteUser("testId");

        // Verify that the deleteUserById method of userRepository was called with the correct userId
        verify(userRepository, times(1)).deleteById("testId");
    }

    @Test
    void testUpdateUserFound() {
        String userId = "1";
        User existingUser = new User("1", "ExistingFirstName", "ExistingLastName", 10);
        User updatedUser = new User("1", "UpdatedFirstName", "UpdatedLastName", 10);

        doReturn(Optional.of(existingUser)).when(userRepository).findById(userId);
        doReturn(updatedUser).when(userRepository).save(any(User.class));

        User result = userService.updateUser(userId, updatedUser);

        assertNotNull(result);
        assertEquals(updatedUser.getFirstName(), result.getFirstName());
        assertEquals(updatedUser.getLastName(), result.getLastName());
        verify(userRepository).save(existingUser); // Verifies if save method was called with the updated user
    }

    @Test
    void testUpdateUserNotFound() {
        String userId = "nonExistingId";
        User updatedUser = new User("nonExistingId", "UpdatedFirstName", "UpdatedLastName", 10);

        doReturn(Optional.empty()).when(userRepository).findById(userId);

        User result = userService.updateUser(userId, updatedUser);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class)); // Ensures save method was never called
    }
}