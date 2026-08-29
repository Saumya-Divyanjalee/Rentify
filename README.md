# 🚗 Rentify - Vehicle Rental Platform with PayHere Payment Gateway Integration

<p align="center">
  <strong>A full-stack vehicle rental platform with secure JWT authentication, PayHere payment integration, real-time availability validation, automated email notifications, interactive maps, and admin management.</strong>
</p>

<p align="center">
  <a href="https://github.com/Saumya-Divyanjalee/Rentify">
    <img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="GitHub">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?style=flat-square&logo=springsecurity" alt="Spring Security">
  <img src="https://img.shields.io/badge/PayHere-Sandbox-red?style=flat-square" alt="PayHere">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="MIT License">
</p>

---

## 📌 Overview

**Rentify** is a full-stack vehicle rental management platform designed to simplify the complete vehicle rental workflow - from browsing available vehicles and creating bookings to processing online payments and managing the fleet.

The application combines a **Spring Boot REST API** with a lightweight web frontend and integrates several real-world services, including **PayHere**, **JWT-based authentication**, **Gmail SMTP**, **Leaflet/OpenStreetMap**, and browser-side PDF generation.

The platform is designed with a focus on **security, transaction integrity, maintainability, and real-world business workflows**.

---

## ✨ Highlights

<table>
<tr>
<td width="50%">

### 🔐 Secure Authentication

* JWT-based stateless authentication
* Spring Security
* BCrypt password hashing
* Role-based authorization
* `USER` and `ADMIN` roles

</td>
<td width="50%">

### 💳 Online Payments

* PayHere Sandbox integration
* Server-side MD5 verification
* Payment webhooks
* Automatic booking confirmation
* Payment status handling

</td>
</tr>

<tr>
<td width="50%">

### 🚘 Vehicle Rental

* Vehicle browsing
* Real-time availability
* Date-based booking
* Automatic rental price calculation
* Insurance validation
* Booking conflict detection

</td>
<td width="50%">

### 📧 Automated Notifications

* Registration emails
* Login security alerts
* Payment confirmations
* Transaction-related notifications
* Fault-tolerant email processing

</td>
</tr>

<tr>
<td width="50%">

### 🗺️ Location Services

* Interactive Leaflet map
* OpenStreetMap integration
* Pickup location selection
* Drop-off location selection
* Reverse geocoding

</td>
<td width="50%">

### 📊 Admin Management

* Fleet management
* Booking management
* Insurance management
* Driver management
* AI-powered revenue & booking forecasting
* PDF reports and analytics

</td>
</tr>
</table>

---

# 🚀 Core Features

## 💳 PayHere Payment Integration

Rentify implements a complete **PayHere Sandbox payment workflow**.

### Payment capabilities

* PayHere checkout integration
* Server-side MD5 hash generation
* Hash verification
* Payment webhook processing
* Payment status handling
* Automatic booking confirmation
* Automatic vehicle status update
* Payment confirmation emails

Supported payment states include:

```text
SUCCESS
PENDING
FAILED
CANCELLED
CHARGEDBACK
```

The application processes the payment webhook through a public development tunnel using **ngrok**.

---

## 🔐 Authentication & Authorization

Security is implemented using **Spring Security, JWT, and BCrypt**.

### Authentication flow

```text
User
 │
 ▼
Login / Registration
 │
 ▼
Spring Security
 │
 ▼
BCrypt Password Verification
 │
 ▼
JWT Token
 │
 ▼
Authenticated API Requests
 │
 ▼
JWTAuthFilter
 │
 ▼
Role-Based Authorization
```

### Security features

* Stateless JWT authentication
* JWT expiration
* BCrypt password hashing
* Custom JWT authentication filter
* Role-based authorization
* CORS configuration
* Protected REST endpoints

The project uses separate `USER` and `ADMIN` roles to control access to application resources.

---

# 🚘 Vehicle Booking System

Rentify performs multiple validations before confirming a vehicle booking.

### Booking validation pipeline

