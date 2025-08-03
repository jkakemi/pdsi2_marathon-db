# Programming Marathon API

> The core backend service for an AI-powered platform to guide students on their competitive programming journey.

This repository contains the backend source code for the MarathonMC project, developed for the "Information Systems Project and Development II" course.

## 📚 About The Project

Competitive programming is a challenging field for many university students. It can be difficult to know which problems to solve, what topics to study next, or how to identify knowledge gaps.

MarathonMC aims to solve this problem by providing a smart recommendation platform. By analyzing a student's submission history from online judges, the system will leverage AI to generate personalized study plans and recommend relevant problems, creating a guided and efficient learning path.

This backend is the foundation of the platform, responsible for data management, business logic, and exposing a RESTful API for the frontend client.

## ✨ Core Features

* **User Management:** Secure user registration and profile management.
* **Data Aggregation:** System architecture designed to integrate with web scrapers to fetch user submission data.
* **Relational Data Model:** Robust PostgreSQL database schema to store students, problems, topics, and submissions.
* **RESTful API:** A clean and well-structured API built with Spring Boot.
* **(In Progress) AI Recommendation Engine:** Future integration with LLMs (via Ollama) to provide intelligent recommendations.
* **(In Progress) Personalized Study Plans:** Logic to generate and manage study paths for users.

## 🛠️ Built With

This project is built using modern and robust technologies:

* **Java 17+**
* **Spring Boot 3**
* **Spring Data JPA (Hibernate)**
* **PostgreSQL**
* **Maven**
* **Lombok**

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

Make sure you have the following software installed on your machine:
* [JDK (Java Development Kit) 17 or higher](https://www.oracle.com/java/technologies/downloads/)
* [Apache Maven](https://maven.apache.org/download.cgi)
* [PostgreSQL](https://www.postgresql.org/download/)
* A database client like [pgAdmin 4](https://www.pgadmin.org/download/)
* [Git](https://git-scm.com/downloads)

### Installation & Setup

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/jkakemi/pdsi2_marathon-db.git
    cd pdsi2_marathon-db
    ```
    2.  **Create the PostgreSQL Database:**
    * Open pgAdmin 4 and connect to your PostgreSQL server.
    * Create a new database named `marathondb`.

3.  **Configure Environment Variables:**
    * This project uses Environment Variables to handle sensitive data like database credentials, ensuring they are not hard-coded.
    * If you are using IntelliJ IDEA, go to `Run` > `Edit Configurations...`.
    * In the `Environment variables` field, add the following:
        ```
        DB_PASSWORD=your_password
        ```
        4.  **Run the application:**
    * You can run the application directly from your IDE by running the `MarathonDbApplication.java` class.
    * The API will be available at `http://localhost:8080`.

## ⚙️ API Endpoints

The API is structured around REST principles. Currently, the following endpoint is available:

### Student Registration

* **URL:** `/api/students`
* **Method:** `POST`
* **Description:** Registers a new student in the system.
* **Request Body:**

    ```json
    {
      "username": "competitor",
      "email": "competitor@example.com",
      "handles": "Codeforces: competitor_handle"
    }
    ```

* **Success Response (201 CREATED):**

    ```json
    {
        "id": 1,
        "username": "competitor",
        "email": "competitor@example.com",
        "handles": "Codeforces: competitor_handle",
        "createdAt": "2025-08-03T18:30:00.123456",
        "userProfile": {
            "id": 1,
            "learningGoal": null,
            "learningStyle": null,
            "timeCommitment": null,
            "currentPeriod": null
        }
    }
    ```

## 👤 Author

**Akemi** * GitHub: [@jkakemi](https://github.com/jkakemi)

## 🙏 Acknowledgments

* Project for the "Information Systems Project and Development" course.
* Federal of Uberlândia University
