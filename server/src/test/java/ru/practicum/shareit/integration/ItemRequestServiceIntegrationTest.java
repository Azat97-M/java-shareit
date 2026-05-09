package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final ItemRequestService requestService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void findAll_ReturnRequestsWithItems() {
        UserDto requester = userService.create(UserDto.builder().name("Феникс").email("fen@mail.ru").build());
        UserDto provider = userService.create(UserDto.builder().name("Аист").email("aist@mail.com").build());

        ItemRequestDto requestDto = requestService.create(requester.getId(),
                ItemRequestDto.builder().description("Хочу плойку").build());

        itemService.create(provider.getId(), ItemDto.builder()
                .name("Плойка")
                .description("Новая")
                .available(true)
                .requestId(requestDto.getId())
                .build());

        List<ItemRequestDto> results = requestService.findAll(provider.getId(), 0, 10);

        assertThat(results, hasSize(1));
        assertThat(results.getFirst().getDescription(), equalTo("Хочу плойку"));
        assertThat(results.getFirst().getItems(), hasSize(1));
        assertThat(results.getFirst().getItems().getFirst().getName(), equalTo("Плойка"));
    }
}
