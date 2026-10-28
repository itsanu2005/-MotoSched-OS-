package com.motosched.service;

import com.motosched.model.ServiceJob;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
 * This simple RR strategy models each job as a burst split into time slices.
 * It returns the order in which jobs receive a time slice. The console app
 * reports that order; actual workshop work is still marked RUNNING only when
 * resources are assigned.
 */
public class RoundRobinStrategy {
    public List<Integer> buildSliceOrder(List<ServiceJob> jobs, int quantum) {
        if (quantum <= 0) throw new IllegalArgumentException("Quantum must be positive.");
        List<ServiceJob> sorted = new ArrayList<>(jobs);
        sorted.sort(Comparator.comparingInt(ServiceJob::getArrivalOrder)
                .thenComparingInt(ServiceJob::getId));

        class SliceJob {
            ServiceJob job;
            int remaining;
            SliceJob(ServiceJob job) { this.job = job; this.remaining = job.getBurstTime(); }
        }

        List<SliceJob> all = new ArrayList<>();
        for (ServiceJob job : sorted) all.add(new SliceJob(job));
        List<Integer> order = new ArrayList<>();
        List<SliceJob> ready = new ArrayList<>();
        int index = 0;
        int clock = sorted.isEmpty() ? 0 : sorted.get(0).getArrivalOrder();

        while (index < all.size() || !ready.isEmpty()) {
            while (index < all.size() && all.get(index).job.getArrivalOrder() <= clock) {
                ready.add(all.get(index++));
            }
            if (ready.isEmpty()) {
                clock = all.get(index).job.getArrivalOrder();
                continue;
            }
            SliceJob current = ready.remove(0);
            order.add(current.job.getId());
            int run = Math.min(quantum, current.remaining);
            current.remaining -= run;
            clock += run;
            while (index < all.size() && all.get(index).job.getArrivalOrder() <= clock) {
                ready.add(all.get(index++));
            }
            if (current.remaining > 0) ready.add(current);
        }
        return order;
    }
}
