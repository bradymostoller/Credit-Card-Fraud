# Full-Stack ML-Powered Credit Card Fraud Detection System

Frontend and ML model is in anomaly-service and backend is in finance 2

A full-stack web application for detecting credit card fraud in real-time, combining machine learning, secure backend services, and a modern frontend interface.

---

## Tech Stack

- **Backend:** Java, Spring Boot, PostgreSQL, JWT Authentication  
- **Frontend:** React  
- **Machine Learning:** Python, Scikit-learn, Pandas, Seaborn  
- **Containerization:** Docker  

---

## Features

- **Fraud Detection ML Model:**  
  - Logistic regression model trained on transaction data  
  - Handles class imbalance and feature engineering for improved accuracy  
  - Real-time predictions via backend APIs  

- **Backend Services:**  
  - Secure REST APIs for transaction submission and ML prediction  
  - JWT-based authentication and user management  
  - PostgreSQL database integration for transaction records  

- **Frontend:**  
  - React-based interface for submitting transactions  
  - Displays fraud prediction results in real-time  
  - User-friendly dashboards and forms  

- **Deployment:**  
  - Containerized with Docker for easy setup and consistent environment  

---

## Getting Started

1. Clone the repository:  
git clone https://github.com/<your-username>/ML-Credit-Card-Fraud.git
2.Backend Setup
cd finance-2
./mvnw spring-boot:run
3. Frontend Setup
cd anomaly-service/frontend
npm install
npm start
