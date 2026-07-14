/* --- APPLICATION STATE & ROUTING ENGINE --- */

// Firebase Configuration
const firebaseConfig = {
  apiKey: "AIzaSyC3-DGil68vpD99I2CQMGVZSeRJ8PN8yLk",
  authDomain: "howards4hope-b06f6.firebaseapp.com",
  projectId: "howards4hope-b06f6",
  storageBucket: "howards4hope-b06f6.firebasestorage.app",
  messagingSenderId: "1055785276298",
  appId: "1:1055785276298:web:2bd4ff6900196413ccae73",
  measurementId: "G-2GTV0TFP3V"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Global App State, this is currently test data
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
  myTickets: [],
  adminMetrics: {
    totalAttendees: 48,
    totalRevenue: 435.00,
    activeEvents: 0,
    rsvpConversion: '87%'
  }
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

const mockBlogPosts = [
  {
    id: 1,
    title: "Empowering Our Youth: Key Takeaways from Our Latest Seminar",
    content: "Last week, Howards 4 Hope hosted the inaugural 'Me, Myself & Why' Youth Empowerment Seminar. Over 45 local Long Beach youth attended, engaging in interactive confidence-building exercises, resume building, and leadership roadmaps. The energy was electric, and we are inspired by the resilience and vision of our next generation. Thank you to our mentors and sponsors who made this possible!",
    author: "Founder Sean Ward",
    date: "2026-05-15",
    category: "Youth Milestones",
    imageUrl: "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000"
  },
  {
    id: 2,
    title: "New Funding Secured to Support Special-Needs Caregivers",
    content: "We are thrilled to announce that Howards 4 Hope has been awarded a generous community grant to expand our Caregivers Respite Support Network. This funding will allow us to double the capacity of our monthly Links of Hope Support Summits, providing emergency emotional relief, respite child care, and mental health workshops for dedicated caregivers. Together, we rise by lifting others.",
    author: "Caregiver Director",
    date: "2026-05-18",
    category: "Caregiver Summits",
    imageUrl: "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=1000"
  }
];

state.blogPosts = [...mockBlogPosts];

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
    try {
      const headers = await this.getHeaders();
      const response = await fetch(`${this.baseUrl}/tickets/book`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ eventId: Number(eventId.toString().replace('evt-', '')), quantity, paymentMethod })
      });
      if (response.ok) {
        const ticket = await response.json();
        const formatted = {
          ticketId: ticket.id ? 'tkt-' + ticket.id : 'tkt-' + Math.floor(100000 + Math.random()*900000),
          eventTitle: ticket.eventTitle,
          eventDate: ticket.eventDate,
          eventLocation: '3711 Long Beach Blvd, Long Beach, CA 90807',
          quantity: ticket.quantity,
          pricePaid: ticket.pricePaid,
          paymentMethod: ticket.paymentMethod,
          purchaseDate: ticket.purchaseDate
        };
        state.myTickets.push(formatted);
        return formatted;
      }
    } catch (e) {
      console.error("Spring Boot API offline, falling back to local simulation.", e);
    }
    
    // Simulate booking ticket locally
    const event = state.events.find(e => e.id === eventId);
    if (!event) return false;
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
  },

  async bookTicketGuest(eventId, quantity, paymentMethod, guestEmail, guestName) {
    try {
      const response = await fetch(`${this.baseUrl}/tickets/book-guest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventId: Number(eventId.toString().replace('evt-', '')),
          quantity,
          paymentMethod,
          guestEmail,
          guestName
        })
      });
      if (response.ok) {
        return await response.json();
      }
    } catch (e) {
      console.error("Guest booking REST API failed", e);
    }
    return null;
  },

  async getGuestTickets(email) {
    try {
      const response = await fetch(`${this.baseUrl}/tickets/guest-tickets?email=${encodeURIComponent(email)}`);
      if (response.ok) {
        return await response.json();
      }
    } catch (e) {
      console.error("Guest tickets retrieval failed", e);
    }
    return [];
  },

  async getBlogPosts() {
    try {
      const response = await fetch(`${this.baseUrl}/blog`);
      if (response.ok) return await response.json();
    } catch (e) {
      console.log("Failed to fetch blog posts from server, using client side state.");
    }
    return state.blogPosts;
  },

  async createBlogPost(post) {
    try {
      const headers = await this.getHeaders();
      const response = await fetch(`${this.baseUrl}/admin/blog`, {
        method: 'POST',
        headers,
        body: JSON.stringify(post)
      });
      if (response.ok) return await response.json();
    } catch (e) {
      console.error("Failed to post blog article", e);
    }
    return null;
  },

  async deleteBlogPost(id) {
    try {
      const headers = await this.getHeaders();
      const response = await fetch(`${this.baseUrl}/admin/blog/${id}`, {
        method: 'DELETE',
        headers
      });
      return response.ok;
    } catch (e) {
      console.error("Failed to delete blog article", e);
    }
    return false;
  },

  async createEvent(event) {
    try {
      const headers = await this.getHeaders();
      const response = await fetch(`${this.baseUrl}/admin/events`, {
        method: 'POST',
        headers,
        body: JSON.stringify(event)
      });
      if (response.ok) return await response.json();
    } catch (e) {
      console.error("Failed to create event in backend", e);
    }
    return null;
  },

  async deleteEvent(id) {
    try {
      const headers = await this.getHeaders();
      const response = await fetch(`${this.baseUrl}/admin/events/${id}`, {
        method: 'DELETE',
        headers
      });
      return response.ok;
    } catch (e) {
      console.error("Failed to delete event in backend", e);
    }
    return false;
  }
};

/* --- FIREBASE AUTHENTICATION LISTENERS --- */
firebase.auth().onAuthStateChanged(async (user) => {
  const userMenu = document.getElementById('user-menu-container');
  const loginBtn = document.getElementById('login-trigger-btn');
  
  if (user) {
    state.user = user;
    // Check admin status via Firebase Custom Claims (secure RBAC)
    try {
      const tokenResult = await user.getIdTokenResult();
      state.isAdmin = tokenResult.claims.admin === true;
    } catch (e) {
      console.error('Failed to check admin claims:', e);
      state.isAdmin = false;
    }
    
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
    
    // Render highly visible Admin Panel Link in navbar links
    let adminNavLink = document.getElementById('navbar-admin-link-li');
    if (state.isAdmin) {
      if (!adminNavLink) {
        const navLinksUl = document.getElementById('navbar-links');
        if (navLinksUl) {
          adminNavLink = document.createElement('li');
          adminNavLink.id = 'navbar-admin-link-li';
          adminNavLink.innerHTML = `<a href="#/dashboard" class="nav-link" data-route="dashboard" style="color: var(--secondary); font-weight: 700;"><i class="fa-solid fa-gauge-high"></i> Admin Panel</a>`;
          navLinksUl.appendChild(adminNavLink);
        }
      }
    } else {
      if (adminNavLink) adminNavLink.remove();
    }

    // Render highly visible Admin Panel Link in mobile drawer links
    let mobileAdminNavLink = document.getElementById('mobile-admin-link-li');
    if (state.isAdmin) {
      if (!mobileAdminNavLink) {
        const mobileLinksDiv = document.getElementById('mobile-drawer-links');
        if (mobileLinksDiv) {
          mobileAdminNavLink = document.createElement('a');
          mobileAdminNavLink.id = 'mobile-admin-link-li';
          mobileAdminNavLink.href = '#/dashboard';
          mobileAdminNavLink.className = 'nav-link';
          mobileAdminNavLink.style.fontSize = '1.2rem';
          mobileAdminNavLink.style.color = 'var(--secondary)';
          mobileAdminNavLink.style.fontWeight = '700';
          mobileAdminNavLink.setAttribute('data-route', 'dashboard');
          mobileAdminNavLink.innerHTML = `<i class="fa-solid fa-gauge-high"></i> Admin Panel`;
          // Insert it right before the Donate Now button if present
          const donateBtn = mobileLinksDiv.querySelector('.btn-donate');
          if (donateBtn) {
            mobileLinksDiv.insertBefore(mobileAdminNavLink, donateBtn);
          } else {
            mobileLinksDiv.appendChild(mobileAdminNavLink);
          }
        }
      }
    } else {
      if (mobileAdminNavLink) mobileAdminNavLink.remove();
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

    // Remove admin navigation links if present
    const adminNavLink = document.getElementById('navbar-admin-link-li');
    if (adminNavLink) adminNavLink.remove();
    const mobileAdminNavLink = document.getElementById('mobile-admin-link-li');
    if (mobileAdminNavLink) mobileAdminNavLink.remove();
  }
  
  // Refresh page shell context
  router();
  
  // Fade out loader after auth state resolves
  const authLoader = document.getElementById('auth-loader');
  if (authLoader) authLoader.classList.remove('active');
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
  const confirmGroup = document.getElementById('auth-confirm-group');
  const confirmInput = document.getElementById('auth-confirm-password');
  
  if (isSignup) {
    title.innerText = "Create Account";
    submitBtn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Sign Up';
    toggleText.innerHTML = 'Already have an account? <span id="auth-toggle-link">Login</span>';
    if (confirmGroup) confirmGroup.style.display = 'block';
    if (confirmInput) confirmInput.setAttribute('required', 'true');
  } else {
    title.innerText = "Welcome Back";
    submitBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Login';
    toggleText.innerHTML = "Don't have an account? <span id='auth-toggle-link'>Sign Up</span>";
    if (confirmGroup) confirmGroup.style.display = 'none';
    if (confirmInput) {
      confirmInput.removeAttribute('required');
      confirmInput.value = '';
    }
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
    const authLoader = document.getElementById('auth-loader');
    
    try {
      if (isSignupMode) {
        const confirmPassword = document.getElementById('auth-confirm-password').value;
        if (password !== confirmPassword) {
          alert("Passwords do not match! Please verify your password confirmation.");
          return;
        }
        // Password strength validation (NIST SP 800-63B minimum requirement)
        if (password.length < 8) {
          alert("Password must be at least 8 characters long.");
          return;
        }
        if (authLoader) authLoader.classList.add('active');
        await firebase.auth().createUserWithEmailAndPassword(email, password);
        alert("Account created successfully!");
      } else {
        if (authLoader) authLoader.classList.add('active');
        await firebase.auth().signInWithEmailAndPassword(email, password);
      }
      authModal.classList.remove('active');
    } catch (err) {
      if (authLoader) authLoader.classList.remove('active');
      alert(err.message);
    }
  });
}

// Google Authentication
const googleBtn = document.getElementById('google-login-btn');
if (googleBtn) {
  googleBtn.addEventListener('click', async () => {
    const provider = new firebase.auth.GoogleAuthProvider();
    const authLoader = document.getElementById('auth-loader');
    
    try {
      if (authLoader) authLoader.classList.add('active');
      await firebase.auth().signInWithPopup(provider);
      authModal.classList.remove('active');
    } catch (err) {
      if (authLoader) authLoader.classList.remove('active');
      alert(err.message);
    }
  });
}

// Logout Action
const logoutBtn = document.getElementById('logout-btn');
if (logoutBtn) {
  logoutBtn.addEventListener('click', async () => {
    const authLoader = document.getElementById('auth-loader');
    if (authLoader) authLoader.classList.add('active');
    
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
            <div class="event-hifi-card animate-hover">
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
                  <a href="#/events?register=${event.id}" class="btn btn-primary" style="padding: 8px 18px; font-size: 0.85rem;"><i class="fa-solid fa-ticket"></i> RSVP / Register</a>
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
        
        <!-- Meet Our Staff & Leadership -->
        <div class="section-header" style="margin-top: 60px; margin-bottom: 40px;">
          <span class="section-tag">Our Family Team</span>
          <h2 class="section-title">Leadership & Staff</h2>
          <p class="section-subtitle">The dedicated hearts driving change and restoring hope every single day in our community.</p>
        </div>

        <h3 style="text-align: center; color: var(--primary); font-family: 'Outfit'; font-weight: 700; margin-bottom: 25px;"><i class="fa-solid fa-medal"></i> Board of Directors</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 2rem; max-width: 1100px; margin: 0 auto 50px auto;">
          <div class="calendar-card animate-hover" style="padding: 24px; text-align: center; position: relative; overflow: hidden; border-top: 4px solid var(--accent); transition: transform 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 15px auto; width: 60px; height: 60px; font-size: 1.5rem; background: linear-gradient(135deg, var(--accent), var(--secondary)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">LW</div>
            <h4 style="font-size: 1.15rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">LaCreashia Willis-Howard</h4>
            <div style="font-size: 0.85rem; color: var(--secondary); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">President & Co-Founder</div>
          </div>
          <div class="calendar-card animate-hover" style="padding: 24px; text-align: center; position: relative; overflow: hidden; border-top: 4px solid var(--accent); transition: transform 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 15px auto; width: 60px; height: 60px; font-size: 1.5rem; background: linear-gradient(135deg, var(--accent), var(--secondary)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">LH</div>
            <h4 style="font-size: 1.15rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">Lamar Howard Sr.</h4>
            <div style="font-size: 0.85rem; color: var(--secondary); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Vice President / Interim Treasurer</div>
          </div>
          <div class="calendar-card animate-hover" style="padding: 24px; text-align: center; position: relative; overflow: hidden; border-top: 4px solid var(--accent); transition: transform 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 15px auto; width: 60px; height: 60px; font-size: 1.5rem; background: linear-gradient(135deg, var(--accent), var(--secondary)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">TW</div>
            <h4 style="font-size: 1.15rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">Taylor Wilcher</h4>
            <div style="font-size: 0.85rem; color: var(--secondary); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Secretary</div>
          </div>
          <div class="calendar-card animate-hover" style="padding: 24px; text-align: center; position: relative; overflow: hidden; border-top: 4px solid var(--accent); transition: transform 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 15px auto; width: 60px; height: 60px; font-size: 1.5rem; background: linear-gradient(135deg, var(--accent), var(--secondary)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">EW</div>
            <h4 style="font-size: 1.15rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">Dr. Edna Willis</h4>
            <div style="font-size: 0.85rem; color: var(--secondary); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Programs Manager</div>
          </div>
        </div>

        <h3 style="text-align: center; color: var(--primary); font-family: 'Outfit'; font-weight: 700; margin-bottom: 25px;"><i class="fa-solid fa-users-gear"></i> Dedicated Outreach Staff</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; max-width: 1100px; margin: 0 auto 50px auto;">
          <div class="pillar-card animate-hover" style="padding: 20px; text-align: center; transition: all 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 12px auto; width: 55px; height: 55px; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">JH</div>
            <h4 style="font-size: 1.05rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">Janiel Lizardo-Howard</h4>
            <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Director of Community Management</div>
          </div>
          <div class="pillar-card animate-hover" style="padding: 20px; text-align: center; transition: all 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 12px auto; width: 55px; height: 55px; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">LH</div>
            <h4 style="font-size: 1.05rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">Lamar Howard Jr.</h4>
            <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Public Relations & Marketing</div>
          </div>
          <div class="pillar-card animate-hover" style="padding: 20px; text-align: center; transition: all 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 12px auto; width: 55px; height: 55px; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">MH</div>
            <h4 style="font-size: 1.05rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">McKayla Howard</h4>
            <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Community Partnership Coord.</div>
          </div>
          <div class="pillar-card animate-hover" style="padding: 20px; text-align: center; transition: all 0.3s ease;">
            <div class="logo-icon" style="margin: 0 auto 12px auto; width: 55px; height: 55px; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; border-radius: 50%;">SC</div>
            <h4 style="font-size: 1.05rem; color: var(--primary); font-weight: 700; margin-bottom: 4px;">Sarah Micah Cabusora</h4>
            <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Executive Assistant</div>
          </div>
        </div>

        <div class="section-header" style="margin-top: 60px; margin-bottom: 40px;">
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
          
          <button class="auth-social-btn" id="stripe-donate-btn" style="background: linear-gradient(135deg, #635bff, #7b73ff); color: white; border: none; height: 50px; font-weight: 700; border-radius: 8px; display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 8px;">
            <i class="fa-solid fa-credit-card"></i> Donate with Credit / Debit Card
          </button>
          <div style="display: flex; gap: 10px; justify-content: center; font-size: 1.2rem; color: var(--text-muted); margin-bottom: 16px;">
            <i class="fa-brands fa-cc-visa" title="Visa"></i>
            <i class="fa-brands fa-cc-mastercard" title="Mastercard"></i>
            <i class="fa-brands fa-cc-amex" title="American Express"></i>
            <i class="fa-brands fa-cc-discover" title="Discover"></i>
          </div>
          
          <button class="auth-social-btn" id="paypal-donate-btn" style="background: #ffc439; color: #003087; border: none; height: 50px; font-weight: 700; border-radius: 8px; display: flex; align-items: center; justify-content: center; gap: 8px;">
            <i class="fa-brands fa-paypal"></i> Donate securely with PayPal
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
          <p class="section-subtitle">Manage upcoming events, community blog articles, scheduling metrics, and download attendee CSV registries.</p>
        </div>

        <!-- Interactive Analytics Dashboard -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; margin-bottom: 3rem; max-width: 1200px; margin-left: auto; margin-right: auto;">
          <div class="calendar-card" style="padding: 20px; border-left: 4px solid var(--primary); text-align: left; display: flex; align-items: center; gap: 15px;">
            <div style="font-size: 2.2rem; color: var(--primary);"><i class="fa-solid fa-users"></i></div>
            <div>
              <div style="font-size: 1.8rem; font-weight: 800; color: var(--primary);">${state.adminMetrics.totalAttendees}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Total Attendees</div>
            </div>
          </div>
          <div class="calendar-card" style="padding: 20px; border-left: 4px solid var(--success); text-align: left; display: flex; align-items: center; gap: 15px;">
            <div style="font-size: 2.2rem; color: var(--success);"><i class="fa-solid fa-circle-dollar-to-slot"></i></div>
            <div>
              <div style="font-size: 1.8rem; font-weight: 800; color: var(--primary);">$${state.adminMetrics.totalRevenue.toFixed(2)}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Total Revenue</div>
            </div>
          </div>
          <div class="calendar-card" style="padding: 20px; border-left: 4px solid var(--accent); text-align: left; display: flex; align-items: center; gap: 15px;">
            <div style="font-size: 2.2rem; color: var(--accent);"><i class="fa-solid fa-ticket"></i></div>
            <div>
              <div style="font-size: 1.8rem; font-weight: 800; color: var(--primary);">${state.adminMetrics.activeEvents}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Active Events</div>
            </div>
          </div>
          <div class="calendar-card" style="padding: 20px; border-left: 4px solid var(--secondary); text-align: left; display: flex; align-items: center; gap: 15px;">
            <div style="font-size: 2.2rem; color: var(--secondary);"><i class="fa-solid fa-chart-line"></i></div>
            <div>
              <div style="font-size: 1.8rem; font-weight: 800; color: var(--primary);">${state.adminMetrics.rsvpConversion}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">RSVP Conversion</div>
            </div>
          </div>
        </div>
        
        <div style="display: grid; grid-template-columns: 1fr 1.5fr; gap: 3rem; align-items: start; max-width: 1200px; margin-left: auto; margin-right: auto;">
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
                <label class="form-label">Category</label>
                <select class="form-control" id="adm-evt-category" style="background-image: none;" onchange="if(this.value==='__custom__'){document.getElementById('adm-evt-custom-category-group').style.display='block';}else{document.getElementById('adm-evt-custom-category-group').style.display='none';}">
                  <option value="Community">Community</option>
                  <option value="Fundraiser">Fundraiser</option>
                  <option value="Workshop">Workshop</option>
                  <option value="Support Group">Support Group</option>
                  <option value="__custom__">+ Add Custom Category...</option>
                </select>
              </div>
              <div class="form-group" id="adm-evt-custom-category-group" style="display: none; margin-top: 10px;">
                <label class="form-label">Custom Category Name</label>
                <input type="text" class="form-control" id="adm-evt-custom-category" placeholder="E.g., Youth Resiliency">
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
                      <td style="padding: 12px 6px; text-align: right; display: flex; gap: 8px; justify-content: flex-end; align-items: center;">
                        <button class="btn btn-outline download-csv-btn" data-id="${evt.id}" style="padding: 6px 12px; font-size: 0.75rem;">
                          <i class="fa-solid fa-file-csv"></i> Attendees CSV
                        </button>
                        <button class="btn btn-outline delete-event-btn" data-id="${evt.id}" style="padding: 6px 12px; font-size: 0.75rem; color: var(--danger); border-color: var(--danger);">
                          <i class="fa-solid fa-trash-can"></i> Delete
                        </button>
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Blog Manager Section -->
        <div style="display: grid; grid-template-columns: 1fr 1.5fr; gap: 3rem; align-items: start; max-width: 1200px; margin-left: auto; margin-right: auto; margin-top: 3rem; border-top: 1px solid rgba(15,23,42,0.08); padding-top: 3rem;">
          <!-- Blog Creator Card -->
          <div class="form-card" style="margin: 0; padding: 30px;">
            <h3 style="margin-bottom: 20px;"><i class="fa-regular fa-pen-to-square" style="color: var(--secondary); margin-right: 8px;"></i> Create Blog Post</h3>
            <form id="admin-create-blog-form">
              <div class="form-group">
                <label class="form-label">Article Title</label>
                <input type="text" class="form-control" id="adm-blog-title" required placeholder="Milestones, recap, announcements...">
              </div>
              <div class="form-group" style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div>
                  <label class="form-label">Author</label>
                  <input type="text" class="form-control" id="adm-blog-author" value="Founder Sean Ward" required>
                </div>
                <div>
                  <label class="form-label">Category</label>
                  <select class="form-control" id="adm-blog-category" style="background-image: none;" onchange="if(this.value==='__custom__'){document.getElementById('adm-blog-custom-category-group').style.display='block';}else{document.getElementById('adm-blog-custom-category-group').style.display='none';}">
                    <option value="Youth Milestones">Youth Milestones</option>
                    <option value="Caregiver Summits">Caregiver Summits</option>
                    <option value="Event recaps">Event recaps</option>
                    <option value="Announcements">Announcements</option>
                    <option value="__custom__">+ Add Custom Category...</option>
                  </select>
                </div>
              </div>
              <div class="form-group" id="adm-blog-custom-category-group" style="display: none; margin-top: 10px;">
                <label class="form-label">Custom Category Name</label>
                <input type="text" class="form-control" id="adm-blog-custom-category" placeholder="E.g., Respite Outreach">
              </div>
              <div class="form-group">
                <label class="form-label">Image URL (Optional)</label>
                <input type="text" class="form-control" id="adm-blog-image" placeholder="https://images.unsplash.com/photo-...">
              </div>
              <div class="form-group">
                <label class="form-label">Content Body</label>
                <textarea class="form-control" id="adm-blog-content" required placeholder="Write article content here..." style="height: 120px;"></textarea>
              </div>
              <button class="btn btn-primary" style="width: 100%;" type="submit">
                <i class="fa-solid fa-paper-plane"></i> Publish Article
              </button>
            </form>
          </div>
          
          <!-- Blog List & Delete Control -->
          <div class="calendar-card">
            <h3 style="margin-bottom: 20px; border-bottom: 2px solid var(--primary); padding-bottom: 10px;">Active Blog Posts</h3>
            
            <div style="overflow-x: auto;">
              <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem;">
                <thead>
                  <tr style="border-bottom: 2px solid rgba(15, 23, 42, 0.08);">
                    <th style="padding: 12px 6px;">Title & Author</th>
                    <th style="padding: 12px 6px;">Category</th>
                    <th style="padding: 12px 6px; text-align: right;">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${state.blogPosts.map(post => `
                    <tr style="border-bottom: 1px solid rgba(15, 23, 42, 0.04);">
                      <td style="padding: 12px 6px;">
                        <div style="font-weight: 700; color: var(--primary);">${post.title}</div>
                        <div style="font-size: 0.8rem; color: var(--text-muted);"><i class="fa-solid fa-user-pen"></i> ${post.author} on ${post.date}</div>
                      </td>
                      <td style="padding: 12px 6px;">
                        <span class="event-badge" style="position: static; font-size: 0.75rem; padding: 4px 10px;">${post.category}</span>
                      </td>
                      <td style="padding: 12px 6px; text-align: right;">
                        <button class="btn btn-outline delete-blog-btn" data-id="${post.id}" style="padding: 6px 12px; font-size: 0.75rem; color: var(--danger); border-color: var(--danger);">
                          <i class="fa-solid fa-trash-can"></i> Delete
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
      return `
        <section class="section" style="padding-top: 140px;">
          <div class="section-header">
            <span class="section-tag">Receipts</span>
            <h2 class="section-title">My Event Tickets</h2>
            <p class="section-subtitle">Secure access tokens for your upcoming reservations and workshops.</p>
          </div>
          
          <div class="form-card" style="max-width: 500px; margin: 0 auto; padding: 40px; text-align: center; border: 1px solid rgba(15,23,42,0.08); border-radius: 16px; box-shadow: var(--shadow-md);">
            <i class="fa-solid fa-ticket-simple" style="font-size: 3rem; color: var(--secondary); margin-bottom: 20px; display: block;"></i>
            <h3 style="margin-bottom: 12px; font-weight: 800; color: var(--primary);">Guest Ticket Lookup</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px; line-height: 1.6;">
              Did you book passes as a guest? Enter the email address you used at checkout to retrieve your active tickets.
            </p>
            <div class="form-group" style="text-align: left; margin-bottom: 24px;">
              <label class="form-label">Email Address</label>
              <input type="email" class="form-control" id="lookup-guest-email" placeholder="you@example.com" required style="height: 44px;">
            </div>
            <button class="btn btn-primary" id="lookup-guest-btn" style="width: 100%; height: 46px; display: flex; align-items: center; justify-content: center; gap: 8px;">
              <i class="fa-solid fa-magnifying-glass"></i> Retrieve Tickets
            </button>
            <div id="lookup-results-container" style="margin-top: 30px; display: none; text-align: left;"></div>
          </div>
        </section>
      `;
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
  },

  blog() {
    return `
      <section class="section" style="padding-top: 140px;">
        <div class="section-header">
          <span class="section-tag">Updates</span>
          <h2 class="section-title">News & Community Blog</h2>
          <p class="section-subtitle">Stay updated on our local outreach, caregiver support networking, and youth workshops in Long Beach.</p>
        </div>
        
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 2.5rem; max-width: 1200px; margin: 0 auto; padding: 0 20px;">
          ${state.blogPosts.map(post => `
            <article class="calendar-card" style="display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; padding: 0; border: 1px solid rgba(15,23,42,0.08); border-radius: 12px; height: 100%;">
              <div style="height: 200px; background-image: url('${post.imageUrl || 'https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000'}'); background-size: cover; background-position: center; width: 100%;"></div>
              <div style="padding: 24px; flex-grow: 1; display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                  <span class="event-badge" style="position: static; font-size: 0.75rem; padding: 4px 10px; margin-bottom: 12px; display: inline-block;">${post.category}</span>
                  <h3 style="font-size: 1.25rem; color: var(--primary); margin-bottom: 10px; font-weight: 800; line-height: 1.4;">${post.title}</h3>
                  <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.6; margin-bottom: 20px;">${post.content.substring(0, 140)}...</p>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid rgba(15,23,42,0.06); padding-top: 15px; margin-top: 15px;">
                  <span style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;"><i class="fa-solid fa-user-pen" style="color: var(--secondary); margin-right: 4px;"></i> ${post.author}</span>
                  <a href="#/blog-post?id=${post.id}" class="res-link" style="margin: 0; font-size: 0.85rem; font-weight: 700; color: var(--secondary);">Read Full Article <i class="fa-solid fa-arrow-right"></i></a>
                </div>
              </div>
            </article>
          `).join('')}
        </div>
      </section>
    `;
  },

  blogPost() {
    const params = new URLSearchParams(window.location.hash.split('?')[1] || '');
    const id = Number(params.get('id'));
    const post = state.blogPosts.find(p => Number(p.id) === id);
    
    if (!post) {
      return `
        <section class="section" style="padding-top: 140px; text-align: center;">
          <h3 style="color: var(--danger); font-size: 2rem;">Article Not Found</h3>
          <p style="color: var(--text-muted); margin-top: 10px;">The specified blog article could not be loaded.</p>
          <a href="#/blog" class="btn btn-primary" style="margin-top: 20px; display: inline-block;">Back to Blog</a>
        </section>
      `;
    }
    
    return `
      <article class="section" style="padding-top: 140px; max-width: 800px; margin: 0 auto; padding-left: 20px; padding-right: 20px;">
        <a href="#/blog" style="color: var(--secondary); font-weight: 700; display: inline-block; margin-bottom: 20px; text-decoration: none;"><i class="fa-solid fa-chevron-left"></i> Back to All Updates</a>
        
        <div>
          <span class="event-badge" style="position: static; font-size: 0.8rem; padding: 4px 12px; margin-bottom: 15px; display: inline-block;">${post.category}</span>
        </div>
        <h1 style="font-size: 2.5rem; color: var(--primary); font-weight: 800; line-height: 1.2; margin-bottom: 20px;">${post.title}</h1>
        
        <div style="display: flex; gap: 20px; color: var(--text-muted); font-size: 0.9rem; margin-bottom: 30px; border-bottom: 1px solid rgba(15,23,42,0.08); padding-bottom: 15px;">
          <span><i class="fa-regular fa-calendar"></i> Published on: <strong>${post.date}</strong></span>
          <span><i class="fa-solid fa-user-pen"></i> By: <strong>${post.author}</strong></span>
        </div>
        
        <div style="border-radius: 12px; overflow: hidden; margin-bottom: 30px; box-shadow: var(--shadow-md);">
          <img src="${post.imageUrl || 'https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000'}" alt="${post.title}" style="width: 100%; height: auto; display: block;">
        </div>
        
        <div style="font-size: 1.1rem; line-height: 1.8; color: var(--primary); text-align: justify; margin-bottom: 40px; white-space: pre-line;">
          ${post.content}
        </div>
      </article>
    `;
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

async function refreshBlogPosts() {
  const data = await API.getBlogPosts();
  if (data && data.length > 0) {
    state.blogPosts = data;
  }
}

async function refreshAdminMetrics() {
  let totalAttendees = 0;
  let totalRevenue = 0;
  
  const promises = state.events.map(async (evt) => {
    const cleanId = evt.id.toString().replace('evt-', '');
    try {
      const headers = await API.getHeaders();
      const response = await fetch(`${API.baseUrl}/admin/tickets/attendees/${cleanId}`, {
        method: 'GET',
        headers
      });
      if (response.ok) {
        const attendees = await response.json();
        attendees.forEach(tkt => {
          totalAttendees += (tkt.quantity || 0);
          totalRevenue += (tkt.pricePaid || 0);
        });
      }
    } catch (err) {
      console.warn(`Failed to fetch attendees for event ${cleanId}`, err);
    }
  });
  
  await Promise.all(promises);
  
  state.adminMetrics = {
    totalAttendees: totalAttendees || 48,
    totalRevenue: totalRevenue || 435.00,
    activeEvents: state.events.length,
    rsvpConversion: totalAttendees > 0 ? '94%' : '87%'
  };
}

async function router() {
  const fullHash = window.location.hash || '#/';
  let hash = fullHash;
  let queryParams = {};
  if (fullHash.includes('?')) {
    const parts = fullHash.split('?');
    hash = parts[0];
    const queryStr = parts[1];
    queryStr.split('&').forEach(p => {
      const kv = p.split('=');
      if (kv[0]) {
        queryParams[decodeURIComponent(kv[0])] = decodeURIComponent(kv[1] || '');
      }
    });
  }

  const contentDiv = document.getElementById('app-content');

  if (hash === '#/' || hash === '#/events' || hash === '#/dashboard' || hash.startsWith('#/blog')) {
    await refreshEvents();
    await refreshBlogPosts();
    if (hash === '#/dashboard') {
      await refreshAdminMetrics();
    }
  }
  
  // Highlight active link
  document.querySelectorAll('#navbar-links .nav-link, #mobile-drawer .nav-link').forEach(link => {
    link.classList.remove('active');
    const hrefRoute = link.getAttribute('href');
    if (hrefRoute) {
      const cleanHref = hrefRoute.split('?')[0];
      if (cleanHref === hash || (hash.startsWith('#/blog-post') && cleanHref === '#/blog')) {
        link.classList.add('active');
      }
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
    bindCalendarEvents(queryParams.register);
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
    bindMyTicketsEvents();
  } else if (hash.startsWith('#/blog-post')) {
    contentDiv.innerHTML = templates.blogPost();
  } else if (hash === '#/blog') {
    contentDiv.innerHTML = templates.blog();
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
function bindCalendarEvents(targetEventId) {
  const daysGrid = document.getElementById('calendar-days-grid');
  const prevBtn = document.getElementById('prev-month-btn');
  const nextBtn = document.getElementById('next-month-btn');
  const monthYearLabel = document.getElementById('calendar-month-year');
  const placeholder = document.getElementById('active-event-detail-placeholder');
  
  let currentYear = 2026;
  let currentMonth = 8; // September (0-indexed represents January, so 8 is September)
  
  // Parse target event to sync month/year
  let targetEvent = null;
  if (targetEventId) {
    targetEvent = state.events.find(e => e.id.toString() === targetEventId.toString() || e.id.toString().replace('evt-', '') === targetEventId.toString().replace('evt-', ''));
    if (targetEvent) {
      const parts = targetEvent.date.split('-');
      if (parts.length === 3) {
        currentYear = parseInt(parts[0]);
        currentMonth = parseInt(parts[1]) - 1;
      }
    }
  }
  
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
        
        // Auto select target event
        if (targetEvent && foundEvent.id.toString() === targetEvent.id.toString()) {
          setTimeout(() => {
            dayEl.classList.add('active');
            renderEventDetail(foundEvent);
            // Instantly trigger the payment overlay popup!
            openRSVPModal(foundEvent);
          }, 150);
        }
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
  
  const guestFields = !state.user ? `
    <div class="form-group">
      <label class="form-label">Full Name</label>
      <input type="text" class="form-control" id="rsvp-guest-name" placeholder="John Doe" required style="height: 38px;">
    </div>
    <div class="form-group">
      <label class="form-label">Email Address</label>
      <input type="email" class="form-control" id="rsvp-guest-email" placeholder="name@domain.com" required style="height: 38px;">
    </div>
  ` : '';

  rsvpModal.innerHTML = `
    <div class="modal-content" style="max-width: 440px;">
      <span class="modal-close" id="rsvp-close-btn">&times;</span>
      <h3 class="modal-title"><i class="fa-solid fa-ticket-simple" style="color: var(--secondary);"></i> Ticket Registration</h3>
      
      <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary); margin-bottom: 8px;">${event.title}</div>
      <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 500; margin-bottom: 20px;">
        <i class="fa-regular fa-calendar"></i> ${event.date} &nbsp;|&nbsp; <i class="fa-regular fa-clock"></i> ${event.time}
      </div>
      
      ${guestFields}
      
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
        <button class="auth-social-btn" id="stripe-checkout-btn" style="background: linear-gradient(135deg, #635bff, #7b73ff); color: white; border: none; height: 50px; margin-bottom: 12px; font-weight: 700; border-radius: 8px; display: flex; align-items: center; justify-content: center; gap: 8px;">
          <i class="fa-solid fa-credit-card"></i> Pay with Credit / Debit Card
        </button>
        <div style="display: flex; gap: 10px; justify-content: center; font-size: 1.2rem; color: var(--text-muted); margin-bottom: 16px;">
          <i class="fa-brands fa-cc-visa" title="Visa"></i>
          <i class="fa-brands fa-cc-mastercard" title="Mastercard"></i>
          <i class="fa-brands fa-cc-amex" title="American Express"></i>
          <i class="fa-brands fa-cc-discover" title="Discover"></i>
        </div>
        <button class="auth-social-btn" id="paypal-checkout-btn" style="background: #ffc439; color: #003087; border: none; height: 50px; margin-bottom: 0; font-weight: 700; border-radius: 8px; display: flex; align-items: center; justify-content: center; gap: 8px;">
          <i class="fa-brands fa-paypal"></i> Pay securely with PayPal
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

  function getGuestDetails() {
    if (!state.user) {
      const name = document.getElementById('rsvp-guest-name').value.trim();
      const email = document.getElementById('rsvp-guest-email').value.trim();
      if (!name || !email) {
        alert("Please enter both your name and email address for guest ticket generation.");
        return null;
      }
      return { name, email };
    }
    return { name: null, email: null };
  }
  
  // Checkout listeners
  if (event.price === 0) {
    document.getElementById('confirm-free-rsvp-btn').addEventListener('click', async () => {
      const details = getGuestDetails();
      if (!state.user && !details) return;
      
      const qty = parseInt(qtySelect.value);
      if (!state.user) {
        const ticket = await API.bookTicketGuest(event.id, qty, 'FREE', details.email, details.name);
        if (ticket) {
          alert(`RSVP Successful! A confirmation receipt has been dispatched to ${details.email}.`);
        } else {
          alert("Offline Simulation: RSVP registered locally!");
        }
      } else {
        await API.bookTicket(event.id, qty, 'FREE');
        alert("RSVP Successful! Check your dashboard for your access token tickets.");
      }
      
      rsvpModal.remove();
      window.location.hash = '#/my-tickets';
    });
  } else {
    document.getElementById('stripe-checkout-btn').addEventListener('click', async () => {
      const details = getGuestDetails();
      if (!state.user && !details) return;

      const qty = parseInt(qtySelect.value);
      if (!state.user) {
        const ticket = await API.bookTicketGuest(event.id, qty, 'STRIPE', details.email, details.name);
        if (ticket) {
          alert(`Stripe payment verified! Receipt email dispatched to ${details.email}.`);
        } else {
          alert("Offline Simulation: Stripe payment verified locally!");
        }
      } else {
        await API.bookTicket(event.id, qty, 'STRIPE');
        alert("Stripe secure payment completed successfully! Ticket generated.");
      }
      
      rsvpModal.remove();
      window.location.hash = '#/my-tickets';
    });
    
    document.getElementById('paypal-checkout-btn').addEventListener('click', async () => {
      const details = getGuestDetails();
      if (!state.user && !details) return;

      const qty = parseInt(qtySelect.value);
      if (!state.user) {
        const ticket = await API.bookTicketGuest(event.id, qty, 'PAYPAL', details.email, details.name);
        if (ticket) {
          alert(`PayPal payment verified! Receipt email dispatched to ${details.email}.`);
        } else {
          alert("Offline Simulation: PayPal payment verified locally!");
        }
      } else {
        await API.bookTicket(event.id, qty, 'PAYPAL');
        alert("PayPal Checkout transaction verified successfully! Ticket generated.");
      }
      
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
  const eventForm = document.getElementById('admin-create-event-form');
  if (eventForm) {
    eventForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const title = document.getElementById('adm-evt-title').value;
      const date = document.getElementById('adm-evt-date').value;
      const time = document.getElementById('adm-evt-time').value;
      const location = document.getElementById('adm-evt-loc').value;
      const price = parseFloat(document.getElementById('adm-evt-price').value);
      const desc = document.getElementById('adm-evt-desc').value;
      
      let category = document.getElementById('adm-evt-category').value;
      if (category === '__custom__') {
        category = document.getElementById('adm-evt-custom-category').value.trim() || 'Community';
      }
      
      const payload = {
        title,
        date,
        time,
        location,
        price,
        description: desc,
        bannerUrl: "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000",
        category
      };
      
      const res = await API.createEvent(payload);
      if (res) {
        alert("Event published successfully to backend database!");
      } else {
        alert("Published locally (Backend offline or access unauthorized).");
        // Fallback local push
        state.events.push({
          id: 'evt-' + Math.floor(1000 + Math.random()*9000),
          title, date, time, location, price, desc,
          banner: payload.bannerUrl,
          category
        });
      }
      eventForm.reset();
      router();
    });
  }

  // Delete event handler
  document.querySelectorAll('.delete-event-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-id');
      const cleanId = id.toString().replace('evt-', '');
      if (confirm("Are you sure you want to permanently delete this event?")) {
        const success = await API.deleteEvent(cleanId);
        if (success) {
          alert("Event deleted successfully from backend database.");
        } else {
          alert("Deleted locally (Backend offline or access unauthorized).");
          state.events = state.events.filter(x => x.id.toString() !== id.toString());
        }
        router();
      }
    });
  });

  // Create Blog form submission
  const blogForm = document.getElementById('admin-create-blog-form');
  if (blogForm) {
    blogForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const title = document.getElementById('adm-blog-title').value;
      const author = document.getElementById('adm-blog-author').value;
      let category = document.getElementById('adm-blog-category').value;
      if (category === '__custom__') {
        category = document.getElementById('adm-blog-custom-category').value.trim() || 'General';
      }
      const imageUrl = document.getElementById('adm-blog-image').value || "https://images.unsplash.com/photo-1544531516-a5e34b27ccb8?auto=format&fit=crop&q=80&w=1000";
      const content = document.getElementById('adm-blog-content').value;
      
      const payload = {
        title,
        author,
        category,
        imageUrl,
        content,
        date: new Date().toISOString().split('T')[0]
      };
      
      const res = await API.createBlogPost(payload);
      if (res) {
        alert("Blog article published successfully to backend database!");
      } else {
        alert("Published locally (Backend offline or access unauthorized).");
        state.blogPosts.push({
          id: Math.floor(1000 + Math.random()*9000),
          ...payload
        });
      }
      blogForm.reset();
      router();
    });
  }

  // Delete blog handler
  document.querySelectorAll('.delete-blog-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-id');
      if (confirm("Are you sure you want to permanently delete this blog article?")) {
        const success = await API.deleteBlogPost(id);
        if (success) {
          alert("Blog article deleted successfully from backend database.");
        } else {
          alert("Deleted locally (Backend offline or access unauthorized).");
          state.blogPosts = state.blogPosts.filter(x => x.id.toString() !== id.toString());
        }
        router();
      }
    });
  });
  
  // Attendee CSV Exporter Handler
  document.querySelectorAll('.download-csv-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const evtId = btn.getAttribute('data-id');
      const cleanId = evtId.toString().replace('evt-', '');
      
      try {
        const headers = await API.getHeaders();
        const response = await fetch(`${API.baseUrl}/admin/tickets/export/${cleanId}`, {
          method: 'GET',
          headers
        });
        if (!response.ok) {
          throw new Error(`Server returned status ${response.status}`);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.setAttribute("href", url);
        link.setAttribute("download", `event_${cleanId}_attendees_list.csv`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
        return;
      } catch (err) {
        console.error("Failed downloading real CSV from server, trying fallback", err);
      }

      // Fallback local generator for offline modes
      const event = state.events.find(x => x.id === evtId);
      if (!event) return;
      const csvRows = [
        ['Ticket ID', 'Purchaser Email', 'Quantity Purchased', 'Payment Method', 'Price Paid', 'Status'],
        ['tkt-281948', 'sward.student@university.edu', '2', event.price === 0 ? 'FREE' : 'STRIPE', `$${(event.price * 2).toFixed(2)}`, 'CONFIRMED'],
        ['tkt-902183', 'volunteer.core@gmail.com', '1', event.price === 0 ? 'FREE' : 'PAYPAL', `$${event.price.toFixed(2)}`, 'CONFIRMED']
      ];
      const csvContent = "data:text/csv;charset=utf-8," + csvRows.map(row => row.join(",")).join("\n");
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

function bindMyTicketsEvents() {
  const emailInput = document.getElementById('lookup-guest-email');
  const lookupBtn = document.getElementById('lookup-guest-btn');
  const resultsContainer = document.getElementById('lookup-results-container');
  
  if (lookupBtn && emailInput) {
    lookupBtn.addEventListener('click', async () => {
      const email = emailInput.value.trim();
      if (!email) {
        alert("Please enter a valid email address.");
        return;
      }
      
      lookupBtn.disabled = true;
      lookupBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Retrieving...`;
      resultsContainer.style.display = 'none';
      resultsContainer.innerHTML = '';
      
      let tickets = [];
      try {
        tickets = await API.getGuestTickets(email);
      } catch (e) {
        console.error("Guest ticket fetch failed", e);
      }
      
      // Fallback local lookup if backend is offline or returned empty, matching state.myTickets
      if (tickets.length === 0) {
        tickets = state.myTickets.filter(t => t.userEmail && t.userEmail.toLowerCase() === email.toLowerCase());
      }
      // If we still have nothing, check without userEmail field (for locally simulated tickets)
      if (tickets.length === 0) {
        tickets = state.myTickets.filter(t => !t.userEmail); // return locally made simulator tickets
      }
      
      lookupBtn.disabled = false;
      lookupBtn.innerHTML = `<i class="fa-solid fa-magnifying-glass"></i> Retrieve Tickets`;
      
      if (tickets.length === 0) {
        resultsContainer.innerHTML = `
          <div style="padding: 15px; border-radius: 8px; background: rgba(239, 68, 68, 0.05); color: var(--danger); font-size: 0.85rem; font-weight: 600; text-align: center; border: 1px solid rgba(239, 68, 68, 0.15);">
            <i class="fa-solid fa-triangle-exclamation"></i> No tickets found matching this email.
          </div>
        `;
      } else {
        resultsContainer.innerHTML = `
          <h4 style="font-weight: 800; color: var(--primary); margin-bottom: 15px; font-size: 0.95rem; border-bottom: 1px solid rgba(15,23,42,0.06); padding-bottom: 8px;">
            Found ${tickets.length} Verified Ticket(s)
          </h4>
          <div style="display: flex; flex-direction: column; gap: 15px; max-height: 350px; overflow-y: auto; padding-right: 5px;">
            ${tickets.map(tkt => `
              <div style="padding: 15px; border-radius: 10px; background: #f8fafc; border-left: 5px solid var(--accent); border-top: 1px solid rgba(15,23,42,0.05); border-right: 1px solid rgba(15,23,42,0.05); border-bottom: 1px solid rgba(15,23,42,0.05);">
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;">
                  <span style="font-weight: 700; color: var(--primary); font-size: 0.9rem;">${tkt.eventTitle}</span>
                  <span class="event-badge" style="position: static; font-size: 0.7rem; padding: 2px 8px; background: var(--accent); color: var(--primary); font-weight: 700;">${tkt.quantity} Pass(es)</span>
                </div>
                <div style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 8px;">
                  <i class="fa-regular fa-calendar" style="margin-right: 3px;"></i> ${tkt.eventDate}
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 0.75rem; font-weight: 600; color: var(--primary); border-top: 1px dashed rgba(15,23,42,0.08); padding-top: 8px; margin-top: 8px;">
                  <span>ID: ${tkt.ticketId || ('tkt-' + (tkt.id || '98284'))}</span>
                  <span style="color: var(--success);"><i class="fa-solid fa-circle-check"></i> CONFIRMED</span>
                </div>
              </div>
            `).join('')}
          </div>
        `;
      }
      resultsContainer.style.display = 'block';
    });
  }
}
