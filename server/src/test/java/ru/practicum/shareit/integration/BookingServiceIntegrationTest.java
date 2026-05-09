package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void findAllByBooker_ReturnBookingsWithStatusWaiting() {
        UserDto owner = userService.create(UserDto.builder().name("Ivan").email("ivan@mail.ru").build());
        UserDto booker = userService.create(UserDto.builder().name("Petr").email("petr@mail.ru").build());
        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Книга").description("Старая").available(true).build());

        BookingDtoIn bookingDtoIn = BookingDtoIn.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.create(booker.getId(), bookingDtoIn);

        List<BookingDtoOut> results = bookingService.findAllByBooker(booker.getId(), "WAITING", 0, 10);

        assertThat(results, hasSize(1));
        assertThat(results.getFirst().getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(results.getFirst().getItem().getName(), equalTo("Книга"));
        assertThat(results.getFirst().getBooker().getName(), equalTo("Petr"));
    }
}
