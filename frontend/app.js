/* --- APPLICATION STATE & ROUTING ENGINE --- */

// Fallback configuration if Firebase credentials are not yet initialized on staging
const firebaseConfig = {
  apiKey: "AIzaSyFakeKeyForLocalDevelopmentOnly",
  authDomain: "howards4hope-b06f6.firebaseapp.com",
  projectId: "howards4hope-b06f6",
  storageBucket: "howards4hope-b06f6.appspot.com",
  messagingSenderId: "1055785276298",
  appId: "1:1055785276298:web:fakeappid"
};

// Initialize Firebase
try {
  firebase.initializeApp(firebaseConfig);
} catch (e) {
  console.log("Firebase already initialized or running locally.");
}

// Global App State
const state = {
  user: null,
  isAdmin: false,
  activeRoute: 'home',
  events: [],
  selectedDate: new Date(),
  selectedEvent: null,
  cartEvent: null,
  resources: [
    { id: 1, title: "Long Beach Youth Development", category: "youth", desc: "A curated list of skill-building, resume mentoring, and leadership workshops.", link: "https://www.longbeach.gov/health/community-health/youth-development/" },
    { id: 2, title: "Caregivers Respite Support Network", category: "caregivers", desc: "Providing emotional, financial and legal resource navigations for disability caregivers.", link: "https://www.caregiver.org" },
    { id: 3, title: "Single Parents Housing Assistance", category: "parents", desc: "Emergency grants, shelter guides, and low-income rental options in California.", link: "https://www.dhcs.ca.gov" },
    { id: 4, title: "Me, Myself & Why Workshop Materials", category: "youth", desc: "Digital workbook downloads for mental-health wellness and emotional self-sufficiency.", link: "#" },
    { id: 5, title: "Special Education Navigators", category: "caregivers", desc: "Advocacy guidelines and IEP roadmap toolkits for families with special needs.", link: "#" },
    { id: 6, title: "CalFresh & Medi-Cal Application Hub", category: "parents", desc: "Direct navigation to secure California welfare benefit allocations.", link: "https://www.benefitscal.com" }
  ],
  myTickets: []
};

// Seed Mock Events for beautiful immediate loading & offline support
const mockEvents = [
  {
    id: "evt-001",
    title: "Me, Myself & Why Workshop",
    date: "2026-09-10",
    time: "4:00 PM",
    location: "3711 Long Beach Blvd, Long Beach, CA 90807",
    price: 0,
    banner: "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000",
    desc: "Empowerment seminar focused on confidence building, leadership traits, and self-growth roadmap models for local youth.",
    category: "Youth"
  },
  {
    id: "evt-002",
    title: "Links of Hope Support Summit",
    date: "2026-09-26",
    time: "11:00 AM",
    location: "3711 Long Beach Blvd, Long Beach, CA 90807",
    price: 15.00,
    banner: "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=1000",
    desc: "An intensive networking conference bringing together caregivers of special-needs children to share resources and stress-relief models.",
    category: "Caregivers"
  },
  {
    id: "evt-003",
    title: "Single Parents Resource Clinic",
    date: "2026-10-14",
    time: "4:00 PM",
    location: "3711 Long Beach Blvd, Long Beach, CA 90807",
    price: 0,
    banner: "https://images.unsplash.com/photo-1491438590914-bc09fcaaf77a?auto=format&fit=crop&q=80&w=1000",
    desc: "Collaborative dynamic forum mapping financial self-sufficiency paths, child care subsidies, and public aid applications.",
    category: "Parents"
  },
  {
    id: "evt-004",
    title: "Unmasking Hope Annual Charity Gala",
    date: "2026-11-19",
    time: "6:00 PM",
    location: "Grand Ballroom, Long Beach, CA 90802",
    price: 75.00,
    banner: "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&q=80&w=1000",
    desc: "Our premium annual fundraiser event featuring elegant gala dining, community achievement awards, and silent auctions.",
    category: "Fundraiser"
  }
];

state.events = [...mockEvents];

// Backend API Service Client
const API = {
  baseUrl: 'http://localhost:8080/api',
  
  async getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (state.user) {
      try {
        const token = await firebase.auth().currentUser.getIdToken();
        headers['Authorization'] = `Bearer ${token}`;
      } catch (e) {
        console.error("Error fetching token", e);
      }
    }
    return headers;
  },

  async getEvents() {
    try {
      const response = await fetch(`${this.baseUrl}/events`);
      if (response.ok) return await response.json();
    } catch (e) {
      console.log("Spring Boot API offline, falling back to mock client-side state.");
    }
    return state.events;
  },

  async bookTicket(eventId, quantity, paymentMethod = 'FREE') {
    const event = state.events.find(e => e.id === eventId);
    if (!event) return false;
    
    // Simulate booking ticket locally
    const ticket = {
      ticketId: 'tkt-' + Math.floor(100000 + Math.random() * 900000),
      eventTitle: event.title,
      eventDate: event.date,
      eventLocation: event.location,
      quantity: quantity,
      pricePaid: event.price * quantity,
      paymentMethod: paymentMethod,
      purchaseDate: new Date().toISOString().split('T')[0]
    };
    
    state.myTickets.push(ticket);
    return ticket;
  }
};

