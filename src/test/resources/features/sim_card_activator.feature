Feature: SIM Card Activation

  Scenario: Successful SIM card activation
    Given a SIM card with ICCID "1255789453849037777" and customer email "success@test.com"
    When the SIM card activation request is submitted
    Then the activation result should be active for record id 1

  Scenario: Failed SIM card activation
    Given a SIM card with ICCID "8944500102198304826" and customer email "failure@test.com"
    When the SIM card activation request is submitted
    Then the activation result should be inactive for record id 2
