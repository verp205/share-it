package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.comment.CommentRepository;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.RequestRepository;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(ItemServiceImpl.class)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private TestEntityManager em;

    @MockBean
    private UserService userService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private RequestRepository requestRepository;

    @Test
    void createItem_shouldPersistItem() {
        User owner = em.persistFlushFind(new User(null, "Owner", "owner@mail.com"));
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);

        ItemRequestDto dto = new ItemRequestDto(null, "Drill", "Powerful drill", true, null);
        ItemResponseDto saved = itemService.createItem(owner.getId(), dto);

        Item persisted = em.find(Item.class, saved.getId());
        assertNotNull(persisted);
        assertEquals("Drill", persisted.getName());
        assertEquals(owner.getId(), persisted.getOwner().getId());
    }
}