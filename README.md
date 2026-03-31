<h1 align="center">
  <img src="https://github.com/user-attachments/assets/af75d83a-96f8-4d59-b2cb-6ae83e106d71" width="50" />
  &nbsp; Attention Companion App
</h1>

AttentionCompanionApp is a Java desktop application that monitors a student’s study focus in real time using a normal webcam, computer vision, and attention-scoring logic. It shows a live attention graph, sends low-attention email alerts, and generates a manual PDF report at the end of the session.

## Features

- Real-time webcam-based face detection.
- Attention estimation using:
  - Face presence
  - Head posture analysis
  - Distance normalization
  - Moving average smoothing
- Live attention graph during the session.
- Email alerts when attention stays low for a sustained time.
- Multi-recipient alert support with fallback email.
- Session analytics and manual PDF report generation.
- Optional app-usage tracking for productivity/distraction analysis.

## Tech Stack

- Java
- Java Swing / desktop UI
- OpenCV
- JavaCV
- JavaMail / Jakarta Mail
- PDF library such as iText or OpenPDF
- Java collections and threading utilities

## Project Workflow

1. Start the application.
2. Enter recipient email configuration.
3. Start a study session.
4. Webcam frames are captured continuously.
5. The system detects a face and computes attention.
6. A smoothed attention value is shown on the live graph.
7. If attention remains below threshold for a defined duration, an email alert is sent.
8. When the session ends, the user can manually generate a PDF report.

## Core Modules

### 1. Main UI Controller
Handles:
- Start/stop session
- Email input
- Live graph updates
- Session lifecycle
- Generate report button

### 2. AttentionEstimator
Responsible for:
- Face detection using Haar cascades
- Posture scoring
- Distance scoring
- Combined attention calculation
- Moving average history

### 3. EmailService
Responsible for:
- SMTP configuration
- Sending alert emails
- Multi-recipient delivery
- Fallback email logic
- Background-thread execution

### 4. PdfReportGenerator
Responsible for:
- Session summary
- Attention charts
- App usage tables
- Recommendations
- Exporting final PDF report

### 5. Session Data Store
Stores:
- Attention history
- Alert logs
- Session metadata
- App usage statistics

## Attention Scoring Logic

The app estimates attention using these ideas:

- **Presence detection**: if no valid face is detected, attention falls to 0.
- **Posture analysis**: if the face drops too low in the frame, the user may be drowsy.
- **Distance normalization**: the app uses relative face size in the frame to estimate whether the user is sitting at a normal distance.
- **Weighted score**: posture contributes more strongly than distance.
- **Moving average smoothing**: recent scores are averaged to reduce noise and avoid false triggers.

## Alert Logic

- The app checks whether the smoothed attention value is below a threshold.
- A low-attention counter increases once per second while attention stays low.
- If the counter reaches the configured duration, an alert email is sent.
- If attention recovers, the counter resets.
- Email delivery runs in a separate thread so monitoring is not blocked.

## PDF Report

Report generation is a **manual** action after the session ends.

The report can include:
- Session date and duration
- Average, minimum, and maximum attention
- Low-attention duration
- Number of alerts triggered
- Attention-over-time chart
- Application usage statistics
- Basic recommendations

## Folder Structure

```text
AttentionCompanionApp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
├── lib/
├── reports/
├── screenshots/
├── README.md
├── .gitignore
└── pom.xml / build.gradle / project files
```

## Prerequisites

Install the following before running:

- Java JDK 8 or above
- IDE such as IntelliJ IDEA, Eclipse, or NetBeans
- OpenCV / JavaCV dependencies
- Internet connection for email alerts
- Gmail account with App Password if using Gmail SMTP

## Setup

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/AttentionCompanionApp.git
cd AttentionCompanionApp
```

### 2. Configure dependencies
Make sure your OpenCV / JavaCV and JavaMail dependencies are available through Maven, Gradle, or local JARs.

### 3. Configure email
Set:
- SMTP host
- SMTP port
- sender email
- app password
- fallback recipient
  
in EmailService.java

### Gmail & App Password Setup Instructions

This project uses Gmail SMTP for sending attention alerts. You must configure your own Gmail account with an App Password for it to work.<br>

Step 1: Enable 2-Factor Authentication (2FA)
- Go to Google Account Security.
- Under "Signing in to Google," enable 2-Step Verification.
- Complete the 2FA setup (phone number, authenticator app, etc.).

Step 2: Generate App Password
- Go to Google App Passwords.
- Select Mail as the app and Other (Custom name) as the device.
- Enter a name like "AttentionCompanionApp" and click Generate.
- Copy the 16-character app password (example: abcd efgh ijkl mnop).
- You will not see it again.

Step 3: Configure in the Application
- In the app's email configuration:

```text
Sender Email: your-gmail@gmail.com
App Password: abcd efgh ijkl mnop
SMTP Host: smtp.gmail.com
SMTP Port: 587
```
Step 4: Test Email
- Click Test Email in the app. You should receive a test message at your Gmail address.

Troubleshooting

- "Authentication failed" → Double-check the app password (no spaces, exactly 16 characters).
- "Connection refused" → Verify port 587 and TLS enabled.
- "535-5.7.8 Username and Password not accepted" → 2FA not enabled or wrong app password.

### 4. Run the application
Run the main Java class from your IDE or build tool.

## Example Git Ignore

```gitignore
# Java
*.class
*.log
*.ctxt
*.jar
*.war
*.nar
*.ear
hs_err_pid*

# IDE
.idea/
*.iml
.project
.classpath
.settings/
.vscode/

# Build
target/
build/
out/

# OS files
.DS_Store
Thumbs.db

# Reports
reports/*.pdf

# Secrets
.env
config.properties
application.properties
secrets.properties
```

## Results
**Low Attention**
<img width="960" height="540" alt="Low Attention" src="https://github.com/user-attachments/assets/f3decb45-eba9-45dd-9b4e-7455fc287089" /> <br>**Good Attention**
<img width="960" height="540" alt="Good Attention" src="https://github.com/user-attachments/assets/6c9e06b9-1cfb-4fc3-8d5c-7715c9b3e02f" /> <br> 
**Attention Report**<br>
<img width="585" height="405" alt="Attention Report" src="https://github.com/user-attachments/assets/94c5c3c5-11be-4bc8-95a3-423ae97bde51" />

## Future Improvements

- Facial landmarks for better drowsiness estimation
- Eye aspect ratio / blink detection
- Better lighting adaptation
- Multi-user support
- Smarter app classification
- More accurate ML-based attention estimation
- AI Based Application Grouping 

## Limitations

- Best performance with near-frontal face position.
- Accuracy may drop in poor lighting.
- Designed mainly for a single user.
- Posture-based drowsiness can confuse reading posture with sleepiness in some cases.

## Documentation References

- OpenCV Java Tutorials
- OpenCV official Java documentation
- JavaCV GitHub and API docs
- JavaMail SMTP/TLS documentation
