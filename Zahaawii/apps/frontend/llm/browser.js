const url = "/node/api/";
const form = document.querySelector(".question");
const chatWindow = document.getElementById("chat-window");
const textarea = form.querySelector("textarea, input[type='text']");
const kba = document.querySelector(".send_to_database");
const popupEl = document.getElementById("popup");
const openBtn = document.querySelector(".open-kba-btn");
const closeBtn = document.querySelector(".close-btn");

let currentUser = null;

const autoResize = (elem) => {
    elem.style.height = 'auto';
    elem.style.height = `${elem.scrollHeight}px`;
};

if (textarea) {
    autoResize(textarea);
    textarea.addEventListener('input', () => {
        autoResize(textarea);
    });
}

async function renderAfterAuth(forceRefresh = false) {
    currentUser = await window.authClient.getCurrentUser(forceRefresh);
    const isLoggedIn = Boolean(currentUser?.username);

    if (!isLoggedIn) {
        if (openBtn) openBtn.style.display = "none";
        if (popupEl) popupEl.style.display = "none";
        return;
    }

    if (openBtn) openBtn.style.display = "inline-flex";
}

const resetTextarea = () => {
    if (textarea) {
        textarea.style.height = '40px';
        textarea.value = '';
    }
};

textarea?.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        form.dispatchEvent(new Event("submit"));
    }
});

popupEl?.addEventListener("keydown", (e) => {
    const articleId = kba.id.value;
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        const upload = confirm("Do you want to upload the article to the database or did you press enter by a mistake?");
        if (articleId.length === 0) {
            alert("You have to add at least one character");
        } else if (upload === true) {
            kba.dispatchEvent(new Event("submit"));
        }
    }
});

form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const question = Object.fromEntries(new FormData(form));

    const userMsg = document.createElement("div");
    userMsg.className = "chat-message user-message";

    const pre = document.createElement("pre");
    pre.textContent = question.text;

    userMsg.appendChild(pre);
    chatWindow.appendChild(userMsg);
    scrollToBottom();

    const botMsg = document.createElement("div");
    botMsg.className = "chat-message bot-message";
    botMsg.textContent = "Thinking...";
    chatWindow.appendChild(botMsg);

    resetTextarea();

    try {
        const res = await fetch(`${url}question`, {
            method: "POST",
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify(question)
        });

        if (!res.ok) {
            throw new Error(`Question request failed: ${res.status}`);
        }

        const data = await res.text();
        botMsg.innerHTML = marked.parse(data);
        scrollToBottom();
    } catch (err) {
        console.error(err);
        botMsg.textContent = "There has been a problem with reaching the server. Try again later";
    }
});

kba?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const dataToDatabase = Object.fromEntries(new FormData(kba));

    try {
        const res = await window.authClient.fetchWithAuth(`${url}database`, {
            method: "POST",
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify(dataToDatabase)
        });

        if (!res.ok) {
            throw new Error(`Upload failed: ${res.status}`);
        }

        const answer = await res.text();
        alert(answer);
        closePopup();
        kba?.reset();
    } catch (err) {
        alert("Failed to upload the article to the database");
        console.error(err);
    }
});

function openPopup() {
    if (!popupEl) return;
    popupEl.style.display = "flex";
    document.body.style.overflow = "hidden";
}

function closePopup() {
    if (!popupEl) return;
    popupEl.style.display = "none";
    document.body.style.overflow = "auto";
}

openBtn?.addEventListener("click", (e) => {
    e.preventDefault();
    openPopup();
});

closeBtn?.addEventListener("click", closePopup);

popupEl?.addEventListener("click", (e) => {
    if (e.target === popupEl) closePopup();
});

function scrollToBottom() {
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

document.addEventListener("DOMContentLoaded", async () => {
    await window.authClient.initializeSession();
    await renderAfterAuth(true);
});

window.addEventListener("focus", () => {
    renderAfterAuth(true).catch(console.error);
});

window.addEventListener("auth:changed", (event) => {
    currentUser = event.detail.user || null;
    if (!currentUser && popupEl) {
        closePopup();
    }
    renderAfterAuth(false).catch(console.error);
});
