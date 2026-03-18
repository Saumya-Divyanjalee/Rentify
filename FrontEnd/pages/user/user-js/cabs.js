/* ============================================================
   RENTIFY — user/cabs.js
   All API endpoints and field names matched to backend exactly:

   Backend entity fields (Cab.java):
     cabId, model, plateNumber, registrationNumber, capacity,
     year, status (CabStatus enum), airConditioning (AirConditioning enum),
     wifi (WifiStatus enum), category (CabCategory enum),
     pricePerDay, image (base64 string), date, description

   Backend CabBookingDTO fields:
     bookingId, customerName, email, phone, model,
     pickupDate, returnDate, pickupTime, returnTime,
     pickupLocation, returnLocation, rentalCategory,
     additionalInfo, totalPrice, status, bookingDate,
     cabId, userId

   Endpoints used:
     GET  /api/v1/cabs                        → all cabs (public)
     GET  /api/v1/cabs/available              → available cabs only
     GET  /api/v1/cabs/category/{category}    → filter by CabCategory
     POST /api/v1/cabBooking/save             → save booking (auth required)
   ============================================================ */

/* ── CONFIG ──────────────────────────────────────────────── */
const API_CABS     = 'http://localhost:8080/api/v1/cabs';
const API_BOOKING  = 'http://localhost:8080/api/v1/cabBooking/save';

/* ── STATE ───────────────────────────────────────────────── */
let allCabs      = [];    // full list from server
let currentCat   = 'ALL'; // active tab
let currentCabId = null;  // cab being booked

/* ============================================================
   INIT
   ============================================================ */
$(document).ready(function () {
    loadCabs();

    // Reveal animation
    const obs = new IntersectionObserver(entries => {
        entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('vis'); });
    }, { threshold: 0.1 });
    document.querySelectorAll('.reveal').forEach(el => obs.observe(el));
});

/* ============================================================
   LOAD ALL CABS  →  GET /api/v1/cabs
   Backend returns: List<Cab> (plain entity, not wrapped)
   ============================================================ */
function loadCabs() {
    showGridLoading();

    $.ajax({
        url: API_CABS,
        method: 'GET',
        success: function (data) {
            // Backend returns a plain List<Cab> (200 OK, no wrapper)
            allCabs = Array.isArray(data) ? data : [];
            updateTabCounts();
            renderGrid(allCabs);
        },
        error: function (xhr) {
            showGridError('Could not load vehicles. Please refresh the page.');
            console.error('Load cabs error:', xhr.status, xhr.responseText);
        }
    });
}

/* ============================================================
   UPDATE TAB COUNTS
   ============================================================ */
function updateTabCounts() {
    const cats = ['WEDDING', 'TRIP', 'EVENT', 'DAILY_USE'];
    const allCount = allCabs.length;

    $('#count-ALL').text(allCount + ' vehicle' + (allCount !== 1 ? 's' : ''));

    cats.forEach(cat => {
        const n = allCabs.filter(c => c.category === cat).length;
        $('#count-' + cat).text(n + ' vehicle' + (n !== 1 ? 's' : ''));
    });
}

/* ============================================================
   QUICK FILTER (booking strip dropdown)
   ============================================================ */
