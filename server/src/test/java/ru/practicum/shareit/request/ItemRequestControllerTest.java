package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestDto input = new ItemRequestDto();
        input.setDescription("Need drill");
        ItemRequestDto output = new ItemRequestDto();
        output.setId(1L);
        output.setDescription("Need drill");
        when(itemRequestService.createRequest(eq(1L), any(ItemRequestDto.class))).thenReturn(output);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Need drill"));
    }

    @Test
    void shouldGetOwnerRequests() throws Exception {
        ItemRequestDto output = new ItemRequestDto();
        output.setId(1L);
        when(itemRequestService.getOwnerRequests(1L)).thenReturn(List.of(output));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        ItemRequestDto output = new ItemRequestDto();
        output.setId(2L);
        when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(output));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        ItemRequestDto output = new ItemRequestDto();
        output.setId(3L);
        output.setDescription("Need saw");
        when(itemRequestService.getRequestById(1L, 3L)).thenReturn(output);

        mockMvc.perform(get("/requests/3")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Need saw"));
    }
}
