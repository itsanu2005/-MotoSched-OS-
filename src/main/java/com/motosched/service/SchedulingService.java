package com.motosched.service;

import com.motosched.model.ServiceJob;
import java.util.List;

public class SchedulingService {
    public void displayOrder(List<ServiceJob> jobs, SchedulingStrategy strategy) {
        if (jobs.isEmpty()) {
            System.out.println("No READY service requests.");
            return;
        }

        List<ServiceJob> ordered = strategy.schedule(jobs);
        int clock = 0;
        int totalWaiting = 0;
        int totalTurnaround = 0;

        System.out.println("\n" + strategy.getName());
        System.out.println("ID | Vehicle | Service | Burst | Priority | CT | WT | TAT");
        System.out.print("Order: ");

        for (int i=0; i<ordered.size(); i++) {
            ServiceJob job = ordered.get(i);
            clock = Math.max(clock, job.getArrivalOrder());
            int waiting = clock - job.getArrivalOrder();
            clock += job.getBurstTime();
            int completion = clock;
            int turnaround = completion - job.getArrivalOrder();
            totalWaiting += waiting;
            totalTurnaround += turnaround;

            System.out.printf("%d | %s | %s | %d | %d | %d | %d | %d%n",
                    job.getId(), job.getRegistration(), job.getServiceType(),
                    job.getBurstTime(), job.getPriority(), completion, waiting, turnaround);
            System.out.print("P" + job.getId() + (i == ordered.size()-1 ? "\n" : " -> "));
        }
        System.out.printf("Average waiting time: %.2f%n", (double) totalWaiting / ordered.size());
        System.out.printf("Average turnaround time: %.2f%n", (double) totalTurnaround / ordered.size());
        System.out.println("Note: this is a scheduling simulation; actual service starts only after resource assignment.");
    }
}
