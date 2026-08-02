# Loan Application Management System

A comprehensive loan application management system built with **Spring Boot 3**, featuring role-based access control, JWT authentication, and a modern dark-themed UI.

---

## 🚀 Features

### **User Authentication & Authorization**
- JWT-based authentication
- Role-based access control (Customer, Officer, Manager, Admin)
- Secure password encryption with BCrypt

### **Loan Application Workflow**
- Customer loan application submission
- Officer verification and forwarding
- Manager final approval/rejection
- Real-time status tracking

### **Dashboards**
- **Customer Dashboard:** Apply for loans, track application status, manage repayments
- **Officer Dashboard:** Verify pending applications
- **Manager Dashboard:** Approve/reject verified applications
- **Admin Dashboard:** System statistics and user management

### **Modern UI**
- Dark-themed responsive design
- Tailwind CSS styling
- Real-time data updates
- Mobile-friendly interface

---

## 🛠️ Technology Stack

### **Backend**
- Java 21
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- MySQL Database
- JWT (JJWT library)
- Lombok

### **Frontend**
- Thymeleaf Templates
- Tailwind CSS
- Bootstrap 5
- Vanilla JavaScript

---

## 📋 Prerequisites

- **Java Development Kit (JDK):** 21 or higher
- **Maven:** 3.6+
- **MySQL Server:** 8.0+
- **IDE:** IntelliJ IDEA, Eclipse, or VS Code

---

## 🗄️ Database Setup

### 1. Create MySQL Database
```sql
CREATE DATABASE loanapp_db;

2. Update Database Credentials
Edit src/main/resources/application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/db_name?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=username
spring.datasource.password=your_password

⚙️ Installation & Setup
1. Clone the Repository
Bash
git clone <repository-url>
cd loanapp
2. Build the Project
Using Maven wrapper:

Bash
# On Windows
mvnw.cmd clean install

# On Linux/Mac
./mvnw clean install
Or using standard Maven:

Bash
mvn clean install
3. Run the Application
Using Maven wrapper:

Bash
**# On Windows**
mvnw.cmd spring-boot:run

**# On Linux/Mac**
./mvnw spring-boot:run
Or run the packaged JAR file:

**Bash**
java -jar target/loanapp-0.0.1-SNAPSHOT.jar
The application will start on http://localhost:8081

**👥 Default User Roles**
Role	Permissions & Capabilities
CUSTOMER	Can apply for loans and track their applications
OFFICER	Can verify loan applications and forward them to the manager
MANAGER	Can approve or reject verified applications
ADMIN	Can view system statistics and manage users
**🔌 API Endpoints**
**Authentication**
POST /api/auth/register — Register a new user

POST /api/auth/login — Login and receive a JWT token

**Loan APIs**
POST /api/loans/apply — Apply for a loan (requires authentication)

GET /api/loans/my-loans — Get current user's loans (requires authentication)

GET /api/loans/user/{userId} — Get loans by user ID

**Officer APIs**
GET /api/officer/pending-loans — Get pending loan applications

POST /api/officer/verify/{id} — Verify and forward a loan application

**Manager APIs**
GET /api/manager/pending-approvals — Get verified applications for approval

POST /api/manager/decision/{id}?status={APPROVE\|REJECT} — Approve or reject a loan

**Admin APIs**
GET /api/admin/stats — Get system statistics

GET /api/admin/users — Get all users

🌐** Web Pages**
**Route	Description**
/	Landing page
/login	Login page
/register	Registration page
/dashboard	Role-based dashboard redirect
/customer/dashboard	Customer dashboard
/officer/dashboard	Officer dashboard
/manager/dashboard	Manager dashboard
/admin/dashboard	Admin dashboard

**📁 Project Structure**
Plaintext
loanapp/
├── src/
│   ├── main/
│   │   ├── java/com/bajajFinserv/loanapp/
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── model/               # Entity classes
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── security/            # Security configuration
│   │   │   ├── service/             # Business logic
│   │   │   └── LoanappApplication.java
│   │   └── resources/
│   │       ├── static/              # CSS, JS, images
│   │       ├── templates/           # Thymeleaf templates
│   │       └── application.properties
│   └── test/                        # Test cases
├── pom.xml                          # Maven configuration
└── README.md                        # Documentation

**⚙️ Configuration**
Application Properties (application.properties)
Properties
# Server Configuration
server.port=8081

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/loanapp_db
spring.datasource.username=root
spring.datasource.password=12345

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Security Logging
logging.level.org.springframework.security=TRACE
Security Configuration (JwtUtils.java)
Java
private final String jwtSecret = "your-secret-key";
private final int jwtExpirationMs = 86400000; // 24 hours
💻 Development
Adding New Features
Add new entity: Create the class in the model/ package.

Add repository: Create an interface in the repository/ package.

Add service: Create a service class in the service/ package.

Add controller: Create a controller in the controller/ package.

Add UI: Create a Thymeleaf template in the templates/ directory.

Testing
Run tests using Maven:

Bash
mvn test
**🚀 Deployment**
**Building for Production**
Bash**
**mvn clean package -DskipTests
This command creates an executable JAR file in the target/ directory.

**Running in Production**
**Bash**
java -jar target/loanapp-0.0.1-SNAPSHOT.jar
Environment Variables
You can override configuration settings using environment variables:

**Bash**
export SPRING_DATASOURCE_URL=jdbc:mysql://production-db:3306/loanapp_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
java -jar target/loanapp-0.0.1-SNAPSHOT.jar
🔧 Troubleshooting
Database Connection Issues
Ensure your MySQL server is running.

Check and verify database credentials in application.properties.

Ensure the loanapp_db database has been created.

Port Already in Use
Change the port inside application.properties:

Properties
server.port=8082
JWT Token Issues
Verify the JWT secret key inside JwtUtils.java.

Ensure token expiration time is properly configured.

Clear browser localStorage if you run into authentication caching loops.

🔒 Security Considerations
Change the default JWT secret key for production environments.

Use strong, complex database passwords.

Enable HTTPS in production.

Implement rate limiting for public and sensitive API endpoints.

Add strict input validation for all user inputs.

Consider enabling CSRF protection for state-changing operations.

**🔮 Future Enhancements**
[ ] Email notifications for loan status updates

[ ] Document upload functionality

[ ] Built-in EMI calculator integration

[ ] Payment gateway integration

[ ] Advanced reporting and analytics

[ ] Companion mobile application development

[ ] Comprehensive audit logging


**📄 License**
This project is intended for educational and demonstration purposes.
**
💡 Support**
For issues, questions, or contributions, please contact the development team.
