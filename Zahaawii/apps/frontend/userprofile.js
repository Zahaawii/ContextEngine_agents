const urlParameter = new URLSearchParams(location.search);
const profileUsername = urlParameter.get("id");
const form = document.querySelector(".login");
const navCenter = document.querySelector(".nav-center");
const userPanel = document.querySelector(".user-panel");
const avatarLink = document.querySelector(".avatar-link");
const avatarImg = document.querySelector(".avatar");
const popupEl = document.querySelector('#popup');
const formEl = document.querySelector('.createblogpost');
const openBtn = document.querySelector('[data-open="post"]');
const closeBtn = popupEl?.querySelector('[data-close]');
const submitBtn = formEl?.querySelector('[type="submit"]');
const logoutBtn = document.querySelector(".logout");
const userProfileBlogsContainer = document.getElementById('userprofilecontainer');
const userProfileInfo = document.getElementById('userprofileinfo');
const createBlogPostBtn = document.querySelector('.btn');

const deleteCommentsUrl = "/api/v1/comments/delete/";
const deleteBlogUrl = '/api/v1/blog/deletepost/';
const loginUrl = "/api/v1/users/auth/login";
const saveblogUrl = '/api/v1/blog/saveblogpost';
const addCommentsUrl = "/api/v1/comments/addcomment";
const date = new Date().toISOString().slice(0, 10);

let currentUser = null;

function logProfile(message, details) {
    if (details === undefined) {
        console.log(`[profile] ${message}`);
        return;
    }
    console.log(`[profile] ${message}`, details);
}

function isAuthenticated() {
    return Boolean(currentUser?.username);
}

function getUsernameByToken() {
    return currentUser?.username || null;
}

async function renderAfterAuth(forceRefresh = false) {
    currentUser = await window.authClient.getCurrentUser(forceRefresh);
    const isLoggedIn = isAuthenticated();

    if (isLoggedIn) {
        if (form) form.style.display = "none";
        if (userPanel) userPanel.style.display = "flex";
        if (avatarLink) {
            avatarLink.href = `userprofile.html?id=${encodeURIComponent(currentUser.username)}`;
        }
        if (avatarImg) {
            avatarImg.src = currentUser.imgPath ? `images/${encodeURIComponent(currentUser.imgPath)}` : "images/default.jpeg";
            avatarImg.alt = currentUser.username;
        }
        if (navCenter) navCenter.style.visibility = "visible";
        if (createBlogPostBtn) createBlogPostBtn.style.display = "flex";
        return;
    }

    if (form) form.style.display = "flex";
    if (userPanel) userPanel.style.display = "none";
    if (navCenter) navCenter.style.visibility = "hidden";
    if (createBlogPostBtn) createBlogPostBtn.style.display = "none";
}

async function logout() {
    try {
        await window.authClient.logout();
    } catch (error) {
        console.error(error);
    }

    currentUser = null;
    form?.reset();
    await renderAfterAuth(false);
    await loadProfileData();
}

logoutBtn?.addEventListener("click", () => {
    logout().catch(console.error);
});

form?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const creds = Object.fromEntries(new FormData(form));

    try {
        await window.authClient.login(creds);
        currentUser = await window.authClient.getCurrentUser(true);
        await renderAfterAuth(false);
        await loadProfileData();
    } catch (err) {
        console.error(err);
    }
});

async function loadProfileData() {
    await Promise.all([
        loadUserBlogs(),
        loadUserProfileCard()
    ]);
}

async function loadUserBlogs() {
    try {
        const response = await fetch(`/api/v1/blog/getbyusername/${profileUsername}`, {
            credentials: "same-origin"
        });
        const blogs = await response.json();

        userProfileBlogsContainer.innerHTML = blogs.map(createUserprofileBlogBox).join('');

        document.querySelectorAll(`.post`).forEach(section => {
            const blogId = section.dataset.blogId;
            const commentsContainer = section.querySelector('.post-comments');

            fetch(`/api/v1/comments/getcomment/${blogId}`, {
                credentials: "same-origin"
            })
                .then(res => res.json())
                .then(comments => {
                    if (!comments || comments.length === 0) {
                        commentsContainer.innerHTML = "";
                        return;
                    }

                    commentsContainer.innerHTML = comments
                        .map(c => `<p id="comment-${c.commentId}">${c.username}: <br> ${c.comment}
                            ${c.username === getUsernameByToken() ? `<i onclick="deleteComment(${c.commentId})"
                            style="cursor: pointer;" class="fa-solid fa-trash"></i></p>` : ""}
                            `)
                        .join('');
                })
                .catch(err => {
                    commentsContainer.innerHTML = `<p> Could not load comments ${err}</p>`;
                    console.error(err);
                });
        });

        document.querySelectorAll('.post-add-comment').forEach(commentForm => {
            commentForm.addEventListener('submit', addComment);
        });
    } catch (err) {
        userProfileBlogsContainer.innerHTML = '<p>Failed to load blog posts.</p>';
        console.error(err);
    }
}

async function loadUserProfileCard() {
    try {
        const response = await fetch(`/api/v1/users/getuserbyname/${profileUsername}`, {
            credentials: "same-origin"
        });
        const user = await response.json();

        userProfileInfo.querySelector(".userprofile-sidebar")?.remove();
        userProfileInfo.insertAdjacentHTML("afterbegin", createUserProfileBox(user));
    } catch (err) {
        userProfileInfo.innerHTML = '<p>User does not exist.</p>';
        console.error(err);
    }
}

