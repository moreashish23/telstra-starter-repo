package au.com.telstra.simcardactivator.dto;

public class ActuatorResponse {
    private boolean success;

    public ActuatorResponse(){

    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
