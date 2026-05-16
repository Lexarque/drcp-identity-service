# drcp-identity-service — Local Development Guide

## 📑 Table of Contents
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Project Directory Structure](#-project-directory-structure)
- [Keycloak Setup](#-keycloak-setup)
- [Branch Naming Convention](#-branch-naming-convention)
- [Flyway Migration File Naming Convention](#-flyway-migration-file-naming-convention)

---

## 📦 Prerequisites

Make sure the following are installed on your local machine (Windows):

| Tool | Version |
| :--- | :--- |
| Docker | Latest |
| Java | 21+ |
| Maven | Latest (or use `mvnw`) |

---

## 🚀 Getting Started

### 1. Ensure Docker is installed and running
```bash
docker --version
```

### 2. Clone the project
```bash
git clone <this-repository-url>
cd drcp-identity-service
```

### 3. Set up environment variables
```bash
cp .env.example .env
```

Then edit the `.env` file and fill in the values:
```env
# PostgreSQL
DB_USER=drcp_admin
DB_PASSWORD=your_password
DB_NAME=drcp_identity
IDENTITY_SVC_DB_NAME=drcp_identity_svc
DB_PORT=5433

# Keycloak
KEYCLOAK_USER=admin
KEYCLOAK_PASSWORD=your_keycloak_password
KEYCLOAK_PORT=4001
KEYCLOAK_REALM=drcp
KEYCLOAK_CLIENT_ID=identity-service
KEYCLOAK_CLIENT_SECRET=           # Fill in after Keycloak setup (Step 4 below)

# Identity Service
IDENTITY_SERVICE_PORT=8081
FRONTEND_URL=http://localhost:5173
```

### 4. Start the infrastructure containers

> ⚠️ Start Docker containers **before** running the Quarkus service. Keycloak and PostgreSQL must be running first.

```bash
docker compose up -d
```

Verify all containers are healthy:
```bash
docker ps
```

You should see three containers running:
- `drcp-identity-db` — PostgreSQL
- `drcp-keycloak` — Keycloak
- `drcp-identity-service` — Quarkus (only when running via Docker)

### 5. Complete the Keycloak setup

> ⚠️ The Keycloak realm, clients, and roles must be configured before the Quarkus service can validate tokens. Follow the full [Keycloak Setup](#-keycloak-setup) section below.

### 6. Run the project in development mode

> 💡 For local development, run Quarkus directly on your machine while the infrastructure runs in Docker. This enables **live coding** — Quarkus automatically recompiles and reloads on every file save without restarting.

```bash
./mvnw quarkus:dev
# Windows:
mvnw.cmd quarkus:dev
```

The service starts at: `http://localhost:8900`

### 7. Daily development workflow

Once the volume data exists, use `stop` and `start` instead of `down` and `up` to preserve your Keycloak configuration:

```bash
# Stop containers (preserves all data)
docker compose stop

# Start containers again
docker compose start
```

> ⚠️ **Never use `docker compose down -v`** during development — this deletes all volumes including your Keycloak realm, clients, roles, and users. You would need to redo the entire Keycloak setup from scratch.

| Command | Containers | Volumes | Use when |
| :--- | :--- | :--- | :--- |
| `docker compose stop` | Stopped | ✅ Kept | Daily stop/start |
| `docker compose down` | Removed | ✅ Kept | Network reset needed |
| `docker compose down -v` | Removed | ❌ Deleted | Full reset only |

---

## 📁 Project Directory Structure

```text
.
├── src/
│   ├── main/
│   │   ├── java/                         # Java source files
│   │   │   └── org.acme/                 # Main package
│   │   │       ├── config/               # Keycloak Admin client config
│   │   │       ├── dto/                  # Data Transfer Objects
│   │   │       ├── resource/             # REST endpoints (JAX-RS resources)
│   │   │       └── service/              # Business logic
│   │   ├── docker/                       # Quarkus-generated Dockerfiles (JVM, Native)
│   │   └── resources/
│   │       ├── db/migration/             # Flyway migration SQL files
│   │       ├── import.sql                # Hibernate seed data (dev only)
│   │       └── application.properties    # Main configuration file
│   └── test/
│       └── java/                         # Integration and unit tests
├── init-db.sh                            # Creates drcp_identity_svc database on first run
├── postgres_data/                        # Local PostgreSQL data (Git ignored)
├── .env                                  # Local environment variables (Git ignored)
├── .env.example                          # Template for environment variables
├── .dockerignore                         # Files excluded from Docker build context
├── .gitignore                            # Files excluded from Git
├── docker-compose.yml                    # Infrastructure setup (PostgreSQL, Keycloak)
├── pom.xml                               # Maven project configuration
└── README.md                             # Project documentation
```

---

## 🔐 Keycloak Setup

Keycloak is the Identity Provider for the entire DRCP platform. It must be fully configured before the Quarkus service or React frontend will work. Complete these steps in order after starting the Docker containers.

Access the Keycloak Admin Console at: `http://localhost:4001`
Login with the credentials set in your `.env` — `KEYCLOAK_USER` and `KEYCLOAK_PASSWORD`.

---

### Step 1 — Create the `drcp` realm

> The `master` realm is for Keycloak administration only. All DRCP configuration lives in a dedicated realm.

- Top-left dropdown → **Create realm**
- Realm name: `drcp`
- Click **Create**

---

### Step 2 — Create realm roles

- Left sidebar → **Realm roles** → **Create role**
- Create the following roles one by one:

| Role | Purpose |
| :--- | :--- |
| `ADMIN` | Full platform access including user management |
| `COORDINATOR` | Can manage incidents and assign responders |
| `RESPONDER` | Field responder, can update incident status |

---

### Step 3 — Create the `drcp-frontend` client (React)

This is the **public client** used by the React frontend for login, logout, and SSO.

- Clients → **Create client**
- Client ID: `drcp-frontend`
- Client authentication: `OFF`
- Authentication flow: check **Direct access grants** only (needed for Postman testing)
- Click **Next** → **Save**
- Go to the **Settings** tab → scroll to **Access settings**:
  - Valid redirect URIs: `http://localhost:5173/*`
  - Web origins: `http://localhost:5173`
- Click **Save**

---

### Step 4 — Create the `identity-service` client (Quarkus backend)

This is the **confidential client** used by the Quarkus service to call the Keycloak Admin REST API for user management.

- Clients → **Create client**
- Client ID: `identity-service`
- Client authentication: `ON`
- Authentication flow: check **Service accounts roles** only
- Click **Next** → **Save**
- Go to the **Credentials** tab → copy the **Client Secret**
- Paste the secret into your `.env` file as `KEYCLOAK_CLIENT_SECRET`

**Assign Admin permissions to the service account:**
- Go to the **Service accounts roles** tab
- Click **Assign role**
- Change filter to **Filter by clients**
- Search for `realm-management`
- Assign both of these roles:
  - `manage-users`
  - `view-users`

> ⚠️ Without these roles, the Quarkus service will receive `401 Unauthorized` when calling the Keycloak Admin API.

---

### Step 5 — Enable user registration

- Left sidebar → **Realm settings** → **Login** tab
- User registration: `ON`
- Click **Save**

---

### Step 6 — Create the first admin user

At least one user with the `ADMIN` role must exist to access the user management features through the application.

- Left sidebar → **Users** → **Add user**
- Fill in: Username, Email, First name, Last name
- Email verified: `ON`
- Click **Create**
- Go to the **Credentials** tab → **Set password**
  - Enter a password
  - Temporary: `OFF`
  - Click **Save**
- Go to the **Role mapping** tab → **Assign role**
  - Filter by realm roles → select `ADMIN` → **Assign**

---

### Verifying the setup with Postman

**Get a token:**
```
POST http://localhost:4001/realms/drcp/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=drcp-frontend
username=<your_admin_email>
password=<your_admin_password>
scope=openid
```

Copy the `access_token` from the response.

**Test a protected endpoint:**
```
GET http://localhost:8900/api/admin/users
Authorization: Bearer <access_token>
```

A `200 OK` response with a user list confirms the full setup is working. A `403 Forbidden` means the user doesn't have the `ADMIN` role. A `401 Unauthorized` means the token is invalid or the Keycloak client secret is wrong.

> 💡 To inspect the roles inside a token, paste the `access_token` into [https://jwt.io](https://jwt.io) and look for the `realm_access.roles` array in the decoded payload.

---

## 🌿 Branch Naming Convention

Always use lowercase and separate words with hyphens (`kebab-case`).

**Format:** `type/short-description`

| Type | Purpose | Example |
| :--- | :--- | :--- |
| `feature/` | A new feature or functionality | `feature/user-authentication` |
| `fix/` | A bug fix | `fix/token-validation-error` |

---

## 🗄️ Flyway Migration File Naming Convention

All Flyway migration files live in `src/main/resources/db/migration/` and must follow this strict naming format:

**Format:** `V{version}__{description}.sql`

> ⚠️ Note the **double underscore** (`__`) between the version number and description.

### Version Numbering

Use a timestamp — do **not** reuse or modify existing version numbers.

| Type | Format | Example |
| :--- | :--- | :--- |
| Timestamped | `V{yyyyMMddHHmm}__description.sql` | `V202506011200__create_users_table.sql` |

### Naming Rules
- Always use **lowercase** with **underscores** for the description.
- Keep descriptions **short but meaningful** — they should reflect what the migration does.
- Never edit or delete a migration file that has already been applied.
- Repeatable migrations (run every time the checksum changes) use the prefix `R__` instead of a version number.

### Examples

```text
db/migration/
├── V20260101__create_audit_log_table.sql
├── V20260102__add_status_column_to_audit_log.sql
└── R__create_or_replace_views.sql     ← repeatable migration
```
