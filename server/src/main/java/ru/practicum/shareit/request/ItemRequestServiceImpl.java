package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.errorHandler.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final Sort sortByCreatedDesc = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User user = getUserOrThrow(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        ItemRequest savedRequest = requestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> findAllByUserId(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = requestRepository.findByRequestor_Id(userId, sortByCreatedDesc);
        return fillRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId, int from, int size) {
        getUserOrThrow(userId);
        Pageable pageable = createPageable(from, size);
        List<ItemRequest> requests = requestRepository.findByRequestor_IdNot(userId, pageable);
        return fillRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = getRequestOrThrow(requestId);

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        dto.setItems(getItemsByRequestId(requestId));
        return dto;
    }

    private List<ItemRequestDto> fillRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Item>> itemsByRequest = getItemsGroupedByRequestId(requests);

        return requests.stream()
                .map(request -> assembleDto(request, itemsByRequest))
                .toList();
    }

    private Map<Long, List<Item>> getItemsGroupedByRequestId(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        return itemRepository.findByRequestIn(requestIds).stream()
                .collect(Collectors.groupingBy(Item::getRequest));
    }

    private ItemRequestDto assembleDto(ItemRequest request, Map<Long, List<Item>> itemsMap) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        List<Item> items = itemsMap.getOrDefault(request.getId(), Collections.emptyList());
        dto.setItems(items.stream()
                .map(ItemMapper::toItemDto)
                .toList());
        return dto;
    }

    private List<ru.practicum.shareit.item.dto.ItemDto> getItemsByRequestId(Long requestId) {
        return itemRepository.findByRequest(requestId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private Pageable createPageable(int from, int size) {
        return PageRequest.of(from / size, size, sortByCreatedDesc);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private ItemRequest getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
    }
}
