package vttp.final_project.controller;

import com.google.api.services.calendar.model.Event;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.GoogleAuthRequest;
import vttp.final_project.models.mealModels.MealPlan;
import vttp.final_project.models.userModels.User;
import vttp.final_project.repository.user.UserSqlRepository;
import vttp.final_project.services.GoogleCalendarService;
import vttp.final_project.services.mealManagement.MealPlanService;
import vttp.final_project.services.userManagement.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService calendarSvc;

    @Autowired
    private MealPlanService mealPlanSvc;

    @Autowired
    private UserService userSvc;

    @Autowired
    private UserSqlRepository  userSqlRepo;

    @PostMapping("/connect")
    public ResponseEntity<String> connectToGoogle(@RequestBody GoogleAuthRequest req, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            userSvc.saveGoogleAuthToken(principal.getName(), req.getCode());

            return ResponseEntity.ok("{\"status\":\"success\",\"message\":\"Connected to Google Calendar\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/mealplan/{id}")
    public ResponseEntity<String> addMealPlanToCalendar(@PathVariable String id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            MealPlan mealPlan = mealPlanSvc.getMealPlanById(id);

            // Verify the meal plan belongs to the user
            if (!mealPlan.getUserId().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Event event = calendarSvc.addMealPlanToCalendar(principal.getName(), mealPlan);

            return ResponseEntity.ok("{\"status\":\"success\",\"message\":\"Meal plan added to calendar\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> getConnectionStatus(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = userSqlRepo.findByEmail(principal.getName());
            boolean connected = user.getGoogleRefreshToken() != null;

            JsonObject response = Json.createObjectBuilder()
                    .add("connected", connected)
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/events")
    public ResponseEntity<String> getCalendarEvents(Principal principal) {
        // Implementation to fetch and return calendar events
        // ...
        return null;
    }

    @GetMapping("/embed-url")
    public ResponseEntity<String> getCalendarEmbedUrl(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get the user's primary calendar ID
            String calendarId = "primary"; // Default to primary calendar

            JsonObject response = Json.createObjectBuilder()
                    .add("embedUrl", "https://calendar.google.com/calendar/embed?src=" +
                                     URLEncoder.encode(principal.getName(), StandardCharsets.UTF_8) +
                                     "&ctz=local")
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
