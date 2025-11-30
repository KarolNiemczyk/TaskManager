# ✔️ TaskManager

**TaskManager** is a Spring Boot application designed for efficient task management.  
It includes a full CRUD system for tasks and categories, filtering, sorting, pagination, exporting to CSV, and a UI built with Thymeleaf.

---

## 🚀 Features

- **CRUD operations** for tasks and categories  
- **Task filtering** by:
  - status  
  - category  
  - title  
- **Pagination & sorting**  
- **CSV export** for all tasks  
- **REST API** documented with Swagger  
- **Web UI** using Thymeleaf  
- **PostgreSQL support**  
- **Unit and integration tests** using Spring Boot Test + Mockito  

---

## 🛠️ Tech Stack & Dependencies

### **Backend**
- Spring Boot **3.5.7**
- Spring Web (REST API)
- Spring Data JPA (database access)
- Spring Validation (DTO validation)
- Spring Boot Starter Thymeleaf (UI layer)

### **Database**
- PostgreSQL (default port **5432**)  

### **OpenAPI / Swagger**
- `springdoc-openapi-starter-webmvc-ui`  
- Swagger UI available at:  
  **http://localhost:8080/swagger-ui.html**

### **CSV Export**
- Apache Commons CSV **1.10.0**

### **Utilities**
- Lombok  
- DevTools  

### **Testing**
- Spring Boot Starter Test  
- Mockito JUnit Jupiter (v5.5.0)  
- Spring Security Test  

---
