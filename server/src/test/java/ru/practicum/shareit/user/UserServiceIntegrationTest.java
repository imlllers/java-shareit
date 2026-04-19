package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@test.com");

        UserDto created = userService.createUser(userDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void shouldUpdateUser() {
        UserDto created = createUser("User", "update@test.com");
        UserDto patch = new UserDto();
        patch.setName("Updated");

        UserDto updated = userService.updateUser(created.getId(), patch);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("update@test.com");
    }

    @Test
    void shouldReturnUserById() {
        UserDto created = createUser("User", "get@test.com");

        UserDto found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("User");
    }

    @Test
    void shouldReturnAllUsers() {
        createUser("First", "first@test.com");
        createUser("Second", "second@test.com");

        Collection<UserDto> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldDeleteUser() {
        UserDto created = createUser("Delete", "delete@test.com");

        userService.deleteUser(created.getId());

        assertThatThrownBy(() -> userService.getUserById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        UserDto userDto = new UserDto();
        userDto.setName(" ");
        userDto.setEmail("blank@test.com");

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        createUser("First", "duplicate@test.com");
        UserDto second = new UserDto();
        second.setName("Second");
        second.setEmail("duplicate@test.com");

        assertThatThrownBy(() -> userService.createUser(second))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowWhenUpdateWithInvalidEmail() {
        UserDto created = createUser("User", "invalid-update@test.com");
        UserDto patch = new UserDto();
        patch.setEmail("invalid-email");

        assertThatThrownBy(() -> userService.updateUser(created.getId(), patch))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenUpdateEmailIsBusy() {
        createUser("First", "busy@test.com");
        UserDto second = createUser("Second", "second-busy@test.com");
        UserDto patch = new UserDto();
        patch.setEmail("busy@test.com");

        assertThatThrownBy(() -> userService.updateUser(second.getId(), patch))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowWhenDeleteMissingUser() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private UserDto createUser(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userService.createUser(userDto);
    }
}
