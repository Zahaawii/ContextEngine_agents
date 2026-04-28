# ContextEngine_Agents

ContextEngine_Agents is a full-stack platform combining a blog/social media
application with a RAG-based AI chatbot.

The system integrates traditional web architecture (Spring + MySQL) with
vector-based AI retrieval and controlled tool access via an MCP server.

This repository contains all services, infrastructure configuration, and
CI/CD pipelines in a structured mono-repository.

------------------------------------------------------------------------

## Overview

The platform consists of the following components:

-   **Blog Backend** --- Spring Boot REST API
-   **Blog Frontend** --- Vanilla JavaScript client
-   **PersonalChatBot** --- Node.js (Express) RAG chatbot
-   **MCP Server** --- Spring-based Model Context Protocol server
-   **Database** --- MySQL

The chatbot is designed to:

-   Answer questions only when relevant data exists in the vector
    database
-   Retrieve blog content
-   Create blog posts through controlled access
-   Operate strictly within defined retrieval constraints

All services are containerized and deployed via Docker Compose. Images
are built and published to GitHub Container Registry (GHCR) through
GitHub Actions workflows.

------------------------------------------------------------------------

## Architecture

### High-Level Design

Client\
    \|\
Apache (Reverse Proxy)\
    \|\
-------------------------------------------------\
Blog Backend (Spring) --- /api\
Node Backend (Express) --- /node\
MCP Server (Spring) --- /mcp\
-------------------------------------------------\
    \|\
MySQL + Vector Database

### Architectural Principles

-   Apache handles public traffic and TLS termination.
-   All containers bind to 127.0.0.1 only.
-   No container ports are publicly exposed.
-   The server never builds application code.
-   Deployments use immutable container images.
-   Staging and production run in parallel on the same VM using separate
    Docker Compose projects and ports.

------------------------------------------------------------------------

## Repository Structure

ContextEngine_Agents/

apps/\
- blog-backend/\
- blog-frontend/\
- personalchatbot/\
- mcp-server/

infra/\
- compose/\
- docker-compose.prod.yml\
- docker-compose.staging.yml\
- scripts/\
- deploy.sh

.github/\
- workflows/\
- release.yml

------------------------------------------------------------------------

## Technology Stack

### Backend

-   Java
-   Spring Boot
-   Spring MVC
-   JPA / JDBC
-   MySQL

### Frontend

-   Vanilla JavaScript
-   HTML
-   CSS

### AI / RAG

-   Node.js (Express)
-   Vector database integration
-   Model Context Protocol (MCP)

### Infrastructure

-   Docker
-   Docker Compose
-   Apache HTTP Server
-   GitHub Actions
-   GitHub Container Registry (GHCR)
-   Linux VM

------------------------------------------------------------------------

## Environments

### Production

-   Triggered by pushes to `main`
-   Uses GHCR `:prod` image tags
-   Bound to fixed localhost ports
-   Public traffic routed through Apache

### Staging

-   Triggered by pushes to `develop`
-   Uses GHCR `:staging` image tags
-   Runs in parallel on separate localhost ports

Both environments run simultaneously on the same virtual machine using
separate Docker Compose projects.

------------------------------------------------------------------------

## CI/CD Pipeline

### Build and Release Flow

Push to `develop`: - Build all service images - Tag with `:staging` and
`:sha-<commit>` - Push to GHCR - Deploy to staging

Push to `main`: - Build all service images - Tag with `:prod` and
`:sha-<commit>` - Push to GHCR - Deploy to production

Deployment consists of:

-   docker compose pull
-   docker compose up -d

Images are immutable and versioned.

------------------------------------------------------------------------

## Local Development

### Requirements

-   Docker
-   Docker Compose
-   Java (for Spring services)
-   Node.js (for chatbot if running outside Docker)
-   MySQL (if not using containerized database)

### Run

docker compose -f infra/compose/docker-compose.staging.yml up --build

------------------------------------------------------------------------

## Environment Variables

Environment variables are not committed to the repository.

Server structure:

/opt/contextengine_agents/prod/.env\
/opt/contextengine_agents/staging/.env

Typical variables:

-   Database credentials
-   API keys
-   Vector database configuration
-   Application secrets
-   GHCR authentication (server side)

------------------------------------------------------------------------

## Security Considerations

-   Containers bind only to 127.0.0.1
-   Apache manages TLS termination
-   No secrets stored in Git
-   Separate staging and production ports
-   Strict image versioning via GHCR
-   Controlled AI scope through RAG filtering and MCP enforcement

------------------------------------------------------------------------

## Project Context

ContextEngine_Agents is developed as a personal and academic project focused
on:

-   Full-stack architecture
-   AI integration in traditional web systems
-   CI/CD automation
-   Containerized deployment strategies
-   Secure reverse proxy routing

------------------------------------------------------------------------

## License

Personal and academic project.
