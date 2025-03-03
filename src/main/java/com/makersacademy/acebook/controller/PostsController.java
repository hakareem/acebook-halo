package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.model.Like;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.LikeRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.*;
import java.nio.file.*;
// import org.apache.commons.lang3.StringUtils;

@Controller
public class PostsController {

    @Autowired
    PostRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    CommentRepository commentRepository;

    private Long getUserId() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Long id = userRepository.findByUsername(authentication.getName()).getId();
        return id;
    }

    private String getUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String name = authentication.getName();
        return name;
    }

    private Timestamp getTimeStamp() {
        LocalDateTime time = LocalDateTime.now();
        System.out.println(time);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp;
    }

    @GetMapping("/posts")
    public String index(Model model) {
        Iterable<Post> posts = repository.findAll(Sort.by(Sort.Direction.DESC, "id")); // filtering by id
        // User user = userRepository.findById(post.getUser_id()).get();\
        List<User> friends = userRepository.findFriends(getUserId());
        List<Post> filteredPosts = new ArrayList<Post>();

        posts.forEach(filteredPosts::add);
        List<Long> friendId = new ArrayList<Long>();

        for (User friend : friends){
            friendId.add(friend.getId());
        }

        filteredPosts.removeIf(post -> !friendId.contains(post.getUser_id()) && post.getUser_id() != getUserId());

        model.addAttribute("repo", repository);
        model.addAttribute("posts", filteredPosts);
        model.addAttribute("post", new Post());
        return "posts/index";
    }

    // @PostMapping("/users/save")
    // public RedirectView saveUser(User user,
    // @RequestParam("image") MultipartFile multipartFile) throws IOException {

    // return new RedirectView("/users", true);
    // }
    @PostMapping("/posts")
    public RedirectView create(@ModelAttribute Post post, @RequestParam("image") MultipartFile multipartFile)
            throws IOException {
        // use the setter to store the user_id

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        post.setImagePost(fileName);
        repository.save(post);
        String uploadDir = "src/main/resources/static/image/" + post.getId();
        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

        post.setUsername(this.getUsername());
        post.setDate(this.getTimeStamp());
        post.setUser_id(getUserId());
        repository.save(post);
        System.out.println(post.getTimeString());
        System.out.println(post.getDateString());
        // System.out.println(post.getDateString());
        Long nLikes = repository.findNumberOfLikesForAPost(post.getId());
        System.out.println(nLikes);

        // Long nComments = repository.findNumberOfCommentsForAPost(post.getId());
        // System.out.println(nLikes);
        // post.likes()
        return new RedirectView("/posts");
    }

    // @PostMapping("/posts/newLike")
    // @ResponseBody()
    // public RedirectView newLike(@RequestParam("postid") Long postId) {
    // Like newLike = new Like();
    // newLike.setUser_id(getUserId());
    // newLike.setPost_id(postId);
    // likeRepository.save(newLike);

    // return new RedirectView("/posts");
    // }

    @RequestMapping("/posts/like")
    @ResponseBody
    public RedirectView newLike(@RequestParam("postid") Long postid) {
        try {
            // System.out.println(postid);
            System.out.println(getUserId());
            Like newLike = new Like();
            newLike.setUserid(getUserId());
            newLike.setDate(this.getTimeStamp());
            newLike.setPostid(postid);
            likeRepository.save(newLike);
        } catch (Exception e) {
            System.out.println("error");
            likeRepository.deleteByUseridAndPostid(getUserId(), postid);
        }
        return new RedirectView("/posts");
    }

    // @RequestMapping("/posts/comment")
    // public String newComment(@RequestParam("postid") Long postid) {
    // try {
    // // System.out.println(postid);
    // System.out.printf("THis is the post id for comments %d", postid);
    // System.out.println(getUserId());
    // Comment newComment = new Comment();
    // newComment.setUserid(getUserId());
    // // newComment.setContent(getContent());
    // newComment.setPostid(postid);
    // // newComment.setContent(content);
    // commentRepository.save(newComment);
    // } catch (Exception e) {
    // System.out.println("error");
    // // commentRepository.deleteByUseridAndPostid(getUserId(), postid);
    // }
    // return "comments/post?postid=" + postid;
    // }

}