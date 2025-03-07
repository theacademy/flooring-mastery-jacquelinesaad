# flooring-mastery-jacquelinesaad

The Flooring Mastery Application is a Java program that allows a flooring company to manage customer orders.
It supports adding, editing, removing, and viewing orders while ensuring data integrity.
Orders are stored in text files, and the system includes functionality for exporting all order data into a sorted backup file.

The project follows an MVC architecture, was built using the Agile Approach Checklist, 
and utilizes Spring framework with dependency injection and annotations to manage components efficiently. 
Unit tests using JUnit and Mockito validate the application's functionality.

Main Components
Controller: Manages user interactions and coordinates between the UI, service, and data layers.

DAO (Data Access Object): Handles reading and writing data from files.
OrderDaoImpl - Manages order storage and retrieval from text files.
ProductDaoImpl - Loads product data from the products file.
TaxDaoImpl - Loads tax rate data from the taxes file.

DTO (Data Transfer Object): Represents the main data models used in the system.
Order - Represents a customer order with product and tax details.
Product - Represents available flooring products with pricing details.
Tax - Represents state tax information.

Service: Contains business logic for processing and validating orders.
OrderServiceImpl - Handles order validation, calculations, and interactions with the DAO layer.

UI (User Interface): Manages the UI, and input and output for the application.

Testing: Contains unit tests for validating functionality.