```text
                    Booking Request
                           │
                           ▼
                ┌─────────────────────┐
                │ Vehicle Availability│
                └──────────┬──────────┘
                           │
                           ▼
                ┌─────────────────────┐
                │ Insurance Status    │
                └──────────┬──────────┘
                           │
                           ▼
                ┌─────────────────────┐
                │ Insurance Expiry    │
                └──────────┬──────────┘
                           │
                           ▼
                ┌─────────────────────┐
                │ Date Overlap Check  │
                └──────────┬──────────┘
                           │
                           ▼
                  Booking Approved
                           │
                           ▼
                    Transaction
                           │
                  ┌────────┴────────┐
                  ▼                 ▼
             Save Booking      Update Vehicle
```

### Booking logic includes

1. Vehicle status validation
2. Insurance availability validation
3. Insurance expiry validation
4. Date-overlap detection using custom JPQL
5. Transactional booking creation
6. Automatic price calculation
7. Vehicle status updates
8. Automatic availability refresh

The booking process uses `@Transactional` to keep related database operations atomic.

---

# 🗺️ Interactive Location Selection

The frontend integrates **Leaflet.js** and **OpenStreetMap** to provide interactive location selection.

### Capabilities

* Click directly on the map
* Select pickup location
* Select drop-off location
* Automatically capture coordinates
* Reverse geocode coordinates into addresses
* Display visual markers

```text
User selects location
        │
        ▼
    Leaflet Map
        │
        ▼
Latitude + Longitude
        │
        ▼
 Nominatim API
        │
        ▼
Readable Address
        │
        ▼
Booking Form
```

No paid map API key is required for the configured OpenStreetMap-based integration.

---

# 📧 Automated Email Notifications

Rentify includes an automated email notification system using **Spring Mail / Gmail SMTP**.

### Email events

| Event                 | Notification                 |
| --------------------- | ---------------------------- |
| 👤 Registration       | Welcome email                |
| 🔐 Login              | Security alert               |
| 💳 Successful payment | Payment confirmation         |
| 🚘 Booking completion | Booking-related notification |

Email processing is designed so that an email delivery failure does **not** interrupt or roll back a successful payment transaction.

---

# 📊 PDF Reporting

Administrators can generate booking reports directly in the browser.

### Reporting features

* Browser-side PDF generation
* jsPDF
* autoTable
* Booking history
* Date-range filtering
* Statistics
* Instant PDF download

No server-side PDF processing is required.

# 🤖 AI-Powered Forecasting

Rentify includes a browser-side AI forecasting engine that predicts next month's revenue and booking volume from historical trends.

### Forecasting capabilities

* Simple Moving Average (SMA) calculation
* Weighted Moving Average (WMA) with 20/30/50 weighting
* Linear trend regression (least-squares)
* Blended forecast model (WMA 60% + Linear 40%)
* Weekend-demand multiplier adjustment
* Confidence scoring based on data variance
* Fleet utilization analysis
* Revenue growth tracking (month-over-month)
* Vehicle category demand signals
* Downloadable AI Forecast PDF report

No external AI API is required — all forecasting runs client-side using historical booking data pulled from the analytics endpoint.

---

# 👑 Admin Panel

The administrator has access to the main management operations of the platform.

### Admin capabilities

* 🚘 Vehicle fleet management
* 📅 Booking management
* 🛡️ Insurance management
* 👨‍✈️ Driver management
* 👥 User management
* 📊 Dashboard statistics
* 📄 PDF report generation
* 🔎 Booking status filtering

---

# 🏗️ System Architecture

Rentify follows a **layered backend architecture** based on Spring Boot.

