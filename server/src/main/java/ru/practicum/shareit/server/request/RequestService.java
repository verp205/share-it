package ru.practicum.shareit.server.request;

import ru.practicum.shareit.server.request.dto.RequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;

import java.util.List;

public interface RequestService {

    RequestResponseDto createRequest(Long userId, RequestDto requestDto);

    List<RequestResponseDto> getUserRequests(Long userId);

    List<RequestResponseDto> getAllRequests(Long userId, int from, int size);

    RequestResponseDto getRequestById(Long userId, Long requestId);
}