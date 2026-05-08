package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;

    @Test
    void findByOwner_ReturnItemsWithOwnerDetails() {
        UserDto owner = userService.create(UserDto.builder().name("Кто-то").email("Кто-то@mail.ru").build());

        ItemDto itemDto = ItemDto.builder()
                .name("Книга")
                .description("Новая")
                .available(true)
                .build();
        itemService.create(owner.getId(), itemDto);

        List<ItemDto> results = itemService.findByOwner(owner.getId());

        assertThat(results, hasSize(1));
        assertThat(results.getFirst().getName(), equalTo("Книга"));
    }
}