```text
┌─────────────────────────────────────────────┐
│                 FRONTEND                    │
│                                             │
│      HTML5 + CSS3 + jQuery + Ajax          │
│      jsPDF + Leaflet + OpenStreetMap        │
└──────────────────────┬──────────────────────┘
                       │
                       │ HTTP / REST
                       ▼
┌─────────────────────────────────────────────┐
│                 BACKEND                     │
│              Spring Boot 3.2               │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │          Security Layer               │  │
│  │      JWT + BCrypt + CORS              │  │
│  └───────────────────┬───────────────────┘  │
│                      ▼                      │
│  ┌───────────────────────────────────────┐  │
│  │       Controller Layer                │  │
│  │         REST Controllers              │  │
│  └───────────────────┬───────────────────┘  │
│                      ▼                      │
│  ┌───────────────────────────────────────┐  │
│  │          Service Layer                │  │
│  │      Business Logic + Validation      │  │
│  └───────────────────┬───────────────────┘  │
│                      ▼                      │
│  ┌───────────────────────────────────────┐  │
│  │         Repository Layer              │  │
│  │     Spring Data JPA + JPQL            │  │
│  └───────────────────┬───────────────────┘  │
│                      ▼                      │
│  ┌───────────────────────────────────────┐  │
│  │           Entity Layer                │  │
│  │        JPA / Hibernate                │  │
│  └───────────────────┬───────────────────┘  │
└──────────────────────┼──────────────────────┘
                       ▼
              ┌─────────────────┐
              │     MySQL 8     │
              │                 │
              │ Users           │
              │ Vehicles        │
              │ Bookings        │
              │ Payments        │
              │ Insurances      │
              │ Drivers         │
              └─────────────────┘
```

The backend is organized into **Config → Controller → Service → Repository → Entity**, with DTOs used for safe data transfer and centralized exception handling through `GlobalExceptionHandler`.

---

# 🛠️ Technology Stack

## Backend

| Technology            | Purpose                        |
| --------------------- | ------------------------------ |
| **Java 17**           | Core programming language      |
| **Spring Boot 3.2**   | Backend framework              |
| **Spring Security**   | Authentication & authorization |
| **JWT / JJWT**        | Stateless authentication       |
| **Spring Data JPA**   | Persistence abstraction        |
| **Hibernate**         | ORM                            |
| **MySQL 8**           | Relational database            |
| **Spring Mail**       | Email notifications            |
| **Lombok**            | Boilerplate reduction          |
| **Swagger / OpenAPI** | API documentation              |

## Frontend

| Technology        | Purpose                |
| ----------------- | ---------------------- |
| **HTML5**         | Page structure         |
| **CSS3**          | Styling                |
| **jQuery 3.7**    | Frontend interactions  |
| **Ajax**          | REST API communication |
| **jsPDF**         | PDF generation         |
| **autoTable**     | PDF table generation   |
| **Leaflet.js**    | Interactive maps       |
| **OpenStreetMap** | Map data               |

## External Services & Tools

| Technology          | Purpose                   |
| ------------------- | ------------------------- |
| **PayHere Sandbox** | Online payment processing |
| **ngrok**           | Local webhook tunneling   |
| **Nominatim**       | Reverse geocoding         |
| **XAMPP**           | Local MySQL environment   |
| **Postman**         | API testing               |

---

# 📁 Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── lk/ijse/aad/backend/
│   │       │
│   │       ├── config/
│   │       │   ├── ApplicationConfig.java
│   │       │   ├── SecurityConfig.java
│   │       │   ├── PayHereConfig.java
│   │       │   └── SwaggerConfig.java
│   │       │
│   │       ├── controller/
│   │       │   └── ...  REST Controllers
│   │       │
│   │       ├── dto/
│   │       │   └── ...  Data Transfer Objects
│   │       │
│   │       ├── entity/
│   │       │   └── ...  JPA Entities
│   │       │
│   │       ├── enums/
│   │       │   └── ...  Application Enums
│   │       │
│   │       ├── exception/
│   │       │   └── GlobalExceptionHandler.java
│   │       │
│   │       ├── repository/
│   │       │   └── ...  JPA Repositories
│   │       │
│   │       ├── service/
│   │       │   ├── custom/
│   │       │   └── impl/
│   │       │
│   │       └── util/
│   │           ├── JWTUtil.java
│   │           ├── JWTAuthFilter.java
│   │           ├── PayHereHashUtil.java
│   │           ├── APIResponse.java
│   │           └── PicEncoder.java
│   │
│   └── resources/
│       ├── static/
│       │   ├── payment-success.html
│       │   └── payment-cancel.html
│       │
│       ├── application.properties
│       └── logback-spring.xml
│
└── frontend/
    └── ... HTML / CSS / JavaScript
