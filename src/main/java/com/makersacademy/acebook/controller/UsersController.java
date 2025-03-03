package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Authority;
import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.AuthoritiesRepository;
import com.makersacademy.acebook.repository.FriendsRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class UsersController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthoritiesRepository authoritiesRepository;
    @Autowired
    FriendsRepository friendsRepository;

    private Long getUserId() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Long id = userRepository.findByUsername(authentication.getName()).getId();
        return id;
    }

    PasswordEncoder passwordEncoder;

    @GetMapping("/users/new")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "users/new"; // static html file
    }

    @PostMapping("/users")
    public RedirectView signup(@ModelAttribute User user) throws Exception {

        if (userRepository.existsByUsername(user.getUsername())) {
            // return ("error/wrong");
            return new RedirectView("users/new?retry");
        } else {
            passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            userRepository.save(user);
            Authority authority = new Authority(user.getUsername(), "ROLE_USER");
            authoritiesRepository.save(authority);

            // String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            // user = userRepository.findByUsername(user.getUsername());
            // user.setImage(fileName);
            // userRepository.save(user);
            // String uploadDir = "src/main/resources/static/image/" + user.getId();
            // FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
            return new RedirectView("/login");
        }
    }

    @GetMapping("/allUsers")
    public String allUsers(Model model) {
        // List<BigInteger> allUserIds = new ArrayList<BigInteger>();

        // for (User user : users) {
        // allUserIds.add(BigInteger.valueOf(user.getId()));
        // }

        // System.out.println(allUserIds);

        // Iterable<Object[]> requestsRecieved =
        // userRepository.requestsSentByOtherUsers(allUserIds, getUserId());

        // iterate over all users and find the ones who have sent a friend request

        Iterable<User> users = userRepository.findAll();
        List<User> result = new ArrayList<User>();
        Iterable<BigInteger> pendingFriends = friendsRepository.pendingFriends(getUserId());
        List<BigInteger> pendingIds = new ArrayList<BigInteger>();
        pendingFriends.forEach(pendingIds::add);
        pendingIds.add(BigInteger.valueOf(getUserId()));
        users.forEach(result::add);
        result.removeIf(u -> pendingIds.contains(BigInteger.valueOf(u.getId())));

        // users.forEach(user -> {
        // System.out.println(user);
        // });

        // get user id (logged in user)
        // get potential friend user id -> stored in the html file
        // add those to the requestSent method to check whether request has been sent
        // return 'Add Friend' link if request not sent
        // return 'Request Sent' string if request has been sent
        model.addAttribute("result", result);
        model.addAttribute("friend", new Friend());
        return "users/all";
    }

}
