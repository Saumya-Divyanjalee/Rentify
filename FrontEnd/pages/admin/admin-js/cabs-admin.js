/* ============================================================
   RENTIFY — admin/admin-cabs.js

   Backend endpoints used (CabController.java):
     GET    /api/v1/cabs              → List<Cab>  (public)
     GET    /api/v1/cabs/{id}         → CabDTO     (public)
     POST   /api/v1/cabs              → multipart/form-data  (ADMIN/OWNER)
     PUT    /api/v1/cabs/{id}         → multipart/form-data  (ADMIN/OWNER)
     DELETE /api/v1/cabs/{id}         → void                 (ADMIN/OWNER)

   All @RequestPart field names from CabController:
     model, plateNumber, registrationNumber, capacity, year,
     status, airConditioning, wifi, category, pricePerDay,
     description, image (MultipartFile)

   Cab entity enum values:
     CabStatus:       AVAILABLE | BOOKED | MAINTENANCE | UNAVAILABLE
     AirConditioning: YES | NO
     WifiStatus:      YES | NO
     CabCategory:     WEDDING | TRIP | EVENT | DAILY_USE
   ============================================================ */

const API = 'http://localhost:8080/api/v1/cabs';

let allCabs    = [];
let editId     = null;
let imageFile  = null;     // File object for new/updated image
let deleteId   = null;

/* ── INIT ─────────────────────────────────────────────────── */
$(document).ready(function () {
    // Check admin auth
    const token = localStorage.getItem('jwtToken');
    const role  = localStorage.getItem('role');
    if (!token || (role !== 'ADMIN' && role !== 'OWNER')) {
        window.location.href = '../sign-in.html';
        return;
    }
    loadCabs();
});

/* ── LOAD ALL CABS  →  GET /api/v1/cabs ─────────────────── */
function loadCabs() {
    showLoading(true);

    $.ajax({
        url: API,
        method: 'GET',
        // No auth needed — public endpoint per CabController
        success: function (data) {
            // Backend returns plain List<Cab> (not wrapped)
            allCabs = Array.isArray(data) ? data : [];
            updateStats();
            applyFilters();
            showLoading(false);
        },
        error: function (xhr) {
            showLoading(false);
            showToast('Failed to load cabs: ' + (xhr.responseText || 'Server error'), 'error');
        }
    });
}

/* ── STATS ───────────────────────────────────────────────── */
function updateStats() {
    $('#st-total').text(allCabs.length);
    $('#st-avail').text(allCabs.filter(c => c.status === 'AVAILABLE').length);
    $('#st-booked').text(allCabs.filter(c => c.status === 'BOOKED').length);
    $('#st-maint').text(allCabs.filter(c => c.status === 'MAINTENANCE').length);
}

/* ── FILTER & RENDER ─────────────────────────────────────── */
function applyFilters() {
    const q  = $('#searchInput').val().toLowerCase();
    const fc = $('#filterCat').val();
    const fs = $('#filterStatus').val();

    const filtered = allCabs.filter(c => {
        const matchQ = !q || (c.model + c.plateNumber + c.registrationNumber).toLowerCase().includes(q);
        const matchC = !fc || c.category === fc;
        const matchS = !fs || c.status === fs;
        return matchQ && matchC && matchS;
    });

    renderCards(filtered);
    renderTable(filtered);
}

