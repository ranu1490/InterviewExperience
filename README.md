# Interview Experience Portal

A production-oriented, full-stack **Interview Experience Portal** where candidates share and discover
real interview experiences. Built as **Spring Boot microservices** behind a **Spring Cloud Gateway**,
with an **Angular 19 + Angular Material** single-page frontend.

- **Anonymous users** can read, search, filter and sort interview experiences (no login required).
- **Authenticated users** can post experiences, edit/delete their own, like/unlike, comment and report spam.
- **Admins** can delete any post, review/resolve reports and ban users.

Every submitted experience is enriched by an **AI difficulty analyser** (mockable now, OpenAI later)
that computes a 1–10 difficulty score, an Easy/Medium/Hard label, a short summary and suggested topics.

---

## Table of contents

1. [Architecture](#architecture)
2. [Technology choices & rationale](#technology-choices--rationale)
3. [Why relational (not MongoDB)](#why-relational-not-mongodb)
4. [Why no Eureka](#why-no-eureka)
5. [Database design (ER diagram)](#database-design-er-diagram)
6. [Security model](#security-model)
7. [AI analysis](#ai-analysis)
8. [API contracts](#api-contracts)
9. [Project structure](#project-structure)
10. [Running locally](#running-locally)
11. [Scalability to ~1M users](#scalability-to-1m-users)
12. [Deployment considerations](#deployment-considerations)

---

## Architecture

```
                          ┌───────────────────────────┐
   Browser (Angular SPA)  │  Angular 19 + Material     │
        :4200 (dev)       │  lazy routes, JWT store    │
                          └─────────────┬─────────────┘
                                        │  HTTPS / REST (Bearer JWT)
                                        ▼
                          ┌───────────────────────────┐
                          │      API Gateway :8080     │  Spring Cloud Gateway (reactive)
                          │  CORS · edge JWT check ·   │
                          │  routing · header inject   │
                          └───────┬───────────┬────────┘
                                  │           │
                 /api/auth/**     │           │   /api/interviews/**
                 /api/users/**    │           │   /api/admin/**
                                  ▼           ▼
              ┌───────────────────────┐   ┌────────────────────────────┐
              │   user-service :8081  │   │  interview-service :8082    │
              │  auth, JWT, refresh,  │   │  CRUD, search, likes,       │
              │  Google, profile, ban │   │  comments, reports, AI      │
              └───────────┬───────────┘   └───────────────┬────────────┘
                          │                               │
                    ┌─────▼─────┐                   ┌─────▼─────┐
                    │  userdb   │                   │interviewdb│    H2 (dev) / MySQL (prod)
                    └───────────┘                   └───────────┘
```

Three deployables plus one shared library:

| Module | Port | Responsibility |
|---|---|---|
| `api-gateway` | 8080 | Single entry point: CORS, edge authentication, routing, forwarding identity headers |
| `user-service` | 8081 | Signup, login, Google login, JWT issuance + refresh rotation, profiles, roles, ban |
| `interview-service` | 8082 | Interview CRUD, search/filter/sort/pagination, likes, comments, reports, AI analysis |
| `common-security` | — | Shared JWT library (sign/verify) used by all three services |

Only **two** business microservices exist, split along the two things with genuinely different
lifecycles and scaling profiles: **identity** (write-light, security-critical) and **content**
(read-heavy, high-volume). No further services were added because there is no requirement that
justifies the extra operational cost — a deliberate anti-over-engineering choice.

---

## Technology choices & rationale

| Area | Choice | Why | Alternative considered |
|---|---|---|---|
| Language | Java 21 | Records, pattern matching, virtual-thread-ready | — |
| Framework | Spring Boot 3.3 | Batteries-included, huge ecosystem | Quarkus/Micronaut (less ubiquitous) |
| Gateway | Spring Cloud Gateway | Reactive, first-class Spring integration | Nginx (less app-aware) |
| Auth | Stateless JWT (HS256) + rotating refresh tokens | Horizontal scaling with no shared session store | Session cookies + Redis (stateful) |
| Persistence | Spring Data JPA | Derived queries, Specifications, less boilerplate | JOOQ/plain JDBC |
| DB (dev) | H2 in-memory | Zero-config, whole stack runs instantly | — |
| DB (prod) | MySQL + HikariCP | Mature, well understood, strong relational integrity | PostgreSQL (equally valid) |
| Frontend | Angular 19 standalone + Material | Reactive forms, signals, lazy loading, batteries-included UI | React (team preference dependent) |
| Docs | springdoc-openapi (Swagger UI) | Always-accurate, interactive API docs | Hand-written docs (drift) |

**Coding standards applied throughout:** constructor injection, DTO pattern (records), Bean Validation,
a global `@RestControllerAdvice` exception handler, SLF4J logging, Lombok on entities, `ResponseEntity`
with correct HTTP status codes, and interface + `*Impl` separation for services (Dependency Inversion).

---

## Why relational (not MongoDB)

The brief allowed MongoDB if "dynamic interview content" justified it. It does **not**, because the
data is strongly relational and integrity matters:

- **Likes** need a `UNIQUE(interview_id, user_id)` constraint to prevent duplicates atomically.
- **Comments/reports** reference an interview; we want referential clarity and easy aggregation.
- Search filters map cleanly to **indexed columns** and SQL `WHERE`/`ORDER BY`.

The only "dynamic" part is the variable set of question categories. That is handled cleanly in a
relational model by a single `interview_questions` table tagged with a `category` enum — instead of
~11 near-identical columns. So we keep the integrity and query power of SQL without any awkwardness.
**H2 in dev, MySQL in prod**, selected purely by Spring profile.

---

## Why no Eureka

Service discovery (Eureka) shines when you have **many, frequently-changing** service instances that
need to find each other dynamically. Here there is a **small, fixed** set of services. The gateway
routes to them by static, environment-overridable URIs (`USER_SERVICE_URI`, `INTERVIEW_SERVICE_URI`),
and in a container/orchestrator these become stable DNS names with the platform doing load balancing.
Adding Eureka now would be **operational overhead without benefit** — it can be introduced later if the
service count grows. This is documented as an intentional simplification.

---

## Database design (ER diagram)

Two independent schemas (one per service — services never share a database).

```
user-service (userdb)
┌──────────────────────────────┐        ┌───────────────────────────┐
│ users                        │        │ user_roles                │
│ PK id                        │1      *│ FK user_id  ── users.id   │
│ UQ username                  │────────│ role (USER|ADMIN)         │
│ UQ email                     │        └───────────────────────────┘
│ password (bcrypt, nullable)  │
│ full_name, bio, avatar_url   │        ┌───────────────────────────┐
│ provider (LOCAL|GOOGLE)      │1      *│ refresh_tokens            │
│ provider_id                  │────────│ PK id                     │
│ banned (bool)                │        │ UQ token                  │
│ created_at, updated_at       │        │ FK user_id                │
│ IDX(email), IDX(username)    │        │ expires_at, revoked       │
└──────────────────────────────┘        └───────────────────────────┘

interview-service (interviewdb)
┌───────────────────────────────────────────┐
│ interviews                                 │
│ PK id                                       │
│ company_name, job_role, experience_level    │      element collections (child tables,
│ years_of_experience, interview_date         │      FK interview_id):
│ location, ctc_offered, number_of_rounds     │        • interview_rounds
│ overall_experience, preparation_tips (LOB)  │        • interview_questions (category, question)
│ selection_status (SELECTED|REJECTED|        │        • interview_resources
│                   OFFER_REJECTED)           │        • interview_tags
│ difficulty_score, difficulty_label          │        • interview_ai_topics
│ ai_summary (LOB)                            │
│ author_id, author_username (denormalised)   │
│ total_likes, total_comments, views          │  ← denormalised counters
│ created_at, updated_at                      │
│ IDX on all filter & sort columns            │
└───────────────────────────────────────────┘
        │1                    │1                    │1
        │*                    │*                    │*
┌───────────────┐   ┌──────────────────┐   ┌──────────────────┐
│ interview_    │   │ comments         │   │ reports          │
│ likes         │   │ PK id            │   │ PK id            │
│ PK id         │   │ FK interview_id  │   │ FK interview_id  │
│ FK interview  │   │ user_id, username│   │ reporter_user_id │
│ user_id       │   │ content, created │   │ reason, status,  │
│ UQ(interview, │   │ IDX(interview_id)│   │ created_at       │
│    user)      │   └──────────────────┘   │ IDX(status)      │
│ IDX(interview)│                          └──────────────────┘
└───────────────┘
```

**Keys / constraints / indexes**
- **Primary keys**: auto-increment `id` on every table.
- **Unique**: `users.username`, `users.email`, `refresh_tokens.token`, `interview_likes(interview_id, user_id)`.
- **Foreign keys**: the element-collection child tables and (logically) likes/comments/reports point
  at `interviews.id`; `user_roles`/`refresh_tokens` point at `users.id`. Cross-service references
  (interview → author) are **denormalised ids**, not DB foreign keys, because they live in different databases.
- **Indexes**: every column used to filter (`company_name`, `job_role`, `experience_level`,
  `selection_status`, `location`, `interview_date`) or sort (`created_at`, `views`, `total_likes`,
  `difficulty_score`) plus `author_id` — so search stays fast at millions of rows.

---

## Security model

- **Stateless JWT.** Access tokens (15 min) carry `userId`, `username`, `email`, `roles` and a unique
  `jti`. No server session — any instance can serve any request.
- **Refresh tokens (7 days) are persisted, rotated and revocable.** Each refresh revokes the old token
  and issues a new pair; logout and ban revoke tokens server-side. This limits the blast radius of a
  stolen refresh token (access tokens are short-lived and simply expire).
- **Password hashing** with BCrypt (adaptive, salted).
- **Google login** via the SPA ID-token flow: the browser obtains a Google ID token, posts it to
  `POST /api/auth/google`, and the backend verifies it (Google `tokeninfo`, with audience check when
  `GOOGLE_CLIENT_ID` is set) and issues its own JWTs — fully stateless, no redirect/callback URLs.
- **Role-based access**: `USER`, `ADMIN`. Enforced at two layers (defence in depth):
  - **Gateway**: rejects unauthenticated writes early; anonymous callers may only issue `GET`.
  - **Each service**: validates the JWT itself and applies `@PreAuthorize`/ownership checks, so it is
    secure even if reached directly.
- **Ownership**: users may edit/delete only their own posts and comments; admins may delete any.

---

## AI analysis

`InterviewAnalyzer` is an interface with a swappable implementation (Strategy pattern):

- `MockInterviewAnalyzer` (default, `ai.provider=mock`) — deterministic, no external calls, fully
  testable. Difficulty is derived from concrete signals (rounds, question volume/breadth, seniority,
  presence of system-design topics), bucketed into Easy/Medium/Hard, with a generated summary and the
  distinct topics extracted from the post.
- A future `OpenAiInterviewAnalyzer` (`ai.provider=openai`) implements the **same contract** and is
  activated by configuration alone — no changes to the calling code.

**Where it fits:** it runs synchronously inside `interview-service` on create/update, enriching the
entity before save. AI-derived fields are never accepted from the client (no spoofing). At high volume
this call would move to an async worker behind a queue so a slow model never blocks the write.

---

## API contracts

All routes are exposed through the gateway at `http://localhost:8080`. Interactive docs:
`http://localhost:8081/swagger-ui.html` and `http://localhost:8082/swagger-ui.html`.

### Authentication (`user-service`)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | public | Register; returns token pair + user |
| POST | `/api/auth/login` | public | Login with username/email + password |
| POST | `/api/auth/google` | public | Login with a Google ID token |
| POST | `/api/auth/refresh-token` | public | Rotate refresh token → new pair |
| POST | `/api/auth/logout` | public | Revoke a refresh token |

### Users (`user-service`)
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/users/me` | user | Current profile |
| PUT | `/api/users/me` | user | Update own profile |
| GET | `/api/users/{id}` | user | Public profile |
| POST | `/api/users/{id}/ban` | admin | Ban a user (revokes their tokens) |
| POST | `/api/users/{id}/unban` | admin | Lift a ban |

### Interviews (`interview-service`)
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/interviews` | public | List/search: filters + `sort` + `page`/`size` |
| GET | `/api/interviews/search` | public | Alias of the above (documented contract) |
| GET | `/api/interviews/{id}` | public | Full detail (records a view) |
| GET | `/api/interviews/mine` | user | The caller's own posts |
| POST | `/api/interviews` | user | Create (AI analysis runs automatically) |
| PUT | `/api/interviews/{id}` | owner | Update own post |
| DELETE | `/api/interviews/{id}` | owner/admin | Delete |
| POST | `/api/interviews/{id}/like` | user | Like (409 if already liked) |
| DELETE | `/api/interviews/{id}/like` | user | Unlike |
| GET | `/api/interviews/{id}/comments` | public | List comments (paged) |
| POST | `/api/interviews/{id}/comments` | user | Add comment |
| DELETE | `/api/interviews/{id}/comments/{commentId}` | owner/admin | Delete comment |
| POST | `/api/interviews/{id}/report` | user | Report as spam/abuse |

### Admin (`interview-service`)
| Method | Path | Auth | Description |
|---|---|---|---|
| DELETE | `/api/admin/interviews/{id}` | admin | Delete any post (spam removal) |
| GET | `/api/admin/reports` | admin | Report queue (filter by `status`) |
| PUT | `/api/admin/reports/{id}?status=` | admin | Resolve/dismiss a report |

**Search filters** (`GET /api/interviews`): `keyword`, `company`, `role`, `experienceLevel`,
`minYearsOfExperience`, `maxYearsOfExperience`, `difficultyLabel`, `selectionStatus`, `location`,
`tag`, `dateFrom`, `dateTo` — combinable. **Sort**: `newest`, `oldest`, `mostViewed`, `mostHelpful`,
`highestDifficulty`.

---

## Project structure

```
backend/
  pom.xml                     # aggregator (Spring Boot parent, Spring Cloud BOM)
  common-security/            # shared JWT sign/verify library
  api-gateway/                # routing + edge auth (WebFlux)
  user-service/               # controller · service · serviceimpl · repository · entity
  interview-service/          #   · dto · mapper · security · config · exception · ai
frontend/
  src/app/
    core/{models,services,interceptors,guards}   # cross-cutting singletons
    features/{landing,auth,interview,profile,admin,not-found}  # lazy-loaded pages
    app.routes.ts             # lazy routes + guards
    app.config.ts             # HttpClient + interceptors + animations
    app.component.ts          # shell / navbar
```

Each backend package has a single responsibility: `controller` (thin HTTP adapters), `service`
(interfaces) + `serviceimpl` (business logic), `repository` (Spring Data), `entity` (JPA),
`dto` (API records), `mapper` (entity⇄DTO), `security` (JWT filter + config), `config` (beans,
OpenAPI, seeders), `exception` (typed errors + global handler), `ai` (analyser strategy).

---

## Running locally

Prerequisites: **JDK 21**, **Maven 3.8+**, **Node 20/22**.

### Backend (three terminals, or `&`)
```bash
cd backend
mvn clean package                       # builds all modules + runs tests

java -jar user-service/target/user-service-1.0.0.jar        # :8081
java -jar interview-service/target/interview-service-1.0.0.jar  # :8082
java -jar api-gateway/target/api-gateway-1.0.0.jar          # :8080
```
The default (`dev`) profile uses in-memory H2 and **seeds a demo admin and two sample interviews**.

Demo admin: **`admin` / `Admin@12345`**.

For MySQL, run with `--spring.profiles.active=prod` and set `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`.
Set a strong shared `JWT_SECRET` on every service.

### Frontend
```bash
cd frontend
npm install
npm start          # ng serve on http://localhost:4200 (proxies /api -> :8080)
```

Then open `http://localhost:4200`.

---

## Scalability to ~1M users

- **Stateless authentication** → any service instance serves any request; scale out freely.
- **Horizontal scaling** → run N replicas of each service behind the gateway/load balancer.
- **Load balancing** → gateway + platform DNS/round-robin across replicas.
- **Connection pooling** → HikariCP (bounded pool per instance) instead of a connection per request.
- **Database indexing** → every filter/sort column is indexed (see ER diagram).
- **Pagination** → every list endpoint is paged (`page`/`size`, hard-capped) via Spring `Pageable`;
  the client never loads more than one page.
- **API optimisation** → slim `InterviewSummaryResponse` for lists vs full `InterviewResponse` for
  detail; denormalised `total_likes/total_comments/views` counters avoid aggregate queries on reads.
- **Caching / Redis** → reads dominate; hot endpoints (interview detail, first search pages) are ideal
  for a Redis/CDN cache. A distributed refresh-token/blacklist store also fits Redis. (Not implemented;
  documented as the next step.)
- **CDN** → serve the Angular static bundle and company logos from a CDN close to users.
- **Read replicas** → route read traffic to MySQL read replicas, writes to the primary.
- **DB optimisation** → short transactions, `open-in-view=false`, atomic counter `UPDATE`s to avoid
  lost updates, and async AI at high volume.

---

## Deployment considerations (explained, not implemented)

- **Containerise** each service (JDK 21 base image) and the built Angular bundle (served by Nginx/CDN).
- **AWS**: services on ECS/Fargate or EC2 behind an ALB; MySQL on RDS (Multi-AZ + read replicas);
  ElastiCache (Redis) for caching; secrets in AWS Secrets Manager; CloudFront CDN for the SPA and assets.
- **Config** via environment variables (12-factor): DB creds, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, service URIs.
- **Observability**: Spring Boot Actuator health/metrics, centralised logs, request tracing.
- Kubernetes is intentionally out of scope for this project.

---

## Tests

- `JwtServiceTest` — token round-trip, tamper rejection, access/refresh types.
- `MockInterviewAnalyzerTest` — difficulty scoring bounds, label derivation, topic extraction.
- Spring context smoke tests for all three services (`mvn test`).

Run: `cd backend && mvn test`.
