# Smart Clinic Management System - Database Architecture Blueprint

## MySQL Database Design

### Table: patients
*   `id`: INT, Primary Key, Auto Increment, Not Null
*   `first_name`: VARCHAR(50), Not Null
*   `last_name`: VARCHAR(50), Not Null
*   `email`: VARCHAR(100), Unique, Not Null
*   `phone`: VARCHAR(20), Not Null
*   `address`: VARCHAR(255)
*   `password`: VARCHAR(255), Not Null

### Table: doctors
*   `id`: INT, Primary Key, Auto Increment, Not Null
*   `name`: VARCHAR(100), Not Null
*   `specialty`: VARCHAR(100), Not Null
*   `email`: VARCHAR(100), Unique, Not Null
*   `phone`: VARCHAR(20), Not Null
*   `password`: VARCHAR(255), Not Null

### Table: doctor_available_times
*   `id`: INT, Primary Key, Auto Increment, Not Null
*   `doctor_id`: INT, Foreign Key referencing doctors(id) ON DELETE CASCADE
*   `available_time`: VARCHAR(50), Not Null

### Table: appointments
*   `id`: INT, Primary Key, Auto Increment, Not Null
*   `doctor_id`: INT, Foreign Key referencing doctors(id) ON DELETE CASCADE
*   `patient_id`: INT, Foreign Key referencing patients(id) ON DELETE CASCADE
*   `appointment_time`: DATETIME, Not Null
*   `status`: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled), Default 0

### Table: admin
*   `id`: INT, Primary Key, Auto Increment, Not Null
*   `username`: VARCHAR(50), Unique, Not Null
*   `password`: VARCHAR(255), Not Null

---

## MongoDB Collection Design

### Collection: prescriptions
```json
{
  "_id": "64abc123456",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  },
  "metadata": {
    "tags": ["pain-relief", "fever"],
    "schemaVersion": "1.0"
  }
}
```
