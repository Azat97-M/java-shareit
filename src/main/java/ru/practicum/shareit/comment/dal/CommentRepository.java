package ru.practicum.shareit.comment.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItem_Id(Long itemId);

    List<Comment> findByItem_IdIn(List<Long> itemIds);
}