const form = document.querySelector(".login");
const navCenter = document.querySelector(".nav-center");
const userPanel = document.querySelector(".user-panel");
const avatarLink = document.querySelector(".avatar-link");
const avatarImg = document.querySelector(".avatar");
const logoutBtn = document.querySelector(".logout");
const popupEl = document.querySelector('#popup');
const formEl = document.querySelector('.createblogpost');
const openBtn = document.querySelector('[data-open="post"]');
const closeBtn = popupEl?.querySelector('[data-close]');
const chatOpenBtn = document.querySelector('[data-open="test"]');
const chatpopUpEl = document.querySelector("#chatpopup");
const chatCloseBtn = chatpopUpEl?.querySelector('[data-close]');
const submitBtn = formEl?.querySelector('[type="submit"]');
const container = document.getElementById('blog-posts-container');
const createBlogPostBtn = document.querySelector('.btn');
const postBar = document.querySelector(".post-nav-bar");
const articleAI = document.querySelector(".post-nav-bar-1");

let currentUser = null;
let chatUsername = null;
let stompClient = null;

const chatPage = document.querySelector('.chat-popup');
const messageForm = document.querySelector('.chat-send-message');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#chat-message-area');
const connectingElement = document.querySelector('.connection');
const connectUsername = document.querySelector('#open-chat');

const colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];
const saveblogUrl = '/api/v1/blog/saveblogpost';
const loginUrl = "/api/v1/users/auth/login";
const getAllBlogPostUrl = '/api/v1/blog/getallblogpost';
const findCommentsUrl = "/api/v1/comments/getcomment/";
const addCommentsUrl = "/api/v1/comments/addcomment";
const deleteCommentsUrl = "/api/v1/comments/delete/";
const deleteBlogUrl = '/api/v1/blog/deletepost/';
const updateUrl = '/api/v1/blog/update/';
const date = new Date().toISOString().slice(0, 10);

function renderMarkdownWhenReady(retries = 20) {
    if (typeof window.renderMarkdown !== "function") {
        return;
    }

    if (typeof window.showdown === "undefined") {
        if (retries > 0) {
            window.setTimeout(() => renderMarkdownWhenReady(retries - 1), 50);
        }
        return;
    }

    window.renderMarkdown();
}

function logApp(message, details) {
    if (details === undefined) {
        console.log(`[frontend] ${message}`);
        return;
    }
    console.log(`[frontend] ${message}`, details);
}

function clearLoginErrorState() {
    document.querySelector('.js_username')?.classList.remove('login-error');
    document.querySelector('.js_password')?.classList.remove('login-error');
}

function setLoginErrorState() {
    document.querySelector('.js_username')?.classList.add('login-error');
    document.querySelector('.js_password')?.classList.add('login-error');
}

function isAuthenticated() {
    return Boolean(currentUser?.username);
}

function getUsernameByToken() {
    return currentUser?.username || null;
}

function getAvatarPath(user) {
    return user?.imgPath ? `images/${encodeURIComponent(user.imgPath)}` : 'images/default.jpeg';
}

async function renderAfterAuth(forceRefresh = false) {
    if (forceRefresh) {
        currentUser = await window.authClient.getCurrentUser(true);
    } else if (!currentUser) {
        currentUser = await window.authClient.getCurrentUser(false);
    }

    const isLoggedIn = isAuthenticated();

    if (isLoggedIn) {
        if (form) form.style.display = "none";
        if (userPanel) userPanel.style.display = "flex";
        if (connectUsername) connectUsername.style.display = "grid";
        if (createBlogPostBtn) createBlogPostBtn.style.display = "flex";
        if (postBar) postBar.style.display = "flex";
        if (articleAI) articleAI.style.display = "flex";

        if (avatarLink) {
            avatarLink.href = `userprofile.html?id=${encodeURIComponent(currentUser.username)}`;
        }
        if (avatarImg) {
            avatarImg.src = getAvatarPath(currentUser);
            avatarImg.alt = currentUser.username;
        }

        if (navCenter) navCenter.style.visibility = "visible";
        return;
    }

    if (form) form.style.display = "flex";
    if (userPanel) userPanel.style.display = "none";
    if (postBar) postBar.style.display = "none";
    if (articleAI) articleAI.style.display = "none";
    if (createBlogPostBtn) createBlogPostBtn.style.display = "none";
    if (connectUsername) connectUsername.style.display = "none";

    if (stompClient) {
        stompClient.disconnect(() => logApp("Disconnected websocket after logout"));
        stompClient = null;
    }
}

