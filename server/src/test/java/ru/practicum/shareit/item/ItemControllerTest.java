package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

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
    private ItemService itemService;

    @Test
    void shouldAddItem() throws Exception {
        ItemDto input = new ItemDto();
        input.setName("Drill");
        input.setDescription("Powerful drill");
        input.setAvailable(true);
        ItemDto output = new ItemDto();
        output.setId(1L);
        output.setName("Drill");
        when(itemService.addItem(eq(1L), any(ItemDto.class))).thenReturn(output);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto input = new ItemDto();
        input.setName("Updated");
        ItemDto output = new ItemDto();
        output.setId(1L);
        output.setName("Updated");
        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(output);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldGetItemById() throws Exception {
        ItemDto output = new ItemDto();
        output.setId(1L);
        output.setName("Drill");
        when(itemService.getItemById(1L, 1L)).thenReturn(output);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldGetOwnerItems() throws Exception {
        ItemDto output = new ItemDto();
        output.setId(1L);
        when(itemService.getOwnerItems(1L)).thenReturn(List.of(output));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFindItems() throws Exception {
        ItemDto output = new ItemDto();
        output.setId(1L);
        output.setName("Drill");
        when(itemService.findItems("drill")).thenReturn(List.of(output));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto input = new CommentDto();
        input.setText("Great");
        CommentDto output = new CommentDto();
        output.setId(1L);
        output.setText("Great");
        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(output);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great"));
    }
}
