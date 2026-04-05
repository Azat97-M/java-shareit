package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private ItemDto itemDto;
    private final String header = "X-Sharer-User-Id";

    @BeforeEach
    void init() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Книга")
                .description("Песнь льда и пламени")
                .available(true)
                .build();
    }

    @Test
    void create_itemDto_whenHeaderAndItemAreValid() throws Exception {
        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(header, 1L) // Проверяем передачу заголовка
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Книга"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_itemDto_whenOwnerUpdatesItem() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header(header, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Песнь льда и пламени"));
    }

    @Test
    void findById_itemDto_whenItemExists() throws Exception {
        when(itemService.findById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Книга"));
    }

    @Test
    void findByOwner_listOfItems_whenOwnerHasItems() throws Exception {
        when(itemService.findByOwner(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(header, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Книга"));
    }

    @Test
    void search_listOfItems_whenTextIsProvided() throws Exception {
        when(itemService.search("книга")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "книга"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Книга"));
    }
}