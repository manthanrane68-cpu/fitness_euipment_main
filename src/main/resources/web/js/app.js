/**
 * PulseFit Fitness Equipment Maintenance Portal - Frontend Controller
 * Complete CRUD, Role-Based State Machine & Dynamic Analytics
 */

// Application State
const state = {
    records: [],
    activeRole: 'STAFF', // STAFF | TECH | ADMIN
    filters: {
        query: '',
        status: 'ALL',
        priority: 'ALL',
        zone: 'ALL'
    },
    selectedRecord: null
};

// DOM Selectors
const elements = {
    // Header & Role
    systemStatusText: document.getElementById('systemStatusText'),
    currentRoleSelector: document.getElementById('currentRoleSelector'),
    btnOpenCreateModal: document.getElementById('btnOpenCreateModal'),
    btnRefreshData: document.getElementById('btnRefreshData'),
    btnResetSeed: document.getElementById('btnResetSeed'),

    // KPI Counters
    kpiTotalValue: document.getElementById('kpiTotalValue'),
    kpiOpenValue: document.getElementById('kpiOpenValue'),
    kpiInProgressValue: document.getElementById('kpiInProgressValue'),
    kpiCriticalValue: document.getElementById('kpiCriticalValue'),
    kpiCostValue: document.getElementById('kpiCostValue'),
    categoryTagsContainer: document.getElementById('categoryTagsContainer'),

    // Filters & Search
    searchInput: document.getElementById('searchInput'),
    filterStatus: document.getElementById('filterStatus'),
    filterPriority: document.getElementById('filterPriority'),
    filterZone: document.getElementById('filterZone'),
    btnClearFilters: document.getElementById('btnClearFilters'),

    // Records Table
    visibleRecordsCount: document.getElementById('visibleRecordsCount'),
    totalRecordsCount: document.getElementById('totalRecordsCount'),
    recordsTableBody: document.getElementById('recordsTableBody'),
    emptyStateContainer: document.getElementById('emptyStateContainer'),

    // Create Modal
    createRecordModal: document.getElementById('createRecordModal'),
    btnCloseCreateModal: document.getElementById('btnCloseCreateModal'),
    btnCancelCreate: document.getElementById('btnCancelCreate'),
    recordForm: document.getElementById('recordForm'),
    inputEquipmentId: document.getElementById('inputEquipmentId'),
    inputEquipmentName: document.getElementById('inputEquipmentName'),
    selectCategory: document.getElementById('selectCategory'),
    selectZone: document.getElementById('selectZone'),
    selectPriority: document.getElementById('selectPriority'),
    inputReportedBy: document.getElementById('inputReportedBy'),
    inputIssueDescription: document.getElementById('inputIssueDescription'),
    inputEstimatedCost: document.getElementById('inputEstimatedCost'),
    inputTechnician: document.getElementById('inputTechnician'),

    // Detail Drawer Modal
    detailModal: document.getElementById('detailModal'),
    btnCloseDetailModal: document.getElementById('btnCloseDetailModal'),
    detailTicketBadge: document.getElementById('detailTicketBadge'),
    detailEquipmentTitle: document.getElementById('detailEquipmentTitle'),
    detailBodyContent: document.getElementById('detailBodyContent'),

    // Toasts
    toastContainer: document.getElementById('toastContainer')
};

// ==========================================================================
// Initialization & Event Listeners
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    loadDashboard();
    loadRecords();
    checkHealthDiagnostics();
});