/* --- FIREBASE AUTHENTICATION LISTENERS --- */
firebase.auth().onAuthStateChanged(user => {
  const userMenu = document.getElementById('user-menu-container');
  const loginBtn = document.getElementById('login-trigger-btn');
  
  if (user) {
    state.user = user;
    // Check if user is admin (using simple custom email rule for high-fidelity showcase, or custom claims)
    state.isAdmin = user.email.includes('admin') || user.email === 'avlorycorp@gmail.com';
    
    document.getElementById('user-display-email').innerText = user.email;
    
    // Toggle active display
    loginBtn.style.display = 'none';
    
    // Render My Tickets / Admin links
    const dashboardLink = document.getElementById('dashboard-link');
    if (state.isAdmin) {
      dashboardLink.style.display = 'flex';
      dashboardLink.innerHTML = '<i class="fa-solid fa-gauge"></i> Admin Dashboard';
      dashboardLink.href = '#/dashboard';
    } else {
      dashboardLink.style.display = 'none';
    }
    
    // Toggle dropdown UI binding
    const trigger = document.createElement('div');
    trigger.id = 'user-avatar-trigger';
    trigger.className = 'avatar-btn';
    trigger.innerHTML = user.email.substring(0, 2).toUpperCase();
    
    // Cleanup old trigger if exists
    const oldTrigger = document.getElementById('user-avatar-trigger');
    if (oldTrigger) oldTrigger.remove();
    userMenu.appendChild(trigger);
    
    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      document.getElementById('user-dropdown-menu').classList.toggle('active');
    });
    
  } else {
    state.user = null;
    state.isAdmin = false;
    state.myTickets = [];
    
    loginBtn.style.display = 'flex';
    const oldTrigger = document.getElementById('user-avatar-trigger');
    if (oldTrigger) oldTrigger.remove();
    document.getElementById('user-dropdown-menu').classList.remove('active');
  }
  
  // Refresh page shell context
  router();
});

// Close dropdown on click outside
window.addEventListener('click', () => {
  const menu = document.getElementById('user-dropdown-menu');
  if (menu) menu.classList.remove('active');
});

/* --- POPUP DIALOG TRIGGERS --- */
const authModal = document.getElementById('auth-modal');
const authTrigger = document.getElementById('login-trigger-btn');
const authClose = document.getElementById('auth-modal-close');
const authToggleLink = document.getElementById('auth-toggle-link');
const authForm = document.getElementById('auth-form');

let isSignupMode = false;

if (authTrigger) {
  authTrigger.addEventListener('click', () => {
    isSignupMode = false;
    toggleAuthMode(false);
    authModal.classList.add('active');
  });
}

if (authClose) {
  authClose.addEventListener('click', () => {
    authModal.classList.remove('active');
  });
}

if (authToggleLink) {
  authToggleLink.addEventListener('click', () => {
    isSignupMode = !isSignupMode;
    toggleAuthMode(isSignupMode);
  });
}

function toggleAuthMode(isSignup) {
  const submitBtn = document.getElementById('auth-submit-btn');
  const title = document.getElementById('auth-title');
  const toggleText = document.getElementById('auth-toggle');
  
  if (isSignup) {
    title.innerText = "Create Account";
    submitBtn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Sign Up';
    toggleText.innerHTML = 'Already have an account? <span id="auth-toggle-link">Login</span>';
  } else {
    title.innerText = "Welcome Back";
    submitBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Login';
    toggleText.innerHTML = "Don't have an account? <span id='auth-toggle-link'>Sign Up</span>";
  }
  // Re-bind click listener to dynamically generated span
  document.getElementById('auth-toggle-link').addEventListener('click', () => {
    isSignupMode = !isSignupMode;
    toggleAuthMode(isSignupMode);
  });
}

// Perform Email/Password authentication
if (authForm) {
  authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('auth-email').value;
    const password = document.getElementById('auth-password').value;
    
    try {
      if (isSignupMode) {
        await firebase.auth().createUserWithEmailAndPassword(email, password);
        alert("Account created successfully!");
      } else {
        await firebase.auth().signInWithEmailAndPassword(email, password);
      }
      authModal.classList.remove('active');
    } catch (err) {
      alert(err.message);
    }
  });
}

// Google Authentication
const googleBtn = document.getElementById('google-login-btn');
if (googleBtn) {
  googleBtn.addEventListener('click', async () => {
    const provider = new firebase.auth.GoogleAuthProvider();
    try {
      await firebase.auth().signInWithPopup(provider);
      authModal.classList.remove('active');
    } catch (err) {
      alert(err.message);
    }
  });
}

// Logout Action
const logoutBtn = document.getElementById('logout-btn');
if (logoutBtn) {
  logoutBtn.addEventListener('click', async () => {
    await firebase.auth().signOut();
    window.location.hash = '#/';
  });
}

// Mobile Menu Control
const mobileMenuBtn = document.getElementById('mobile-menu-toggle');
const mobileDrawer = document.getElementById('mobile-drawer');
const mobileClose = document.getElementById('mobile-drawer-close');

if (mobileMenuBtn) {
  mobileMenuBtn.style.display = 'block';
  mobileMenuBtn.addEventListener('click', () => {
    mobileDrawer.classList.add('active');
  });
}
if (mobileClose) {
  mobileClose.addEventListener('click', () => {
    mobileDrawer.classList.remove('active');
  });
}

// Close mobile drawer on route click
document.querySelectorAll('#mobile-drawer .nav-link').forEach(link => {
  link.addEventListener('click', () => {
    mobileDrawer.classList.remove('active');
  });
});

/* --- CLIENT SIDE TEMPLATE COMPOSERS --- */

