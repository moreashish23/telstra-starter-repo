package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.ActivationRequest;
import au.com.telstra.simcardactivator.dto.ActivationStatusResponse;
import au.com.telstra.simcardactivator.dto.ActuatorRequest;
import au.com.telstra.simcardactivator.dto.ActuatorResponse;
import au.com.telstra.simcardactivator.entity.SimActivation;
import au.com.telstra.simcardactivator.repository.SimActivationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/activate-sim")
public class SimActivationController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SimActivationRepository simActivationRepository;

    // POST: Activate SIM + Save result
    @PostMapping
    public ResponseEntity<String> activateSim(@RequestBody ActivationRequest request) {

        // Call actuator service
        ActuatorRequest actuatorRequest = new ActuatorRequest();
        actuatorRequest.setIccid(request.getIccid());

        ResponseEntity<ActuatorResponse> response =
                restTemplate.postForEntity(
                        "http://localhost:8444/actuate",
                        actuatorRequest,
                        ActuatorResponse.class
                );

        boolean success = response.getBody().isSuccess();

        // Save result in database
        SimActivation activation = new SimActivation(
                request.getIccid(),
                request.getCustomerEmail(),
                success
        );
        simActivationRepository.save(activation);

        return ResponseEntity.ok("Activation result: " + success);
    }

    // GET: Fetch activation record by ID
    @GetMapping
    public ResponseEntity<ActivationStatusResponse> getSimActivation(
            @RequestParam Long simCardId) {

        return simActivationRepository.findById(simCardId)
                .map(record -> ResponseEntity.ok(
                        new ActivationStatusResponse(
                                record.getIccid(),
                                record.getCustomerEmail(),
                                record.isActive()
                        )
                ))
                .orElse(ResponseEntity.notFound().build());
    }
}
