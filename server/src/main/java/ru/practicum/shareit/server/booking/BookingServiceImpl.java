package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.dto.BookingState;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.*;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingInputDto bookingInputDto) {
        log.debug("Creating booking for user: {}, item: {}", userId, bookingInputDto.getItemId());

        User booker = userService.getUserEntity(userId);

        Item item = itemRepository.findById(bookingInputDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь", bookingInputDto.getItemId()));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь", "недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь", "нельзя забронировать свою же вещь");
        }

        if (bookingInputDto.getEnd().isBefore(bookingInputDto.getStart()) ||
                bookingInputDto.getEnd().isEqual(bookingInputDto.getStart())) {
            throw new ValidationException("Даты бронирования", "дата окончания должна быть позже даты начала");
        }

        boolean hasOverlappingBookings = bookingRepository.existsOverlappingBookings(
                item.getId(),
                bookingInputDto.getStart(),
                bookingInputDto.getEnd()
        );

        if (hasOverlappingBookings) {
            throw new ValidationException("Время бронирования",
                    String.format("уже занято другим бронированием с %s по %s",
                            bookingInputDto.getStart(), bookingInputDto.getEnd()));
        }

        Booking booking = BookingMapper.toBooking(bookingInputDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created with id: {}", savedBooking.getId());
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        log.debug("Approving booking: {} by owner: {}, approved: {}", bookingId, ownerId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование", bookingId));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования", "уже обработан");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking {} {} by owner {}", bookingId,
                approved ? "approved" : "rejected", ownerId);

        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.debug("Getting booking: {} for user: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование", bookingId));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("просмотреть бронирование");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size) {
        log.debug("Getting bookings for user: {}, state: {}", userId, state);

        userService.getUserEntity(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByBooker(userId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findPastBookingsByBooker(userId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureBookingsByBooker(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("State", "неподдерживаемое значение: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, int from, int size) {
        log.debug("Getting bookings for owner: {}, state: {}", ownerId, state);

        userService.getUserEntity(ownerId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByOwner(ownerId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findPastBookingsByOwner(ownerId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureBookingsByOwner(ownerId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("State", "неподдерживаемое значение: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}