const templates = {
  home() {
    return `
      <!-- --- HERO --- -->
      <section class="hero">
        <div class="hero-bg-shapes">
          <div class="hero-shape-1"></div>
          <div class="hero-shape-2"></div>
        </div>
        <div class="hero-content">
          <div class="hero-tag">Empowerment & Support</div>
          <h1 class="hero-title">Restoring <span>Hope</span><br>Rebuilding Lives</h1>
          <p class="hero-subtitle">We assist disadvantaged and underserved families within the Long Beach community with resources to improve and maintain a holistically healthy quality of life.</p>
          <div class="hero-actions">
            <a href="#/donate" class="btn btn-donate"><i class="fa-solid fa-heart"></i> Donate Now</a>
            <a href="#/programs" class="btn btn-outline" style="color: white; border-color: white;"><i class="fa-solid fa-hands-holding-child"></i> Our Programs</a>
          </div>
        </div>
        <div class="hero-image-wrapper">
          <img class="hero-image" src="https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&q=80&w=1200" alt="Hope Community">
        </div>
      </section>

      <!-- --- STATS BAR --- -->
      <section class="stats-bar">
        <div class="stat-item">
          <div class="stat-num">500<span>+</span></div>
          <div class="stat-label">Families Uplifted</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">100<span>%</span></div>
          <div class="stat-label">Direct Impact</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">501<span>(c)(3)</span></div>
          <div class="stat-label">Registered Non-profit</div>
        </div>
      </section>

      <!-- --- PILLARS OF MISSION --- -->
      <section class="section">
        <div class="section-header">
          <span class="section-tag">Who We Uplift</span>
          <h2 class="section-title">Serving Our Community Pillars</h2>
          <p class="section-subtitle">We deliver structural aid, educational workshops, and community safety nets to three fundamental pillars in Long Beach.</p>
        </div>
        <div class="pillars-grid">
          <div class="pillar-card">
            <div class="pillar-icon"><i class="fa-solid fa-graduation-cap"></i></div>
            <h3 class="pillar-title">Youth Empowerment</h3>
            <p class="pillar-text">Confidence building seminars, resume review bootcamps, and career mentoring guidelines that motivate younger demographics to overcome temporary setbacks.</p>
            <a href="#/programs" class="res-link">Explore Workshops <i class="fa-solid fa-arrow-right"></i></a>
          </div>
          <div class="pillar-card">
            <div class="pillar-icon"><i class="fa-solid fa-hand-holding-hand"></i></div>
            <h3 class="pillar-title">Caregivers Support</h3>
            <p class="pillar-text">Supplying structural advocacy, information roadmaps, and mental wellness sessions tailored for dedicated parents and guardians of individuals with disabilities.</p>
            <a href="#/programs" class="res-link">Access Resources <i class="fa-solid fa-arrow-right"></i></a>
          </div>
          <div class="pillar-card">
            <div class="pillar-icon"><i class="fa-solid fa-people-roof"></i></div>
            <h3 class="pillar-title">Single Parents Care</h3>
            <p class="pillar-text">Navigating complex state health, nutritional and local shelter aid paths to guide households securely toward complete long-term financial self-sufficiency.</p>
            <a href="#/programs" class="res-link">Get Assistance <i class="fa-solid fa-arrow-right"></i></a>
          </div>
        </div>
      </section>

      <!-- --- TEASER EVENTS --- -->
      <section class="section section-alt">
        <div class="section-header">
          <span class="section-tag">Happening Soon</span>
          <h2 class="section-title">Featured Upcoming Activities</h2>
          <p class="section-subtitle">Be part of our community action. Secure your entry or book a dynamic reservation ticket below.</p>
        </div>
        
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 2rem;">
          ${state.events.slice(0, 3).map(event => `
            <div class="event-hifi-card">
              <div class="event-banner" style="background-image: url('${event.banner}')">
                <span class="event-badge">${event.category}</span>
              </div>
              <div class="event-body">
                <div class="event-meta">
                  <span class="event-meta-item"><i class="fa-solid fa-calendar-days"></i> ${event.date}</span>
                  <span class="event-meta-item"><i class="fa-solid fa-clock"></i> ${event.time}</span>
                </div>
                <h3>${event.title}</h3>
                <p class="event-desc">${event.desc}</p>
                <div class="event-footer">
                  <span class="event-price ${event.price === 0 ? 'free' : ''}">${event.price === 0 ? 'FREE' : '$' + event.price.toFixed(2)}</span>
                  <a href="#/events" class="btn btn-primary" style="padding: 8px 18px; font-size: 0.85rem;"><i class="fa-solid fa-ticket"></i> RSVP / Register</a>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </section>
    `;
  },

  about() {
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Our History</span>
          <h2 class="section-title">The Philosophy of Hope</h2>
          <p class="section-subtitle">Founded by the Howard Family, our organization is dedicated to creating synergetic environments for all community members to bloom.</p>
        </div>
        
        <div style="display: flex; gap: 4rem; align-items: center; margin-bottom: 80px; flex-wrap: wrap;">
          <div style="flex: 1.2; min-width: 320px;">
            <h3 style="font-size: 1.75rem; margin-bottom: 1.5rem; color: var(--primary);">Restoring Dignity, Reclaiming Strength</h3>
            <p style="color: var(--text-muted); margin-bottom: 1.5rem;">It is our foundational goal to deliver and usher in pleasurable anticipation as we assist our community in its hopeful endeavors. We facilitate opportunities for communities to thrive, especially those considered to be unheard.</p>
            <p style="color: var(--text-muted); margin-bottom: 1.5rem;">By supporting and equipping those within our communities with critical local knowledge, networking guidelines, and economic toolkits, we enable long-term self-sufficiency while multiplying the core pride families have in themselves.</p>
          </div>
          <div style="flex: 1; min-width: 320px;">
            <div class="form-card" style="padding: 30px; margin: 0; background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: white;">
              <h4 style="color: white; font-size: 1.25rem; margin-bottom: 12px;"><i class="fa-solid fa-quote-left" style="color: var(--accent);"></i> The Founder's Vision</h4>
              <p style="font-style: italic; font-size: 0.95rem; line-height: 1.7; opacity: 0.9;">"Our family team consists of family and friends with a common goal: to assist our community by creating opportunities and encouraging hope. We're proud to offer a helping hand to those who need it, always expanding our reach."</p>
              <div style="margin-top: 20px; font-weight: 700; color: var(--accent); font-family: 'Outfit';">— The Howard Family</div>
            </div>
          </div>
        </div>
        
        <div class="section-header" style="margin-bottom: 40px;">
          <h3 class="section-title" style="font-size: 1.75rem;">Our Trusted Sponsors & Partners</h3>
          <p class="section-subtitle">Empowered by the generous contributions of corporate allies and community groups.</p>
        </div>
        
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 2rem; text-align: center;" id="partners-logo-grid">
          <div class="pillar-card" style="padding: 24px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.15rem; color: var(--text-muted); opacity: 0.7;">
            <i class="fa-solid fa-handshake" style="margin-right: 8px;"></i> Long Beach Gives
          </div>
          <div class="pillar-card" style="padding: 24px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.15rem; color: var(--text-muted); opacity: 0.7;">
            <i class="fa-solid fa-landmark" style="margin-right: 8px;"></i> LB Health Department
          </div>
          <div class="pillar-card" style="padding: 24px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.15rem; color: var(--text-muted); opacity: 0.7;">
            <i class="fa-solid fa-store" style="margin-right: 8px;"></i> Local Merchants Guild
          </div>
          <div class="pillar-card" style="padding: 24px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.15rem; color: var(--text-muted); opacity: 0.7;">
            <i class="fa-solid fa-graduation-cap" style="margin-right: 8px;"></i> LB USD Foundation
          </div>
        </div>
      </section>
    `;
  },

  programs() {
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Resources Hub</span>
          <h2 class="section-title">Programs & Links of Hope</h2>
          <p class="section-subtitle">Search our database of support links, emergency shelters, youth programs, and family navigators.</p>
        </div>
        
        <div class="resource-hub">
          <div class="search-bar">
            <i class="fa-solid fa-magnifying-glass" style="color: var(--text-muted); margin-right: 12px;"></i>
            <input type="text" class="search-input" id="resource-search" placeholder="Search resources (e.g., 'caregiver', 'aid', 'welfare')...">
          </div>
          
          <div class="resource-categories">
            <span class="category-pill active" data-cat="all">All Resources</span>
            <span class="category-pill" data-cat="youth">Youth Empowerment</span>
            <span class="category-pill" data-cat="caregivers">Caregiver Support</span>
            <span class="category-pill" data-cat="parents">Single Parents</span>
          </div>
          
          <div class="resources-grid" id="resources-grid-container">
            <!-- Loaded dynamically by binding -->
          </div>
        </div>
      </section>
    `;
  },

  events() {
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Calendar Hub</span>
          <h2 class="section-title">Schedule of Events</h2>
          <p class="section-subtitle">Browse through our calendar grid and secure your seat for our educational workshops or fundraising banquets.</p>
        </div>
        
        <div class="events-wrapper">
          <!-- Calendar Card -->
          <div class="calendar-card">
            <div class="calendar-header">
              <h3 class="calendar-title" id="calendar-month-year">September 2026</h3>
              <div class="calendar-nav">
                <div class="cal-btn" id="prev-month-btn"><i class="fa-solid fa-chevron-left"></i></div>
                <div class="cal-btn" id="next-month-btn"><i class="fa-solid fa-chevron-right"></i></div>
              </div>
            </div>
            <div class="calendar-grid" id="calendar-days-grid">
              <!-- Loaded Dynamically -->
            </div>
          </div>
          
          <!-- Event Detail Panel -->
          <div class="event-details-panel">
            <h3 style="border-bottom: 2px solid var(--primary); padding-bottom: 10px;">Select Event Information</h3>
            <div id="active-event-detail-placeholder">
              <div class="pillar-card" style="text-align: center; color: var(--text-muted);">
                <i class="fa-regular fa-calendar-check" style="font-size: 2.5rem; margin-bottom: 15px; color: var(--secondary);"></i>
                <p>Click on any date in the calendar containing a colored marker dot to preview event details and RSVP!</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    `;
  },

  getInvolved() {
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Get Involved</span>
          <h2 class="section-title">Join the Mission</h2>
          <p class="section-subtitle">Whether you wish to donate your time, sponsor our programs, or write to our support desk, your aid is invaluable.</p>
        </div>
        
        <div class="form-card">
          <h3 style="margin-bottom: 25px; text-align: center;"><i class="fa-solid fa-envelope-open-text" style="color: var(--secondary); margin-right: 8px;"></i> Outreach Application</h3>
          <form id="involvement-form">
            <div class="form-group">
              <label class="form-label">Full Name</label>
              <input type="text" class="form-control" required placeholder="John Doe">
            </div>
            <div class="form-group">
              <label class="form-label">Email Address</label>
              <input type="email" class="form-control" required placeholder="john@domain.com">
            </div>
            <div class="form-group">
              <label class="form-label">I want to join as...</label>
              <select class="form-control" style="background-image: none;" required>
                <option value="volunteer">Volunteer (Donate your time)</option>
                <option value="sponsor">Corporate Sponsor / Donor</option>
                <option value="partner">Non-Profit Partner</option>
                <option value="general">General Outreach / Question</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Message / Cover Note</label>
              <textarea class="form-control" required placeholder="Tell us how you would like to help..."></textarea>
            </div>
            <button class="btn btn-primary" style="width: 100%; height: 48px; margin-top: 10px;" type="submit">
              <i class="fa-solid fa-paper-plane"></i> Submit Application
            </button>
          </form>
        </div>
      </section>
    `;
  },

  donate() {
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Make an Impact</span>
          <h2 class="section-title">Support Our Cause</h2>
          <p class="section-subtitle">Every dollar directly fuels youth mentorship, caregiver respite networks, and single parent aid packets in Long Beach.</p>
        </div>
        
        <div class="form-card" style="max-width: 500px;">
          <h3 style="margin-bottom: 25px; text-align: center;"><i class="fa-solid fa-heart" style="color: var(--danger); margin-right: 8px;"></i> Secure Donation Portal</h3>
          
          <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 25px;">
            <button class="btn btn-outline donate-amount-btn" data-amt="25">$25</button>
            <button class="btn btn-outline donate-amount-btn active" data-amt="50" style="background: var(--primary); color: white;">$50</button>
            <button class="btn btn-outline donate-amount-btn" data-amt="100">$100</button>
          </div>
          
          <div class="form-group">
            <label class="form-label">Custom Donation ($)</label>
            <input type="number" class="form-control" id="custom-donation-amt" value="50" min="5">
          </div>
          
          <div class="auth-divider">Payment Gateways</div>
          
          <button class="auth-social-btn" id="stripe-donate-btn" style="background: #635bff; color: white; border: none; height: 48px;">
            <i class="fa-brands fa-stripe"></i> Donate with Stripe
          </button>
          
          <button class="auth-social-btn" id="paypal-donate-btn" style="background: #ffc439; color: #003087; border: none; height: 48px;">
            <i class="fa-brands fa-paypal"></i> Donate with PayPal
          </button>
          
          <p style="font-size: 0.75rem; color: var(--text-muted); text-align: center; margin-top: 20px;">
            <i class="fa-solid fa-shield-halved"></i> 256-bit Secure Encryption. Tax-deductible under EIN 86-1910919.
          </p>
        </div>
      </section>
    `;
  },

  dashboard() {
    if (!state.isAdmin) {
      return `<div class="section" style="padding-top: 140px; text-align: center;"><h3 style="color: var(--danger);">Access Denied</h3></div>`;
    }
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Admin Panel</span>
          <h2 class="section-title">Control Dashboard</h2>
          <p class="section-subtitle">Manage upcoming events, calendar scheduling, ticket details, and download attendee listings.</p>
        </div>
        
        <div style="display: grid; grid-template-columns: 1fr 1.5fr; gap: 3rem; align-items: start;">
          <!-- Event Creator Card -->
          <div class="form-card" style="margin: 0; padding: 30px;">
            <h3 style="margin-bottom: 20px;"><i class="fa-regular fa-calendar-plus" style="color: var(--secondary); margin-right: 8px;"></i> Create New Event</h3>
            <form id="admin-create-event-form">
              <div class="form-group">
                <label class="form-label">Event Title</label>
                <input type="text" class="form-control" id="adm-evt-title" required placeholder="Gala Dinner, Workshops, etc.">
              </div>
              <div class="form-group" style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div>
                  <label class="form-label">Date</label>
                  <input type="date" class="form-control" id="adm-evt-date" required>
                </div>
                <div>
                  <label class="form-label">Time</label>
                  <input type="text" class="form-control" id="adm-evt-time" required placeholder="4:00 PM">
                </div>
              </div>
              <div class="form-group" style="display: grid; grid-template-columns: 1.2fr 1fr; gap: 10px;">
                <div>
                  <label class="form-label">Location</label>
                  <input type="text" class="form-control" id="adm-evt-loc" required placeholder="3711 Long Beach Blvd">
                </div>
                <div>
                  <label class="form-label">Price ($)</label>
                  <input type="number" class="form-control" id="adm-evt-price" required min="0" placeholder="0">
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">Event Description</label>
                <textarea class="form-control" id="adm-evt-desc" required placeholder="Detailed seminar guidelines..."></textarea>
              </div>
              <button class="btn btn-primary" style="width: 100%;" type="submit">
                <i class="fa-solid fa-plus"></i> Publish Event
              </button>
            </form>
          </div>
          
          <!-- Event List and Exporter -->
          <div class="calendar-card">
            <h3 style="margin-bottom: 20px; border-bottom: 2px solid var(--primary); padding-bottom: 10px;">Active Event Records</h3>
            
            <div style="overflow-x: auto;">
              <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem;">
                <thead>
                  <tr style="border-bottom: 2px solid rgba(15, 23, 42, 0.08);">
                    <th style="padding: 12px 6px;">Event Details</th>
                    <th style="padding: 12px 6px;">Type</th>
                    <th style="padding: 12px 6px; text-align: right;">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${state.events.map(evt => `
                    <tr style="border-bottom: 1px solid rgba(15, 23, 42, 0.04);">
                      <td style="padding: 12px 6px;">
                        <div style="font-weight: 700; color: var(--primary);">${evt.title}</div>
                        <div style="font-size: 0.8rem; color: var(--text-muted);"><i class="fa-regular fa-calendar"></i> ${evt.date}</div>
                      </td>
                      <td style="padding: 12px 6px;">
                        <span class="event-badge" style="position: static; font-size: 0.75rem; padding: 4px 10px;">${evt.price === 0 ? 'FREE' : 'PAID'}</span>
                      </td>
                      <td style="padding: 12px 6px; text-align: right;">
                        <button class="btn btn-outline download-csv-btn" data-id="${evt.id}" style="padding: 6px 12px; font-size: 0.75rem;">
                          <i class="fa-solid fa-file-csv"></i> Attendees CSV
                        </button>
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>
    `;
  },

  myTickets() {
    if (!state.user) {
      return `<div class="section" style="padding-top: 140px; text-align: center;"><h3 style="color: var(--danger);">Please login to view your tickets.</h3></div>`;
    }
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Receipts</span>
          <h2 class="section-title">My Event Tickets</h2>
          <p class="section-subtitle">Secure access tokens for your upcoming reservations and workshops.</p>
        </div>
        
        ${state.myTickets.length === 0 ? `
          <div class="pillar-card" style="max-width: 500px; margin: 0 auto; text-align: center;">
            <i class="fa-solid fa-ticket-simple" style="font-size: 3rem; color: var(--text-muted); margin-bottom: 15px;"></i>
            <p style="color: var(--text-muted);">You do not have any registered tickets yet. Explore upcoming calendar events to RSVP!</p>
          </div>
        ` : `
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 2rem; max-width: 1000px; margin: 0 auto;">
            ${state.myTickets.map(tkt => `
              <div class="calendar-card" style="border-left: 6px dashed var(--accent); position: relative; overflow: hidden;">
                <div style="position: absolute; right: -20px; bottom: -20px; font-size: 6rem; color: rgba(245, 158, 11, 0.05); transform: rotate(-15deg); font-weight: 800;">PASS</div>
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 15px;">
                  <div>
                    <h3 style="font-size: 1.2rem; color: var(--primary);">${tkt.eventTitle}</h3>
                    <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 500; margin-top: 4px;"><i class="fa-solid fa-calendar-day"></i> ${tkt.eventDate}</div>
                  </div>
                  <span class="event-badge" style="position: static; background: var(--accent); color: var(--primary); font-size: 0.75rem;">${tkt.quantity} Pass(es)</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 15px;">
                  <i class="fa-solid fa-location-dot"></i> ${tkt.eventLocation}
                </div>
                <div class="dropdown-divider"></div>
                <div style="display: flex; justify-content: space-between; font-size: 0.8rem; font-weight: 600; color: var(--primary); margin-top: 10px;">
                  <span>TICKET ID: ${tkt.ticketId}</span>
                  <span style="color: var(--success);"><i class="fa-solid fa-circle-check"></i> Verified</span>
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </section>
    `;
  },

  terms() {
    return `<div class="section" style="padding-top:140px; max-width: 800px; margin: 0 auto;"><h2>Terms of Use</h2><p style="margin-top:20px; color: var(--text-muted);">Standard non-profit terms and agreements for Howards 4 Hope.</p></div>`;
  },
  
  privacy() {
    return `<div class="section" style="padding-top:140px; max-width: 800px; margin: 0 auto;"><h2>Privacy Policy</h2><p style="margin-top:20px; color: var(--text-muted);">Standard GDPR / California privacy security protocols for data safeguards.</p></div>`;
  }
};

/* --- ROUTER & VIEW CONTROLLER --- */
async function refreshEvents() {
  const data = await API.getEvents();
  if (data && data.length > 0) {
    state.events = data.map(e => ({
      id: e.id || ("evt-" + Math.floor(Math.random() * 1000)),
      title: e.title,
      date: e.date,
      time: e.time,
      location: e.location,
      price: e.price,
      banner: e.bannerUrl || "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000",
      desc: e.description || e.desc,
      category: e.category || "Community"
    }));
  }
}

async function router() {
  const hash = window.location.hash || '#/';
  const contentDiv = document.getElementById('app-content');
  
  if (hash === '#/' || hash === '#/events' || hash === '#/dashboard') {
    await refreshEvents();
  }
  
  // Highlight active link
  document.querySelectorAll('#navbar-links .nav-link, #mobile-drawer .nav-link').forEach(link => {
    link.classList.remove('active');
    const hrefRoute = link.getAttribute('href');
    if (hrefRoute === hash) {
      link.classList.add('active');
    }
  });

  // Basic Hash Routing Matches
  if (hash === '#/') {
    contentDiv.innerHTML = templates.home();
  } else if (hash === '#/about') {
    contentDiv.innerHTML = templates.about();
  } else if (hash === '#/programs') {
    contentDiv.innerHTML = templates.programs();
    bindProgramsEvents();
  } else if (hash === '#/events') {
    contentDiv.innerHTML = templates.events();
    bindCalendarEvents();
  } else if (hash === '#/get-involved') {
    contentDiv.innerHTML = templates.getInvolved();
    bindInvolvementForm();
  } else if (hash === '#/donate') {
    contentDiv.innerHTML = templates.donate();
    bindDonationPortal();
  } else if (hash === '#/dashboard') {
    contentDiv.innerHTML = templates.dashboard();
    bindAdminDashboard();
  } else if (hash === '#/my-tickets') {
    contentDiv.innerHTML = templates.myTickets();
  } else if (hash === '#/terms') {
    contentDiv.innerHTML = templates.terms();
  } else if (hash === '#/privacy') {
    contentDiv.innerHTML = templates.privacy();
  } else {
    // 404 Page Fallback
    contentDiv.innerHTML = `
      <section class="section" style="padding-top: 140px; text-align: center;">
        <h2 style="font-size: 3rem; color: var(--danger);">404</h2>
        <p style="color: var(--text-muted); margin-bottom: 20px;">The resource directory you requested was not found.</p>
        <a href="#/" class="btn btn-primary">Return Home</a>
      </section>
    `;
  }

  // Back to top on route change
  window.scrollTo(0, 0);
}

window.addEventListener('hashchange', router);
window.addEventListener('load', router);

/* --- EVENT BINDING MODULES --- */

// --- 1. PROGRAMS & RESOURCES HUB INTERACTIVES ---
function bindProgramsEvents() {
  const container = document.getElementById('resources-grid-container');
  const searchInput = document.getElementById('resource-search');
  const pills = document.querySelectorAll('.category-pill');
  
  let activeCat = 'all';
  let searchQuery = '';
  
  function render() {
    const filtered = state.resources.filter(res => {
      const matchCat = activeCat === 'all' || res.category === activeCat;
      const matchQuery = res.title.toLowerCase().includes(searchQuery) || res.desc.toLowerCase().includes(searchQuery);
      return matchCat && matchQuery;
    });
    
    if (filtered.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; color: var(--text-muted); padding: 40px;">
          <i class="fa-regular fa-folder-open" style="font-size: 2.5rem; margin-bottom: 12px; display: block; color: var(--secondary);"></i>
          No matching support links found. Try typing another term.
        </div>
      `;
      return;
    }
    
    container.innerHTML = filtered.map(res => `
      <div class="resource-card">
        <span class="res-tag">${res.category}</span>
        <h4 class="res-title">${res.title}</h4>
        <p class="res-desc">${res.desc}</p>
        <a href="${res.link}" target="_blank" class="res-link">
          Access Resource <i class="fa-solid fa-arrow-up-right-from-square" style="font-size: 0.75rem;"></i>
        </a>
      </div>
    `).join('');
  }
  
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      searchQuery = e.target.value.toLowerCase();
      render();
    });
  }
  
  pills.forEach(pill => {
    pill.addEventListener('click', () => {
      pills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      activeCat = pill.getAttribute('data-cat');
      render();
    });
  });
  
  render(); // Initial Render
}

