package com.motosched.model;

public class ServiceJob {
    private final int id;
    private final String registration;
    private final String serviceType;
    private final int arrivalOrder;
    private final int burstTime;
    private final int priority;

    public ServiceJob(int id, String registration, String serviceType,
                      int arrivalOrder, int burstTime, int priority) {
        this.id = id;
        this.registration = registration;
        this.serviceType = serviceType;
        this.arrivalOrder = arrivalOrder;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    public int getId() { return id; }
    public String getRegistration() { return registration; }
    public String getServiceType() { return serviceType; }
    public int getArrivalOrder() { return arrivalOrder; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
}