async function logout() {
    try {
        await window.authClient.logout();
    } catch (error) {
        console.error(error);
    }

    currentUser = null;
    if (form) form.reset();
    await renderAfterAuth(false);
    await loadBlogs();
}

logoutBtn?.addEventListener("click", () => {
    logout().catch(console.error);
});

form?.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearLoginErrorState();

    const creds = Object.fromEntries(new FormData(form));

    try {
        await window.authClient.login(creds);
        currentUser = await window.authClient.getCurrentUser(true);
        logApp("Login succeeded", currentUser);
        await renderAfterAuth(false);
        await loadBlogs();
    } catch (err) {
        console.error(err);
        setLoginErrorState();
    }
});

function createBlogBox(blog) {
    const currentUsername = getUsernameByToken();
    return `
    <div class="blog-section" data-blog-id="${blog.blogId}" id="${blog.blogId}">
        <div class="blog-box">
            <div class="blog-userInfo-logo">
                <a href="userprofile.html?id=${blog.author?.name}" target="_blank"> <img src="images/${blog.author?.imgPath || 'default.jpeg'}"></a>
                <p id="${blog.blogId}-author" data-blog-name=${blog.author?.name}>${blog.author?.name || 'Unknown'}</p>
                <div class="actions" id="${blog.blogId}-actions">
                ${blog.author?.name === currentUsername ? `<i onclick="editBlog(${blog.blogId})" class="fa-solid fa-pen-to-square"></i> <i onclick="deleteBlog(${blog.blogId})"
                class="fa-solid fa-trash"></i>` : ""}
                </div>
            </div>
            <div id="${blog.blogId}-body">
            <div class="blog-post-subject">
                <a href="blogpost.html?id=${blog.blogId}" id="${blog.blogId}-subject"><h2>${blog.subject}</h2></a>
                <p id="${blog.blogId}-date">${blog.publishDate || ""}</p>
            </div>
            <div class="blog-post-body">
                <md id="${blog.blogId}-tekst" class="body-test">${blog.body}</md>
            </div>
            </div>
            <div class="blog-interaction">
                <div class="blog-likes-section">
                    <ul>
                        <li><p class="interaction"><i class="fa-regular fa-thumbs-up"></i></li>
                        <li><p class="interaction" onclick="scrollToComment(${blog.blogId})"><i class="fa-regular fa-comments"></i></li>
                        <li><p class="interaction" onclick="copyUrl(${blog.blogId})"><i class="fa-regular fa-share-from-square"></i></p></li>
                        <div class="urlcopied">
                        <span> Blog link has been copied to your clipboard </span>
                        </div>
                    </ul>
                </div>
                <div class="blog-see-all-comments"></div>
                ${isAuthenticated() ? `
                    <div class="blog-add-comments">
                        <form class="post-add-comment" data-blog-id="${blog.blogId}">
                            <label class="sr-only" for="comment-input-${blog.blogId}"></label>
                            <input class="comment-input"
                            name="comment"
                            id="comment-input-${blog.blogId}"
                            type="text"
                            placeholder="Add a comment…" />
                            <button class="btn" type="submit" id="add-comment">Send</button>
                        </form>
                    </div>
                ` : ""}
            </div>
        </div>
    </div>
    `;
}

async function loadBlogs() {
    try {
        const res = await fetch(getAllBlogPostUrl, {
            credentials: "same-origin"
        });

        if (!res.ok) {
            throw new Error(`Failed to load blogs (${res.status})`);
        }

        const data = await res.json();
        container.innerHTML = data.map(createBlogBox).join('');
        renderMarkdownWhenReady();

        document.querySelectorAll('.blog-section').forEach(section => {
            const blogId = section.dataset.blogId;
            const commentsContainer = section.querySelector('.blog-see-all-comments');

            fetch(`${findCommentsUrl}${blogId}`, {
                credentials: "same-origin"
            })
                .then(response => response.json())
                .then(comments => {
                    if (!comments || comments.length === 0) {
                        commentsContainer.innerHTML = "";
                        return;
                    }

                    commentsContainer.innerHTML = comments
                        .map(c => `<p id="${c.commentId}">${c.username}: <br> ${c.comment}
                            ${c.username === getUsernameByToken() ? `<i onclick="deleteComment(${c.commentId})"
                            style="cursor: pointer;" class="fa-solid fa-trash"></i></p>` : ""}
                            `)
                        .join('');
                })
                .catch(err => {
                    commentsContainer.innerHTML = `<p> Could not load comments </p>`;
                    console.error(err);
                });
        });

        document.querySelectorAll('.post-add-comment').forEach(commentForm => {
            commentForm.addEventListener('submit', addComment);

            const input = commentForm.querySelector('.comment-input');
            if (!input) {
                return;
            }

            input.addEventListener('keydown', function (event) {
                if (event.key === "Enter" && !event.shiftKey) {
                    event.preventDefault();
                    commentForm.requestSubmit();
                }
            });
        });
    } catch (err) {
        container.innerHTML = `<p>Failed to load blog posts ${err}.</p>`;
        console.error(err);
    }
}

