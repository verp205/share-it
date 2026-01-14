package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.request.dto.RequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.model.Request;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(RequestServiceImpl.class)
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private TestEntityManager em;

    @MockBean
    private UserService userService;

    @Test
    void createRequest_shouldPersistRequest() {
        User requester = em.persistFlushFind(new User(null, "Requester", "req@mail.com"));
        when(userService.getUserEntity(requester.getId())).thenReturn(requester);

        RequestDto dto = new RequestDto("Need drill");
        RequestResponseDto saved = requestService.createRequest(requester.getId(), dto);

        Request persisted = em.find(Request.class, saved.getId());
        assertNotNull(persisted);
        assertEquals("Need drill", persisted.getDescription());
        assertEquals(requester.getId(), persisted.getRequester().getId());
    }
}