package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.*;
import au.com.telstra.simcardactivator.entity.SimActivation;
import au.com.telstra.simcardactivator.repository.SimActivationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/activate-sim")
public class SimActivationController {

    private final RestTemplate restTemplate;
    private final SimActivationRepository simActivationRepository;
    private final String actuatorServiceUrl;

    //  Constructor Injection (Sonar Reliability Fix)
    public SimActivationController(
            RestTemplate restTemplate,
            SimActivationRepository simActivationRepository,
            @Value("${actuator.service.url}") String actuatorServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.simActivationRepository = simActivationRepository;
        this.actuatorServiceUrl = actuatorServiceUrl;
    }

    //  POST: Activate SIM + Save result
    @PostMapping
    public ResponseEntity<String> activateSim(@RequestBody ActivationRequest request) {

        //  Input validation
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
                        actuatorServiceUrl,
                        actuatorRequest,
                        ActuatorResponse.class
                );

        // Validate HTTP response
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Actuator service failed"
            );
        }

        // Null safety check
        ActuatorResponse body = response.getBody();
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Empty response from actuator service"
            );
        }

        boolean success = body.isSuccess();

        //  Persist activation result
        SimActivation activation = new SimActivation(
                request.getIccid(),
                request.getCustomerEmail(),
                success
        );
        simActivationRepository.save(activation);

        return ResponseEntity.ok("Activation result: " + success);
    }

    //  GET: Fetch activation record by ID
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
