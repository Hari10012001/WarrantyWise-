# 🚀 WarrantyWise — Tanglish Guide (ELI5 Version)

> **Welcome!** Indha document-la WarrantyWise project-a pathi rumba simple-ah, fun-ah **Tanglish (Tamil + English)**-la puriyura madhiri explain pannirukom!

---

## 🎯 1. WarrantyWise-na Enna? (What is WarrantyWise?)

Namma veetula irukkura gadgets (Mobile, Laptop, TV, Washing Machine, Fridge, etc.) oda:
1. **Warranty Bill & Expiry Date**
2. **Service Records & Repair Costs**
3. **Renewal Reminders**

idhu ellathaiyum ore jagathula safe-ah store panni, expiry vandrappo namakku automatic alert kudukkura **Smart SaaS Platform** dhaan **WarrantyWise**!

---

## ⚡ 2. Single Click Start & Stop (Super Easy Launch!)

Project-a run panna terminal-la periya command Type panna thevai illai! Direct-ah double-click panna podhum:

### 🟢 Server-a Start Panna (`start.bat`):
* Root folder-la irukkura **`start.bat`** (illana **`bin/start.bat`**)-a double click pannunga.
* Java & Maven check panni, Spring Boot server-a auto-start pannum.
* Port 8080 ready aanadhum, browser automatically `http://localhost:8080/pages/login.html` launch aagum!

### 🔴 Server-a Stop Panna (`stop.bat`):
* Root folder-la irukkura **`stop.bat`** (illana **`bin/stop.bat`**)-a double click pannunga.
* Port 8080-la run aagura server background process-a clean-ah stop pannidum.

---

## 📁 3. Professional Folder Structure (Project Organization)

```text
WarrantyWise_Product Warranty, Service History and Renewal Reminder/
├── 📄 start.bat               <-- 1-Click Server & Browser Launcher
├── 📄 stop.bat                <-- 1-Click Graceful Shutdown Script
├── 📁 bin/                    <-- Automation Execution Scripts
│   ├── start.bat
│   └── stop.bat
├── 📄 pom.xml                 <-- Maven Dependencies (Java 21, Spring Boot 3.3.x)
├── 📁 src/                    <-- Application Source Code
│   ├── 📁 main/java/com/warrantywise/
│   │   ├── config/            <-- Spring Security, CORS & JPA Config
│   │   ├── controller/        <-- REST Controllers (/api/v1/*)
│   │   ├── dto/               <-- Data Transfer Objects
│   │   ├── entity/            <-- Database Entities (JPA)
│   │   ├── repository/        <-- Spring Data JPA Repositories
│   │   ├── security/          <-- JWT Authentication Provider & Filters
│   │   └── service/           <-- Business Logic & Intelligence Engines
│   └── 📁 main/resources/
│       ├── application.properties <-- Database & JWT Configuration
│       └── static/            <-- Modern Frontend (HTML, CSS, Vanilla JS)
│           ├── index.html     <-- Root App Launcher & Router
│           ├── 📁 css/        <-- Glassmorphism Design System (variables, global, components)
│           ├── 📁 js/         <-- API Client, Auth, Command Palette, UI Engine
│           └── 📁 pages/      <-- Dashboard, Products, Warranties, Services, Reports
├── 📁 target/                 <-- Compiled JAR artifact (warrantywise-0.0.1-SNAPSHOT.jar)
└── 📁 uploads/                <-- User uploaded invoices & documents
```

---

## 💡 4. Key Features & Super Cool UI Highlights

### 📊 1. Premium SaaS Dashboard
* **Warranty Health Score**: Ungaloda ella products-kum warranty ethana percent active-ah irukku nu Health Bar-la kaattum.
* **Attention Required Alert**: Expiry aaga pora policies (<30 days) top alert-la flash aagum.
* **Upcoming Expiry Timeline**: Endha product warranty endha date-la mukiyama mudiyudhu nu clear timeline list kaattum.

### 📱 2. Product Profile & Drag & Drop Upload
* Product Specs, Purchase Price, Serial Number ellam store pannalaam.
* **Profile Tabs**: Overview, Warranty, Service, Documents tabs irukku.
* **Drag & Drop Dropzone**: Bills & Receipts PDF/JPG-a drag & drop panni upload pannalaam!