```

The uploaded project structure documents the backend configuration, controllers, DTOs, entities, repositories, services, utilities, and static payment pages.

---

# 🚀 Getting Started

## Prerequisites

Install the following before running the application:

* Java 17+
* Maven
* MySQL 8.0
* XAMPP *(optional, for local MySQL)*
* PayHere Sandbox account
* ngrok account

---

## 1. Clone the Repository

```bash
git clone https://github.com/Saumya-Divyanjalee/Rentify.git
cd Rentify
```

---

## 2. Create the Database

Create the Rentify database in MySQL:

```sql
CREATE DATABASE rentify_db;
```

Hibernate will create the required tables when the application starts.

---

## 3. Configure Application Properties

Create/update your `application.properties`:

```properties
# ==============================
# DATABASE
# ==============================
spring.datasource.url=jdbc:mysql://localhost:3306/rentify_db
spring.datasource.username=root
spring.datasource.password=your_password

# ==============================
# JWT
# ==============================
jwt.secret=your_jwt_secret_key_min_32_characters
jwt.expiration=864000000

# ==============================
# EMAIL
# ==============================
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password

# ==============================
# PAYHERE
# ==============================
payhere.merchant-id=your_merchant_id
payhere.merchant-secret=your_merchant_secret
payhere.sandbox=true

payhere.notify-url=https://your-ngrok-domain/api/v1/payments/payhere/notify
payhere.return-url=http://localhost:8081/payment-success.html
payhere.cancel-url=http://localhost:8081/payment-cancel.html
```

> ⚠️ **Never commit real passwords, JWT secrets, merchant credentials, or Gmail App Passwords to GitHub.**

---

## 4. Start ngrok

For local PayHere webhook testing:

```bash
ngrok http --domain=your-static-domain.ngrok-free.app 8081
```

The generated public URL must be configured as the PayHere notification URL.

---

## 5. Run the Backend

```bash
mvn spring-boot:run
```

---

## 6. Open the Frontend

Open the frontend through IntelliJ IDEA's built-in server or another local web server.

The original project configuration uses port `63342` for the frontend during development.

---

# 📡 REST API

## 🔐 Authentication

| Method | Endpoint              | Access |
| ------ | --------------------- | ------ |
| `POST` | `/api/v1/auth/signup` | Public |
| `POST` | `/api/v1/auth/signin` | Public |

---

## 🚘 Vehicles

| Method   | Endpoint                | Access |
| -------- | ----------------------- | ------ |
| `GET`    | `/api/v1/vehicles`      | Public |
| `POST`   | `/api/v1/vehicles`      | Admin  |
| `PUT`    | `/api/v1/vehicles/{id}` | Admin  |
| `DELETE` | `/api/v1/vehicles/{id}` | Admin  |

---

## 📅 Bookings

| Method | Endpoint                     | Access        |
| ------ | ---------------------------- | ------------- |
| `POST` | `/api/v1/bookings`           | User          |
| `GET`  | `/api/v1/bookings/{id}`      | Authenticated |
| `GET`  | `/api/v1/bookings/user/{id}` | Authenticated |
| `PUT`  | `/api/v1/bookings/{id}`      | Authenticated |

---

## 💳 Payments

| Method | Endpoint                            | Access          |
| ------ | ----------------------------------- | --------------- |
| `POST` | `/api/v1/payments/payhere/initiate` | Authenticated   |
| `POST` | `/api/v1/payments/payhere/notify`   | PayHere Webhook |
| `GET`  | `/api/v1/payments/booking/{id}`     | Authenticated   |

---

## 👑 Administration

| Method | Endpoint                  | Access |
| ------ | ------------------------- | ------ |
| `GET`  | `/api/v1/admin/dashboard` | Admin  |
| `GET`  | `/api/v1/user`            | Admin  |
| `GET`  | `/api/v1/insurances`      | Admin  |
| `POST` | `/api/v1/insurances`      | Admin  |
| `GET`  | `/api/v1/bookings/analytics` | Admin  |
---

## 📖 API Documentation

Once the backend is running, Swagger UI is available at:

```text
http://localhost:8081/swagger-ui/index.html
```

---

# 💳 PayHere Payment Flow

```text
┌──────────────────────┐
│ Select Vehicle       │
│ + Rental Dates       │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Create Booking       │
│ Status: PENDING      │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Initiate PayHere     │
│ Generate MD5 Hash    │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ PayHere Checkout     │
└──────────┬───────────┘
           ▼
      ┌────┴────┐
      ▼         ▼
 Browser       Webhook
 Redirect      Notification
      │         │
      │         ▼
      │   Verify MD5 Hash
      │         │
      │         ▼
      │   Payment COMPLETED
      │         │
      │         ▼
      │   Booking CONFIRMED
      │         │
      │         ▼
      │   Vehicle BOOKED
      │         │
      │         ▼
      │   Email Confirmation
      │
      ▼