async function deleteComment(id) {
    if (!confirm("Are you sure?")) return;

    try {
        const response = await window.authClient.fetchWithAuth(`${deleteCommentsUrl}${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(await response.text() || `Delete failed (${response.status})`);
        }

        document.getElementById(`comment-${id}`)?.remove();
    } catch (error) {
        console.error("Could not delete comment:", error);
    }
}

async function deleteBlog(id) {
    if (!confirm("Are you sure?")) return;

    try {
        const response = await window.authClient.fetchWithAuth(`${deleteBlogUrl}${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(await response.text() || `Delete failed (${response.status})`);
        }

        document.getElementById(`${id}`)?.remove();
    } catch (error) {
        console.error("Could not delete blog:", error);
    }
}

async function addComment(event) {
    event.preventDefault();
    const commentForm = event.target;
    const input = commentForm.querySelector('.comment-input');
    const comment = input.value.trim();

    if (!comment) {
        return;
    }

    const payload = {
        comment,
        blogId: Number(commentForm.dataset.blogId),
        userId: null,
        date
    };

    try {
        const response = await window.authClient.fetchWithAuth(addCommentsUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(await response.text() || `Comment failed (${response.status})`);
        }

        input.value = "";
        await loadUserBlogs();
    } catch (error) {
        console.error("Comment failed:", error);
    }
}

function createUserprofileBlogBox(blog) {
    const currentUsername = getUsernameByToken();

    return `
        <article class="post" data-blog-id="${blog.blogId}" id="${blog.blogId}">
            <header class="post-header">
                <h2 class="post-title"> ${blog.subject} </h2>
                <time class="post-date"> ${blog.publishDate} </time>
            </header>
            ${blog.author?.name === currentUsername ? `<i onclick="deleteBlog(${blog.blogId})"
            style="cursor: pointer;"
            class="fa-solid fa-trash"></i>` : ""}

            <div class="post-body">
                <pre> ${blog.body} </pre>
            </div>
            <footer class="post-footer">
                <nav class="post-actions">
                    <button class="abtn btn-ghost"><i class="fa-regular fa-thumbs-up"></i></button>
                    <button class="abtn btn-ghost"><i class="fa-regular fa-comments"></i></button>
                    <button class="abtn btn-ghost"><i class="fa-regular fa-share-from-square"></i></button>
                </nav>
                <div class="post-comments"></div>

                ${isAuthenticated() ? `
                    <form class="post-add-comment" data-blog-id="${blog.blogId}">
                        <label class="sr-only" for="comment-input-${blog.blogId}"></label>
                        <input class="comment-input" id="comment-input-${blog.blogId}" type="text" placeholder="Add a comment…" />
                        <button class="btn">Send</button>
                    </form>
                ` : "" }
            </footer>
        </article>
    `;
}

function createUserProfileBox(box) {
    return `
        <aside class="userprofile-sidebar">
            <img src="images/${box.imgPath || 'default.jpeg'}" alt="profile picture" class="userprofile-avatar">
            <h2 class="userprofile-name"> ${box.name} </h2>
            <p class="userprofile-bio">This is the user description</p>
            <p class="userprofile-joined">${box.createdDate}</p>

            <div class="userprofile-stats">
                <div>
                    <span class="stat-label">Post</span>
                    <span class="stat-value"> 25 </span>
                </div>
                <div>
                    <span class="stat-label"> Test </span>
                    <span class="stat-value"> 25</span>
                </div>
                <div>
                    <span class="stat-label"> Test </span>
                    <span class="stat-value"> 25</span>
                </div>
            </div>
        </aside>
    `;
}

function openPopup() {
    if (!popupEl) return;
    popupEl.classList.add('active');
    document.addEventListener('keydown', escHandler);
}

function closePopup() {
    if (!popupEl) return;
    popupEl.classList.remove('active');
    document.removeEventListener('keydown', escHandler);
    formEl?.reset();
}

function escHandler(e) {
    if (e.key === 'Escape') closePopup();
}

openBtn?.addEventListener('click', (e) => {
    e.preventDefault();
    openPopup();
});

closeBtn?.addEventListener('click', () => closePopup());

formEl?.addEventListener('submit', async (e) => {
    e.preventDefault();

    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';

        if (!isAuthenticated()) {
            throw new Error('Du er ikke logget ind');
        }

        const fd = new FormData(formEl);
        fd.set('publishDate', date);
        const payload = Object.fromEntries(fd);

        const response = await window.authClient.fetchWithAuth(saveblogUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const msg = await response.text();
            throw new Error(msg || `Fejl: ${response.status}`);
        }

        closePopup();
        await loadProfileData();
    } catch (err) {
        console.error(err);
        alert(err.message || 'Noget gik galt');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create blog post';
    }
});

document.addEventListener("DOMContentLoaded", async () => {
    await window.authClient.initializeSession();
    currentUser = window.authClient.getCachedUser();
    logProfile("Initialized profile page", { profileUsername, currentUser });
    await renderAfterAuth(false);
    await loadProfileData();
});

window.addEventListener("focus", () => {
    renderAfterAuth(true)
        .then(() => loadProfileData())
        .catch(console.error);
});

window.addEventListener("auth:changed", async (event) => {
    currentUser = event.detail.user || null;
    await renderAfterAuth(false);
});
