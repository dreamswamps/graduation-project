# Multimodal Business Collaboration Platform

> A multimodal business collaboration platform based on `Vue3` and `Springboot`. It integrates functions such as "Todo Management", "Article Management", "Application and Approval", and "User Permission Management" into a comprehensive management system.
> Tips: This .md is translated by LLM.

## Language

- [English](README.md)
- [中文](README-ZH.md)

## ✨ Features Description

1. **Todo Management**
    - **Status Management**: Mark todos with "completed" and "favorite" statuses, with animation feedback when switching states
    - **Interactive Display**: Display todo titles by default, with background gradient animation when expanding/collapsing details
    - **Flexible Sorting**: Support drag-and-drop to modify todo positions, allowing manual saving to persist sort results
    - **Procrastination Prevention**: Limit to 7 todos, disable adding new ones and show a prompt when exceeded
    - **Smart Time**: When creating a new todo, the time range defaults to "current time to one hour later"
    - **Empty State Handling**: Display a placeholder image when there are no todos

2. **User Permission Management**
    - **Basic Services**: Provide user registration, login, password modification and reset functions
    - **Request Authentication**: For restricted interfaces, use the `X-Current-User-ID` request header to identify user identity and isolate data
    - **Batch Operations**: Administrators can perform batch soft deletion and batch rollback operations on user information
    - **Data Operations**: Support importing and exporting user data in `Excel` format

3. **Article Management**
    - **Rich Text Editing**: Integrated `wangEditor` editor, providing formatted editing capabilities for article content
    - **Permission Control**: Users can "write articles", administrators can perform batch management on articles

4. **Application and Approval**
    - **[In Development]**
    - **Example: Password Reset Application**: Support two password reset methods: first, complete image verification code and submit a password reset application; second, send email verification code for quick password reset, skipping the approval process
    - **Standardized Application**: Fill in user information according to different reset types to generate standardized application forms
    - **Progress Query**: Query account's historical application records and results
    - **Approval Concurrency Control**: When administrators approve applications, the system automatically locks the application to ensure only one administrator can approve at a time, preventing data conflicts
    - **Timeout Release**: Start timing when administrators begin approval. If not completed within 15 minutes, trigger heartbeat detection to release locked resources. The backend can release all abnormally unreleased locked resources.
    - **Approval Traceability**: Application results are divided into "unapproved"/"approved", and can trace the approver and approval time
    - **Architecture Design**: Use strategy pattern to facilitate future expansion of approval functions

## 🚮 Deprecated Features

1. **Game Module**
    - Goal: A small game similar to Google's T-Rex game, supporting score saving and leaderboard display
    - Current: Only simple controls, pause, and collision detection are implemented

## 🔧 Tech Stack

### Frontend
- Vue 3
- Element Plus
- WangEditor Rich Text Editor
- CSS + Transition Animations

### Backend
- Spring Boot 3.5
- MyBatis
- MySQL 8.0
- JWT Authentication
- QQ SMTP Email Service

### Utilities
- Captcha Generation (Image/Email)
- Excel Import/Export
- [ ] Strategy Pattern Approval Process

## 🚀 Quick Start

### Requirements

- `Node.js`: 20.18.2
- `Java`: 17
- `Spring Boot`: 3.5.5
- `MySQL`: 8.0.40

### Installation and Running

1. Clone the project:
    ```bash
    git clone https://github.com/dreamswamps/graduation-project.git
    ```

2. Install frontend dependencies:
    Open terminal in /frontend folder
    ```bash
    npm install
    ```

3. Install backend dependencies:
    IDEA may automatically complete this step, execute as needed
    Open terminal in /backend folder
    ```bash
    mvn clean install
    ```

4. Configure frontend environment variables: <a id="frontend_env"></a>
    - Create a file named `.env` in the /frontend directory
    - Edit the `.env` file and fill in the following content:
    ```
    VITE_API_BASE_URL = 'http://localhost:8081'
    ```
    If you need to change the port, replace 8081 with another value

5. Configure backend environment variables: <a id="backend_env"></a>
    - Create a file named `.env` in the /backend directory
    - Edit the `.env` file and fill in/modify the following content:
    ```
    DB_USERNAME=<MySQL username>
    DB_PASSWORD=<MySQL password>
    DB_URL=jdbc:mysql://localhost:3306/project?serverTimezone=Asia/Shanghai
    MAIL_HOST=smtp.qq.com
    MAIL_USERNAME=<QQ email>
    MAIL_PASSWORD=<Email authorization code>
    JWT_SECRET=<JWT Base64 secret key>
    ```

6. Configure database:
    See [db.sql](db.sql) for details

## 📖 Usage Instructions <a id="configuration"></a>

1. **Backend Service**:
    - Move the /backend folder to IDEA and open it
    - Run the SpringBoot project

2. **Frontend Service**:
    - Move the /frontend folder to VSCode and open it
    - Enter the following in the terminal
    ```bash
    npm run dev
    ```
    - Access the URL shown in the terminal

