package org.howards4hope.repository;

import org.howards4hope.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findAllByOrderByDateDesc();
    List<BlogPost> findByCategoryIgnoreCase(String category);
}
