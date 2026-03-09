package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Integer id, UserDto userDto);

    void deleteUser(Integer id);

    UserDto getUserById(Integer id);

    Collection<UserDto> getAllUsers();
}
