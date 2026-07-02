/**
 * Reusable Custom Doctor View Controller Component Document Fragment Engine
 */

// Interface boundary abstraction declarations
import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData, showBookingOverlay } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
    // Generate structural bounding box wrapper context
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    const role = localStorage.getItem("userRole");

    // Info Layer Generator Context Block
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name;

    const specialization = document.createElement("p");
    specialization.classList.add("doctor-spec");
    specialization.textContent = `Specialty: ${doctor.specialty || doctor.specialization}`;

    const email = document.createElement("p");
    email.classList.add("doctor-email");
    email.textContent = `Email: ${doctor.email}`;

    const availability = document.createElement("p");
    availability.classList.add("doctor-availability");
    
    // Array string layout parsing parser safely converting structures to lists
    if (Array.isArray(doctor.availability)) {
        availability.textContent = `Available Times: ${doctor.availability.join(", ")}`;
    } else if (doctor.availability) {
        availability.textContent = `Available Times: ${doctor.availability}`;
    } else {
        availability.textContent = "Available Times: See Schedule";
    }

    // Append child structural frames into information wrapper
    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    // Interactive Action Interface Layer Matrix
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // Dynamic Context Engine Button Evaluator
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.classList.add("btn-delete");
        removeBtn.textContent = "Delete";
        
        removeBtn.addEventListener("click", async () => {
            if (confirm(`Are you sure you want to delete profile for ${doctor.name}?`)) {
                try {
                    const token = localStorage.getItem("token");
                    const success = await deleteDoctor(doctor.id, token);
                    if (success) {
                        card.remove(); // Thread safe live element DOM tree extraction pruning
                    }
                } catch (error) {
                    console.error("Critical execution abort inside delete operation pipeline:", error);
                    alert("Failed to delete doctor entry. System runtime error encountered.");
                }
            }
        });
        actionsDiv.appendChild(removeBtn);

    } else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.classList.add("btn-book-locked");
        bookNow.textContent = "Book Now";
        
        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });
        actionsDiv.appendChild(bookNow);

    } else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.classList.add("btn-book-active");
        bookNow.textContent = "Book Now";
        
        bookNow.addEventListener("click", async (e) => {
            try {
                const token = localStorage.getItem("token");
                const patientData = await getPatientData(token);
                showBookingOverlay(e, doctor, patientData);
            } catch (error) {
                console.error("Critical resolution failure parsing authenticated booking runtime workflow:", error);
                alert("Could not initialize appointment portal components. Check connection layer.");
            }
        });
        actionsDiv.appendChild(bookNow);
    }

    // Assemble completed tree elements into final view node execution array
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}
