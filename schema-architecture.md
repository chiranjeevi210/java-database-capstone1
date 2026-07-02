# Smart Clinic Management System - Architecture Design

## Section 1: Architecture Summary
The Smart Clinic Management System is built on a robust three-tier web architecture leveraging the Spring Boot framework. The presentation layer features a hybrid setup, combining server-rendered Thymeleaf templates for the administrative and clinical dashboards with scalable REST APIs for managing patient records and appointments. 

At the application core, a unified business service layer processes requests from both sets of controllers and decouples the web logic from data access. The system implements polyglot persistence in the data tier to leverage the distinct advantages of different database management systems. Core relational data—such as administrative profiles, doctor records, patient demographics, and appointment schedules—is mapped to MySQL using Spring Data JPA entities. Conversely, highly variable, document-based data like prescriptions is managed through Spring Data MongoDB as flexible document models, facilitating easy schema evolution.

## Section 2: Numbered Flow of Data and Control
1. **User Interface Layer:** The end-user interacts with the application dashboards via server-rendered Thymeleaf views or triggers actions on the client-facing REST modules.
2. **Controller Layer:** The incoming HTTP requests are caught by the presentation layer, routing dashboard traffic to Thymeleaf Controllers and API interactions to REST Controllers.
3. **Service Layer:** The active controller validates the parameters and invokes the core Service Layer where business validations and operational logic are applied.
4. **Repository Layer:** The Service Layer communicates with the Repository Layer, delegating queries to either the MySQL Repositories or the MongoDB Repository.
5. **Database Access:** The respective repositories execute database-level actions directly on the underlying MySQL database for structured data or the MongoDB database for unstructured prescriptions.
6. **Model Binding:** The raw persisted data retrieved from the engine is dynamically bound into object-oriented Java models, translating to `@Entity` classes for MySQL or `@Document` objects for MongoDB.
7. **Application Models in Use:** The backend hands the bound models back up to the controllers, where they are either injected into Thymeleaf templates to render dynamic HTML or serialized directly into JSON payloads for REST API responses.
