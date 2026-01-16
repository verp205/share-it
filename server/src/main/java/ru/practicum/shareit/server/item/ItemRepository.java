package ru.practicum.shareit.server.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.server.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> searchAvailableItems(@Param("text") String text);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.owner " +
            "LEFT JOIN FETCH i.request " +
            "WHERE i.id = :id")
    Optional<Item> findByIdWithOwnerAndRequest(@Param("id") Long id);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.owner LEFT JOIN FETCH i.request WHERE i.owner.id = :ownerId")
    List<Item> findByOwnerIdWithOwnerAndRequest(@Param("ownerId") Long ownerId);
}