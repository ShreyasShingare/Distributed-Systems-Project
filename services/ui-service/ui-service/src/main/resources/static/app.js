// Configuration
// Get the base URL (remove port from origin)
const getBaseUrl = () => {
    const origin = window.location.origin;
    // Remove port if present (works for both 8080 and 8084)
    return origin.replace(/:\d+$/, '');
};

const API_BASE_URL = getBaseUrl();
const AMENITY_API = `${API_BASE_URL}:8081/api/amenities`;
const USER_API = `${API_BASE_URL}:8085/api`;
const BOOKING_API = `${API_BASE_URL}:8082/api`;

// Log API URLs for debugging
console.log('API Configuration:', {
    origin: window.location.origin,
    baseUrl: API_BASE_URL,
    amenityApi: AMENITY_API,
    bookingApi: BOOKING_API
});

// State
let sessionToken = localStorage.getItem('sessionToken');
let currentUsername = localStorage.getItem('username');
let currentUserRole = localStorage.getItem('userRole');
let amenities = [];
let bookings = [];
let adminBookings = [];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    checkAuthStatus();
    loadAmenities();
});

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const tab = e.target.dataset.tab;
            switchTab(tab);
        });
    });

    // Login form
    document.getElementById('login-form-element').addEventListener('submit', handleLogin);

    // Register form
    document.getElementById('register-form-element').addEventListener('submit', handleRegister);

    // Logout
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    // Load slots
    document.getElementById('load-slots-btn').addEventListener('click', loadTimeSlots);

    // Load bookings
    document.getElementById('load-bookings-btn').addEventListener('click', loadUserBookings);

    // Admin dashboard
    document.getElementById('admin-dashboard-btn').addEventListener('click', showAdminDashboard);
    document.getElementById('load-all-bookings-btn').addEventListener('click', loadAllBookings);
    document.getElementById('load-stats-btn').addEventListener('click', loadAdminStats);
    document.getElementById('admin-amenity-filter').addEventListener('change', filterAdminBookings);
}

function checkAuthStatus() {
    if (sessionToken && currentUsername) {
        showBookingSection();
    } else {
        showAuthSection();
    }
}

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tab}"]`).classList.add('active');

    document.getElementById('login-form').classList.toggle('hidden', tab !== 'login');
    document.getElementById('register-form').classList.toggle('hidden', tab !== 'register');
}

function showAuthSection() {
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('booking-section').classList.add('hidden');
    document.getElementById('user-info').classList.add('hidden');
}

function showBookingSection() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('booking-section').classList.remove('hidden');
    document.getElementById('user-info').classList.remove('hidden');
    document.getElementById('username-display').textContent = currentUsername;

    // Show admin badge and button if user is admin
    const isAdmin = currentUserRole === 'ADMIN';
    document.getElementById('admin-badge').classList.toggle('hidden', !isAdmin);
    document.getElementById('admin-dashboard-btn').classList.toggle('hidden', !isAdmin);

    // Hide admin dashboard section for normal users
    if (!isAdmin) {
        document.getElementById('admin-dashboard-section').classList.add('hidden');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        console.log('Attempting login to:', `${USER_API}/login`);
        const response = await fetch(`${USER_API}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        console.log('Login response status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('Login successful, data:', data);
            sessionToken = data.token;
            currentUsername = data.username;
            currentUserRole = data.role || 'USER';
            localStorage.setItem('sessionToken', sessionToken);
            localStorage.setItem('username', currentUsername);
            localStorage.setItem('userRole', currentUserRole);
            showBookingSection();
            loadUserBookings();
        } else {
            const errorText = await response.text();
            console.error('Login failed:', response.status, errorText);
            showError('login-error', `Login failed: ${response.status} - ${errorText || 'Invalid username or password'}`);
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('login-error', `Login failed: ${error.message}. Check console for details.`);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('register-username').value;
    const password = document.getElementById('register-password').value;
    const name = document.getElementById('register-name').value;
    const flatNo = document.getElementById('register-flat-no').value;
    const contactNumber = document.getElementById('register-contact').value;

    try {
        console.log('Attempting registration to:', `${USER_API}/register`);
        const response = await fetch(`${USER_API}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username,
                password,
                name,
                flatNo,
                contactNumber
            })
        });

        console.log('Registration response status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('Registration successful, data:', data);
            sessionToken = data.token;
            currentUsername = data.username;
            currentUserRole = data.role || 'USER';
            localStorage.setItem('sessionToken', sessionToken);
            localStorage.setItem('username', currentUsername);
            localStorage.setItem('userRole', currentUserRole);
            showBookingSection();
            loadUserBookings();
        } else {
            const errorText = await response.text();
            console.error('Registration failed:', response.status, errorText);
            showError('register-error', `Registration failed: ${response.status} - ${errorText || 'Username may already exist'}`);
        }
    } catch (error) {
        console.error('Registration error:', error);
        showError('register-error', `Registration failed: ${error.message}. Check console for details.`);
    }
}

