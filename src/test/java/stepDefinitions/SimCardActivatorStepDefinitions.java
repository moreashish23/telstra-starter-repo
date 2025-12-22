package stepDefinitions;

import au.com.telstra.simcardactivator.dto.ActivationRequest;
import au.com.telstra.simcardactivator.dto.ActivationStatusResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SimCardActivatorStepDefinitions {

    @Autowired
    private RestTemplate restTemplate;

    private ActivationRequest activationRequest;

    @Given("a SIM card with ICCID {string} and customer email {string}")
    public void a_sim_card_with_iccid_and_customer_email(String iccid, String email) {
        activationRequest = new ActivationRequest();
        activationRequest.setIccid(iccid);
        activationRequest.setCustomerEmail(email);
    }

    @When("the SIM card activation request is submitted")
    public void the_sim_card_activation_request_is_submitted() {
        restTemplate.postForEntity(
                "http://localhost:8081/activate-sim",
                activationRequest,
                String.class
        );
    }

    @Then("the activation result should be active for record id {long}")
    public void the_activation_result_should_be_active(Long id) {
        ActivationStatusResponse response =
                restTemplate.getForObject(
                        "http://localhost:8081/activate-sim?simCardId=" + id,
                        ActivationStatusResponse.class
                );

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isActive());
    }

    @Then("the activation result should be inactive for record id {long}")
    public void the_activation_result_should_be_inactive(Long id) {
        ActivationStatusResponse response =
                restTemplate.getForObject(
                        "http://localhost:8081/activate-sim?simCardId=" + id,
                        ActivationStatusResponse.class
                );

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isActive());
    }
}
