// shared-sidebar.js — inject sidebar into every admin page
function getSidebar(activePage) {
    const pages = [
        { id: 'dashboard', icon: '📊', label: 'Dashboard', href: 'admin-dashboard.html' },
        { id: 'bookings',  icon: '📅', label: 'Bookings',  href: 'bookings.html' },
        { id: 'users',     icon: '👥', label: 'Users',     href: 'users.html' },
        { id: 'cabs',      icon: '🚗', label: 'Cabs',      href: 'user-vehicle.html' },
        { id: 'drivers',   icon: '🧑‍✈️', label: 'Drivers',   href: 'drivers.html' },
        { id: 'payment',   icon: '💳', label: 'Payments',  href: 'payment.html' },
        { id: 'settings',  icon: '⚙️', label: 'Settings',  href: 'settings.html' },
    ];

    const navItems = pages.map(p => `
    <li class="nav-item">
      <a href="${p.href}" class="${activePage === p.id ? 'active' : ''}">
        <span class="nav-icon">${p.icon}</span>
        <span>${p.label}</span>
      </a>
    </li>`).join('');

    return `
  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="brand-icon">R</div>
      <div class="brand-text">Rent<span>ify</span></div>
    </div>
    <div class="sidebar-section-label">Main Menu</div>
    <ul class="nav-menu">${navItems}</ul>
    <div class="sidebar-footer">
      <div class="admin-info">
        <div class="admin-avatar">A</div>
        <div>
          <div class="admin-name">Admin</div>
          <div class="admin-role">Super Admin</div>
        </div>
      </div>
      <button class="btn-logout" onclick="doLogout()">🚪 Logout</button>
    </div>
  </aside>`;
}

function doLogout() {
    if(confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = 'sign-in.html';
    }
}

function showToast(msg, type='success') {
    const t = document.getElementById('toast');
    t.textContent = (type==='success'?'✅ ':'❌ ') + msg;
    t.className = 'show ' + type;
    clearTimeout(t._t);
    t._t = setTimeout(()=>{ t.className=''; }, 3200);
}