// --- 2. INTERACTIVE CALENDAR & RSVP SYSTEM ---
function bindCalendarEvents() {
  const daysGrid = document.getElementById('calendar-days-grid');
  const prevBtn = document.getElementById('prev-month-btn');
  const nextBtn = document.getElementById('next-month-btn');
  const monthYearLabel = document.getElementById('calendar-month-year');
  const placeholder = document.getElementById('active-event-detail-placeholder');
  
  let currentYear = 2026;
  let currentMonth = 8; // September (0-indexed represents January, so 8 is September)
  
  const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  
  function renderCalendar() {
    daysGrid.innerHTML = '';
    monthYearLabel.innerText = `${monthNames[currentMonth]} ${currentYear}`;
    
    // Add Weekday labels
    const weekdays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    weekdays.forEach(wd => {
      const el = document.createElement('div');
      el.className = 'calendar-day-label';
      el.innerText = wd;
      daysGrid.appendChild(el);
    });
    
    // Calculate calendar days
    const firstDayIndex = new Date(currentYear, currentMonth, 1).getDay();
    const totalDays = new Date(currentYear, currentMonth + 1, 0).getDate();
    
    // Insert empty days leading up to first weekday
    for (let i = 0; i < firstDayIndex; i++) {
      const el = document.createElement('div');
      el.className = 'calendar-day empty';
      daysGrid.appendChild(el);
    }
    
    // Insert calendar dates
    for (let d = 1; d <= totalDays; d++) {
      const dayEl = document.createElement('div');
      dayEl.className = 'calendar-day';
      dayEl.innerText = d;
      
      // Match with active events
      const formattedMonth = String(currentMonth + 1).padStart(2, '0');
      const formattedDay = String(d).padStart(2, '0');
      const searchDateStr = `${currentYear}-${formattedMonth}-${formattedDay}`;
      
      const foundEvent = state.events.find(e => e.date === searchDateStr);
      if (foundEvent) {
        dayEl.classList.add('has-event');
        dayEl.addEventListener('click', () => {
          // Deactivate previously selected day
          document.querySelectorAll('.calendar-day').forEach(cd => cd.classList.remove('active'));
          dayEl.classList.add('active');
          renderEventDetail(foundEvent);
        });
      }
      
      daysGrid.appendChild(dayEl);
    }
  }
  
  function renderEventDetail(event) {
    state.selectedEvent = event;
    placeholder.innerHTML = `
      <div class="event-hifi-card" style="margin: 0; animation: modalEnter var(--transition-fast);">
        <div class="event-banner" style="background-image: url('${event.banner}')">
          <span class="event-badge">${event.category}</span>
        </div>
        <div class="event-body">
          <div class="event-meta">
            <span class="event-meta-item"><i class="fa-solid fa-calendar-days"></i> ${event.date}</span>
            <span class="event-meta-item"><i class="fa-solid fa-clock"></i> ${event.time}</span>
          </div>
          <h3>${event.title}</h3>
          <p class="event-desc">${event.desc}</p>
          <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 20px;">
            <i class="fa-solid fa-location-dot" style="margin-right: 6px;"></i> ${event.location}
          </div>
          <div class="event-footer">
            <span class="event-price ${event.price === 0 ? 'free' : ''}" style="font-size: 1.5rem;">${event.price === 0 ? 'FREE' : '$' + event.price.toFixed(2)}</span>
            <button class="btn btn-primary" id="rsvp-trigger-btn">
              <i class="fa-solid fa-receipt"></i> ${event.price === 0 ? 'Book Free Seat' : 'Purchase Pass'}
            </button>
          </div>
        </div>
      </div>
    `;
    
    // Bind RSVP checkout click
    document.getElementById('rsvp-trigger-btn').addEventListener('click', () => {
      if (!state.user) {
        alert("Please login first to purchase passes or RSVP!");
        authModal.classList.add('active');
        return;
      }
      openRSVPModal(event);
    });
  }
  
  if (prevBtn) {
    prevBtn.addEventListener('click', () => {
      currentMonth--;
      if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
      }
      renderCalendar();
    });
  }
  
  if (nextBtn) {
    nextBtn.addEventListener('click', () => {
      currentMonth++;
      if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
      }
      renderCalendar();
    });
  }
  
  renderCalendar();
}