## 📁 Project Structure

> Deprecated parts have been removed.
> Redundant parts in Springboot structure are omitted. For example, if there is already an `A.xml` file in Mapper, similar `A.java` files will not appear in Controller and other layers.
> If `[ ]` appears, it means that part of the code is under development.

- Root Directory
```
graduation-project/
├── 📁 backend      # Backend SpringBoot project
├── 📁 frontend     # Frontend Vue3 project
├── 📁 docs         # Project documentation
└── .gitignore
```

- Backend Folder
```
backend/
├── 📄 files   # User uploaded files (auto-generated)
├── 📁 src/main
|   ├── 📁 java/com/example
|   |   ├── 📁 Controller
|   |   |   ├── FileController.java     # User file operations
|   |   |   └── UtilController.java     # Methods specifically using /Util
|   |   |
|   |   ├── 📁 Service
|   |   |   ├── 📁 Strategy    # [ ] Application-related strategy pattern
|   |   |   |   ├── ApplyStrategyMap.java
|   |   |   |   ├── ApprovalStrategy.java
|   |   |   |   └── PwdResetStrategy.java
|   |   |   |
|   |   |   ├── ApplyMapService.java    # [ ] Application type matching
|   |   |   ├── RandomIntService.java   # Random ID generation
|   |   |   └── UtilService.java        # Util method business logic
|   |   |
|   |   ├── 📁 POJO
|   |   |
|   |   ├── 📁 Mapper
|   |   |
|   |   ├── 📁 Exception
|   |   |   ├── CustomException.java    # Custom exception class
|   |   |   └── GlobalExceptionHandler.java     # Exception handling
|   |   |
|   |   ├── 📁 Util
|   |   |   ├── 📁 ValidateCode
|   |   |   |   ├── EmailHandlerConfig.java     # Email sending class
|   |   |   |   ├── EmailHandlerCreator.java    # Email sending method
|   |   |   |   ├── ValidateCodeConfig.java     # Image verification code class
|   |   |   |   └── ValidateCodeCreator.java    # Image verification code method
|   |   |   |
|   |   |   ├── CorsConfig.java     # CORS handling
|   |   |   ├── JWTManager.java     # JWT management
|   |   |   └── Result.java         # Unified response data class
|   |   |
|   |   └── SpringBootDemoApplication.java
|   |
|   └── 📁 resources
|       ├── 📁 Mapper
|       |   ├── AdminAuthMapper.xml     # User identity authentication
|       |   ├── AdminMapper.xml         # User information
|       |   ├── ApplicationMapper.xml   # User applications
|       |   ├── ApplyMapper.xml         # Administrator approval
|       |   ├── ArticleMapper.xml       # Article management
|       |   └── TodoListMapper.xml      # Todo management
|       |
|       ├── 📁 templates
|       |   └── EmailTemplate.html      # Email verification code template
|       |
|       └── application.yml     # Property configuration
|
├── ⚙️ .env     # Environment configuration (needs to be created manually)
└── pom.xml     # Dependency configuration
```

- Frontend Folder
```
frontend/
├── 📁 public       # Few image resources
|
├── 📁 src
|   ├── 📁 assets       # Static resources
|   |
|   ├── 📁 components/base  # Custom encapsulated components
|   |   └── RippleCircleButton.vue  # Ripple animation button
|   |
|   ├── 📁 router
|   |   └── index.js    # Route management
|   |
|   ├── 📁 utils
|   |   └── request.js  # Request management
|   |
|   ├── 📁 views
|   |   ├── 📁 Login
|   |   |   ├── 📁 ResetPwdCom              # Password reset sub-pages
|   |   |   |   ├── CheckApplication.vue    # View application
|   |   |   |   ├── InfoConfirm.vue         # Information confirmation
|   |   |   |   ├── InfoGather.vue          # Information gathering
|   |   |   |   ├── StepResult.vue          # Result display
|   |   |   |   └── VirefyCode.vue          # Verification code validation
|   |   |   |
|   |   |   ├── Forgetpwd.vue   # Password reset page
|   |   |   ├── Login.vue       # Login page
|   |   |   └── Register.vue    # Registration page
|   |   |
|   |   ├── 404.vue             # Unknown route redirect
|   |   ├── Admin.vue           # User information management
|   |   ├── Article.vue         # Article management
|   |   ├── Board.vue           # Todo management
|   |   ├── Data.vue            # Data management
|   |   ├── ElementTest.vue     # Test page
|   |   ├── Home.vue            # Test page
|   |   ├── Manager.vue         # Background parent component
|   |   └── UserInfo.vue        # Personal information
|   |
|   ├── App.vue
|   └── main.js
|
├── ⚙️ .env         # Environment configuration (needs to be created manually)
├── index.html
├── jsconfig.json
├── package-lock.json
├── package.json
└── vite.config.js
```

