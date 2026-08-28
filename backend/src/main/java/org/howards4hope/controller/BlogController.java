package org.howards4hope.controller;

import org.howards4hope.model.BlogPost;
import org.howards4hope.repository.BlogRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BlogController {

    private final BlogRepository blogRepository;

    public BlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // --- PUBLIC READ PATHS ---

    @GetMapping("/blog")
    public ResponseEntity<List<BlogPost>> getAllPosts() {
        List<BlogPost> posts = blogRepository.findAllByOrderByDateDesc();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/blog/{id}")
    public ResponseEntity<BlogPost> getPostById(@PathVariable Long id) {
        Optional<BlogPost> post = blogRepository.findById(id);
        return post.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/blog/category/{category}")
    public ResponseEntity<List<BlogPost>> getPostsByCategory(@PathVariable String category) {
        List<BlogPost> posts = blogRepository.findByCategoryIgnoreCase(category);
        return ResponseEntity.ok(posts);
    }

    // --- SECURED ADMIN PATHS ---

    @PostMapping("/admin/blog")
    public ResponseEntity<BlogPost> createPost(@RequestBody BlogPost post) {
        if (post.getDate() == null || post.getDate().isEmpty()) {
            post.setDate(LocalDate.now().toString());
        }
        BlogPost savedPost = blogRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    @DeleteMapping("/admin/blog/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!blogRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        blogRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
