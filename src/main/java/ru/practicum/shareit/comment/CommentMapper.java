package ru.practicum.shareit.comment;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor() != null ? comment.getAuthor().getName() : null,
                comment.getCreated()
        );
    }

    public static Comment toComment(CommentDto dto) {
        if (dto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setCreated(java.time.LocalDateTime.now());
        return comment;
    }
}