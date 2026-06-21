const searchForm = document.getElementById('searchForm');
const locationInput = document.getElementById('location');
const positionSelect = document.getElementById('position');
const locationError = document.getElementById('locationError');
const statusDiv = document.getElementById('status');
const searchBtn = document.getElementById('searchBtn');
const tableBody = document.getElementById('jobsTableBody');
const jobCount = document.getElementById('jobCount');

searchForm.addEventListener('submit', event => {
    event.preventDefault();
    searchJobs();
});

locationInput.addEventListener('input', () => {
    if (locationInput.value.trim()) {
        clearLocationError();
    }
});

// Функція пошуку вакансій
async function searchJobs() {
    const location = locationInput.value.trim();
    const position = positionSelect.value.trim();

    if (!validateSearchForm(location)) {
        renderStatus('error', 'Please enter a location before searching.');
        clearResults();
        return;
    }
    
    setSearchButtonLoading(true);
    renderStatus('loading', `Searching for jobs in ${location}...`);
    renderLoadingState();
    
    try {
        // Викликаємо API сервера
        const response = await fetch(`/search?location=${encodeURIComponent(location)}&position=${encodeURIComponent(position)}`);
        
        const data = await response.json();

        if (!response.ok || data.success === false) {
            throw new Error(data.message || 'Network response was not ok');
        }

        const jobs = data.jobs || [];
        const count = data.count ?? jobs.length;
        const warnings = data.warnings || [];
        
        tableBody.innerHTML = '';
        
        if (jobs.length === 0) {
            renderEmptyState();
            renderStatus(warnings.length > 0 ? 'warning' : 'empty', 'No jobs found. Try adjusting your search.', warnings);
        } else {
            // Add each job to the table
            jobs.forEach(job => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td><strong>${escapeHtml(job.title)}</strong></td>
                    <td>${escapeHtml(job.company)}</td>
                    <td>${escapeHtml(job.location)}</td>
                    <td>
                        <a href="${escapeHtml(job.url)}" target="_blank" class="apply-btn">
                            View Job →
                        </a>
                    </td>
                `;
                tableBody.appendChild(row);
            });
            
            renderStatus(warnings.length > 0 ? 'warning' : 'success', `Found ${count} jobs in ${location}`, warnings);
        }
        
        jobCount.textContent = count;
        
    } catch (error) {
        console.error('Error:', error);
        const errorMessage = error.message || 'Error loading jobs. Please try again.';
        renderStatus('error', `Error: ${errorMessage}`);
        renderErrorState(errorMessage);
        jobCount.textContent = '0';
    } finally {
        setSearchButtonLoading(false);
    }
}

function renderStatus(type, message, warnings = []) {
    statusDiv.textContent = buildStatusMessage(message, warnings);
    statusDiv.className = `status ${type}`;
}

function renderLoadingState() {
    tableBody.innerHTML = `
        <tr>
            <td colspan="4" class="table-state loading-state">
                Loading jobs...
            </td>
        </tr>
    `;
}

function renderEmptyState() {
    tableBody.innerHTML = `
        <tr>
            <td colspan="4" class="table-state empty-state">
                No jobs found. Try a different location or position.
            </td>
        </tr>
    `;
}

function renderErrorState(message) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="4" class="table-state error-state">
                ${escapeHtml(message)}
            </td>
        </tr>
    `;
}

function clearResults() {
    tableBody.innerHTML = '';
    jobCount.textContent = '0';
}

function setSearchButtonLoading(isLoading) {
    searchBtn.disabled = isLoading;
    searchBtn.textContent = isLoading ? 'Searching...' : 'Search Jobs';
}

function validateSearchForm(location) {
    if (location) {
        clearLocationError();
        return true;
    }

    locationInput.classList.add('input-error');
    locationInput.setAttribute('aria-invalid', 'true');
    locationError.textContent = 'Location is required.';
    locationInput.focus();
    return false;
}

function clearLocationError() {
    locationInput.classList.remove('input-error');
    locationInput.removeAttribute('aria-invalid');
    locationError.textContent = '';
}

// Захист від XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function buildStatusMessage(message, warnings) {
    if (!warnings || warnings.length === 0) {
        return message;
    }
    return `${message} ${warnings.join(' ')}`;
}

// Автоматичний пошук при завантаженні сторінки (опціонально)
window.addEventListener('load', () => {
    console.log('Job Aggregator loaded');
});