function initEventListeners() {
    // Role change
    elements.currentRoleSelector.addEventListener('change', (e) => {
        state.activeRole = e.target.value;
        showToast(`Switched active role to: ${getRoleDisplayName(state.activeRole)}`, 'info');
        renderRecords();
    });

    // Refresh & Reset
    elements.btnRefreshData.addEventListener('click', () => {
        loadDashboard();
        loadRecords();
        showToast('Telemetry and records refreshed', 'success');
    });

    elements.btnResetSeed.addEventListener('click', async () => {
        if (confirm('Reset database to default seed records?')) {
            try {
                const res = await fetch('/api/reset', { method: 'POST' });
                if (res.ok) {
                    showToast('Database reset to clean baseline', 'success');
                    loadDashboard();
                    loadRecords();
                }
            } catch (err) {
                showToast('Failed to reset database', 'error');
            }
        }
    });

    // Search & Filter listeners
    elements.searchInput.addEventListener('input', debounce((e) => {
        state.filters.query = e.target.value.trim();
        loadRecords();
    }, 250));

    elements.filterStatus.addEventListener('change', (e) => {
        state.filters.status = e.target.value;
        loadRecords();
    });

    elements.filterPriority.addEventListener('change', (e) => {
        state.filters.priority = e.target.value;
        loadRecords();
    });

    elements.filterZone.addEventListener('change', (e) => {
        state.filters.zone = e.target.value;
        loadRecords();
    });

    elements.btnClearFilters.addEventListener('click', () => {
        elements.searchInput.value = '';
        elements.filterStatus.value = 'ALL';
        elements.filterPriority.value = 'ALL';
        elements.filterZone.value = 'ALL';
        state.filters = { query: '', status: 'ALL', priority: 'ALL', zone: 'ALL' };
        loadRecords();
        showToast('All filters cleared', 'info');
    });

    // Modals
    elements.btnOpenCreateModal.addEventListener('click', () => openCreateModal());
    elements.btnCloseCreateModal.addEventListener('click', () => closeCreateModal());
    elements.btnCancelCreate.addEventListener('click', () => closeCreateModal());
    elements.btnCloseDetailModal.addEventListener('click', () => closeDetailModal());

    // Close on overlay click
    window.addEventListener('click', (e) => {
        if (e.target === elements.createRecordModal) closeCreateModal();
        if (e.target === elements.detailModal) closeDetailModal();
    });

    // Close on Escape key
    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeCreateModal();
            closeDetailModal();
        }
    });

    // Form Submit
    elements.recordForm.addEventListener('submit', handleRecordSubmit);

    // 1-Click Equipment Fault Presets
    document.querySelectorAll('.preset-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const preset = e.target.getAttribute('data-preset');
            applyEquipmentPreset(preset);
        });
    });
}

function applyEquipmentPreset(preset) {
    if (preset === 'treadmill') {
        elements.inputEquipmentId.value = 'TM-08';
        elements.inputEquipmentName.value = 'Matrix Commercial Treadmill T7xe';
        elements.selectCategory.value = 'Cardio';
        elements.selectZone.value = 'Zone A - Cardio Deck';
        elements.selectPriority.value = 'High';
        elements.inputIssueDescription.value = 'Running belt slips violently when exceeding 10 km/h; display shows motor error code E-04.';
        elements.inputEstimatedCost.value = '175.00';
    } else if (preset === 'cable') {
        elements.inputEquipmentId.value = 'CB-05';
        elements.inputEquipmentName.value = 'LifeFitness Cable Crossover Jungle';
        elements.selectCategory.value = 'Strength';
        elements.selectZone.value = 'Zone B - Strength Zone';
        elements.selectPriority.value = 'Critical';
        elements.inputIssueDescription.value = 'Upper steel cable sheath stripped; multiple strands frayed. Machine immediately locked out for safety.';
        elements.inputEstimatedCost.value = '90.00';
    } else if (preset === 'elliptical') {
        elements.inputEquipmentId.value = 'EB-04';
        elements.inputEquipmentName.value = 'Technogym Synchro Excite';
        elements.selectCategory.value = 'Cardio';
        elements.selectZone.value = 'Zone A - Cardio Deck';
        elements.selectPriority.value = 'Medium';
        elements.inputIssueDescription.value = 'Left pedal crank emits repetitive knocking noise during forward stride; magnetic brake sluggish.';
        elements.inputEstimatedCost.value = '65.00';
    } else if (preset === 'smith') {
        elements.inputEquipmentId.value = 'SM-02';
        elements.inputEquipmentName.value = 'Hammer Strength Olympic Smith Machine';
        elements.selectCategory.value = 'Strength';
        elements.selectZone.value = 'Zone B - Strength Zone';
        elements.selectPriority.value = 'High';
        elements.inputIssueDescription.value = 'Safety catch peg on right vertical guide rod is bent and fails to lock into bar catch notch.';
        elements.inputEstimatedCost.value = '120.00';
    }
    showToast(`Loaded preset template: ${preset.toUpperCase()}`, 'info');
}

