package vttp.final_project.controller;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
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
            // Check if user has Google tokens
            User user = userSqlRepo.findByEmail(principal.getName());
            System.out.println("User: " + user.getEmail() + ", Has refresh token: " + (user.getGoogleRefreshToken() != null));

            if (user.getGoogleRefreshToken() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"status\":\"error\",\"message\":\"User not connected to Google Calendar\"}");
            }

            // Get the meal plan
            MealPlan mealPlan = mealPlanSvc.getMealPlanById(id);
            if (mealPlan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"status\":\"error\",\"message\":\"Meal plan not found\"}");
            }

            // Verify the meal plan belongs to the user
            if (!mealPlan.getUserId().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"status\":\"error\",\"message\":\"You don't have permission to access this meal plan\"}");
            }

            // Add to calendar
            Event event = calendarSvc.addMealPlanToCalendar(principal.getName(), mealPlan);

            return ResponseEntity.ok("{\"status\":\"success\",\"message\":\"Meal plan added to calendar\"}");
        } catch (Exception e) {
            // Log the detailed error
            e.printStackTrace();
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
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Calendar service = calendarSvc.getCalendarService(principal.getName());

            // Get events from the next 7 days
            DateTime now = new DateTime(System.currentTimeMillis());
            DateTime oneWeekLater = new DateTime(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);

            Events events = service.events().list("primary")
                    .setTimeMin(now)
                    .setTimeMax(oneWeekLater)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Event event : events.getItems()) {
                JsonObjectBuilder eventBuilder = Json.createObjectBuilder()
                        .add("id", event.getId())
                        .add("summary", event.getSummary() != null ? event.getSummary() : "")
                        .add("description", event.getDescription() != null ? event.getDescription() : "")
                        .add("htmlLink", event.getHtmlLink() != null ? event.getHtmlLink() : "");

                if (event.getStart().getDateTime() != null) {
                    eventBuilder.add("start", event.getStart().getDateTime().toStringRfc3339());
                }

                if (event.getEnd().getDateTime() != null) {
                    eventBuilder.add("end", event.getEnd().getDateTime().toStringRfc3339());
                }

                arrayBuilder.add(eventBuilder);
            }

            return ResponseEntity.ok(arrayBuilder.build().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/embed-url")
    public ResponseEntity<String> getCalendarEmbedUrl(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Primary calendar ID is often the user's email
            String calendarId = principal.getName();

            JsonObject response = Json.createObjectBuilder()
                    .add("embedUrl", "https://calendar.google.com/calendar/embed?src=" +
                                     URLEncoder.encode(calendarId, StandardCharsets.UTF_8) +
                                     "&ctz=local&showTitle=0&showNav=1")
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