### ⌨️ 3. Command Palette (`Ctrl + K`)
* Keyboard-la `Ctrl + K` (Cmd + K on Mac) press panna sleek Command Palette open aagum!
* Endha page-kum instantaneous-ah jump pannalaam.

### 🔔 4. Notifications & Reminders Hub
* Warranty 90 days, 30 days, 15 days, 7 days, 1 day irukkum podhu system automatic alerts generate pannum.

### 📈 5. Reports & 1-Click CSV Export
* Product, Warranty, Service, Product Lifecycle reports-a visual graphs/tables-la paarkalaam.
* **Export CSV** button-a click panna instant Excel sheet download aagum!

---

## 🛠️ 5. Tech Stack (Simple Terms-la)

* **Backend**: Java 21, Spring Boot 3.3.x, Spring Security 6, JWT Authentication.
* **Database**: MySQL Server (Database Name: `warrantywise_db`).
* **Frontend**: HTML5, CSS3 Glassmorphism, Bootstrap 5, Vanilla JavaScript.
* **Security**: BCrypt Password Encodings & Role-Based Access Control (RBAC).

---

## ❓ 6. FAQ & Quick Troubleshooting

**Q: Server start aagala, error varudhu?**
* Check if MySQL is running on port 3306 (`root` / `Hari2025@`).
* `warrantywise_db` database MySQL-la exist aagi irukanum.

**Q: How to run full compilation manually?**
```bash
mvn clean package
```

**Q: Port 8080 already in use nu vandha enna panradhu?**
* Double-click `stop.bat` to clear port 8080, then run `start.bat` again!

---

## 📝 7. Demo Script (Perfect Dummy Data for Presentation)

Project-a staff kitta kaatum podhu indha exact details-a use panni fill pannunga. Dashboard graphs automatically super-ah populate aagidum, and ellam states (Active, Expiring Soon, Expired) cover aagum!

### 🟢 1. Gadget 1 (Expiring Soon - Needs Attention)
* **Product:** `MacBook Pro M3` (Category: Electronics) | Price: `150000` | Date: `15-Sep-2025`
* **Warranty:** `Apple Care+` | Start: `15-Sep-2025` | End: `15-Sep-2026` | Status: `ACTIVE` (Will show as Expiring Soon based on current date)
* **Service Record:** `Keyboard replaced` | Cost: `0` (Under Warranty) | Status: `COMPLETED`

### 🔴 2. Gadget 2 (Expired - Out of Coverage)
* **Product:** `Samsung Galaxy S24 Ultra` (Category: Electronics) | Price: `129999` | Date: `15-May-2024`
* **Warranty:** `Samsung India Electronics` | Start: `15-May-2024` | End: `15-May-2025` | Status: `EXPIRED`
* **Service Record:** `Screen Replacement` | Service Type: `Repair` | Cost: `15000` | Status: `COMPLETED` | Work Performed: `Replaced display and flashed OS`

### 🟢 3. Home Appliance (Active - Safe)
* **Product:** `LG 8kg Front Load Washing Machine` (Category: Home Appliances) | Price: `35000` | Date: `10-Jan-2026`
* **Warranty:** `LG Extended Care` | Start: `10-Jan-2026` | End: `10-Jan-2029` | Status: `ACTIVE`
* **Service Record:** `Routine Drum Cleaning` | Cost: `500` | Status: `COMPLETED`

### 🟢 4. Vehicle (Active - Long Term)
* **Product:** `Royal Enfield Classic 350` (Category: Vehicles) | Price: `240000` | Date: `05-Mar-2026`
* **Warranty:** `RE Sure` | Start: `05-Mar-2026` | End: `05-Mar-2030` | Status: `ACTIVE`
* **Service Record:** `First Free Service` | Cost: `800` (Oil change) | Status: `COMPLETED`

### 🟢 5. Personal Audio (Active)
* **Product:** `Sony WH-1000XM5 Headphones` (Category: Electronics) | Price: `29990` | Date: `01-Aug-2026`
* **Warranty:** `Sony India Guarantee` | Start: `01-Aug-2026` | End: `01-Aug-2027` | Status: `ACTIVE`
* **Service Record:** *(No service history yet - helps to show empty states!)*

---

*Enjoy managing your product warranties effortlessly with WarrantyWise!* 🛡️✨