// ==========================================================================
// API Operations
// ==========================================================================
async function loadRecords() {
    try {
        const queryParams = new URLSearchParams();
        if (state.filters.query) queryParams.append('query', state.filters.query);
        if (state.filters.status && state.filters.status !== 'ALL') queryParams.append('status', state.filters.status);
        if (state.filters.priority && state.filters.priority !== 'ALL') queryParams.append('priority', state.filters.priority);
        if (state.filters.zone && state.filters.zone !== 'ALL') queryParams.append('zone', state.filters.zone);

        const res = await fetch(`/api/records?${queryParams.toString()}`);
        if (!res.ok) throw new Error('Network error');
        state.records = await res.json();
        renderRecords();
    } catch (err) {
        console.error('Error loading records:', err);
        showToast('Unable to fetch maintenance records from server', 'error');
    }
}

async function loadDashboard() {
    try {
        const res = await fetch('/api/dashboard');
        if (!res.ok) throw new Error('Dashboard stats error');
        const stats = await res.json();
        renderDashboard(stats);
    } catch (err) {
        console.error('Error loading dashboard:', err);
    }
}

async function checkHealthDiagnostics() {
    try {
        const res = await fetch('/api/health');
        if (res.ok) {
            const data = await res.json();
            elements.systemStatusText.textContent = `Live: ${data.status} | Heap: ${data.heapMemoryMb}`;
        }
    } catch (err) {
        elements.systemStatusText.textContent = 'System: Offline';
    }
}

async function handleRecordSubmit(e) {
    e.preventDefault();

    const recordData = {
        equipmentId: elements.inputEquipmentId.value.trim(),
        equipmentName: elements.inputEquipmentName.value.trim(),
        category: elements.selectCategory.value,
        zone: elements.selectZone.value,
        priority: elements.selectPriority.value,
        reportedBy: elements.inputReportedBy.value.trim(),
        issueDescription: elements.inputIssueDescription.value.trim(),
        estimatedCost: parseFloat(elements.inputEstimatedCost.value) || 0.0,
        assignedTechnician: elements.inputTechnician.value.trim() || 'Unassigned',
        status: 'Reported'
    };

    try {
        const res = await fetch('/api/records', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(recordData)
        });

        if (res.ok) {
            const created = await res.json();
            showToast(`Incident logged successfully! Ticket ID: ${created.id}`, 'success');
            closeCreateModal();
            loadDashboard();
            loadRecords();
        } else {
            const error = await res.json();
            showToast(`Submission failed: ${error.error || 'Invalid input'}`, 'error');
        }
    } catch (err) {
        console.error('Error submitting record:', err);
        showToast('Network error while saving ticket', 'error');
    }
}