function handleLogout() {
    sessionToken = null;
    currentUsername = null;
    currentUserRole = null;
    localStorage.removeItem('sessionToken');
    localStorage.removeItem('username');
    localStorage.removeItem('userRole');

    // Hide admin dashboard on logout
    document.getElementById('admin-dashboard-section').classList.add('hidden');

    showAuthSection();
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
}

async function loadAmenities() {
    try {
        const response = await fetch(AMENITY_API);
        if (response.ok) {
            amenities = await response.json();
            const select = document.getElementById('amenity-select');
            select.innerHTML = '<option value="">Select an amenity</option>';
            amenities.forEach(amenity => {
                const option = document.createElement('option');
                option.value = amenity.id;
                option.textContent = amenity.name;
                select.appendChild(option);

                // Also add to admin filter
                const adminSelect = document.getElementById('admin-amenity-filter');
                if (adminSelect) {
                    const adminOption = document.createElement('option');
                    adminOption.value = amenity.id;
                    adminOption.textContent = amenity.name;
                    adminSelect.appendChild(adminOption);
                }
            });
        }
    } catch (error) {
        console.error('Failed to load amenities:', error);
    }
}

async function loadTimeSlots() {
    const amenityId = document.getElementById('amenity-select').value;
    const date = document.getElementById('date-picker').value;

    if (!amenityId || !date) {
        alert('Please select both amenity and date');
        return;
    }

    try {
        const response = await fetch(`${BOOKING_API}/availability?amenityId=${amenityId}&date=${date}`);
        if (response.ok) {
            const data = await response.json();
            console.log('Availability data:', data);
            displayTimeSlots(data, amenityId, date);
        } else {
            const errorText = await response.text();
            console.error('API Error:', response.status, errorText);
            alert('Failed to load time slots. Check console for details.');
        }
    } catch (error) {
        console.error('Failed to load time slots:', error);
        alert('Failed to load time slots: ' + error.message);
    }
}