/* ── RENDER CARDS ────────────────────────────────────────── */
function renderCards(cabs) {
    if (cabs.length === 0) {
        $('#cardView').html('<div class="empty-state"><div class="es-icon">🚗</div><h3>No cabs found</h3><p>Try adjusting your filters or add a new cab.</p></div>');
        return;
    }

    const html = cabs.map(c => {
        const badge = statusBadge(c.status);
        const tags  = buildTags(c);
        const imgEl = c.image
            ? `<img src="${c.image}" alt="${esc(c.model)}" onerror="this.style.display='none'">`
            : '🚗';

        return `
        <div class="cab-card">
            <div class="cab-img-wrap">${imgEl}</div>
            <div class="cab-body">
                <div class="cab-name">${esc(c.model)}</div>
                <div class="cab-meta">
                    Plate: ${esc(c.plateNumber)} &nbsp;·&nbsp;
                    Reg: ${esc(c.registrationNumber)} &nbsp;·&nbsp;
                    ${c.capacity} seats &nbsp;·&nbsp; ${c.year}
                </div>
                <div class="cab-tags">${tags}</div>
                ${badge}
                <div class="cab-footer">
                    <span style="font-weight:700;color:var(--green);font-family:'Syne',sans-serif">
                        LKR ${Number(c.pricePerDay).toLocaleString()}/day
                    </span>
                    <div style="display:flex;gap:6px">
                        <button class="btn btn-outline btn-sm" onclick="openEditModal(${c.cabId})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="confirmDelete(${c.cabId},'${esc(c.model)}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');

    $('#cardView').html(html);
}

/* ── RENDER TABLE ────────────────────────────────────────── */
function renderTable(cabs) {
    if (cabs.length === 0) {
        $('#cabTable').html('<tr><td colspan="13" style="text-align:center;color:var(--muted);padding:40px">No cabs found</td></tr>');
        return;
    }

    const html = cabs.map(c => {
        const imgCell = c.image
            ? `<div class="cab-thumb"><img src="${c.image}" alt="" onerror="this.parentNode.textContent='🚗'"></div>`
            : '<div class="cab-thumb">🚗</div>';

        return `
        <tr>
            <td><code style="font-family:'Roboto Mono',monospace;font-size:11px;background:var(--bg);padding:2px 6px;border-radius:4px">#${c.cabId}</code></td>
            <td>${imgCell}</td>
            <td><strong>${esc(c.model)}</strong></td>
            <td><code style="font-size:12px">${esc(c.plateNumber)}</code></td>
            <td style="font-size:12px">${esc(c.registrationNumber)}</td>
            <td>${c.category || '—'}</td>
            <td>${c.capacity}</td>
            <td>${c.year}</td>
            <td style="font-weight:700;color:var(--green)">LKR ${Number(c.pricePerDay).toLocaleString()}</td>
            <td>${c.airConditioning === 'YES' ? '✅' : '❌'}</td>
            <td>${c.wifi === 'YES' ? '✅' : '❌'}</td>
            <td>${statusBadge(c.status)}</td>
            <td>
                <div style="display:flex;gap:6px">
                    <button class="btn btn-outline btn-sm" onclick="openEditModal(${c.cabId})"><i class="fas fa-edit"></i> Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="confirmDelete(${c.cabId},'${esc(c.model)}')"><i class="fas fa-trash"></i></button>
                </div>
            </td>
        </tr>`;
    }).join('');

    $('#cabTable').html(html);
}

/* ── VIEW TOGGLE ─────────────────────────────────────────── */
function setView(v) {
    $('#cardView').toggle(v === 'card');
    $('#tableView').toggle(v === 'table');
    $('#vCard').toggleClass('active', v === 'card');
    $('#vTable').toggleClass('active', v === 'table');
}

/* ── OPEN ADD MODAL ──────────────────────────────────────── */
function openModal() {
    editId = null;
    resetForm();
    $('#modalTitle').text('Add New Cab');
    $('#saveBtnText').text('Save Cab');
    $('#modalOverlay').addClass('open');
}

/* ── OPEN EDIT MODAL  →  GET /api/v1/cabs/{id} ─────────── */
function openEditModal(id) {
    showLoading(true);

    $.ajax({
        url: API + '/' + id,
        method: 'GET',
        success: function (data) {
            showLoading(false);

            // Backend returns CabDTO directly (not wrapped in APIResponse)
            const c = data;
            editId = id;

            // Populate form with exact CabDTO field names
            $('#f-model').val(c.model || '');
            $('#f-plateNumber').val(c.plateNumber || '');
            $('#f-registrationNumber').val(c.registrationNumber || '');
            $('#f-capacity').val(c.capacity || '');
            $('#f-year').val(c.year || '');
            $('#f-pricePerDay').val(c.pricePerDay || '');
            $('#f-status').val(c.status || 'AVAILABLE');
            $('#f-category').val(c.category || 'TRIP');
            $('#f-airConditioning').val(c.airConditioning || 'YES');
            $('#f-wifi').val(c.wifi || 'YES');
            $('#f-description').val(c.description || '');

            // Show existing image preview
            if (c.image) {
                $('#imgPreview').attr('src', c.image);
                $('#previewWrap').show();
                $('#uploadZone').hide();
            } else {
                removeImage();
            }

            $('#modalTitle').text('Edit Cab');
            $('#saveBtnText').text('Update Cab');
            $('#modalOverlay').addClass('open');
        },
        error: function (xhr) {
            showLoading(false);
            showToast('Failed to load cab details: ' + (xhr.responseText || 'Error'), 'error');
        }
    });
}

function closeModal() {
    $('#modalOverlay').removeClass('open');
    resetForm();
}

function resetForm() {
    $('#f-model,#f-plateNumber,#f-registrationNumber,#f-capacity,#f-year,#f-pricePerDay,#f-description').val('');
    $('#f-status').val('AVAILABLE');
    $('#f-category').val('TRIP');
    $('#f-airConditioning').val('YES');
    $('#f-wifi').val('YES');
    imageFile = null;
    removeImage();
    editId = null;
}

/* ── SAVE / UPDATE  →  POST or PUT /api/v1/cabs ─────────── */
/*
   Backend CabController uses multipart/form-data with @RequestPart.
   Each field is sent as a separate part.
   The image is sent as a MultipartFile.
   PicEncoder.generatePicture() on the backend converts it to base64.
*/
function saveCab() {
    // ── Validation ──────────────────────────────────────────
    const model  = $('#f-model').val().trim();
    const plate  = $('#f-plateNumber').val().trim();
    const reg    = $('#f-registrationNumber').val().trim();
    const cap    = $('#f-capacity').val();
    const year   = $('#f-year').val();
    const price  = $('#f-pricePerDay').val();

    if (!model || !plate || !reg || !cap || !year || !price) {
        showToast('Please fill all required fields.', 'error');
        return;
    }

    // Image is required for new cab, optional for edit
    if (!editId && !imageFile) {
        showToast('Please upload a cab image.', 'error');
        return;
    }

    // ── Build FormData — field names match @RequestPart exactly ─
    const fd = new FormData();
    fd.append('model',              model);
    fd.append('plateNumber',        plate.toUpperCase());
    fd.append('registrationNumber', reg);
    fd.append('capacity',           cap);
    fd.append('year',               year);
    fd.append('status',             $('#f-status').val());           // CabStatus enum
    fd.append('airConditioning',    $('#f-airConditioning').val());  // AirConditioning enum
    fd.append('wifi',               $('#f-wifi').val());             // WifiStatus enum
    fd.append('category',           $('#f-category').val());         // CabCategory enum
    fd.append('pricePerDay',        price);
    fd.append('description',        $('#f-description').val().trim());

    if (imageFile) {
        fd.append('image', imageFile);  // MultipartFile
    } else if (editId) {
        // For edit without new image, send empty file blob so @RequestPart doesn't fail
        fd.append('image', new Blob([]), 'unchanged.jpg');
    }

    const token = localStorage.getItem('jwtToken');
    const isEdit = editId !== null;

    showLoading(true);
    $('#saveBtn').prop('disabled', true);

    $.ajax({
        url:         isEdit ? (API + '/' + editId) : API,
        method:      isEdit ? 'PUT' : 'POST',
        headers:     { 'Authorization': 'Bearer ' + token },
        data:        fd,
        processData: false,   // must be false for FormData
        contentType: false,   // must be false for FormData
        success: function () {
            showLoading(false);
            $('#saveBtn').prop('disabled', false);
            closeModal();
            showToast(isEdit ? '✅ Cab updated successfully!' : '✅ Cab added successfully!', 'success');
            loadCabs();
        },
        error: function (xhr) {
            showLoading(false);
            $('#saveBtn').prop('disabled', false);
            const msg = xhr.responseJSON?.message || xhr.responseText || 'Operation failed';
            showToast('❌ ' + msg, 'error');
        }
    });
}

/* ── DELETE  →  DELETE /api/v1/cabs/{id} ────────────────── */
function confirmDelete(id, name) {
    deleteId = id;
    $('#confirmMsg').text('Delete "' + name + '"? This will remove it from the user site immediately.');
    $('#confirmOverlay').addClass('open');

    $('#confirmDeleteBtn').off('click').on('click', function () {
        closeConfirm();
        doDelete(id);
    });
}

function closeConfirm() {
    $('#confirmOverlay').removeClass('open');
    deleteId = null;
}

function doDelete(id) {
    const token = localStorage.getItem('jwtToken');
    showLoading(true);

    $.ajax({
        url:     API + '/' + id,
        method:  'DELETE',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function () {
            showLoading(false);
            showToast('🗑️ Cab deleted successfully.', 'error');
            loadCabs();
        },
        error: function (xhr) {
            showLoading(false);
            showToast('Failed to delete: ' + (xhr.responseText || 'Error'), 'error');
        }
    });
}

/* ── IMAGE HANDLING ──────────────────────────────────────── */
function handleImageSelect(input) {
    const file = input.files[0];
    if (!file) return;
    if (file.size > 10 * 1024 * 1024) {
        showToast('Image must be under 10MB.', 'error');
        return;
    }
    setImageFile(file);
}

function handleDrop(e) {
    e.preventDefault();
    $('#uploadZone').removeClass('drag');
    const file = e.dataTransfer.files[0];
    if (file && file.type.startsWith('image/')) setImageFile(file);
}

function setImageFile(file) {
    imageFile = file;
    const reader = new FileReader();
    reader.onload = function (ev) {
        $('#imgPreview').attr('src', ev.target.result);
        $('#previewWrap').show();
        $('#uploadZone').hide();
    };
    reader.readAsDataURL(file);
}

function removeImage() {
    imageFile = null;
    $('#f-image').val('');
    $('#imgPreview').attr('src', '');
    $('#previewWrap').hide();
    $('#uploadZone').show();
}

/* ── AUTH ────────────────────────────────────────────────── */
function logout() {
    localStorage.clear();
    window.location.href = '../sign-in.html';
}

/* ── HELPERS ─────────────────────────────────────────────── */
function statusBadge(status) {
    const map = {
        'AVAILABLE':   'badge-green',
        'BOOKED':      'badge-yellow',
        'MAINTENANCE': 'badge-red',
        'UNAVAILABLE': 'badge-gray'
    };
    const cls = map[status] || 'badge-gray';
    return `<span class="badge ${cls}">${status || '—'}</span>`;
}

function buildTags(c) {
    const tags = [];
    if (c.category)                  tags.push(c.category.replace('_', ' '));
    if (c.airConditioning === 'YES') tags.push('AC');
    if (c.wifi === 'YES')            tags.push('WiFi');
    return tags.map(t => `<span class="cab-tag">${t}</span>`).join('');
}

function showLoading(v) { $('#loadingOverlay').toggleClass('show', v); }

function showToast(msg, type) {
    const t = $(`<div class="toast-item ${type || ''}">${msg}</div>`);
    $('#toast').append(t);
    setTimeout(() => t.fadeOut(300, () => t.remove()), 3500);
}

function esc(str) {
    return String(str || '')
        .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
        .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}

// Close modal on outside click
$('#modalOverlay').on('click', function (e) {
    if ($(e.target).is('#modalOverlay')) closeModal();
});