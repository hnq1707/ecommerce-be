package com.hnq.e_commerce.auth.controllers;

import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.services.AuthenticationService;
import com.hnq.e_commerce.auth.services.OAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    OAuth2Service oAuth2Service;

    @Autowired
    private AuthenticationService authSerivce;

    @GetMapping("/success")
    public void callbackOAuth2(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            HttpServletResponse response
                              ) throws IOException {

        String userName = oAuth2User.getAttribute("email");
        Optional<User> user = oAuth2Service.getUser(userName);
        if (null == user) {
            user = Optional.ofNullable(oAuth2Service.createUser(oAuth2User, "google"));
        }

        String token = authSerivce.generateToken(user);

        response.sendRedirect(
                "http://localhost:3000/oauth2/callback?token=" + token);

    }
}