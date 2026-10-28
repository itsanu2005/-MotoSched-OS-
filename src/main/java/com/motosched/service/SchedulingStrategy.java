package com.motosched.service;

import com.motosched.model.ServiceJob;
import java.util.List;

public interface SchedulingStrategy {
    String getName();
    List<ServiceJob> schedule(List<ServiceJob> jobs);
}
