package org.howards4hope.config;

import org.howards4hope.model.Event;
import org.howards4hope.model.BlogPost;
import org.howards4hope.model.PageAnalytics;
import org.howards4hope.repository.EventRepository;
import org.howards4hope.repository.BlogRepository;
import org.howards4hope.repository.AnalyticsRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class EventDataSeeder implements CommandLineRunner {
    private final EventRepository eventRepository;
    private final BlogRepository blogRepository;
    private final AnalyticsRepository analyticsRepository;

    public EventDataSeeder(EventRepository eventRepository,
                           BlogRepository blogRepository,
                           AnalyticsRepository analyticsRepository) {
        this.eventRepository = eventRepository;
        this.blogRepository = blogRepository;
        this.analyticsRepository = analyticsRepository;
    }
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
                    "Youth",
                    "#1E2761"
            ));

            eventRepository.save(new Event(
                    "Links of Hope Support Summit",
                    "An intensive networking conference bringing together caregivers of special-needs children to share resources and stress-relief models.",
                    "2026-09-26",
                    "11:00 AM",
                    "3711 Long Beach Blvd, Long Beach, CA 90807",
                    15.00,
                    "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=1000",
                    "Caregivers",
                    "#F39C12"
            ));

            eventRepository.save(new Event(
                    "Single Parents Resource Clinic",
                    "Collaborative dynamic forum mapping financial self-sufficiency paths, child care subsidies, and public aid applications.",
                    "2026-10-14",
                    "4:00 PM",
                    "3711 Long Beach Blvd, Long Beach, CA 90807",
                    0.0,
                    "https://images.unsplash.com/photo-1491438590914-bc09fcaaf77a?auto=format&fit=crop&q=80&w=1000",
                    "Parents",
                    "#007C92"
            ));

            eventRepository.save(new Event(
                    "Unmasking Hope Annual Charity Gala",
                    "Our premium annual fundraiser event featuring elegant gala dining, community achievement awards, and silent auctions.",
                    "2026-11-19",
                    "6:00 PM",
                    "Grand Ballroom, Long Beach, CA 90802",
                    75.00,
                    "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&q=80&w=1000",
                    "Fundraiser",
                    "#27AE60"
            ));

            System.out.println(">>> Event seeding complete. " + eventRepository.count() + " events available.");
        }

        if (blogRepository.count() == 0) {
            System.out.println(">>> Seeding database with high-fidelity template blog posts...");

            blogRepository.save(new BlogPost(
                    "Empowering Our Youth: Key Takeaways from Our Latest Seminar",
                    "Last week, Howards 4 Hope hosted the inaugural 'Me, Myself & Why' Youth Empowerment Seminar. Over 45 local Long Beach youth attended, engaging in interactive confidence-building exercises, resume building, and leadership roadmaps. The energy was electric, and we are inspired by the resilience and vision of our next generation. Thank you to our mentors and sponsors who made this possible!",
                    "Founder LaCreashia Willis-Howard",
                    "2026-05-15",
                    "Youth Milestones",
                    "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000"
            ));

            blogRepository.save(new BlogPost(
                    "New Funding Secured to Support Special-Needs Caregivers",
                    "We are thrilled to announce that Howards 4 Hope has been awarded a generous community grant to expand our Caregivers Respite Support Network. This funding will allow us to double the capacity of our monthly Links of Hope Support Summits, providing emergency emotional relief, respite child care, and mental health workshops for dedicated caregivers. Together, we rise by lifting others.",
                    "Caregiver Director",
                    "2026-05-18",
                    "Caregiver Summits",
                    "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=1000"
            ));

            System.out.println(">>> Blog seeding complete. " + blogRepository.count() + " blog posts seeded.");
        }

        if (analyticsRepository.count() == 0) {
            System.out.println(">>> Seeding baseline traffic analytics for overtime usage and unique reporting...");
            String[] paths = {"/", "/#events", "/#programs", "/#about", "/#gala", "/#contact", "/#blog"};
            Random random = new Random();
            LocalDateTime now = LocalDateTime.now();

            for (int day = 30; day >= 0; day--) {
                LocalDateTime dayTime = now.minusDays(day);
                // Daily baseline volume: 15 to 45 page views across 8 to 25 unique visitors
                int uniqueCount = 8 + random.nextInt(18);
                int viewsCount = uniqueCount + random.nextInt(20);

                for (int v = 0; v < viewsCount; v++) {
                    int visIndex = random.nextInt(uniqueCount) + 1;
                    String visitorId = "vis_" + String.format("%03d", (day * 100) + visIndex);
                    String path = paths[random.nextInt(paths.length)];
                    LocalDateTime timestamp = dayTime.plusHours(random.nextInt(23)).plusMinutes(random.nextInt(59));
                    analyticsRepository.save(new PageAnalytics(path, visitorId, timestamp));
                }
            }
            System.out.println(">>> Traffic analytics seeding complete. " + analyticsRepository.count() + " records available.");
        }
    }
}
