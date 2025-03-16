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
import java.time.LocalDate;
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

            // Calculate the event date for a full day event
            LocalDate eventDate = calculateEventDate(mealPlan.getDayOfWeek());

            // For a full-day event, we use dates without times
            EventDateTime startDate = new EventDateTime()
                    .setDate(new DateTime(eventDate.toString())); // Use string format YYYY-MM-DD
            event.setStart(startDate);
            System.out.println("Set event start date: " + startDate.getDate());

            // For a full-day event, the end date should be the next day
            EventDateTime endDate = new EventDateTime()
                    .setDate(new DateTime(true, eventDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), 0));
            event.setEnd(endDate);
            System.out.println("Set event end date: " + endDate.getDate());

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

    private LocalDate calculateEventDate(Integer dayOfWeek) {
        // Get the current date
        LocalDate today = LocalDate.now();
        System.out.println("Today is: " + today + " (Day " + today.getDayOfWeek().getValue() + ")");

        // If dayOfWeek is specified, calculate the next occurrence of that day
        if (dayOfWeek != null) {
            // Convert between different day numbering systems
            // Your system: 0=Sunday, 1=Monday, ..., 6=Saturday
            // Java's system: 1=Monday, 2=Tuesday, ..., 7=Sunday

            // Convert your dayOfWeek to Java's DayOfWeek value
            int javaDayOfWeek;
            if (dayOfWeek == 0) {
                javaDayOfWeek = 7; // Sunday in Java is 7
            } else {
                javaDayOfWeek = dayOfWeek; // Other days (Mon-Sat) are just offset by 0
            }

            System.out.println("Target day: " + dayOfWeek + " (Java day: " + javaDayOfWeek + ")");

            int currentJavaDayOfWeek = today.getDayOfWeek().getValue(); // 1-7 (Monday-Sunday)
            System.out.println("Current Java day of week: " + currentJavaDayOfWeek);

            // Calculate days to add to reach target day
            int daysToAdd = (javaDayOfWeek - currentJavaDayOfWeek + 7) % 7;

            // If target day is today (daysToAdd = 0), schedule for next week
            if (daysToAdd == 0) {
                daysToAdd = 7;
            }

            System.out.println("Days to add: " + daysToAdd);
            LocalDate targetDate = today.plusDays(daysToAdd);
            System.out.println("Target date: " + targetDate + " (Day " + targetDate.getDayOfWeek().getValue() + ")");

            return targetDate;
        } else {
            // If no day specified, use tomorrow (not today)
            LocalDate tomorrow = today.plusDays(1);
            System.out.println("No day specified, using tomorrow: " + tomorrow);
            return tomorrow;
        }
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