async function updateRecordStatus(id, newStatus, technician = null, notes = null, cost = null) {
    try {
        const payload = { status: newStatus };
        if (technician !== null) payload.assignedTechnician = technician;
        if (notes !== null) payload.resolutionNotes = notes;
        if (cost !== null) payload.estimatedCost = cost;

        const res = await fetch(`/api/records/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast(`Ticket ${id} transitioned to [${newStatus}]`, 'success');
            loadDashboard();
            loadRecords();
            closeDetailModal();
        } else {
            showToast('Failed to update status', 'error');
        }
    } catch (err) {
        showToast('Network error updating status', 'error');
    }
}

async function deleteRecord(id) {
    if (state.activeRole !== 'ADMIN') {
        showToast('Only Facility Admins have permission to delete tickets.', 'error');
        return;
    }

    if (confirm(`Permanently delete maintenance record ${id}?`)) {
        try {
            const res = await fetch(`/api/records/${id}`, { method: 'DELETE' });
            if (res.ok) {
                showToast(`Record ${id} deleted`, 'info');
                loadDashboard();
                loadRecords();
            } else {
                showToast('Failed to delete record', 'error');
            }
        } catch (err) {
            showToast('Network error deleting record', 'error');
        }
    }
}

// ==========================================================================
// Rendering Engine
// ==========================================================================
function renderDashboard(stats) {
    elements.kpiTotalValue.textContent = stats.totalRecords;
    elements.kpiOpenValue.textContent = stats.openIssues;
    elements.kpiInProgressValue.textContent = stats.inProgress;
    elements.kpiCriticalValue.textContent = stats.criticalCount;
    elements.kpiCostValue.textContent = `$${stats.totalCost.toFixed(2)}`;

    // Render Category Pills
    elements.categoryTagsContainer.innerHTML = '';
    for (const [cat, count] of Object.entries(stats.categoryBreakdown || {})) {
        const pill = document.createElement('span');
        pill.className = 'cat-pill';
        pill.innerHTML = `${cat}: <strong>${count}</strong>`;
        elements.categoryTagsContainer.appendChild(pill);
    }
}

function renderRecords() {
    const list = state.records;
    elements.visibleRecordsCount.textContent = list.length;
    elements.totalRecordsCount.textContent = list.length;

    if (list.length === 0) {
        elements.recordsTableBody.innerHTML = '';
        elements.emptyStateContainer.classList.remove('hidden');
        return;
    }

    elements.emptyStateContainer.classList.add('hidden');
    elements.recordsTableBody.innerHTML = list.map(r => `
        <tr>
            <td><span class="badge-ticket">${escapeHtml(r.id)}</span></td>
            <td>
                <strong>${escapeHtml(r.equipmentName)}</strong><br>
                <small style="color:var(--text-muted);">${escapeHtml(r.equipmentId)}</small>
            </td>
            <td>
                <span>${escapeHtml(r.zone)}</span><br>
                <small style="color:var(--accent-cyan); font-weight:600;">${escapeHtml(r.category)}</small>
            </td>
            <td style="max-width:240px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${escapeHtml(r.issueDescription)}">
                ${escapeHtml(r.issueDescription)}
            </td>
            <td>
                <span class="badge-priority ${escapeHtml(r.priority)}">${escapeHtml(r.priority)}</span>
            </td>
            <td>
                <span class="badge-status ${escapeHtml(r.status.replace(/\s+/g, '-'))}">
                    ${escapeHtml(r.status)}
                </span>
            </td>
            <td>${escapeHtml(r.assignedTechnician || 'Unassigned')}</td>
            <td style="font-family:var(--font-mono); font-weight:600;">$${(r.estimatedCost || 0).toFixed(2)}</td>
            <td>
                <div class="table-actions">
                    <button class="action-btn" onclick="openDetailModal('${r.id}')" title="View / Triage Ticket">
                        Inspect
                    </button>
                    ${state.activeRole === 'ADMIN' ? `
                        <button class="action-btn btn-delete" onclick="deleteRecord('${r.id}')" title="Delete Ticket">
                            &times;
                        </button>
                    ` : ''}
                </div>
            </td>
        </tr>
    `).join('');
}

// ==========================================================================
// Modal Handlers
// ==========================================================================
function openCreateModal() {
    elements.recordForm.reset();
    elements.inputEstimatedCost.value = '0.00';
    if (state.activeRole === 'STAFF') {
        elements.inputReportedBy.value = 'Marcus Vance (Floor Trainer)';
    } else if (state.activeRole === 'TECH') {
        elements.inputReportedBy.value = 'Dave Miller (Technician)';
    } else {
        elements.inputReportedBy.value = 'Facility Admin';
    }
    elements.createRecordModal.classList.remove('hidden');
}

function closeCreateModal() {
    elements.createRecordModal.classList.add('hidden');
}

function openDetailModal(id) {
    const record = state.records.find(r => r.id === id);
    if (!record) return;

    state.selectedRecord = record;
    elements.detailTicketBadge.textContent = record.id;
    elements.detailEquipmentTitle.textContent = `${record.equipmentName} (${record.equipmentId})`;

    const canEditStatus = state.activeRole === 'TECH' || state.activeRole === 'ADMIN';

    elements.detailBodyContent.innerHTML = `
        <div class="detail-grid">
            <div class="detail-item">
                <span class="detail-label">Status</span>
                <span class="detail-val"><span class="badge-status ${escapeHtml(record.status.replace(/\s+/g, '-'))}">${escapeHtml(record.status)}</span></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Priority Severity</span>
                <span class="detail-val"><span class="badge-priority ${escapeHtml(record.priority)}">${escapeHtml(record.priority)}</span></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Zone Location</span>
                <span class="detail-val">${escapeHtml(record.zone)}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Equipment Category</span>
                <span class="detail-val">${escapeHtml(record.category)}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Reported By</span>
                <span class="detail-val">${escapeHtml(record.reportedBy)}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Assigned Technician</span>
                <span class="detail-val">${escapeHtml(record.assignedTechnician || 'Unassigned')}</span>
            </div>
            <div class="detail-item" style="grid-column: span 2;">
                <span class="detail-label">Reported Defect Symptoms</span>
                <span class="detail-val" style="font-weight:400; margin-top:4px;">${escapeHtml(record.issueDescription)}</span>
            </div>
            <div class="detail-item" style="grid-column: span 2;">
                <span class="detail-label">Technician Resolution / Parts Notes</span>
                <span class="detail-val" style="font-weight:400; margin-top:4px; font-style:${record.resolutionNotes ? 'normal' : 'italic'}; color:${record.resolutionNotes ? 'var(--text-primary)' : 'var(--text-muted)'}">
                    ${escapeHtml(record.resolutionNotes || 'No technician notes recorded yet.')}
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Maintenance Spend</span>
                <span class="detail-val text-emerald">$${(record.estimatedCost || 0).toFixed(2)}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Last Updated</span>
                <span class="detail-val" style="font-size:0.75rem; color:var(--text-secondary);">${new Date(record.updatedAt).toLocaleString()}</span>
            </div>
        </div>

        ${canEditStatus ? `
            <div class="workflow-action-box">
                <h4 style="font-size:0.85rem; text-transform:uppercase; color:var(--accent-cyan); font-weight:700;">Role Workflow Action (${getRoleDisplayName(state.activeRole)})</h4>
                <div class="form-row">
                    <div class="form-group col-6">
                        <label>Transition Status To:</label>
                        <select id="modalTransitionStatus" class="filter-select" style="width:100%;">
                            <option value="Reported" ${record.status === 'Reported' ? 'selected' : ''}>Reported</option>
                            <option value="Triaged" ${record.status === 'Triaged' ? 'selected' : ''}>Triaged</option>
                            <option value="In Repair" ${record.status === 'In Repair' ? 'selected' : ''}>In Repair</option>
                            <option value="Awaiting Parts" ${record.status === 'Awaiting Parts' ? 'selected' : ''}>Awaiting Parts</option>
                            <option value="Resolved" ${record.status === 'Resolved' ? 'selected' : ''}>Resolved</option>
                            ${state.activeRole === 'ADMIN' ? `<option value="Closed" ${record.status === 'Closed' ? 'selected' : ''}>Closed</option>` : ''}
                        </select>
                    </div>
                    <div class="form-group col-6">
                        <label>Assigned Technician:</label>
                        <input type="text" id="modalTechInput" value="${escapeHtml(record.assignedTechnician || 'Dave Miller')}">
                    </div>
                </div>
                <div class="form-group">
                    <label>Parts & Repair Notes:</label>
                    <textarea id="modalNotesInput" rows="2" placeholder="Record parts replaced, torque specs, recalibration notes...">${escapeHtml(record.resolutionNotes || '')}</textarea>
                </div>
                <div class="form-group">
                    <label>Updated Maintenance Cost ($):</label>
                    <input type="number" id="modalCostInput" step="0.01" value="${record.estimatedCost || 0}">
                </div>
                <button class="btn btn-primary" onclick="submitStatusTransition('${record.id}')" style="margin-top:6px;">
                    Apply Workflow Transition
                </button>
            </div>
        ` : `
            <div style="padding:10px; background:rgba(255,255,255,0.03); border-radius:var(--radius-sm); font-size:0.78rem; color:var(--text-muted); text-align:center;">
                Note: Switch to <strong>Maintenance Technician</strong> or <strong>Facility Admin</strong> role to transition ticket workflow status.
            </div>
        `}
    `;

    elements.detailModal.classList.remove('hidden');
}

function closeDetailModal() {
    elements.detailModal.classList.add('hidden');
}

function submitStatusTransition(id) {
    const statusSelect = document.getElementById('modalTransitionStatus');
    const techInput = document.getElementById('modalTechInput');
    const notesInput = document.getElementById('modalNotesInput');
    const costInput = document.getElementById('modalCostInput');

    const newStatus = statusSelect.value;
    const newTech = techInput.value.trim();
    const newNotes = notesInput.value.trim();
    const newCost = parseFloat(costInput.value) || 0;

    updateRecordStatus(id, newStatus, newTech, newNotes, newCost);
}

// ==========================================================================
// Helper Utilities
// ==========================================================================
function getRoleDisplayName(role) {
    if (role === 'STAFF') return 'Gym Floor Staff / Trainer';
    if (role === 'TECH') return 'Maintenance Technician';
    if (role === 'ADMIN') return 'Facility Operations Admin';
    return role;
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(30px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
