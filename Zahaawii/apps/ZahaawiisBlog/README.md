# Zahaawii Blog

A personal blog platform built as a full-stack web application.  
The project focuses on clean architecture, secure authentication, and a maintainable codebase suitable for real deployment.

## 🚀 Tech Stack

**Backend**
- Java 17
- Spring Boot  
- Spring MVC  
- Spring Security (JWT authentication)
- JPA / Hibernate  
- MySQL (SQL database)
- Maven

**Frontend**
- HTML5  
- CSS3  
- JavaScript (Vanilla JS)

**Other**
- DTOs + Service Layer Architecture  
- REST API endpoints  
- Lombok  
- Validation (Jakarta)  
- Docker

## 📌 Project Overview

This blog system allows users to:
- Create, read, update and delete blog posts.  
- Register and log in securely using JWT-based authentication.  
- View posts on a responsive front-end.  
- Manage users, authentication, and posts through a clean RESTful backend.

The project was built to train real-world development patterns:
- Proper controller/service/repository structure  
- Separation of concerns  
- Secure authentication layer  
- Full SQL-based persistence  
- Reusable domain models  
- Schema-driven database design  

## 📁 Project Structure

```
/src
 └── main
     ├── java
     │    └── com.project.blog
     │          ├── controller
     │          ├── service
     │          ├── repository
     │          ├── model
     │          ├── security
     │          └── dto
     └── resources
          ├── application.properties
          ├── static (CSS, JS)
          └── templates (HTML)
```

## 🔐 Authentication (JWT)

The blog uses **JWT tokens** to secure API routes.  
Flow:
1. User logs in → server validates credentials.  
2. Server issues JWT containing user ID + roles.  
3. Frontend stores the token and attaches it to each request.  
4. Protected routes require a valid token for access.

This structure mirrors modern production-grade APIs.

## 🗄 Database

The application uses **MySQL** with proper relational schema:
- `users`  
- `posts`  
- `comments` 

JPA handles entity mapping, and repositories manage queries.

## 🧪 Testing (TODO)

- Spring Boot integration tests  
- Unit tests for services  
- MockMvc for controller tests  

## 🐳 Deployment / DevOps

```
docker build -t blog-backend .
docker run -p 8080:8080 blog-backend
```

Future additions:
- Server deployment (DigitalOcean)

## 📦 Installation & Run

1. Clone the repo  
2. Configure `application.properties`:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/blog
   spring.datasource.username=...
   spring.datasource.password=...
   ```
3. Run:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
4. Open in browser:
   ```
   http://localhost:8080
   ```

## 📚 Purpose of the Project

This blog is part of a broader learning journey:
- Understanding real backend development  
- Building secure user systems  
- Practicing clean architectural design  
- Working with full-stack concepts end-to-end  
- Preparing for professional full-stack/Java positions  

## 📜 License

Free to use, modify, or extend.

## ✨ Future Improvements

- Adding AI chat to the site for fun and learning

## 👤 Author

**Zahaa Al-Khakani**  
Built as part of ongoing full-stack and backend development studies.
