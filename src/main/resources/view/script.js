// Функція пошуку вакансій
async function searchJobs() {
    const location = document.getElementById('location').value;
    const position = document.getElementById('position').value;
    const statusDiv = document.getElementById('status');
    const searchBtn = document.getElementById('searchBtn');
    const tableBody = document.getElementById('jobsTableBody');
    const jobCount = document.getElementById('jobCount');
    
    // Показати статус завантаження
    statusDiv.textContent = `Searching for jobs in ${location}...`;
    statusDiv.className = 'status loading';
    searchBtn.disabled = true;
    
    // Очистити таблицю
    tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Loading...</td></tr>';
    
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
        
        // Очищаємо таблицю
        tableBody.innerHTML = '';
        
        if (jobs.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="empty-state">
                        <p>No jobs found. Try different filters.</p>
                    </td>
                </tr>
            `;
            statusDiv.textContent = buildStatusMessage('No jobs found. Try adjusting your search.', warnings);
            statusDiv.className = warnings.length > 0 ? 'status warning' : 'status';
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
            
            statusDiv.textContent = buildStatusMessage(`Found ${count} jobs in ${location}`, warnings);
            statusDiv.className = warnings.length > 0 ? 'status warning' : 'status success';
        }
        
        jobCount.textContent = count;
        
    } catch (error) {
        console.error('Error:', error);
        const errorMessage = error.message || 'Error loading jobs. Please try again.';
        statusDiv.textContent = `Error: ${errorMessage}`;
        statusDiv.className = 'status';
        tableBody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align: center; color: #dc3545;">
                    Error: ${escapeHtml(errorMessage)}
                </td>
            </tr>
        `;
    } finally {
        searchBtn.disabled = false;
    }
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
