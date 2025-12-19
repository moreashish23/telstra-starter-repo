package au.com.telstra.simcardactivator.dto;

public class ActuatorRequest {
    private String iccid;

    public ActuatorRequest() {
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }
}
