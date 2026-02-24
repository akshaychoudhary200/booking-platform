# User Authentication & JWT Security Implementation

This document describes the implementation of the authentication and authorization system in the `booking-service`. The architecture uses **Spring Security 6** and **JSON Web Tokens (JWT)** for stateless, scalable authentication.

## 🎯 Architecture Overview

We have organized the codebase using the **Package-by-Feature** approach to maintain high cohesion and encapsulation.

The feature is split across three main packages:
1. `user/`: The core domain representing the `User` and their permissions (`Role`).
2. `config/`: The Spring Security configuration and JWT utility classes.
3. `auth/`: The REST endpoints and business logic for registering and authenticating users.

---

## 🛠️ Step-by-Step Implementation

Here is an explanation of what we built and why:

### 1. The Core Domain (`org...user`)
Before we could authenticate users, we needed a way to represent them.
* **`User.java`**: A JPA Entity mapped to a `users` table in PostgreSQL. Crucially, it implements Spring Security's `UserDetails` interface. This allows Spring to understand that this entity represents a logged-in user and how to retrieve their username (`email`), password, and granted authorities (roles).
* **`Role.java`**: An enum defining `USER` and `ADMIN` permissions for Role-Based Access Control (RBAC).
* **`UserRepository.java`**: A standard Spring Data JPA repository. It includes `findByEmail(String email)` because the email acts as the username for login.

### 2. Token Management (`org...config.JwtService`)
We cannot use standard HTTP Sessions (`JSESSIONID`) because they require the server to store state in memory, which scales poorly in microservice architectures. Instead, we use JWTs.
* **`JwtService.java`**: This utility class uses the `jjwt` library to generate and parse tokens.
  * **Generation**: When a user logs in, it creates a JWT containing their `email` (as the "subject") and signs the token using the secret key defined in `application.yml`.
  * **Validation**: When a user sends a token to a protected endpoint, this class verifies the signature (to ensure it wasn't tampered with) and checks that it hasn't expired.

### 3. Securing the Request Lifecycle (`org...config`)
* **`ApplicationConfig.java`**: Holds the core Spring Security beans.
  * `UserDetailsService`: Tells Spring how to load user data from the database.
  * `AuthenticationProvider`: We configure a `DaoAuthenticationProvider` using our `UserDetailsService` and a `BCryptPasswordEncoder` to securely compare the requested password against the hashed password in the database.
* **`JwtAuthenticationFilter.java`**: This class extends `OncePerRequestFilter`. It intercepts *every single incoming HTTP request*.
  1. It checks for the `Authorization` header.
  2. If it finds a `Bearer` token, it hands it to `JwtService` to extract the email.
  3. It loads the `UserDetails` from the database.
  4. If the token is valid, it manually populates the Spring `SecurityContextHolder` with an `AuthenticationToken`, telling Spring "this user is authenticated!"
* **`SecurityConfiguration.java`**: The grand central station of our security setup.
  * We build the `SecurityFilterChain`.
  * We disable CSRF (Cross-Site Request Forgery) because JWTs and stateless APIs are immune to it.
  * We enforce `SessionCreationPolicy.STATELESS` so Spring never creates an HTTP session.
  * We whitelist the `/api/v1/auth/**` endpoints so anyone can register or log in. All other endpoints are locked down.
  * Finally, we insert our custom `JwtAuthenticationFilter` *before* the default Spring filter.

### 4. The Endpoints (`org...auth`)
* **Data Transfer Objects (DTOs)**: We created `RegisterRequest`, `AuthenticationRequest`, and `AuthenticationResponse` to clearly define the JSON contract for the API rather than binding raw Entities to the controller layer.
* **`AuthenticationService.java`**: Holds the business logic.
  * `register()`: Creates a new `User`, hashes the password with BCrypt, saves it to the DB, and generates the initial JWT.
  * `authenticate()`: Uses Spring's `AuthenticationManager` to securely verify the credentials. If successful, it fetches the user from the DB and generates a new JWT token.
* **`AuthenticationController.java`**: Exposes the actual HTTP `POST` endpoints (`/register` and `/authenticate`) to the outside world.

---

## 🔒 Best Practices Followed
1. **Stateless Authentication**: By using JWTs and configuring `STATELESS` sessions, the backend scales effortlessly.
2. **Password Hashing**: Passwords are never stored in plain-text. They are hashed using BCrypt.
3. **Package-by-Feature Design**: Code is grouped by its purpose (`auth`, `user`, `config`) rather than its technical layer (`controllers`, `services`, `repositories`), making the microservice highly cohesive and easy to navigate or extract later.
4. **DTO Pattern**: Using Data Transfer Objects prevents overposting attacks and decouples the internal Domain model from the external API contract.
