// Weather API Configuration
const API_KEY = 'b6fd43b8a3c7c7654ca3c10b6b79c6ea'; // OpenWeatherMap API key
const API_BASE_URL = 'https://api.openweathermap.org/data/2.5';

// Weather icon mapping
const WEATHER_ICONS = {
    '01d': '☀️',
    '01n': '🌙',
    '02d': '⛅',
    '02n': '☁️',
    '03d': '☁️',
    '03n': '☁️',
    '04d': '☁️',
    '04n': '☁️',
    '09d': '🌧️',
    '09n': '🌧️',
    '10d': '🌦️',
    '10n': '🌧️',
    '11d': '⛈️',
    '11n': '⛈️',
    '13d': '❄️',
    '13n': '❄️',
    '50d': '🌫️',
    '50n': '🌫️'
};

let currentCity = 'London';

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    getWeatherData(currentCity);
});

// Setup event listeners
function setupEventListeners() {
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');
    const locationBtn = document.getElementById('locationBtn');

    searchBtn.addEventListener('click', () => {
        const city = searchInput.value.trim();
        if (city) {
            getWeatherData(city);
            searchInput.value = '';
        }
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            const city = searchInput.value.trim();
            if (city) {
                getWeatherData(city);
                searchInput.value = '';
            }
        }
    });

    locationBtn.addEventListener('click', getUserLocation);
}

// Get user's current location
function getUserLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                getWeatherByCoords(latitude, longitude);
            },
            (error) => {
                showError('Unable to access your location');
            }
        );
    } else {
        showError('Geolocation is not supported by your browser');
    }
}

// Get weather by coordinates
function getWeatherByCoords(lat, lon) {
    fetchWeatherData(`${API_BASE_URL}/weather?lat=${lat}&lon=${lon}&appid=${API_KEY}&units=metric`)
        .then(data => {
            currentCity = data.name;
            displayCurrentWeather(data);
            getForecastData(lat, lon);
        })
        .catch(error => showError('Failed to fetch weather data'));
}

// Get weather data
function getWeatherData(city) {
    const weatherUrl = `${API_BASE_URL}/weather?q=${city}&appid=${API_KEY}&units=metric`;
    const forecastUrl = `${API_BASE_URL}/forecast?q=${city}&appid=${API_KEY}&units=metric`;

    Promise.all([
        fetchWeatherData(weatherUrl),
        fetchWeatherData(forecastUrl)
    ])
        .then(([weatherData, forecastData]) => {
            currentCity = weatherData.name;
            displayCurrentWeather(weatherData);
            displayForecast(forecastData);
        })
        .catch(error => showError('City not found. Please try again.'));
}

// Fetch data from API
function fetchWeatherData(url) {
    return fetch(url).then(response => {
        if (!response.ok) {
            throw new Error('API Error');
        }
        return response.json();
    });
}

// Get forecast data
function getForecastData(lat, lon) {
    const url = `${API_BASE_URL}/forecast?lat=${lat}&lon=${lon}&appid=${API_KEY}&units=metric`;
    fetchWeatherData(url)
        .then(data => displayForecast(data))
        .catch(error => showError('Failed to fetch forecast data'));
}

