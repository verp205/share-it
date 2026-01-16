package ru.practicum.shareit.gateway.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.gateway.comment.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemRequestDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {

    private ItemClient itemClient;
    private ItemController itemController;

    @BeforeEach
    void setup() {
        itemClient = mock(ItemClient.class);
        itemController = new ItemController(itemClient);
    }

    @Test
    void createItem_callsClientAndReturnsResponse() {
        ItemRequestDto dto = new ItemRequestDto(1L, "Item", "Description", true, null);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(dto, HttpStatus.OK);

        when(itemClient.createItem(1L, dto)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.createItem(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(itemClient, times(1)).createItem(1L, dto);
    }

    @Test
    void updateItem_callsClientAndReturnsResponse() {
        ItemRequestDto dto = new ItemRequestDto(1L, "Updated", "Desc", true, null);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(dto, HttpStatus.OK);

        when(itemClient.updateItem(1L, 1L, dto)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.updateItem(1L, 1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(itemClient, times(1)).updateItem(1L, 1L, dto);
    }

    @Test
    void getItem_callsClientAndReturnsResponse() {
        ItemRequestDto dto = new ItemRequestDto(1L, "Item", "Description", true, null);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(dto, HttpStatus.OK);

        when(itemClient.getItem(1L, 1L)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.getItem(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(itemClient, times(1)).getItem(1L, 1L);
    }

    @Test
    void getItemsOfOwner_callsClientAndReturnsResponse() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(itemClient.getItemsOfOwner(1L)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.getItemsOfOwner(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(itemClient, times(1)).getItemsOfOwner(1L);
    }

    @Test
    void searchItems_callsClientAndReturnsResponse() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(itemClient.searchItems("query")).thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.searchItems("query");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(itemClient, times(1)).searchItems("query");
    }

    @Test
    void addComment_callsClientAndReturnsResponse() {
        CommentDto comment = new CommentDto("Text");
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(itemClient.addComment(1L, 1L, comment)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.addComment(1L, 1L, comment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(itemClient, times(1)).addComment(1L, 1L, comment);
    }
}
