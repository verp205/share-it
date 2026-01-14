package ru.practicum.shareit.server.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.server.request.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterIdOrderByCreatedDesc(Long requesterId);

    List<Request> findAllByRequesterIdNotOrderByCreatedDesc(Long requesterId, Pageable pageable);
}