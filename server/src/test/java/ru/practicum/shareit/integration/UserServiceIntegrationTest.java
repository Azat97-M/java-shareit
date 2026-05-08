package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {

    private final UserService service;

    @Test
    void saveUser_ReturnSameUser() {
        UserDto userDto = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto savedUser = service.create(userDto);
        UserDto fetchedUser = service.findById(savedUser.getId());

        assertThat(fetchedUser.getId(), notNullValue());
        assertThat(fetchedUser.getName(), equalTo(userDto.getName()));
        assertThat(fetchedUser.getEmail(), equalTo(userDto.getEmail()));
    }
}
