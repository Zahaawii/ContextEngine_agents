(function () {
    const draftKey = "infoPageDraft";
    const state = JSON.parse(JSON.stringify(window.infoPageData));

    const els = {
        adminToggle: document.querySelector("#admin-toggle"),
        adminDialog: document.querySelector("#admin-dialog"),
        adminClose: document.querySelector("#admin-close"),
        adminForm: document.querySelector("#admin-login-form"),
        adminError: document.querySelector("#admin-error"),
        editorDrawer: document.querySelector("#editor-drawer"),
        editorForm: document.querySelector("#profile-editor"),
        dataEditor: document.querySelector("#data-editor"),
        logoutButton: document.querySelector("#logout-button"),
        saveDraft: document.querySelector("#save-draft"),
        resetDraft: document.querySelector("#reset-draft"),
        exportData: document.querySelector("#export-data")
    };

    const iconMap = {
        Java: "J",
        JavaScript: "JS",
        Python: "Py",
        SQL: "DB",
        HTML5: "5",
        CSS3: "3",
        "Vanilla JS": "JS",
        "Responsive UI": "UI",
        "Spring Boot": "SB",
        "Node.js": "Nd",
        "REST API": "API",
        WebSocket: "WS",
        MySQL: "MY",
        ChromaDB: "CH",
        Docker: "DK",
        GHCR: "GH",
        Git: "Git",
        GitHub: "GH",
        MCP: "MCP",
        Postman: "PM"
    };

    function escapeHtml(value) {
        return String(value || "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function applyDraft() {
        const draft = localStorage.getItem(draftKey);
        if (!draft) {
            return;
        }

        try {
            Object.assign(state, JSON.parse(draft));
        } catch (error) {
            console.warn("Ignoring invalid info page draft", error);
        }
    }

    function renderProfile() {
        const { profile } = state;
        document.querySelector("#profile-name").textContent = profile.name;
        document.querySelector("#profile-title").textContent = profile.title;
        document.querySelector("#profile-summary").textContent = profile.summary;

        const avatar = document.querySelector("#profile-avatar");
        avatar.src = profile.avatar;
        avatar.alt = `${profile.name} profile photo`;
    }

    function renderProjects() {
        document.querySelector("#project-timeline").innerHTML = state.projects
            .map((project, index) => `
                <article class="project-card ${index % 2 === 0 ? "left" : "right"}">
                    <div class="project-icon" aria-hidden="true">${escapeHtml(project.name.slice(0, 2))}</div>
                    <div class="project-body">
                        <div class="project-heading">
                            <h3>${escapeHtml(project.name)}</h3>
                            <span>${escapeHtml(project.period)}</span>
                        </div>
                        <p>${escapeHtml(project.summary)}</p>
                        <ul>
                            ${project.stack.map((tag) => `<li>${escapeHtml(tag)}</li>`).join("")}
                        </ul>
                    </div>
                    ${project.link ? `<a href="${escapeHtml(project.link)}" target="_blank" rel="noopener" aria-label="Open ${escapeHtml(project.name)}"></a>` : ""}
                </article>
            `)
            .join("");
    }

    function renderTech() {
        document.querySelector("#tech-grid").innerHTML = state.techGroups
            .map((group) => `
                <div class="tech-row">
                    <div class="tech-label">${escapeHtml(group.label)}</div>
                    <div class="tech-items">
                        ${group.items.map((item) => `
                            <div class="tech-pill">
                                <span>${escapeHtml(iconMap[item] || item.slice(0, 2))}</span>
                                ${escapeHtml(item)}
                            </div>
                        `).join("")}
                    </div>
                </div>
            `)
            .join("");
    }

    function renderCertificates() {
        document.querySelector("#certificate-grid").innerHTML = state.certificates
            .map((certificate) => `
                <article class="certificate-card">
                    <div class="certificate-image">
                        ${certificate.image ? `<img src="${escapeHtml(certificate.image)}" alt="${escapeHtml(certificate.name)}">` : `<span>${escapeHtml(certificate.name)}</span>`}
                    </div>
                    <h3>${escapeHtml(certificate.name)}</h3>
                    <p>${escapeHtml(certificate.issuer)}</p>
                </article>
            `)
            .join("");
    }

    function renderContact() {
        const links = [
            ["GitHub", state.contact.github, "GH"],
            ["Email", state.contact.email ? `mailto:${state.contact.email}` : "", "@"],
            ["Website", state.contact.website, "WWW"],
            ["LinkedIn", state.contact.linkedin, "in"]
        ].filter(([, href]) => href);

        document.querySelector("#contact-links").innerHTML = links
            .map(([label, href, icon]) => `
                <a class="contact-card" href="${escapeHtml(href)}" target="_blank" rel="noopener">
                    <span class="contact-icon">${escapeHtml(icon)}</span>
                    <strong>${escapeHtml(label)}</strong>
                    <small>${escapeHtml(href.replace("mailto:", ""))}</small>
                    <em aria-hidden="true"></em>
                </a>
            `)
            .join("");
    }

    function renderDots() {
        const sections = Array.from(document.querySelectorAll(".scroll-view"));
        const dots = Array.from(document.querySelectorAll(".section-dots a"));

        if (!("IntersectionObserver" in window)) {
            return;
        }

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (!entry.isIntersecting) {
                    return;
                }

                dots.forEach((dot) => {
                    dot.classList.toggle("active", dot.getAttribute("href") === `#${entry.target.id}`);
                });
            });
        }, { threshold: 0.58 });

        sections.forEach((section) => observer.observe(section));
    }

    function renderAll() {
        renderProfile();
        renderProjects();
        renderTech();
        renderCertificates();
        renderContact();
    }

    function buildEditor() {
        els.editorForm.innerHTML = `
            <label>
                Name
                <input name="name" value="${escapeHtml(state.profile.name)}">
            </label>
            <label>
                Title
                <input name="title" value="${escapeHtml(state.profile.title)}">
            </label>
            <label>
                Email
                <input name="email" value="${escapeHtml(state.contact.email)}">
            </label>
            <label>
                GitHub
                <input name="github" value="${escapeHtml(state.contact.github)}">
            </label>
            <label>
                Summary
                <textarea name="summary" rows="5">${escapeHtml(state.profile.summary)}</textarea>
            </label>
        `;
        els.dataEditor.value = JSON.stringify(state, null, 4);
    }

    function openEditor() {
        buildEditor();
        els.editorDrawer.classList.add("is-open");
        els.adminToggle.textContent = "Editing";
    }

    function closeEditor() {
        els.editorDrawer.classList.remove("is-open");
        els.adminToggle.textContent = "Login / Edit";
    }

    async function initializeAuth() {
        if (!window.authClient) {
            return;
        }

        const user = await window.authClient.initializeSession();
        if (user) {
            openEditor();
        }
    }

    function readEditor() {
        const formData = Object.fromEntries(new FormData(els.editorForm));
        let fullData = state;

        try {
            fullData = JSON.parse(els.dataEditor.value);
        } catch (error) {
            throw new Error("The full page data field must contain valid JSON.");
        }

        Object.keys(state).forEach((key) => delete state[key]);
        Object.assign(state, fullData);

        state.profile.name = formData.name.trim();
        state.profile.title = formData.title.trim();
        state.profile.summary = formData.summary.trim();
        state.contact.email = formData.email.trim();
        state.contact.github = formData.github.trim();
    }

    function downloadDataFile() {
        const source = `window.infoPageData = ${JSON.stringify(state, null, 4)};\n`;
        const blob = new Blob([source], { type: "text/javascript" });
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = "data.js";
        link.click();
        URL.revokeObjectURL(link.href);
    }

    els.adminToggle.addEventListener("click", () => {
        if (els.editorDrawer.classList.contains("is-open")) {
            els.editorDrawer.scrollIntoView({ behavior: "smooth", block: "start" });
            return;
        }
        els.adminDialog.showModal();
    });

    els.adminClose.addEventListener("click", () => els.adminDialog.close());

    els.adminForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        els.adminError.textContent = "";

        if (!window.authClient) {
            els.adminError.textContent = "Auth client is not available.";
            return;
        }

        try {
            await window.authClient.login(Object.fromEntries(new FormData(els.adminForm)));
            els.adminDialog.close();
            openEditor();
        } catch (error) {
            els.adminError.textContent = "Login failed. Check your backend account.";
        }
    });

    els.logoutButton.addEventListener("click", async () => {
        if (window.authClient) {
            await window.authClient.logout();
        }
        closeEditor();
    });

    els.saveDraft.addEventListener("click", () => {
        try {
            readEditor();
            localStorage.setItem(draftKey, JSON.stringify(state));
            renderAll();
            buildEditor();
        } catch (error) {
            alert(error.message);
        }
    });

    els.resetDraft.addEventListener("click", () => {
        localStorage.removeItem(draftKey);
        window.location.reload();
    });

    els.exportData.addEventListener("click", () => {
        try {
            readEditor();
            downloadDataFile();
        } catch (error) {
            alert(error.message);
        }
    });

    applyDraft();
    renderAll();
    renderDots();
    initializeAuth().catch((error) => console.warn("Admin auth initialization failed", error));
})();
