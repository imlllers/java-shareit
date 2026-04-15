package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldAddItem() throws Exception {
        ItemDto itemDto = createItemDto();
        when(itemClient.addItem(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "requestId", 2L)));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(2L));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Updated");
        when(itemClient.updateItem(eq(1L), eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "name", "Updated")));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemClient.getItemById(1L, 1L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "name", "Drill")));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldGetOwnerItems() throws Exception {
        when(itemClient.getOwnerItems(1L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1L))));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFindItems() throws Exception {
        when(itemClient.findItems("drill"))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1L, "name", "Drill"))));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great");
        when(itemClient.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L, "text", "Great")));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great"));
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);
        itemDto.setRequestId(2L);
        return itemDto;
    }
}
