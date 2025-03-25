package vttp.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class OAuthController {

    @GetMapping("/calendar/callback")
    public String handleCalendarCallback(@RequestParam String code) {
        // Redirect to the Angular route with just the authorization code
        return "redirect:/#/calendar/callback?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
    }
}