// Display current weather
function displayCurrentWeather(data) {
    const container = document.getElementById('currentWeatherContainer');
    const { name, sys, main, weather, wind, clouds } = data;
    const { temp, feels_like, humidity, pressure } = main;
    const { description, icon } = weather[0];
    const weatherIcon = WEATHER_ICONS[icon] || '🌡️';
    const sunrise = new Date(sys.sunrise * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const sunset = new Date(sys.sunset * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    container.innerHTML = `
        <div class="weather-left">
            <div class="weather-city">${name}, ${sys.country}</div>
            <div class="weather-description">${description}</div>
            <div class="weather-icon">${weatherIcon}</div>
            <div class="weather-temp">${Math.round(temp)}°C</div>
            <div class="weather-feel">Feels like ${Math.round(feels_like)}°C</div>
        </div>
        <div class="weather-right">
            <div class="weather-stat">
                <div class="stat-label">Humidity</div>
                <div class="stat-value">${humidity}%</div>
            </div>
            <div class="weather-stat">
                <div class="stat-label">Wind Speed</div>
                <div class="stat-value">${wind.speed.toFixed(1)} m/s</div>
            </div>
            <div class="weather-stat">
                <div class="stat-label">Pressure</div>
                <div class="stat-value">${pressure} hPa</div>
            </div>
            <div class="weather-stat">
                <div class="stat-label">Cloud Cover</div>
                <div class="stat-value">${clouds.all}%</div>
            </div>
            <div class="weather-stat">
                <div class="stat-label">Sunrise</div>
                <div class="stat-value">${sunrise}</div>
            </div>
            <div class="weather-stat">
                <div class="stat-label">Sunset</div>
                <div class="stat-value">${sunset}</div>
            </div>
        </div>
    `;
}

// Display forecast
function displayForecast(data) {
    displayHourlyForecast(data);
    displayWeeklyForecast(data);
    displayWeatherDetails(data);
}

// Display hourly forecast
function displayHourlyForecast(data) {
    const container = document.getElementById('hourlyContainer');
    container.innerHTML = '';

    data.list.slice(0, 8).forEach(item => {
        const time = new Date(item.dt * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        const icon = WEATHER_ICONS[item.weather[0].icon] || '🌡️';
        const temp = Math.round(item.main.temp);
        const rain = item.rain ? Math.round(item.rain['3h'] || 0) : 0;

        const hourly = document.createElement('div');
        hourly.className = 'hourly-item';
        hourly.innerHTML = `
            <div class="hourly-time">${time}</div>
            <div class="hourly-icon">${icon}</div>
            <div class="hourly-temp">${temp}°C</div>
            <div class="hourly-rain">💧 ${rain}mm</div>
        `;
        container.appendChild(hourly);
    });
}

// Display weekly forecast
function displayWeeklyForecast(data) {
    const container = document.getElementById('weeklyContainer');
    container.innerHTML = '';

    const dailyData = {};

    // Group data by day
    data.list.forEach(item => {
        const date = new Date(item.dt * 1000).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });

        if (!dailyData[date]) {
            dailyData[date] = {
                temps: [],
                icon: item.weather[0].icon,
                condition: item.weather[0].main
            };
        }
        dailyData[date].temps.push(item.main.temp);
    });

    // Display first 7 days
    Object.entries(dailyData).slice(0, 7).forEach(([date, data]) => {
        const high = Math.round(Math.max(...data.temps));
        const low = Math.round(Math.min(...data.temps));
        const icon = WEATHER_ICONS[data.icon] || '🌡️';

        const daily = document.createElement('div');
        daily.className = 'daily-item';
        daily.innerHTML = `
            <div class="daily-day">${date}</div>
            <div class="daily-icon">${icon}</div>
            <div class="daily-temps">
                <span class="daily-high">${high}°</span>
                <span class="daily-low">${low}°</span>
            </div>
            <div class="daily-condition">${data.condition}</div>
        `;
        container.appendChild(daily);
    });
}

// Display weather details
function displayWeatherDetails(data) {
    const container = document.getElementById('detailsContainer');
    const current = data.list[0];

    const details = [
        { label: 'UV Index', value: '🌞 Moderate' },
        { label: 'Visibility', value: `${(current.visibility / 1000).toFixed(1)} km` },
        { label: 'Dew Point', value: `${Math.round(current.main.temp - (100 - current.main.humidity) / 5)}°C` },
        { label: 'Wind Gust', value: `${(current.wind.gust || current.wind.speed).toFixed(1)} m/s` },
        { label: 'Feels Like', value: `${Math.round(current.main.feels_like)}°C` },
        { label: 'Rain Chance', value: `${Math.round((current.clouds.all))}%` }
    ];

    container.innerHTML = details.map(detail => `
        <div class="detail-card">
            <div class="detail-label">${detail.label}</div>
            <div class="detail-value">${detail.value}</div>
        </div>
    `).join('');
}

// Show error message
function showError(message) {
    const errorContainer = document.getElementById('errorMessage');
    errorContainer.textContent = message;
    errorContainer.classList.add('show');

    setTimeout(() => {
        errorContainer.classList.remove('show');
    }, 4000);
}
