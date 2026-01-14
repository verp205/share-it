package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.dto.RequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.model.Request;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;

    @Transactional
    @Override
    public RequestResponseDto createRequest(Long userId, RequestDto requestDto) {
        log.debug("Creating item request for user: {}", userId);

        User requester = userService.getUserEntity(userId);

        Request request = RequestMapper.toItemRequest(requestDto, requester);
        Request savedRequest = requestRepository.save(request);

        log.info("Item request created with id: {}", savedRequest.getId());
        return RequestMapper.toItemRequestResponseDto(savedRequest);
    }

    @Override
    public List<RequestResponseDto> getUserRequests(Long userId) {
        log.debug("Getting item requests for user: {}", userId);

        userService.getUserEntity(userId);

        List<Request> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(RequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestResponseDto> getAllRequests(Long userId, int from, int size) {
        log.debug("Getting all item requests for user: {}, from: {}, size: {}", userId, from, size);

        userService.getUserEntity(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Request> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable);

        return requests.stream()
                .map(RequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestResponseDto getRequestById(Long userId, Long requestId) {
        log.debug("Getting item request by id: {} for user: {}", requestId, userId);

        userService.getUserEntity(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос", requestId));

        return RequestMapper.toItemRequestResponseDto(request);
    }
}