function displayTimeSlots(data, amenityId, date) {
    const container = document.getElementById('slots-container');
    const amenityType = getAmenityType(amenityId);

    // Always use local check first - if local says it's day-based, use that
    // API flag is only used if local check is inconclusive (not HALL or BBQ)
    const localIsDayBased = amenityType === 'HALL' || amenityType === 'BBQ';
    // If local check says day-based, use that. Otherwise, trust API if available
    const isDayBased = localIsDayBased ? true : (data.isDayBased === true);

    // Debug logging
    console.log('Display Time Slots:', {
        amenityId,
        amenityName: getAmenityName(amenityId),
        amenityType,
        localIsDayBased,
        apiIsDayBased: data.isDayBased,
        finalIsDayBased: isDayBased,
        willShowDayBased: isDayBased
    });

    if (isDayBased) {
        // For day-based amenities (HALL, BBQ), show ONLY a single "Book for this day" button
        // Do NOT show any time slots
        container.innerHTML = `<h2>${getAmenityName(amenityId)} - ${date}</h2>`;

        // Check if the day is already booked
        const isBooked = data.isBooked || (data.bookedSlots && Object.keys(data.bookedSlots).length > 0);
        const bookingCount = data.bookingCount || (data.bookedSlots ? Object.keys(data.bookedSlots).length : 0);

        // Set correct capacity based on amenity type - explicit checks
        let maxCapacity;
        if (amenityType === 'HALL') {
            maxCapacity = 1; // HALL: 1 per day
        } else if (amenityType === 'BBQ') {
            maxCapacity = 4; // BBQ: 4 per day
        } else {
            // Fallback - should not happen for day-based amenities
            console.warn('Unknown day-based amenity type:', amenityType);
            maxCapacity = 1;
        }

        // Debug logging for day-based amenities
        console.log('Day-based capacity:', {
            amenityId,
            amenityName: getAmenityName(amenityId),
            amenityType,
            maxCapacity,
            bookingCount,
            availableSlots: maxCapacity - bookingCount
        });

        const bookingCard = document.createElement('div');
        bookingCard.style.cssText = 'padding: 30px; border: 2px solid #ddd; border-radius: 8px; text-align: center; margin: 20px 0; background-color: #f9f9f9;';

        if (isBooked) {
            bookingCard.innerHTML = `
                <div style="color: #d32f2f; font-size: 20px; margin-bottom: 15px;">
                    <strong>❌ Already Booked</strong>
                </div>
                <div style="color: #666; font-size: 16px; margin-bottom: 10px;">
                    This ${amenityType === 'HALL' ? 'Community Hall' : 'BBQ Area'} is already booked for this date.
                </div>
                <div style="color: #666; font-size: 14px;">
                    Bookings: ${bookingCount}/${maxCapacity}
                </div>
            `;
        } else {
            const availableSlots = maxCapacity - bookingCount;
            bookingCard.innerHTML = `
                <div style="font-size: 20px; margin-bottom: 15px; color: #4caf50;">
                    <strong>✅ Available for Booking</strong>
                </div>
                <div style="color: #666; font-size: 16px; margin-bottom: 10px;">
                    ${amenityType === 'HALL' ? 'Book the Community Hall' : amenityType === 'BBQ' ? 'Book the BBQ Area' : 'Book this amenity'} for the entire day
                </div>
                <div style="color: #4caf50; font-size: 14px; margin-bottom: 20px; font-weight: bold;">
                    Available Slots: ${availableSlots}/${maxCapacity}
                </div>
                <button class="slot-book-btn" onclick="bookSlot(${amenityId}, '${date}', '09:00')" style="padding: 12px 40px; font-size: 18px; font-weight: bold; background-color: #4caf50; color: white; border: none; border-radius: 5px; cursor: pointer;">
                    Book for This Day
                </button>
            `;
        }

        container.appendChild(bookingCard);
    } else {
        // For slot-based amenities, show hourly slots with booking counts and available slots
        container.innerHTML = `<h2>Time Slots for ${getAmenityName(amenityId)} on ${date}</h2>`;

        const slotsGrid = document.createElement('div');
        slotsGrid.className = 'slots-grid';

        // Get capacity limits based on amenity type - explicit checks
        const getCapacity = () => {
            // Check TENNIS first to ensure it gets priority
            if (amenityType === 'TENNIS') {
                return 2;  // Tennis Court: max 2
            }
            if (amenityType === 'GYM') {
                return 10;  // Gym: max 10
            }
            if (amenityType === 'SWIMMING') {
                return 10;  // Swimming Pool: max 10
            }
            if (amenityType === 'PARKING') {
                return 10;  // Parking: max 10
            }
            // Default fallback - should not happen for known amenities
            console.warn('Unknown slot-based amenity type:', amenityType);
            return 10;
        };
        const maxCapacity = getCapacity();

        // Debug logging for capacity
        console.log('Slot-based capacity check:', {
            amenityId,
            amenityName: getAmenityName(amenityId),
            amenityType,
            maxCapacity,
            detectedCorrectly: amenityType === 'TENNIS' ? maxCapacity === 2 : true
        });

        // Generate hourly slots from 09:00 to 17:00
        for (let hour = 9; hour < 17; hour++) {
            const slotTime = `${hour.toString().padStart(2, '0')}:00`;
            const slot = document.createElement('div');
            slot.className = 'slot';

            const bookingCount = data.bookedSlots && data.bookedSlots[slotTime] ? parseInt(data.bookedSlots[slotTime]) : 0;
            const availableSlots = maxCapacity - bookingCount;
            const isAvailable = data.availableSlots && data.availableSlots.includes(slotTime);
            const isFullyBooked = bookingCount >= maxCapacity;

            if (isFullyBooked) {
                slot.classList.add('booked');
                slot.innerHTML = `
                    <div><strong>${slotTime}</strong></div>
                    <div style="font-size: 11px; margin-top: 5px; color: #d32f2f; font-weight: bold;">
                        FULL
                    </div>
                    <div style="font-size: 11px; margin-top: 3px; color: #666;">
                        ${bookingCount}/${maxCapacity} booked
                    </div>
                    <div style="font-size: 11px; margin-top: 3px; color: #d32f2f;">
                        Available: 0
                    </div>
                `;
            } else if (bookingCount > 0) {
                slot.classList.add('partially-booked');
                slot.style.border = '2px solid #ff9800';
                slot.innerHTML = `
                    <div><strong>${slotTime}</strong></div>
                    <div style="font-size: 11px; margin-top: 5px; color: #ff9800; font-weight: bold;">
                        ${availableSlots} Available
                    </div>
                    <div style="font-size: 11px; margin-top: 3px; color: #666;">
                        ${bookingCount}/${maxCapacity} booked
                    </div>
                    <button class="slot-book-btn" onclick="bookSlot(${amenityId}, '${date}', '${slotTime}')" style="margin-top: 8px; padding: 6px 12px;">Book</button>
                `;
            } else if (isAvailable) {
                slot.classList.add('available');
                slot.innerHTML = `
                    <div><strong>${slotTime}</strong></div>
                    <div style="font-size: 11px; margin-top: 5px; color: #4caf50; font-weight: bold;">
                        ${maxCapacity} Available
                    </div>
                    <div style="font-size: 11px; margin-top: 3px; color: #666;">
                        0/${maxCapacity} booked
                    </div>
                    <button class="slot-book-btn" onclick="bookSlot(${amenityId}, '${date}', '${slotTime}')" style="margin-top: 8px;">Book</button>
                `;
            } else {
                slot.innerHTML = `
                    <div><strong>${slotTime}</strong></div>
                    <div style="font-size: 11px; margin-top: 5px; color: #999;">Not available</div>
                `;
            }

            slotsGrid.appendChild(slot);
        }

        container.appendChild(slotsGrid);
    }
}

