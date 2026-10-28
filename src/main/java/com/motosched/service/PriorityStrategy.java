package com.motosched.service;

import com.motosched.model.ServiceJob;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorityStrategy implements SchedulingStrategy {
    public String getName() { return "Priority (1 = highest)"; }

    public List<ServiceJob> schedule(List<ServiceJob> jobs) {
        List<ServiceJob> result = new ArrayList<>(jobs);
        result.sort(Comparator.comparingInt(ServiceJob::getPriority)
                .thenComparingInt(ServiceJob::getArrivalOrder)
                .thenComparingInt(ServiceJob::getId));
        return result;
    }
}