function quickFilter() {
    const cat = $('#quickFilterCat').val();

    if (cat) {
        // Switch tab
        const tabEl = document.querySelector('[data-cat="' + cat + '"]');
        if (tabEl) showCat(cat, tabEl);
    }

    // Scroll to grid
    document.querySelector('.cat-tabs').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

/* ============================================================
   TAB SWITCHER
   ============================================================ */
function showCat(cat, el) {
    currentCat = cat;

    // Update active tab style
    document.querySelectorAll('.cat-tab').forEach(t => t.classList.remove('active'));
    el.classList.add('active');

    // Filter cabs
    const filtered = cat === 'ALL'
        ? allCabs
        : allCabs.filter(c => c.category === cat);

    renderGrid(filtered);

    // Smooth scroll to grid
    window.scrollTo({
        top: document.querySelector('.cat-tabs').offsetTop - 80,
        behavior: 'smooth'
    });
}

/* ============================================================
   RENDER GRID
   ============================================================ */
function renderGrid(cabs) {
    if (cabs.length === 0) {
        $('#cabGrid').html(`
            <div class="empty-state">
                <div class="es-icon">🚗</div>
                <h3>No vehicles found</h3>
                <p>Try a different category or check back soon.</p>
            </div>
        `);
        return;
    }

    const html = cabs.map((c, i) => buildCard(c, i === 0)).join('');
    $('#cabGrid').html(html);
}

/* ============================================================
   BUILD A SINGLE VEHICLE CARD
   Uses exact Cab entity field names from backend
   ============================================================ */
function buildCard(c, featured) {
    // ── Status badge — matches Cab.CabStatus enum ──────────
    let statusClass = 'avail-n';
    let statusText  = '● Unavailable';
    if (c.status === 'AVAILABLE')    { statusClass = 'avail-y'; statusText = '● Available'; }
    else if (c.status === 'BOOKED')  { statusClass = 'avail-n'; statusText = '● Booked'; }
    else if (c.status === 'MAINTENANCE') { statusClass = 'avail-m'; statusText = '● Maintenance'; }

    // ── Feature tags from entity enums ─────────────────────
    const tags = [];
    if (c.airConditioning === 'YES') tags.push('AC');
    if (c.wifi === 'YES')            tags.push('WiFi');
    if (c.category)                  tags.push(c.category.replace('_', ' '));

    const tagHTML = tags.map(t => `<span class="v-tag">${t}</span>`).join('');

    // ── Image — backend stores as base64 LONGTEXT ───────────
    const imgHTML = c.image
        ? `<img src="${c.image}" alt="${esc(c.model)}" loading="lazy"
               onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">`
        : '';
    const placeholderStyle = c.image ? 'display:none' : 'display:flex';

    // ── onclick data (all field names from Cab entity) ──────
    const cabData = JSON.stringify(c).replace(/'/g, '&#39;');

    return `
        <div class="v-card${featured ? ' featured' : ''}" onclick='openVModal(${cabData})'>
            <div class="v-img">
                ${imgHTML}
                <div class="v-img-placeholder" style="${placeholderStyle};align-items:center;justify-content:center;width:100%;height:100%;font-size:64px">🚗</div>
                <span class="v-status ${statusClass}">${statusText}</span>
            </div>
            <div class="v-body">
                <h3>${esc(c.model)}</h3>
                <div class="v-sub">Plate: ${esc(c.plateNumber)} · Year: ${c.year}</div>
                <div class="v-tags">${tagHTML}</div>
                <div class="v-specs">
                    <div class="v-spec"><i class="fas fa-users"></i>${c.capacity} Seats</div>
                    <div class="v-spec"><i class="fas fa-snowflake"></i>${c.airConditioning === 'YES' ? 'AC' : 'No AC'}</div>
                    <div class="v-spec"><i class="fas fa-wifi"></i>${c.wifi === 'YES' ? 'WiFi' : 'No WiFi'}</div>
                </div>
                <div class="v-footer">
                    <div class="v-price">LKR ${c.pricePerDay.toLocaleString()}<span>/day</span></div>
                    <button class="btn-book" onclick="event.stopPropagation();openVModal(${cabData.replace(/"/g, '&quot;')})">
                        <i class="fas fa-car"></i>Book Now
                    </button>
                </div>
            </div>
        </div>
    `;
}

/* ============================================================
   OPEN VEHICLE MODAL
   c = full Cab entity object from backend
   ============================================================ */
function openVModal(c) {
    currentCabId = c.cabId;

    // ── Image ───────────────────────────────────────────────
    const imgEl = document.getElementById('vm-img');
    if (c.image) {
        imgEl.src = c.image;
        imgEl.style.display = 'block';
        document.getElementById('vm-placeholder').style.display = 'none';
    } else {
        imgEl.src = '';
        imgEl.style.display = 'none';
        document.getElementById('vm-placeholder').style.display = 'flex';
    }

    // ── Meta ────────────────────────────────────────────────
    document.getElementById('vm-name').textContent     = c.model + ' (' + c.plateNumber + ')';
    document.getElementById('vm-desc').textContent     = c.description || 'Premium cab for your journey.';
    document.getElementById('vm-seats').textContent    = c.capacity;
    document.getElementById('vm-year').textContent     = c.year;
    document.getElementById('vm-price').textContent    = 'LKR ' + c.pricePerDay.toLocaleString();

    // ── Badges ──────────────────────────────────────────────
    document.getElementById('vm-category').textContent = c.category ? c.category.replace('_', ' ') : '';
    document.getElementById('vm-ac').textContent       = c.airConditioning === 'YES' ? 'AC' : 'No AC';
    document.getElementById('vm-wifi').textContent     = c.wifi === 'YES' ? 'WiFi' : 'No WiFi';

    // ── Pre-fill booking form ───────────────────────────────
    document.getElementById('bk-cabId').value = c.cabId;

    // Get userId from localStorage (set during login)
    const storedUserId = localStorage.getItem('userId');
    document.getElementById('bk-userId').value = storedUserId || '';

    // Pre-fill model in case needed, set rentalCategory from cab's category
    const catSelect = document.getElementById('bk-rentalCategory');
    if (c.category) catSelect.value = c.category;

    // Pre-fill email/name if user is logged in
    const storedName  = localStorage.getItem('fullName');
    const storedEmail = localStorage.getItem('email');
    if (storedName)  document.getElementById('bk-name').value  = storedName;
    if (storedEmail) document.getElementById('bk-email').value = storedEmail;

    document.getElementById('vModal').classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeVModal(e) {
    if (e.target.id === 'vModal') closeModal();
}

function closeModal() {
    document.getElementById('vModal').classList.remove('show');
    document.body.style.overflow = '';
}

/* ============================================================
   SUBMIT BOOKING  →  POST /api/v1/cabBooking/save
   Body matches CabBookingDTO exactly
   ============================================================ */
function submitBooking() {
    // ── Validation ──────────────────────────────────────────
    const name        = $('#bk-name').val().trim();
    const email       = $('#bk-email').val().trim();
    const phone       = $('#bk-phone').val().trim();
    const pickupDate  = $('#bk-pickupDate').val();
    const returnDate  = $('#bk-returnDate').val();
    const pickupLoc   = $('#bk-pickup').val().trim();
    const returnLoc   = $('#bk-return').val().trim();
    const cabId       = $('#bk-cabId').val();
    const userId      = $('#bk-userId').val();

    if (!name || !email || !phone || !pickupDate || !returnDate || !pickupLoc || !returnLoc) {
        showToast('Please fill all required fields.', 'error');
        return;
    }
    if (!userId) {
        showToast('Please sign in before booking.', 'error');
        return;
    }
    if (new Date(returnDate) < new Date(pickupDate)) {
        showToast('Return date cannot be before pickup date.', 'error');
        return;
    }

    // ── Build payload — field names match CabBookingDTO exactly ─
    const payload = {
        customerName:    name,
        email:           email,
        phone:           phone,
        model:           document.getElementById('vm-name').textContent.split(' (')[0],
        pickupDate:      pickupDate,       // "YYYY-MM-DD"
        returnDate:      returnDate,
        pickupTime:      $('#bk-pickupTime').val() || null,
        returnTime:      $('#bk-returnTime').val() || null,
        pickupLocation:  pickupLoc,
        returnLocation:  returnLoc,
        rentalCategory:  $('#bk-rentalCategory').val(),  // CabCategory enum
        additionalInfo:  $('#bk-additionalInfo').val().trim() || null,
        totalPrice:      parseFloat($('#bk-totalPrice').val()) || 0,
        cabId:           parseInt(cabId),
        userId:          parseInt(userId)
    };

    const token = localStorage.getItem('jwtToken');
    if (!token) {
        showToast('Session expired. Please sign in again.', 'error');
        return;
    }

    // ── Disable button while submitting ────────────────────
    const btn = document.querySelector('.btn-book-submit');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting…';

    $.ajax({
        url: API_BOOKING,
        method: 'POST',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + token },
        data: JSON.stringify(payload),
        success: function () {
            closeModal();
            showToast('✅ Booking submitted! We will confirm shortly.', 'success');
            // Reset form
            $('#bk-name,#bk-email,#bk-phone,#bk-pickup,#bk-return,#bk-pickupDate,#bk-returnDate,#bk-pickupTime,#bk-returnTime,#bk-additionalInfo,#bk-totalPrice').val('');
        },
        error: function (xhr) {
            const msg = xhr.responseJSON?.message || xhr.responseText || 'Booking failed. Please try again.';
            showToast('❌ ' + msg, 'error');
        },
        complete: function () {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-check"></i> Confirm Booking';
        }
    });
}

/* ============================================================
   HELPERS
   ============================================================ */
function showGridLoading() {
    $('#cabGrid').html(`
        <div class="loading-state">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Loading vehicles…</p>
        </div>
    `);
}

function showGridError(msg) {
    $('#cabGrid').html(`
        <div class="empty-state">
            <div class="es-icon">⚠️</div>
            <h3>Could not load vehicles</h3>
            <p>${msg}</p>
        </div>
    `);
}

function showToast(msg, type) {
    const t = $(`<div class="toast ${type || ''}">${msg}</div>`);
    $('#toastBox').append(t);
    setTimeout(() => t.fadeOut(300, () => t.remove()), 4000);
}

// HTML escape helper
function esc(str) {
    return String(str || '')
        .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
        .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}