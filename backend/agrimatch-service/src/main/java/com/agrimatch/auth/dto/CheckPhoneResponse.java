package com.agrimatch.auth.dto;

public class CheckPhoneResponse {
    private boolean registered;

    public CheckPhoneResponse() {}
    public CheckPhoneResponse(boolean registered) { this.registered = registered; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
}