function scrollToComment(id) {
    const commentIcon = document.getElementById(`comment-input-${id}`);
    commentIcon?.focus();
}

function editBlog(id) {
    const editBody = document.getElementById(`${id}-body`);
    const editDate = document.getElementById(`${id}-date`);
    const saveButton = document.getElementById(`${id}-actions`);
    const existing = saveButton.querySelector(".fa-paper-plane");

    if (editBody.contentEditable === "true") {
        editBody.contentEditable = "false";
        editDate.contentEditable = "false";
        existing?.remove();
        return;
    }

    editBody.contentEditable = "true";
    editDate.contentEditable = "false";

    if (existing) {
        existing.remove();
        return;
    }

    const button = document.createElement('i');
    button.classList.add("fa-solid", "fa-paper-plane");
    saveButton.appendChild(button);
    button.addEventListener('click', () => updateBlogPost(id));
}

async function updateBlogPost(id) {
    const input = document.getElementById(`${id}-tekst`);
    const value = input.textContent;

    try {
        const res = await window.authClient.fetchWithAuth(`${updateUrl}${id}`, {
            method: 'PUT',
            headers: {
                'Content-type': 'application/json',
            },
            body: value
        });

        if (!res.ok) {
            throw new Error(await res.text() || `Update failed (${res.status})`);
        }

        const edit = document.getElementById(`${id}-body`);
        const saveButton = document.getElementById(`${id}-actions`);
        const existing = saveButton.querySelector(".fa-paper-plane");
        edit.contentEditable = "false";
        existing?.remove();
        logApp("Updated blog post", { id });
    } catch (error) {
        console.error("Could not update blog:", error);
        alert(error.message || "Could not update blog post");
    }
}

async function deleteComment(id) {
    if (!confirm("Are you sure?")) return;

    try {
        const res = await window.authClient.fetchWithAuth(`${deleteCommentsUrl}${id}`, {
            method: 'DELETE'
        });

        if (!res.ok) {
            throw new Error(await res.text() || `Delete failed (${res.status})`);
        }

        document.getElementById(`${id}`)?.remove();
    } catch (error) {
        console.error("Could not delete comment:", error);
    }
}