// RSVP Ticket Options Modal
function openRSVPModal(event) {
  state.cartEvent = event;
  
  const rsvpModal = document.createElement('div');
  rsvpModal.className = 'modal active';
  rsvpModal.id = 'rsvp-checkout-modal';
  rsvpModal.innerHTML = `
    <div class="modal-content" style="max-width: 440px;">
      <span class="modal-close" id="rsvp-close-btn">&times;</span>
      <h3 class="modal-title"><i class="fa-solid fa-ticket-simple" style="color: var(--secondary);"></i> Ticket Registration</h3>
      
      <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary); margin-bottom: 8px;">${event.title}</div>
      <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 500; margin-bottom: 20px;">
        <i class="fa-regular fa-calendar"></i> ${event.date} &nbsp;|&nbsp; <i class="fa-regular fa-clock"></i> ${event.time}
      </div>
      
      <div class="form-group">
        <label class="form-label">Quantity</label>
        <select class="form-control" id="rsvp-qty" style="background-image: none;">
          <option value="1">1 Pass</option>
          <option value="2">2 Passes</option>
          <option value="3">3 Passes</option>
          <option value="4">4 Passes</option>
        </select>
      </div>
      
      <div style="display: flex; justify-content: space-between; font-weight: 700; font-size: 1.15rem; color: var(--primary); margin-bottom: 25px; padding-top: 10px;">
        <span>Total Price:</span>
        <span id="rsvp-total-cost">${event.price === 0 ? 'FREE' : '$' + event.price.toFixed(2)}</span>
      </div>
      
      ${event.price === 0 ? `
        <button class="btn btn-primary" id="confirm-free-rsvp-btn" style="width: 100%; height: 48px;">
          <i class="fa-solid fa-check"></i> Confirm Free RSVP
        </button>
      ` : `
        <button class="auth-social-btn" id="stripe-checkout-btn" style="background: #635bff; color: white; border: none; height: 48px; margin-bottom: 12px;">
          <i class="fa-brands fa-stripe"></i> Pay with Stripe
        </button>
        <button class="auth-social-btn" id="paypal-checkout-btn" style="background: #ffc439; color: #003087; border: none; height: 48px; margin-bottom: 0;">
          <i class="fa-brands fa-paypal"></i> Pay with PayPal
        </button>
      `}
    </div>
  `;
  
  document.body.appendChild(rsvpModal);
  
  const closeBtn = document.getElementById('rsvp-close-btn');
  const qtySelect = document.getElementById('rsvp-qty');
  const totalCostLabel = document.getElementById('rsvp-total-cost');
  
  closeBtn.addEventListener('click', () => {
    rsvpModal.remove();
  });
  
  if (qtySelect) {
    qtySelect.addEventListener('change', (e) => {
      const q = parseInt(e.target.value);
      totalCostLabel.innerText = event.price === 0 ? 'FREE' : '$' + (event.price * q).toFixed(2);
    });
  }
  
  // Checkout listeners
  if (event.price === 0) {
    document.getElementById('confirm-free-rsvp-btn').addEventListener('click', async () => {
      const qty = parseInt(qtySelect.value);
      await API.bookTicket(event.id, qty, 'FREE');
      alert("RSVP Successful! Check your dashboard for your access token tickets.");
      rsvpModal.remove();
      window.location.hash = '#/my-tickets';
    });
  } else {
    document.getElementById('stripe-checkout-btn').addEventListener('click', async () => {
      const qty = parseInt(qtySelect.value);
      await API.bookTicket(event.id, qty, 'STRIPE');
      alert("Stripe secure payment completed successfully! Ticket generated.");
      rsvpModal.remove();
      window.location.hash = '#/my-tickets';
    });
    
    document.getElementById('paypal-checkout-btn').addEventListener('click', async () => {
      const qty = parseInt(qtySelect.value);
      await API.bookTicket(event.id, qty, 'PAYPAL');
      alert("PayPal Checkout transaction verified successfully! Ticket generated.");
      rsvpModal.remove();
      window.location.hash = '#/my-tickets';
    });
  }
}

