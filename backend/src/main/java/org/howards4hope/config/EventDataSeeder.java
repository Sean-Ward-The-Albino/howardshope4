package org.howards4hope.config;

import org.howards4hope.model.Event;
import org.howards4hope.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EventDataSeeder implements CommandLineRunner {

    @Autowired
    private EventRepository eventRepository;

    @Override
    public void run(String... args) throws Exception {
        if (eventRepository.count() == 0) {
            System.out.println(">>> Seeding database with high-fidelity template events...");

            eventRepository.save(new Event(
                    "Me, Myself & Why Workshop",
                    "Empowerment seminar focused on confidence building, leadership traits, and self-growth roadmap models for local youth.",
                    "2026-09-10",
                    "4:00 PM",
                    "3711 Long Beach Blvd, Long Beach, CA 90807",
                    0.0,
                    "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000",
                    "Youth"
            ));

            eventRepository.save(new Event(
                    "Links of Hope Support Summit",
                    "An intensive networking conference bringing together caregivers of special-needs children to share resources and stress-relief models.",
                    "2026-09-26",
                    "11:00 AM",
                    "3711 Long Beach Blvd, Long Beach, CA 90807",
                    15.00,
                    "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=1000",
                    "Caregivers"
            ));

            eventRepository.save(new Event(
                    "Single Parents Resource Clinic",
                    "Collaborative dynamic forum mapping financial self-sufficiency paths, child care subsidies, and public aid applications.",
                    "2026-10-14",
                    "4:00 PM",
                    "3711 Long Beach Blvd, Long Beach, CA 90807",
                    0.0,
                    "https://images.unsplash.com/photo-1491438590914-bc09fcaaf77a?auto=format&fit=crop&q=80&w=1000",
                    "Parents"
            ));

            eventRepository.save(new Event(
                    "Unmasking Hope Annual Charity Gala",
                    "Our premium annual fundraiser event featuring elegant gala dining, community achievement awards, and silent auctions.",
                    "2026-11-19",
                    "6:00 PM",
                    "Grand Ballroom, Long Beach, CA 90802",
                    75.00,
                    "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&q=80&w=1000",
                    "Fundraiser"
            ));

            System.out.println(">>> Event seeding complete. " + eventRepository.count() + " events available.");
        }
    }
}
