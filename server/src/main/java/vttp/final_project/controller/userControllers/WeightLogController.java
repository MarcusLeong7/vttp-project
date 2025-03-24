package vttp.final_project.controller.userControllers;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.userModels.WeightLog;
import vttp.final_project.services.userManagement.WeightLogService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/weight-logs")
public class WeightLogController {

    @Autowired
    private WeightLogService weightLogService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWeightLogs(
            @RequestParam(defaultValue = "30") int days,
            Principal principal) {

        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            List<WeightLog> logs = weightLogService.getWeightLogs(principal.getName(), days);

            JsonArrayBuilder logsArray = Json.createArrayBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

            for (WeightLog log : logs) {
                JsonObjectBuilder logBuilder = Json.createObjectBuilder()
                        .add("id", log.getId())
                        .add("weight", log.getWeight())
                        .add("date", log.getDate().format(formatter));

                if (log.getNotes() != null) {
                    logBuilder.add("notes", log.getNotes());
                } else {
                    logBuilder.addNull("notes");
                }

                logsArray.add(logBuilder.build());
            }

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("logs", logsArray.build())
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to retrieve weight logs: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addWeightLog(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            BigDecimal weight = new BigDecimal(payload.get("weight").toString());
            String notes = payload.get("notes") != null ? payload.get("notes").toString() : null;

            WeightLog log = weightLogService.addWeightLog(principal.getName(), weight, notes);

            JsonObjectBuilder logBuilder = Json.createObjectBuilder()
                    .add("id", log.getId())
                    .add("weight", log.getWeight())
                    .add("date", log.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

            if (log.getNotes() != null) {
                logBuilder.add("notes", log.getNotes());
            } else {
                logBuilder.addNull("notes");
            }

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Weight log added successfully")
                    .add("log", logBuilder.build())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to add weight log: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateWeightLog(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            Principal principal) {

        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            BigDecimal weight = new BigDecimal(payload.get("weight").toString());
            String notes = payload.get("notes") != null ? payload.get("notes").toString() : null;

            WeightLog log = weightLogService.updateWeightLog(principal.getName(), id, weight, notes);

            JsonObjectBuilder logBuilder = Json.createObjectBuilder()
                    .add("id", log.getId())
                    .add("weight", log.getWeight())
                    .add("date", log.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

            if (log.getNotes() != null) {
                logBuilder.add("notes", log.getNotes());
            } else {
                logBuilder.addNull("notes");
            }

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Weight log updated successfully")
                    .add("log", logBuilder.build())
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to update weight log: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteWeightLog(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            boolean deleted = weightLogService.deleteWeightLog(principal.getName(), id);

            if (deleted) {
                JsonObject response = Json.createObjectBuilder()
                        .add("status", "success")
                        .add("message", "Weight log deleted successfully")
                        .build();

                return ResponseEntity.ok(response.toString());
            } else {
                JsonObject error = Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Weight log not found")
                        .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
            }
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to delete weight log: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }
}
