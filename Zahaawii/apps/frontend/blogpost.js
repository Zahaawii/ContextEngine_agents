const params = new URLSearchParams(window.location.search);
const blogId = params.get("id");

const subjectElement = document.getElementById("blog-subject");
const dateElement = document.getElementById("blog-date");
const authorElement = document.getElementById("blog-author");
const bodyElement = document.getElementById("blog-body");

function setErrorState(message) {
    subjectElement.textContent = "Could not load post";
    dateElement.textContent = "";
    authorElement.textContent = "Author: -";
    bodyElement.textContent = message;
}

async function loadBlogPostById(id) {
    if (!id) {
        setErrorState("Missing blog id in URL. Use blogpost.html?id=<blogId>.");
        return;
    }

    try {
        const response = await fetch(`/api/v1/blog/getbyid/${encodeURIComponent(id)}`, {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }

        const blog = await response.json();
        if (!blog) {
            throw new Error("No blog post found");
        }

        subjectElement.textContent = blog.subject || "Untitled";
        dateElement.textContent = blog.publishDate || "";
        authorElement.textContent = `Author: ${blog.userInfo?.name || blog.author?.name || "Unknown"}`;
        bodyElement.textContent = blog.body || "No content";
    } catch (error) {
        console.error("[blogpost] Failed to load post", error);
        setErrorState("The blog post could not be loaded. Please verify the id and try again.");
    }
}

loadBlogPostById(blogId);
