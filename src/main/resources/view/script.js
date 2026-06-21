const searchForm = document.getElementById('searchForm');
const locationInput = document.getElementById('location');
const positionSelect = document.getElementById('position');
const sortSelect = document.getElementById('sort');
const workTypeSelect = document.getElementById('workType');
const senioritySelect = document.getElementById('seniority');
const minimumSalarySelect = document.getElementById('minimumSalary');
const postedWithinDaysSelect = document.getElementById('postedWithinDays');
const locationError = document.getElementById('locationError');
const statusDiv = document.getElementById('status');
const searchBtn = document.getElementById('searchBtn');
const jobsList = document.getElementById('jobsList');
const jobCount = document.getElementById('jobCount');
const visibleJobsSummary = document.getElementById('visibleJobsSummary');
const loadMoreBtn = document.getElementById('loadMoreBtn');
const JOBS_PER_PAGE = 15;
let allJobs = [];
let visibleJobCount = 0;

searchForm.addEventListener('submit', event => {
    event.preventDefault();
    searchJobs();
});

locationInput.addEventListener('input', () => {
    if (locationInput.value.trim()) {
        clearLocationError();
    }
});

loadMoreBtn.addEventListener('click', () => {
    visibleJobCount += JOBS_PER_PAGE;
    renderVisibleJobs();
});

// Функція пошуку вакансій
async function searchJobs() {
    const location = locationInput.value.trim();
    const position = positionSelect.value.trim();
    const sort = sortSelect.value;
    const workType = workTypeSelect.value;
    const seniority = senioritySelect.value;
    const minimumSalary = minimumSalarySelect.value;
    const postedWithinDays = postedWithinDaysSelect.value;

    if (!validateSearchForm(location)) {
        renderStatus('error', 'Please enter a location before searching.');
        clearResults();
        return;
    }
    
    setSearchButtonLoading(true);
    renderStatus('loading', `Searching for jobs in ${location}...`);
    renderLoadingState();
    
    try {
        const params = new URLSearchParams({
            location,
            position,
            sort,
            workType,
            seniority,
            minimumSalary,
            postedWithinDays
        });
        const response = await fetch(`/search?${params.toString()}`);
        
        const data = await response.json();

        if (!response.ok || data.success === false) {
            throw new Error(data.message || 'Network response was not ok');
        }

        allJobs = data.jobs || [];
        const count = allJobs.length;
        const warnings = data.warnings || [];
        
        jobsList.innerHTML = '';
        
        if (allJobs.length === 0) {
            renderEmptyState();
            renderStatus(warnings.length > 0 ? 'warning' : 'empty', 'No jobs found. Try adjusting your search.', warnings);
        } else {
            visibleJobCount = JOBS_PER_PAGE;
            renderVisibleJobs();
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
    loadMoreBtn.hidden = true;
    jobsList.innerHTML = `
        <div class="results-state loading-state">
            Loading jobs...
        </div>
    `;
}

function renderEmptyState() {
    loadMoreBtn.hidden = true;
    jobsList.innerHTML = `
        <div class="results-state empty-state">
            No jobs found. Try a different location or position.
        </div>
    `;
}

function renderErrorState(message) {
    loadMoreBtn.hidden = true;
    jobsList.innerHTML = `
        <div class="results-state error-state">
            ${escapeHtml(message)}
        </div>
    `;
}

function clearResults() {
    jobsList.innerHTML = '';
    jobCount.textContent = '0';
    allJobs = [];
    visibleJobCount = 0;
    updateLoadMoreButton();
}

function setSearchButtonLoading(isLoading) {
    searchBtn.disabled = isLoading;
    searchBtn.textContent = isLoading ? 'Searching...' : 'Search Jobs';
}

function renderVisibleJobs() {
    jobsList.innerHTML = '';
    allJobs
        .slice(0, visibleJobCount)
        .forEach(job => jobsList.appendChild(createJobCard(job)));
    updateLoadMoreButton();
}

function updateLoadMoreButton() {
    const displayedJobs = Math.min(visibleJobCount, allJobs.length);
    const hasJobs = allJobs.length > 0;
    const hasMoreJobs = displayedJobs < allJobs.length;

    visibleJobsSummary.hidden = !hasJobs;
    if (hasJobs) {
        visibleJobsSummary.textContent = `Showing ${displayedJobs} of ${allJobs.length} jobs`;
    }

    loadMoreBtn.hidden = !hasMoreJobs;
    if (hasMoreJobs) {
        const remainingJobs = allJobs.length - displayedJobs;
        const nextBatchSize = Math.min(JOBS_PER_PAGE, remainingJobs);
        loadMoreBtn.textContent = `Load ${nextBatchSize} More`;
    }
}

function createJobCard(job) {
    const card = document.createElement('article');
    card.className = 'job-card';

    const title = job.title || 'Untitled role';
    const company = job.company || 'Company not listed';
    const location = job.location || 'Location not listed';
    const source = job.source || job.website || 'Unknown source';
    const url = job.url || '#';
    const salary = job.salary || '';
    const salaryHtml = salary
        ? `<span class="job-salary">${escapeHtml(salary)}</span>`
        : '';

    card.innerHTML = `
        <div class="job-card-header">
            <div>
                <h3 class="job-title">${escapeHtml(title)}</h3>
                <p class="job-company">${escapeHtml(company)}</p>
            </div>
            <span class="source-badge">${escapeHtml(source)}</span>
        </div>
        <div class="job-meta">
            <span>${escapeHtml(location)}</span>
            ${salaryHtml}
        </div>
        <div class="job-card-actions">
            <a href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer" class="apply-btn">
                View Job
            </a>
        </div>
    `;

    return card;
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