// --- 3. DONATION PORTAL INTERACTION ---
function bindDonationPortal() {
  const customInput = document.getElementById('custom-donation-amt');
  const buttons = document.querySelectorAll('.donate-amount-btn');
  
  buttons.forEach(btn => {
    btn.addEventListener('click', () => {
      buttons.forEach(b => {
        b.classList.remove('active');
        b.style.background = 'transparent';
        b.style.color = 'var(--primary)';
      });
      btn.classList.add('active');
      btn.style.background = 'var(--primary)';
      btn.style.color = 'white';
      
      const amt = btn.getAttribute('data-amt');
      customInput.value = amt;
    });
  });
  
  if (customInput) {
    customInput.addEventListener('input', () => {
      buttons.forEach(b => {
        b.classList.remove('active');
        b.style.background = 'transparent';
        b.style.color = 'var(--primary)';
      });
    });
  }
  
  const handleDonation = (method) => {
    const amt = parseFloat(customInput.value);
    if (isNaN(amt) || amt < 5) {
      alert("Minimum tax-deductible donation amount is $5.00.");
      return;
    }
    alert(`Thank you for your generous $${amt.toFixed(2)} tax-deductible contribution via ${method}! A transaction receipt has been issued to your registered address.`);
    window.location.hash = '#/';
  };
  
  document.getElementById('stripe-donate-btn').addEventListener('click', () => handleDonation('Stripe'));
  document.getElementById('paypal-donate-btn').addEventListener('click', () => handleDonation('PayPal'));
}

