
    $(function () {
    const API_BASE = 'http://localhost:8081/api/v1';

    // ── CRITICAL FIX: Clear any stale login session on page load ──
    // Without this, after signup the sign-in page sees a valid token
    // and auto-redirects to dashboard before the user even signs in.
    ['rentify_token','rentify_role','rentify_auth','rentify_username',
    'rentify_fullName','rentify_email','rentify_userId','rentify_phone']
    .forEach(k => localStorage.removeItem(k));

    function showToast(msg, isError = false) {
    const $t = $('#toastBox');
    $t.text(msg).removeClass('error show').addClass('show' + (isError ? ' error' : ''));
    clearTimeout($t.data('timer'));
    $t.data('timer', setTimeout(() => $t.removeClass('show'), 4000));
}

    // Password strength meter
    $('#pw1').on('input', function () {
    const pw = $(this).val();
    const $bars = [$('#bar1'), $('#bar2'), $('#bar3')];
    const $label = $('#pwLabel');
    $bars.forEach(b => b.attr('class', 'pw-bar'));
    $label.attr('class', 'pw-label').text('');
    if (!pw) return;
    const score = (pw.length >= 8 ? 1 : 0) + (pw.length >= 12 ? 1 : 0)
    + (/[A-Z]/.test(pw) ? 1 : 0) + (/[0-9]/.test(pw) ? 1 : 0)
    + (/[^A-Za-z0-9]/.test(pw) ? 1 : 0);
    if (score <= 1) {
    $bars[0].addClass('weak'); $label.addClass('weak').text('// weak — add numbers & symbols');
} else if (score <= 3) {
    $bars[0].add($bars[1]).addClass('medium'); $label.addClass('medium').text('// fair — getting there!');
} else {
    $bars.forEach(b => b.addClass('strong')); $label.addClass('strong').text('// strong password!');
}
});

    // Toggle password visibility
    $(document).on('click', '.toggle-pw', function () {
    const $inp = $('#' + $(this).data('target'));
    $inp.attr('type', $inp.attr('type') === 'password' ? 'text' : 'password');
});

    // Remove error class on input
    $(document).on('input', 'input', function () { $(this).removeClass('error'); });

    // ── REDIRECT: always go to sign-in with ?redirect=1 ──
    // The ?redirect=1 flag tells sign-in.html to skip auto-redirect
    // so the user sees the login form and can sign in manually.
    function goToSignIn() {
    window.location.href = 'sign-in.html?redirect=1';
}

    $('#goSignIn').on('click', goToSignIn);
    $('#goLoginBtn').on('click', goToSignIn);

    function resetBtn() {
    $('#signupBtn').prop('disabled', false);
    $('#signupSpinner').hide();
    $('#signupIcon').show();
    $('#signupText').text("SIGN UP — IT'S FREE");
}

    // Form submission
    $('#signupForm').on('submit', function (e) {
    e.preventDefault();

    const firstName = $.trim($('#firstName').val());
    const lastName  = $.trim($('#lastName').val());
    const email     = $.trim($('#email').val());
    const phone     = $.trim($('#phone').val());
    const username  = $.trim($('#username').val());
    const pw1       = $('#pw1').val();
    const pw2       = $('#pw2').val();

    let valid = true;
    $.each(['firstName','lastName','email','phone','username','pw1','pw2'], function (_, id) {
    if (!$.trim($('#' + id).val())) { $('#' + id).addClass('error'); valid = false; }
});

    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    $('#email').addClass('error'); valid = false;
}
    if (pw1 && pw2 && pw1 !== pw2) {
    $('#pw2').addClass('error');
    showToast('❌ Passwords do not match.', true); return;
}
    if (!$('#agreeCheck').prop('checked')) {
    showToast('❌ Please agree to the Terms of Service.', true); return;
}
    if (!valid) {
    $('.error').first()[0]?.scrollIntoView({ behavior:'smooth', block:'center' });
    showToast('❌ Please fill all required fields.', true); return;
}

    const payload = {
    fullName: firstName + ' ' + lastName,
    username, email, phone, password: pw1, role: 'USER'
};

    $('#signupBtn').prop('disabled', true);
    $('#signupSpinner').show();
    $('#signupIcon').hide();
    $('#signupText').text('CREATING ACCOUNT...');

    $.ajax({
    url: API_BASE + '/auth/signup',
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(payload),
    success: function (result) {
    if (result && (result.code === 200 || result.code === undefined)) {
    $('#formView').fadeOut(300, () => $('#successView').fadeIn(400));
    showToast('✅ Account created! Redirecting to sign in...', false);
    setTimeout(goToSignIn, 3000);
} else {
    resetBtn();
    showToast('❌ ' + (result.message || 'Registration failed.'), true);
}
},
    error: function (xhr) {
    resetBtn();
    let msg = 'Something went wrong. Please try again.';
    if (xhr.responseJSON?.message) msg = xhr.responseJSON.message;
    else if (xhr.status === 409) msg = 'Username or email already exists.';
    else if (xhr.status === 400) msg = 'Invalid data. Check your inputs.';
    else if (xhr.status === 0)   msg = 'Cannot connect to server.';
    showToast('❌ ' + msg, true);
}
});
});
});
