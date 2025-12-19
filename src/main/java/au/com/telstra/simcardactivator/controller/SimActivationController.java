package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.ActivationRequest;
import au.com.telstra.simcardactivator.dto.ActuatorRequest;
import au.com.telstra.simcardactivator.dto.ActuatorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/activate-sim")
public class SimActivationController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<String> activateSim(@RequestBody ActivationRequest request) {

        // Prepare request for actuator service
        ActuatorRequest actuatorRequest = new ActuatorRequest();
        actuatorRequest.setIccid(request.getIccid());

        // Call actuator microservice
        ResponseEntity<ActuatorResponse> response =
                restTemplate.postForEntity(
                        "http://localhost:8444/actuate",
                        actuatorRequest,
                        ActuatorResponse.class
                );

        boolean success = response.getBody().isSuccess();

        // Log result
        if (success) {
            System.out.println("SIM activated successfully for ICCID: " + request.getIccid());
        } else {
            System.out.println("SIM activation failed for ICCID: " + request.getIccid());
        }

        return ResponseEntity.ok("Activation result: " + success);
    }
}