async function bookSlot(amenityId, date, time) {
    if (!sessionToken) {
        alert('Please login first');
        return;
    }

    // Determine amenity type from amenity name
    const amenityType = getAmenityType(amenityId);

    // For day-based amenities (HALL, BBQ), timeSlot should be null
    const isDayBased = amenityType === 'HALL' || amenityType === 'BBQ';

    let slotStart, slotEnd, timeSlot;

    if (isDayBased) {
        // For day-based bookings, use full day slots (9 AM to 10 PM for HALL, 9 AM to 6 PM for BBQ)
        slotStart = new Date(`${date}T09:00:00`);
        slotEnd = new Date(`${date}T${amenityType === 'HALL' ? '22:00:00' : '18:00:00'}`);
        timeSlot = null;
    } else {
        // For slot-based bookings, use the selected time
        slotStart = new Date(`${date}T${time}:00`);
        slotEnd = new Date(slotStart.getTime() + 60 * 60 * 1000); // Add 1 hour
        timeSlot = formatTimeSlot(slotStart.toISOString(), slotEnd.toISOString());
    }

    try {
        const response = await fetch(`${BOOKING_API}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-SESSION-TOKEN': sessionToken
            },
            body: JSON.stringify({
                amenityId: amenityId,
                amenityType: amenityType,
                bookingDate: date,
                timeSlot: timeSlot,
                slotStart: slotStart.toISOString(),
                slotEnd: slotEnd.toISOString()
            })
        });

        if (response.ok) {
            alert('Booking created successfully!');
            loadTimeSlots();
            loadUserBookings();
        } else {
            const errorData = await response.json().catch(() => ({ error: 'Unknown error' }));
            const errorMessage = errorData.error || `Failed to create booking (HTTP ${response.status})`;

            if (response.status === 409) {
                alert(errorMessage);
                loadTimeSlots();
            } else if (response.status === 400) {
                alert(`Invalid request: ${errorMessage}`);
            } else {
                alert(`Failed to create booking: ${errorMessage}`);
            }
        }
    } catch (error) {
        console.error('Booking error:', error);
        alert('Failed to create booking: ' + error.message);
    }
}

async function loadUserBookings() {
    if (!sessionToken) return;

    try {
        const response = await fetch(`${BOOKING_API}/bookings`, {
            headers: { 'X-SESSION-TOKEN': sessionToken }
        });

        if (response.ok) {
            bookings = await response.json();
            displayBookings();
        }
    } catch (error) {
        console.error('Failed to load bookings:', error);
    }
}

function displayBookings() {
    const container = document.getElementById('bookings-list');

    if (bookings.length === 0) {
        container.innerHTML = '<p>No bookings found.</p>';
        return;
    }

    container.innerHTML = bookings.map(booking => {
        const slotStart = new Date(booking.slotStart);
        const slotEnd = new Date(booking.slotEnd);
        const amenityName = getAmenityName(booking.amenityId);
        const bookingId = booking.id || 'N/A';
        const amenityType = booking.amenityType || getAmenityType(booking.amenityId);
        const timeSlot = booking.timeSlot || null;
        const bookingDate = booking.bookingDate || slotStart.toLocaleDateString();

        // Format display based on amenity type
        let timeDisplay = '';
        if (amenityType === 'HALL' || amenityType === 'BBQ') {
            // Day-based: show date only
            timeDisplay = `<strong>Date:</strong> ${bookingDate}<br>`;
        } else {
            // Slot-based: show date and time slot
            timeDisplay = `<strong>Date:</strong> ${bookingDate}<br>`;
            if (timeSlot) {
                timeDisplay += `<strong>Time Slot:</strong> ${timeSlot}<br>`;
            }
            timeDisplay += `<strong>Time:</strong> ${slotStart.toLocaleTimeString()} - ${slotEnd.toLocaleTimeString()}`;
        }

        return `
            <div class="booking-item" style="border: 1px solid #ddd; padding: 15px; margin-bottom: 10px; border-radius: 5px; background-color: #f9f9f9;">
                <div style="flex: 1;">
                    <div style="margin-bottom: 8px;">
                        <span style="color: #666; font-size: 12px;">Booking ID:</span>
                        <strong style="color: #1976d2; font-size: 14px;">#${bookingId}</strong>
                    </div>
                    <div style="margin-bottom: 5px;">
                        <strong style="font-size: 16px; color: #333;">${amenityName}</strong>
                    </div>
                    <div style="color: #666; font-size: 14px;">
                        ${timeDisplay}
                    </div>
                </div>
                <div style="display: flex; align-items: center;">
                    <button class="cancel-btn" onclick="cancelBooking(${booking.id})" style="padding: 8px 16px; background-color: #d32f2f; color: white; border: none; border-radius: 4px; cursor: pointer;">Cancel</button>
                </div>
            </div>
        `;
    }).join('');
}

async function cancelBooking(bookingId) {
    if (!confirm('Are you sure you want to cancel this booking?')) {
        return;
    }

    try {
        const response = await fetch(`${BOOKING_API}/bookings/${bookingId}`, {
            method: 'DELETE',
            headers: { 'X-SESSION-TOKEN': sessionToken }
        });

        if (response.ok) {
            alert('Booking cancelled successfully!');
            loadTimeSlots();
            loadUserBookings();
        } else {
            alert('Failed to cancel booking');
        }
    } catch (error) {
        console.error('Cancel error:', error);
        alert('Failed to cancel booking');
    }
}

function getAmenityName(amenityId) {
    // Convert to number for comparison (amenityId might come as string from HTML)
    const id = typeof amenityId === 'string' ? parseInt(amenityId, 10) : amenityId;
    const amenity = amenities.find(a => a.id === id);
    if (!amenity) {
        console.warn('Amenity not found for ID:', amenityId, '(parsed as:', id, ')', 'Available amenities:', amenities);
        return `Amenity ${amenityId}`;
    }
    return amenity.name;
}

function getAmenityType(amenityId) {
    // Convert to number for comparison (amenityId might come as string from HTML)
    const id = typeof amenityId === 'string' ? parseInt(amenityId, 10) : amenityId;
    const amenity = amenities.find(a => a.id === id);
    if (!amenity) {
        console.warn('Amenity not found for ID:', amenityId, '(parsed as:', id, ')', 'Available amenities:', amenities);
        return 'GYM'; // Default fallback
    }

    const name = amenity.name.toUpperCase();
    console.log('getAmenityType check:', { amenityId, parsedId: id, amenityName: amenity.name, upperName: name });

    // Check for BBQ first (before HALL) to avoid conflicts
    // "BBQ Area" should match BBQ, not HALL
    if (name.includes('BBQ') || name.includes('BARBECUE')) {
        console.log('  -> Detected as BBQ');
        return 'BBQ';
    }
    // Check for HALL (includes PARTY HALL)
    if (name.includes('HALL') || name.includes('PARTY')) {
        console.log('  -> Detected as HALL');
        return 'HALL';
    }
    // Check for TENNIS before GYM (Tennis Court should not match GYM)
    if (name.includes('TENNIS')) {
        console.log('  -> Detected as TENNIS');
        return 'TENNIS';
    }
    if (name.includes('GYM')) {
        console.log('  -> Detected as GYM');
        return 'GYM';
    }
    if (name.includes('SWIMMING') || name.includes('POOL')) {
        console.log('  -> Detected as SWIMMING');
        return 'SWIMMING';
    }
    if (name.includes('PARKING') || name.includes('CAR')) {
        console.log('  -> Detected as PARKING');
        return 'PARKING';
    }
    console.warn('  -> No match found, defaulting to GYM');
    return 'GYM'; // Default fallback
}

function formatTimeSlot(startTime, endTime) {
    // Format as "HH:mm-HH:mm"
    const start = startTime.split('T')[1].substring(0, 5); // Extract HH:mm
    const end = endTime.split('T')[1].substring(0, 5); // Extract HH:mm
    return `${start}-${end}`;
}

function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    errorElement.textContent = message;
    errorElement.classList.remove('hidden');
    setTimeout(() => {
        errorElement.classList.add('hidden');
    }, 5000);
}

// Admin functions
function showAdminDashboard() {
    // Check if user is admin before showing dashboard
    if (currentUserRole !== 'ADMIN') {
        alert('Access denied. Admin privileges required.');
        return;
    }

    document.getElementById('admin-dashboard-section').classList.remove('hidden');
    loadAllBookings();
}

async function loadAllBookings() {
    if (!sessionToken) return;

    // Double check admin role
    if (currentUserRole !== 'ADMIN') {
        alert('Access denied. Admin privileges required.');
        document.getElementById('admin-dashboard-section').classList.add('hidden');
        return;
    }

    try {
        const response = await fetch(`${BOOKING_API}/admin/bookings`, {
            headers: { 'X-SESSION-TOKEN': sessionToken }
        });

        if (response.ok) {
            adminBookings = await response.json();
            displayAdminBookings();
        } else if (response.status === 403) {
            alert('Access denied. Admin privileges required.');
            document.getElementById('admin-dashboard-section').classList.add('hidden');
        } else {
            alert('Failed to load bookings');
        }
    } catch (error) {
        console.error('Failed to load admin bookings:', error);
        alert('Failed to load bookings');
    }
}

function displayAdminBookings() {
    const container = document.getElementById('admin-bookings-list');

    if (adminBookings.length === 0) {
        container.innerHTML = '<p>No bookings found.</p>';
        return;
    }

    container.innerHTML = `
        <table class="admin-table">
            <thead>
                <tr>
                    <th>Booking ID</th>
                    <th>Amenity</th>
                    <th>User</th>
                    <th>Name</th>
                    <th>Flat No</th>
                    <th>Contact</th>
                    <th>Date & Time</th>
                    <th>Created At</th>
                </tr>
            </thead>
            <tbody>
                ${adminBookings.map(booking => {
        const slotStart = new Date(booking.slotStart);
        const slotEnd = new Date(booking.slotEnd);
        const createdAt = new Date(booking.createdAt);
        const amenityName = getAmenityName(booking.amenityId);

        return `
                        <tr>
                            <td>${booking.id}</td>
                            <td>${amenityName}</td>
                            <td>${booking.userName || 'N/A'}</td>
                            <td>${booking.userFullName || 'N/A'}</td>
                            <td>${booking.flatNo || 'N/A'}</td>
                            <td>${booking.contactNumber || 'N/A'}</td>
                            <td>${slotStart.toLocaleDateString()} ${slotStart.toLocaleTimeString()} - ${slotEnd.toLocaleTimeString()}</td>
                            <td>${createdAt.toLocaleString()}</td>
                        </tr>
                    `;
    }).join('')}
            </tbody>
        </table>
    `;
}

async function loadAdminStats() {
    if (!sessionToken) return;

    // Double check admin role
    if (currentUserRole !== 'ADMIN') {
        alert('Access denied. Admin privileges required.');
        return;
    }

    try {
        const response = await fetch(`${BOOKING_API}/admin/stats`, {
            headers: { 'X-SESSION-TOKEN': sessionToken }
        });

        if (response.ok) {
            const stats = await response.json();
            displayAdminStats(stats);
        } else if (response.status === 403) {
            alert('Access denied. Admin privileges required.');
        } else {
            alert('Failed to load statistics');
        }
    } catch (error) {
        console.error('Failed to load admin stats:', error);
        alert('Failed to load statistics');
    }
}

function displayAdminStats(stats) {
    const container = document.getElementById('admin-stats');
    container.innerHTML = `
        <div class="stats-card">
            <h3>Booking Statistics</h3>
            <p><strong>Total Bookings:</strong> ${stats.totalBookings}</p>
        </div>
    `;

    if (stats.bookings && stats.bookings.length > 0) {
        adminBookings = stats.bookings;
        displayAdminBookings();
    }
}

function filterAdminBookings() {
    // Check admin role
    if (currentUserRole !== 'ADMIN') {
        alert('Access denied. Admin privileges required.');
        return;
    }

    const amenityId = document.getElementById('admin-amenity-filter').value;

    if (!amenityId) {
        loadAllBookings();
        return;
    }

    if (!sessionToken) return;

    try {
        fetch(`${BOOKING_API}/admin/bookings/amenity/${amenityId}`, {
            headers: { 'X-SESSION-TOKEN': sessionToken }
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 403) {
                    alert('Access denied. Admin privileges required.');
                    return [];
                } else {
                    alert('Failed to load bookings');
                    return [];
                }
            })
            .then(bookings => {
                adminBookings = bookings;
                displayAdminBookings();
            })
            .catch(error => {
                console.error('Failed to filter bookings:', error);
                alert('Failed to filter bookings');
            });
    } catch (error) {
        console.error('Failed to filter bookings:', error);
    }
}

