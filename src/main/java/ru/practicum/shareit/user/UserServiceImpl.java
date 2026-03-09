package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        boolean uniqueEmail = userRepository.getAll().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));

        if (uniqueEmail) {
            throw new ConflictException("Email занят");
        }

        return UserMapper.toUserDto(userRepository.create(user));
    }

    public UserDto updateUser(Integer id, UserDto userDto) {
        validateUserId(id);
        User user = userRepository.getById(id);

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail(), id);
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.update(user));
    }

    public void deleteUser(Integer id) {
        validateUserId(id);
        userRepository.delete(id);
    }

    public UserDto getUserById(Integer id) {
        validateUserId(id);
        return UserMapper.toUserDto(userRepository.getById(id));
    }

    public Collection<UserDto> getAllUsers() {
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void validateUserId(Integer id) {
        if (userRepository.getById(id) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private void validateEmail(String email, Integer excludeId) {
        for (User u : userRepository.getAll()) {
            if (u.getEmail().equals(email) && !u.getId().equals(excludeId)) {
                throw new ConflictException("Email занят");
            }
        }
    }
}
