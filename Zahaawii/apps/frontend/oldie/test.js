    const url = 'http://localhost:8080/api/v1/blog/getallblogpost';
    const container = document.getElementById('blog-posts-container');
    var date = new Date().toISOString().slice(0,10);

    function createBlogBox(blog) {
        return `
    <div class="blog-section">
        <div class="blog-box">
            <div class="blog-userInfo-logo">
                <img src="${blog.userInfo?.imgPath || 'default.jpeg'}">
                <p>${blog.userInfo?.name || 'Unknown'}</p>
            </div>
            <div class="blog-post-subject">
                <h2>${blog.subject}</h2>
                <p>${blog.publishDate}</p>
            </div>
            <div class="blog-post-body">
                <p>${blog.body}</p>
            </div>
            <div class="blog-interaction">
                <div class="blog-likes-section">
                    <ul>
                        <li><a href="#" class="interaction">like</a></li>
                        <li><a href="#" class="interaction">comment</a></li>
                        <li><a href="#" class="interaction">share</a></li>
                    </ul>
                </div>
                <div class="blog-see-all-comments">
                    <p>this is a comment</p>
                    <p>this is a comment</p>
                    <p>this is a comment</p>
                </div>
                <div class="blog-add-comments">
                    <label>
                        <input type="text" placeholder="Add a comment here">
                    </label>
                </div>
            </div>
        </div>
    </div>
    `;
    }

    fetch(url)
        .then(res => res.json())
        .then(data => {
            container.innerHTML = data.map(createBlogBox).join('');
        })
        .catch(err => {
            container.innerHTML = '<p>Failed to load blog posts.</p>';
            console.error(err);
        });

    document.querySelector(".btn").addEventListener("click", function(){
        document.querySelector(".popup").classList.add("active");
    });
    document.querySelector(".popup .close-btn").addEventListener("click", function(){
        document.querySelector(".popup").classList.remove("active");
    });


    function submitForm() {


        const formEl = document.querySelector('.createblogpost');

        formEl.addEventListener('submit', event => {
            event.preventDefault();

            const formData = new FormData(formEl);
            formData.set('publishDate', date);
            const data = Object.fromEntries(formData);

            fetch('http://localhost:8080/api/blog/saveblogpost', {
                method: 'POST',
                headers: {
                    'Content-type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(res => res.json())
                .then(data => console.log(data))
                .catch(error => console.log(error))
        });
        alert("Blog post created")
    };


/*
.blog-section {
    margin-right: 40%;
    margin-left: 5%;
    display: block;
    border: 1px solid var(--border);
    border-radius: 15px;
    padding: 2em;
    margin-top: 5%;
    word-break: break-word;
    overflow-wrap: break-word;
    box-sizing: border-box;
}

.blog-user-logo {
    display: flex;
    justify-content: start;
    align-items: start;
    border: 1px solid cornflowerblue;
}

.blog-likes-section ul {
    border: 1px solid cornflowerblue;
    border-radius: 15px;
    display: flex;
    justify-content: space-evenly;
    align-items: center;
    list-style: none;
}
/*