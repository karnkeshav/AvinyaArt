# Distributed Digital Creation Platform (DDCP)

A production-quality prototype for a distributed digital creation platform with Android frontend and Python FastAPI backend.

## Components

1.  **Backend (FastAPI)**:
    *   Auth with Geo-Spatial validation (Shapely).
    *   Skill Badging & Telemetry Analysis.
    *   Fairness Routing Engine.
    *   Human Verification Engine.
    *   Attribution & Merging Logic.
    *   Dockerized with PostgreSQL, Redis, and Celery.

2.  **Frontend (Android/Kotlin)**:
    *   MVVM + Clean Architecture.
    *   Jetpack Compose UI.
    *   Retrofit for API communication.
    *   Room for offline storage.
    *   Canvas for drawing and telemetry capture (Pressure, timestamps).

## Prerequisites

*   Docker & Docker Compose
*   Android Studio (for running the mobile app)
*   Java JDK 17+

## Setup & Run

### Backend

1.  Navigate to the root directory.
2.  Run the backend start script:
    ```bash
    ./run_backend.sh
    ```
    This will build the Docker images and start FastAPI, PostgreSQL, Redis, and Celery.

    The API will be available at `http://localhost:8000`.
    Swagger Docs: `http://localhost:8000/docs`

3.  **Data Seeding**: The backend automatically seeds demo data (Villages, Users) on startup if the database is empty.
    *   Demo Users: `artist1`, `artist2` ... `artist5` (Password: `password`)
    *   Team Leader: `leader1` (Password: `password`)

### Frontend (Android)

1.  Open the `android` folder in Android Studio.
2.  Sync Gradle project.
3.  Create an emulator or connect a physical device.
    *   **Note**: For the emulator to connect to the backend running on the host machine, the API URL is set to `http://10.0.2.2:8000`.
4.  Run the app.
5.  **Login**:
    *   Username: `artist1`
    *   Password: `password`
    *   The app will perform a mock Geo-Validation check.

## Workflow Demo

1.  **Onboarding**: User logs in and location is validated against the village registry (Mock GeoJSON).
2.  **Dashboard**: User sees assigned tasks (routed via Fairness Engine).
3.  **Creation**: User opens a task, draws on the canvas. Telemetry (pressure, speed) is captured.
4.  **Submission**: User submits the artwork.
    *   Backend verifies human origin (Mock score).
    *   Backend calculates ownership score.
5.  **Review**: Team leader (API side) can view status (Android screen for leader is not fully detailed in this prototype but API supports it).

## Project Structure

*   `/backend`: FastAPI application, Docker config, Alembic (if used), requirements.
*   `/android`: Android Kotlin project.