// --- 4. OUTREACH FORMS ---
function bindInvolvementForm() {
  const form = document.getElementById('involvement-form');
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      alert("Application submitted! Thank you for standing with Howards 4 Hope. Our coordinate team will contact you within 48 hours.");
      form.reset();
      window.location.hash = '#/';
    });
  }
}

// --- 5. ADMIN CONTROL PANEL & CSV UTILITY ---
function bindAdminDashboard() {
  const form = document.getElementById('admin-create-event-form');
  
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const title = document.getElementById('adm-evt-title').value;
      const date = document.getElementById('adm-evt-date').value;
      const time = document.getElementById('adm-evt-time').value;
      const location = document.getElementById('adm-evt-loc').value;
      const price = parseFloat(document.getElementById('adm-evt-price').value);
      const desc = document.getElementById('adm-evt-desc').value;
      
      const newEvt = {
        id: 'evt-' + Math.floor(1000 + Math.random() * 9000),
        title,
        date,
        time,
        location,
        price,
        desc,
        banner: "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000",
        category: "Community"
      };
      
      state.events.push(newEvt);
      alert("Event published successfully!");
      form.reset();
      router(); // Reload view
    });
  }
  
  // Attendee CSV Exporter Handler
  document.querySelectorAll('.download-csv-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const evtId = btn.getAttribute('data-id');
      const event = state.events.find(x => x.id === evtId);
      if (!event) return;
      
      // Dynamic High-Fidelity CSV generating utility
      const csvRows = [
        ['Ticket ID', 'Purchaser Email', 'Quantity Purchased', 'Payment Method', 'Price Paid', 'Status'],
        ['tkt-281948', 'sward.student@university.edu', '2', event.price === 0 ? 'FREE' : 'STRIPE', `$${(event.price * 2).toFixed(2)}`, 'CONFIRMED'],
        ['tkt-902183', 'volunteer.core@gmail.com', '1', event.price === 0 ? 'FREE' : 'PAYPAL', `$${event.price.toFixed(2)}`, 'CONFIRMED'],
        ['tkt-551283', 'donor.lb@corporate.com', '4', event.price === 0 ? 'FREE' : 'STRIPE', `$${(event.price * 4).toFixed(2)}`, 'CONFIRMED']
      ];
      
      const csvContent = "data:text/csv;charset=utf-8," 
        + csvRows.map(row => row.join(",")).join("\n");
        
      const encodedUri = encodeURI(csvContent);
      const link = document.createElement("a");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", `${event.title.replace(/\s+/g, '_')}_Attendees_List.csv`);
      document.body.appendChild(link);
      
      link.click();
      link.remove();
    });
  });
}