async function deleteBlog(id) {
    if (!confirm("Are you sure?")) return;

    try {
        const res = await window.authClient.fetchWithAuth(`${deleteBlogUrl}${id}`, {
            method: 'DELETE'
        });

        if (!res.ok) {
            throw new Error(await res.text() || `Delete failed (${res.status})`);
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
    const commentsToSend = input.value.trim();

    if (!isAuthenticated()) {
        alert("Du er ikke logget ind, så du kan ikke kommenterer");
        return;
    }

    if (!commentsToSend) {
        alert("Kommentaren kan ikke være tom");
        return;
    }

    const blogId = commentForm.dataset.blogId;
    const send = {
        comment: commentsToSend,
        blogId: parseInt(blogId, 10),
        userId: null,
        date
    };

    try {
        const res = await window.authClient.fetchWithAuth(addCommentsUrl, {
            method: 'POST',
            headers: {
                'Content-type': 'application/json'
            },
            body: JSON.stringify(send)
        });

        if (!res.ok) {
            throw new Error(await res.text() || `Comment failed (${res.status})`);
        }

        input.value = "";
        await loadBlogs();
    } catch (error) {
        console.error("Comment failed:", error);
    }
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

chatOpenBtn?.addEventListener('click', (e) => {
    e.preventDefault();
    chatOpenPopUp();
});

chatCloseBtn?.addEventListener('click', () => chatClosePopUp());

function chatOpenPopUp() {
    if (!chatpopUpEl) return;
    chatpopUpEl.classList.add('active');
    document.addEventListener('keydown', escHandler);
}

function chatClosePopUp() {
    if (!chatpopUpEl) return;
    chatpopUpEl.classList.remove('active');
    document.removeEventListener('keydown', escHandler);
}

closeBtn?.addEventListener('click', () => closePopup());

formEl?.addEventListener('submit', async (e) => {
    e.preventDefault();

    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';

        if (!isAuthenticated()) throw new Error('Du er ikke logget ind');

        const fd = new FormData(formEl);
        fd.set('publishDate', date);
        const payload = Object.fromEntries(fd);

        const res = await window.authClient.fetchWithAuth(saveblogUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const msg = await res.text();
            throw new Error(msg || `Fejl: ${res.status}`);
        }

        closePopup();
        await loadBlogs();
    } catch (err) {
        console.error(err);
        alert(err.message || 'Noget gik galt');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create blog post';
    }
});

function connect(event) {
    if (event) {
        event.preventDefault();
    }

    chatUsername = getUsernameByToken();
    if (!chatUsername) {
        return;
    }

    if (typeof window.SockJS === "undefined" || typeof window.Stomp === "undefined") {
        if (connectingElement) {
            connectingElement.textContent = 'Chat is temporarily unavailable';
            connectingElement.classList.remove('hidden');
        }
        console.error("WebSocket libraries are not loaded");
        return;
    }

    if (stompClient && stompClient.connected) {
        return;
    }

    if (connectingElement) {
        connectingElement.textContent = 'Connecting...';
        connectingElement.classList.remove('hidden');
    }

    const socket = new window.SockJS('/ws');
    stompClient = window.Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    if (!stompClient) {
        return;
    }

    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.send("/app/chat.addUser", {}, JSON.stringify({ type: 'JOIN' }));

    if (connectingElement) {
        connectingElement.classList.add('hidden');
    }
}

function onError(error) {
    console.error("WebSocket error", error);
    if (connectingElement) {
        connectingElement.textContent = 'Could not connect to chat';
        connectingElement.classList.remove('hidden');
    }
}

function sendMessage(event) {
    event.preventDefault();
    const messageContent = messageInput.value.trim();
    if (!messageContent || !stompClient) {
        return;
    }

    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
        content: messageContent,
        type: 'CHAT'
    }));
    messageInput.value = '';
}

function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    const index = Math.abs(hash % colors.length);
    return colors[index];
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    const messageElement = document.createElement('li');

    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        const p = document.createElement('p');
        p.textContent = message.type === 'JOIN' ? `${message.sender} joined!` : `${message.sender} left!`;
        messageElement.appendChild(p);
    } else {
        messageElement.classList.add('chat-message');

        const avatarElement = document.createElement('i');
        avatarElement.text = (message.sender || '?')[0].toUpperCase();
        avatarElement.style.backgroundColor = getAvatarColor(message.sender || 'unknown');

        const header = document.createElement('div');
        header.classList.add('chat-header-row');

        const name = document.createElement('span');
        name.classList.add('chat-username');
        name.textContent = message.sender || 'Unknown';
        header.appendChild(name);

        const text = document.createElement('p');
        text.classList.add('chat-text');
        text.textContent = message.content || '';

        const body = document.createElement('div');
        body.classList.add('chat-body');
        body.appendChild(header);
        body.appendChild(text);

        messageElement.appendChild(avatarElement);
        messageElement.appendChild(body);
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function copyUrl(id) {
    const copyLink = `/blogpost.html?id=${id}`;
    navigator.clipboard.writeText(copyLink);
    const notice = document.querySelector(".urlcopied");
    if (!notice) {
        return;
    }

    notice.style.display = "flex";
    notice.classList.remove("fade-out");
    notice.classList.add("fade-in");

    setTimeout(() => {
        notice.classList.remove("fade-in");
        notice.classList.add("fade-out");
    }, 2000);

    setTimeout(() => {
        notice.style.display = "none";
        notice.classList.remove("fade-out");
    }, 3000);
}

connectUsername?.addEventListener('click', connect, true);
messageForm?.addEventListener('submit', sendMessage, true);

document.addEventListener("DOMContentLoaded", async () => {
    await window.authClient.initializeSession();
    currentUser = window.authClient.getCachedUser();
    await renderAfterAuth(false);
    await loadBlogs();
});

window.addEventListener("focus", () => {
    renderAfterAuth(true)
        .then(() => loadBlogs())
        .catch(console.error);
});

window.addEventListener("auth:changed", async (event) => {
    currentUser = event.detail.user || null;
    await renderAfterAuth(false);
});
