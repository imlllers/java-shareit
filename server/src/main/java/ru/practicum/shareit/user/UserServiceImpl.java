package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (!userDto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }

        User user = UserMapper.toUser(userDto);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email занят");
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }


    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new IllegalArgumentException("Имя пользователя не может быть пустым");
            }
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email не может быть пустым");
            }

            if (!userDto.getEmail().contains("@")) {
                throw new IllegalArgumentException("Некорректный email");
            }

            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
                throw new ConflictException("Email занят");
            }

            user.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(
                userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь не найден"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
