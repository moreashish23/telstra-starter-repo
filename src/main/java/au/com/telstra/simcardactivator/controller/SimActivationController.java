package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.*;
import au.com.telstra.simcardactivator.entity.SimActivation;
import au.com.telstra.simcardactivator.repository.SimActivationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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

        // ✅ Input validation (Bug fix #3)
        if (request == null || request.getIccid() == null || request.getCustomerEmail() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ICCID and customer email must be provided"
            );
        }

        ActuatorRequest actuatorRequest = new ActuatorRequest();
        actuatorRequest.setIccid(request.getIccid());

        ResponseEntity<ActuatorResponse> response =
                restTemplate.postForEntity(
                        "http://localhost:8444/actuate",
                        actuatorRequest,
                        ActuatorResponse.class
                );

        // ✅ HTTP status validation (Bug fix #2)
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Actuator service failed"
            );
        }

        // ✅ Null safety check (Bug fix #1)
        ActuatorResponse body = response.getBody();
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Empty response from actuator service"
            );
        }

        boolean success = body.isSuccess();

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
