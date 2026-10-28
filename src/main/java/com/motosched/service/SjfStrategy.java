package com.motosched.service;

import com.motosched.model.ServiceJob;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SjfStrategy implements SchedulingStrategy {
    public String getName() { return "SJF (non-preemptive)"; }

    public List<ServiceJob> schedule(List<ServiceJob> jobs) {
        List<ServiceJob> result = new ArrayList<>(jobs);
        result.sort(Comparator.comparingInt(ServiceJob::getBurstTime)
                .thenComparingInt(ServiceJob::getArrivalOrder)
                .thenComparingInt(ServiceJob::getId));
        return result;
    }
}
