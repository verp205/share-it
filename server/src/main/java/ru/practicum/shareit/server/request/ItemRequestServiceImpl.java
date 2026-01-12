package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserService userService;

    @Transactional
    @Override
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto requestDto) {
        log.debug("Creating item request for user: {}", userId);

        User requester = userService.getUserEntity(userId);

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, requester);
        ItemRequest savedRequest = requestRepository.save(request);

        log.info("Item request created with id: {}", savedRequest.getId());
        return ItemRequestMapper.toItemRequestResponseDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        log.debug("Getting item requests for user: {}", userId);

        userService.getUserEntity(userId);

        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        log.debug("Getting all item requests for user: {}, from: {}, size: {}", userId, from, size);

        userService.getUserEntity(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        log.debug("Getting item request by id: {} for user: {}", requestId, userId);

        userService.getUserEntity(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос", requestId));

        return ItemRequestMapper.toItemRequestResponseDto(request);
    }
}