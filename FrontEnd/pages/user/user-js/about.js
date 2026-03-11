// Counter animation
    function animateCounters(){
    document.querySelectorAll('.stat-num').forEach(el=>{
        const target=parseInt(el.dataset.count);
        let cur=0;
        const step=Math.max(1,Math.floor(target/60));
        const timer=setInterval(()=>{
            cur=Math.min(cur+step,target);
            el.textContent=cur.toLocaleString()+(target>=98?'%':'');
            if(cur>=target)clearInterval(timer);
        },25);
    });
}
    setTimeout(animateCounters,600);

    // Scroll reveal
    const observer=new IntersectionObserver((entries)=>{
    entries.forEach(e=>{if(e.isIntersecting)e.target.classList.add('visible');});
},{threshold:0.12});
    document.querySelectorAll('.reveal').forEach(el=>observer.observe(el));

    // Rating widget
    let selectedRating=0;
    const ratingLabels=['','Awful 😞','Poor 😕','Okay 🙂','Good 😊','Excellent! 🤩'];
    const stars=document.querySelectorAll('#starSelector .s');
    stars.forEach(s=>{
    s.addEventListener('mouseenter',()=>{
        const v=+s.dataset.val;
        stars.forEach(st=>st.classList.toggle('active',+st.dataset.val<=v));
    });
    s.addEventListener('mouseleave',()=>{
    stars.forEach(st=>st.classList.toggle('active',+st.dataset.val<=selectedRating));
});
    s.addEventListener('click',()=>{
    selectedRating=+s.dataset.val;
    stars.forEach(st=>st.classList.toggle('active',+st.dataset.val<=selectedRating));
    document.getElementById('ratingMsg').textContent='You rated: '+ratingLabels[selectedRating];
});
});

    // Comment star selector
    let commentRating=5;
    const cStars=document.querySelectorAll('#commentStarSelector .s');
    cStars.forEach(s=>{
    s.addEventListener('mouseenter',()=>{const v=+s.dataset.val;cStars.forEach(st=>st.classList.toggle('active',+st.dataset.val<=v));});
    s.addEventListener('mouseleave',()=>{cStars.forEach(st=>st.classList.toggle('active',+st.dataset.val<=commentRating));});
    s.addEventListener('click',()=>{commentRating=+s.dataset.val;cStars.forEach(st=>st.classList.toggle('active',+st.dataset.val<=commentRating));});
});
    cStars.forEach(st=>st.classList.add('active'));

    // Add comment
    function escapeHtml(t){return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');}
    function addComment(){
    const name=document.getElementById('cName').value.trim();
    const text=document.getElementById('cText').value.trim();
    const service=document.getElementById('cService').value;
    if(!name||!text){alert('Please fill in your name and message!');return;}
    const initials=name.split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase();
    const starsHtml='★'.repeat(commentRating)+'☆'.repeat(5-commentRating);
    const item=document.createElement('div');
    item.className='comment-item';
    item.innerHTML=`<div class="comment-avatar">${initials}</div><div class="comment-body"><h6>${escapeHtml(name)}</h6><div class="c-meta">${escapeHtml(service)} · Just now</div><p>${escapeHtml(text)}</p><div class="c-stars">${starsHtml}</div></div>`;
    const feed=document.getElementById('commentFeed');
    feed.insertBefore(item,feed.firstChild);
    document.getElementById('cName').value='';
    document.getElementById('cText').value='';
}
