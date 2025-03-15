package vttp.final_project.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vttp.final_project.models.mealModels.MealPlan;
import vttp.final_project.models.mealModels.MealPlanItem;
import vttp.final_project.models.userModels.User;
import vttp.final_project.repository.user.UserRepository;
import vttp.final_project.repository.user.UserSqlRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "NutriSense";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance(); // Use GsonFactory instead

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Autowired
    private UserSqlRepository userSqlRepo;

    public Calendar getCalendarService(String userId) throws IOException, GeneralSecurityException {
        // Retrieve user's Google credentials
        User user = userSqlRepo.findByEmail(userId);
        if (user.getGoogleRefreshToken() == null) {
            throw new RuntimeException("User not connected to Google Calendar");
        }

        // Build credential object
        Credential credential = getCredentials(user.getGoogleRefreshToken());

        // Create a secure NetHttpTransport
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Build calendar service
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Event addMealPlanToCalendar(String userId, MealPlan mealPlan) {
        try {
            System.out.println("Starting to add meal plan to calendar for user: " + userId);

            Calendar service = getCalendarService(userId);
            System.out.println("Successfully obtained Calendar service");

            // Create event
            Event event = new Event()
                    .setSummary("NutriSense Meal Plan: " + mealPlan.getName())
                    .setDescription(buildEventDescription(mealPlan));
            System.out.println("Created event object: " + event.getSummary());

            // Set event time based on meal plan day
            DateTime startDateTime = calculateEventDateTime(mealPlan.getDayOfWeek());
            event.setStart(new EventDateTime().setDateTime(startDateTime));
            System.out.println("Set event start time: " + startDateTime.toString());

            // Calculate end time (1 hour later)
            DateTime endDateTime = new DateTime(startDateTime.getValue() + 3600000);
            event.setEnd(new EventDateTime().setDateTime(endDateTime));
            System.out.println("Set event end time: " + endDateTime.toString());

            // Insert event
            System.out.println("Attempting to insert event to primary calendar");
            Event createdEvent = service.events().insert("primary", event).execute();
            System.out.println("Successfully created event with ID: " + createdEvent.getId());

            return createdEvent;
        } catch (Exception e) {
            System.err.println("Error adding meal plan to calendar: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add meal plan to calendar: " + e.getMessage(), e);
        }
    }

    private String buildEventDescription(MealPlan mealPlan) {
        // Build a detailed description of the meal plan
        StringBuilder description = new StringBuilder();
        description.append("Meal Plan: ").append(mealPlan.getName()).append("\n\n");

        // Group meals by type
        Map<String, List<MealPlanItem>> mealsByType = mealPlan.getItems().stream()
                .collect(Collectors.groupingBy(MealPlanItem::getMealType));

        for (Map.Entry<String, List<MealPlanItem>> entry : mealsByType.entrySet()) {
            description.append(entry.getKey().toUpperCase()).append(":\n");
            for (MealPlanItem meal : entry.getValue()) {
                description.append("- ").append(meal.getMealTitle()).append("\n");
            }
            description.append("\n");
        }

        return description.toString();
    }

    private DateTime calculateEventDateTime(Integer dayOfWeek) {
        // Get the current date/time
        LocalDateTime now = LocalDateTime.now();

        // If dayOfWeek is specified, calculate the next occurrence of that day
        if (dayOfWeek != null) {
            int daysUntilNextOccurrence = (dayOfWeek - now.getDayOfWeek().getValue() + 7) % 7;
            now = now.plusDays(daysUntilNextOccurrence);
        }

        // Set time to noon
        now = now.withHour(12).withMinute(0).withSecond(0).withNano(0);

        // Convert to DateTime
        return new DateTime(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private Credential getCredentials(String refreshToken) {
        try {
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                    .setInstalled(new GoogleClientSecrets.Details()
                            .setClientId(clientId)
                            .setClientSecret(clientSecret));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(), JSON_FACTORY, clientSecrets,
                    Collections.singletonList(CalendarScopes.CALENDAR))
                    .setAccessType("offline")
                    .build();

            Credential credential = flow.createAndStoreCredential(
                    new TokenResponse().setRefreshToken(refreshToken), null);

            return credential;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create credentials", e);
        }
    }
}