Payment Success Page
```

The PayHere workflow uses both the browser redirect and server-side webhook notification, with the backend verifying the payment before updating booking and vehicle states.

---

# 🌍 Environment Configuration

| Property                     | Description                 |
| ---------------------------- | --------------------------- |
| `spring.datasource.url`      | MySQL connection URL        |
| `spring.datasource.username` | MySQL username              |
| `spring.datasource.password` | MySQL password              |
| `jwt.secret`                 | JWT signing secret          |
| `jwt.expiration`             | JWT expiration time         |
| `spring.mail.username`       | Gmail SMTP account          |
| `spring.mail.password`       | Gmail App Password          |
| `payhere.merchant-id`        | PayHere merchant ID         |
| `payhere.merchant-secret`    | PayHere merchant secret     |
| `payhere.sandbox`            | PayHere sandbox/live mode   |
| `payhere.notify-url`         | PayHere webhook URL         |
| `payhere.return-url`         | Successful payment redirect |
| `payhere.cancel-url`         | Cancelled payment redirect  |

---

# 🐛 Engineering Challenges Solved

During development, several real-world integration and backend issues were identified and resolved.

| Problem                             | Root Cause                    | Solution                                    |
| ----------------------------------- | ----------------------------- | ------------------------------------------- |
| PayHere credentials were `null`     | Incorrect `@Value` import     | Replaced with Spring `@Value`               |
| Payment API returned `500`          | Missing `@RestController`     | Added controller annotation                 |
| PayHere webhook returned `401`      | Security rule ordering        | Corrected `permitAll()` configuration       |
| Webhook returned `404`              | Incorrect notification URL    | Corrected endpoint path                     |
| PayHere rejected requests           | Localhost configuration       | Configured PayHere dashboard                |
| MD5 mismatch                        | IntelliJ query parameters     | Added clean return URL handling             |
| Payment success page returned `404` | Static page routing issue     | Moved pages to Spring Boot static resources |
| Success-page buttons failed         | Incorrect frontend paths      | Corrected frontend base path handling       |
| Payment email failed                | `LazyInitializationException` | Pre-loaded required data                    |

These fixes demonstrate practical debugging across **Spring Security, PayHere webhooks, URL routing, Hibernate lazy loading, and payment processing**.

---

# 🧪 Testing

The REST API can be tested using:

* Postman
* Swagger UI
* PayHere Sandbox

### Areas tested

* Authentication
* JWT authorization
* Vehicle CRUD
* Booking creation
* Booking validation
* Payment initiation
* Payment webhook
* Payment status handling
* Email notifications
* Admin operations

---

# 🔒 Security Considerations

The application implements several security measures:

* JWT-based stateless authentication
* BCrypt password hashing
* Role-based authorization
* Protected REST endpoints
* Server-side payment hash verification
* CORS configuration
* No plain-text password storage

### Production recommendations

Before deploying to production:

* Move secrets to environment variables
* Use HTTPS
* Replace PayHere Sandbox with production credentials
* Restrict CORS origins
* Use a production database user instead of `root`
* Configure secure JWT secret management
* Protect webhook endpoints appropriately
* Disable debug/development configurations

---

# 🧠 What This Project Demonstrates

This project demonstrates practical knowledge of:

* Java 17
* Spring Boot
* Spring Security
* JWT Authentication
* BCrypt
* RESTful API development
* Spring Data JPA
* Hibernate ORM
* MySQL
* DTO architecture
* Layered architecture
* Transaction management
* JPQL
* Global exception handling
* Payment gateway integration
* Webhook processing
* Email automation
* Interactive maps
* Reverse geocoding
* PDF report generation
* API documentation
* Postman testing
* Git & GitHub

---

# 📸 Screenshots

 

### 🏠 Home / Vehicle Listing

<img width="1917" height="921" alt="Screenshot 2026-08-09 011928" src="https://github.com/user-attachments/assets/6d0789d0-25b8-4a5e-acb9-ca976c56479a" />


### 🔐 Login

<img width="1917" height="906" alt="Screenshot 2026-08-09 011947" src="https://github.com/user-attachments/assets/c0241144-d38c-4e89-a1fb-468373b3aee1" />


### 🚘 Vehicle Details

<img width="1917" height="856" alt="Screenshot 2026-08-09 012501" src="https://github.com/user-attachments/assets/c4a49e83-5624-4422-9863-c4aaa9af63c4" />
 


### 📅 Booking

<img width="1917" height="861" alt="Screenshot 2026-08-09 012419" src="https://github.com/user-attachments/assets/9b96d2c0-b49e-49f2-82df-c326696e2ac4" />


### 💳 PayHere Checkout

<img width="1915" height="872" alt="Screenshot 2026-08-09 012726" src="https://github.com/user-attachments/assets/a63c8ceb-56bf-454e-9fb8-0de14b905fde" />


### 👑 Admin Dashboard

<img width="1917" height="855" alt="Screenshot 2026-08-09 012921" src="https://github.com/user-attachments/assets/85364c33-6d07-4a4d-8c46-829d09c5073e" />
<img width="1917" height="861" alt="Screenshot 2026-08-09 012933" src="https://github.com/user-attachments/assets/78567361-db99-4e06-9a6b-568abf748a38" />


### 📊 Reports

<img width="1916" height="857" alt="Screenshot 2026-08-09 012955" src="https://github.com/user-attachments/assets/ddc7d7e8-3f7f-4129-9b24-5438779f6ae0" />
 
 

# 🗺️ Future Improvements

* [ ] Mobile-responsive UI improvements
* [ ] Advanced search and filtering
* [ ] Vehicle availability calendar
* [ ] Automated booking reminders
* [ ] SMS notifications
* [ ] Advanced analytics dashboard
* [ ] Customer review and rating system
* [ ] Driver assignment automation
* [ ] Production deployment
* [ ] Docker containerization
* [ ] CI/CD pipeline
* [ ] Cloud database integration

---

# 👨‍💻 Author

## Saumya Divyanjalee

**Software Engineering Undergraduate — IJSE, Sri Lanka**

<p>
  <a href="https://github.com/Saumya-Divyanjalee">GitHub</a> •
  <a href="https://saumya-divyanjalee.vercel.app">Portfolio</a>
</p>

---

# 📄 License

This project is licensed under the **MIT License**.

---

<p align="center">
  <strong>⭐ If you found this project interesting, consider giving it a star!</strong>
</p>

<p align="center">
  Built with ☕ Java & Spring Boot
</p>
