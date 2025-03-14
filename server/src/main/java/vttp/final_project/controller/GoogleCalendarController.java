package vttp.final_project.controller;

import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.mealModels.MealPlan;
import vttp.final_project.services.GoogleCalendarService;
import vttp.final_project.services.mealManagement.MealPlanService;
import vttp.final_project.services.userManagement.UserService;

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

    @PostMapping("/connect")
    public ResponseEntity<String> connectToGoogle(@RequestBody GoogleAuthRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            userSvc.saveGoogleAuthToken(principal.getName(), request.getCode());

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
}
