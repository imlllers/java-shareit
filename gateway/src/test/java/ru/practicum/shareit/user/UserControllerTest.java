package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void shouldCreateUser() throws Exception {
        UserDto userDto = createUserDto();
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "name", "User")));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Updated");
        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "name", "Updated")));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userClient.deleteUser(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(userClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "email", "mail@test.com")));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mail@test.com"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1L), Map.of("id", 2L))));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private UserDto createUserDto() {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("mail@test.com");
        return userDto;
    }
}
