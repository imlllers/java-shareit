package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private Integer id = 1;

    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public void delete(Integer id) {
        users.remove(id);
    }

    public User getById(Integer id) {
        return users.get(id);
    }

    public Collection<User> getAll() {
        return users.values();
    }

    private Integer getNextId() {
        return id++;
    }
}
