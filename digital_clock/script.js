// List of all available timezones
const TIMEZONES = [
    'UTC',
    'Europe/London',
    'Europe/Paris',
    'Europe/Berlin',
    'Europe/Moscow',
    'Asia/Dubai',
    'Asia/Kolkata',
    'Asia/Bangkok',
    'Asia/Singapore',
    'Asia/Hong_Kong',
    'Asia/Tokyo',
    'Asia/Seoul',
    'Australia/Sydney',
    'Pacific/Auckland',
    'America/New_York',
    'America/Chicago',
    'America/Denver',
    'America/Los_Angeles',
    'America/Anchorage',
    'Pacific/Honolulu',
    'Brazil/Sao_Paulo',
    'America/Argentina/Buenos_Aires',
    'Africa/Cairo',
    'Africa/Johannesburg',
    'Asia/Istanbul',
    'America/Toronto',
    'America/Mexico_City'
];

let selectedTimezones = [];

// Initialize the app
document.addEventListener('DOMContentLoaded', () => {
    loadTimezones();
    setupEventListeners();
    loadSavedTimezones();
    updateClocks();
    setInterval(updateClocks, 1000);
});

// Setup event listeners
function setupEventListeners() {
    const addBtn = document.getElementById('addClockBtn');
    const modal = document.getElementById('timezoneModal');
    const closeBtn = document.querySelector('.close');
    const searchInput = document.getElementById('searchInput');
    const timezoneSearch = document.getElementById('timezoneSearch');

    addBtn.addEventListener('click', () => modal.style.display = 'block');
    closeBtn.addEventListener('click', () => modal.style.display = 'none');
    window.addEventListener('click', (e) => {
        if (e.target === modal) modal.style.display = 'none';
    });

    searchInput.addEventListener('input', filterClocks);
    timezoneSearch.addEventListener('input', filterTimezoneList);
}

// Load timezone options into modal
function loadTimezones() {
    const timezoneList = document.getElementById('timezoneList');
    timezoneList.innerHTML = '';

    TIMEZONES.forEach(tz => {
        const div = document.createElement('div');
        div.className = 'timezone-option';
        div.textContent = tz;
        div.addEventListener('click', () => addTimezone(tz));
        timezoneList.appendChild(div);
    });
}

// Add new timezone
function addTimezone(timezone) {
    if (!selectedTimezones.includes(timezone)) {
        selectedTimezones.push(timezone);
        saveTimezones();
        renderClocks();
        document.getElementById('timezoneModal').style.display = 'none';
    }
}

// Remove timezone
function removeTimezone(timezone) {
    selectedTimezones = selectedTimezones.filter(tz => tz !== timezone);
    saveTimezones();
    renderClocks();
}

// Save timezones to localStorage
function saveTimezones() {
    localStorage.setItem('selectedTimezones', JSON.stringify(selectedTimezones));
}

// Load timezones from localStorage
function loadSavedTimezones() {
    const saved = localStorage.getItem('selectedTimezones');
    if (saved) {
        selectedTimezones = JSON.parse(saved);
        renderClocks();
    } else {
        // Default timezones
        selectedTimezones = ['UTC', 'Asia/Kolkata', 'America/New_York', 'Europe/London'];
        saveTimezones();
        renderClocks();
    }
}

// Render clock cards
function renderClocks() {
    const grid = document.getElementById('clocksGrid');
    grid.innerHTML = '';

    selectedTimezones.forEach(timezone => {
        const card = document.createElement('div');
        card.className = 'clock-card';
        card.innerHTML = `
            <div class="timezone-name">${timezone}</div>
            <div class="timezone-info" id="info-${timezone}"></div>
            <div class="digital-time" id="time-${timezone}">00:00:00</div>
            <div class="date-display" id="date-${timezone}"></div>
            <button class="remove-btn" onclick="removeTimezone('${timezone}')">Remove</button>
        `;
        grid.appendChild(card);
    });
}

// Update all clocks
function updateClocks() {
    const now = new Date();

    selectedTimezones.forEach(timezone => {
        try {
            const time = new Date(now.toLocaleString('en-US', { timeZone: timezone }));
            const hours = String(time.getHours()).padStart(2, '0');
            const minutes = String(time.getMinutes()).padStart(2, '0');
            const seconds = String(time.getSeconds()).padStart(2, '0');

            document.getElementById(`time-${timezone}`).textContent = `${hours}:${minutes}:${seconds}`;

            const dateStr = time.toLocaleDateString('en-US', {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
            document.getElementById(`date-${timezone}`).textContent = dateStr;

            const offset = getTimezoneOffset(timezone);
            document.getElementById(`info-${timezone}`).textContent = `GMT${offset}`;
        } catch (e) {
            console.error(`Error updating timezone ${timezone}:`, e);
        }
    });
}

// Get timezone offset
function getTimezoneOffset(timezone) {
    const now = new Date();
    const utcDate = new Date(now.toLocaleString('en-US', { timeZone: 'UTC' }));
    const tzDate = new Date(now.toLocaleString('en-US', { timeZone: timezone }));
    const offset = (tzDate - utcDate) / 3600000;
    const sign = offset > 0 ? '+' : '';
    return `${sign}${offset}`;
}

// Filter clocks by search
function filterClocks() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const cards = document.querySelectorAll('.clock-card');

    cards.forEach(card => {
        const timezone = card.querySelector('.timezone-name').textContent.toLowerCase();
        card.style.display = timezone.includes(searchTerm) ? 'block' : 'none';
    });
}

// Filter timezone list in modal
function filterTimezoneList() {
    const searchTerm = document.getElementById('timezoneSearch').value.toLowerCase();
    const options = document.querySelectorAll('.timezone-option');

    options.forEach(option => {
        const text = option.textContent.toLowerCase();
        option.style.display = text.includes(searchTerm) ? 'block' : 'none';
    });
}
