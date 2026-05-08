package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
    private ItemRequestService requestService;

    private ItemRequestDto requestDto;
    private final String header = "X-Sharer-User-Id";

    @BeforeEach
    void init() {
        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Что-то")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    @Test
    void create_ReturnDto_whenValid() throws Exception {
        when(requestService.create(anyLong(), any(ItemRequestDto.class))).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header(header, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Что-то"));
    }

    @Test
    void findAllByUserId_ReturnList() throws Exception {
        when(requestService.findAllByUserId(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header(header, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Что-то"));
    }

    @Test
    void findAll_ReturnListWithPagination() throws Exception {
        when(requestService.findAll(anyLong(), anyInt(), anyInt())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header(header, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void findById_ReturnDto() throws Exception {
        when(requestService.findById(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(get("/requests/1")
                        .header(header, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Что-то"));
    }
}
