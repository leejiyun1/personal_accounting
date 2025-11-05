package com.personalaccount.domain.book.repository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Optional<Book> findByUserIdAndBookTypeAndIsActive(
            Long userId,
            BookType bookType,
            Boolean isActive
    );

    long countByUserIdAndIsActive(Long userId, Boolean isActive);

    Optional<Book> findByIdAndIsActive(Long id, Boolean isActive);
}
