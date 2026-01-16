package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.client.BaseClient;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl) {
        super(serverUrl);
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post(API_PREFIX, userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserDto userDto) {
        return patch(API_PREFIX + "/" + userId, userDto);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        return get(API_PREFIX + "/" + userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get(API_PREFIX);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete(API_PREFIX + "/" + userId);
    }
}
