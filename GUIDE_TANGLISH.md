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
WarrantyWise_Project/
|-- start.bat               <-- 1-Click Server & Browser Launcher
|-- stop.bat                <-- 1-Click Graceful Shutdown Script
|-- pom.xml                 <-- Maven Dependencies (Java 21, Spring Boot)
|-- src/                    <-- Application Source Code
|   |-- main/java/com/warrantywise/
|   |   |-- config/         <-- Spring Security, CORS & JPA Config
|   |   |-- controller/     <-- REST Controllers (/api/v1/*)
|   |   |-- dto/            <-- Data Transfer Objects
|   |   |-- entity/         <-- Database Entities (JPA)
|   |   |-- repository/     <-- Spring Data JPA Repositories
|   |   |-- security/       <-- JWT Authentication Provider & Filters
|   |   |-- service/        <-- Business Logic & Intelligence Engines
|   |-- main/resources/
|       |-- application.properties <-- Database & JWT Configuration
|       |-- static/         <-- Modern Frontend (HTML, CSS, Vanilla JS)
|           |-- index.html  <-- Root App Launcher & Router
|           |-- css/        <-- Glassmorphism Design System
|           |-- js/         <-- API Client, Auth, UI Engine
|           |-- pages/      <-- Dashboard, Products, Warranties, Services
|-- target/                 <-- Compiled JAR artifact
|-- uploads/                <-- User uploaded invoices & documents
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

Project-a staff kitta kaatum podhu indha exact details-a copy-paste panni fill pannunga. Dashboard graphs automatically super-ah populate aagidum, and ellam states (Active, Expiring Soon, Expired) cover aagum! Idhu table format-la irukku, so copy panna easy-ah irukkum.

### 📦 1. Products (First Add These)
| Product Name | Category | Purchase Price | Purchase Date | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `MacBook Pro M3` | Electronics | `150000` | `15-Sep-2025` | Needs Attention / Expiring Soon |
| `Samsung Galaxy S24 Ultra` | Electronics | `129999` | `15-May-2024` | Expired / Out of Coverage |
| `LG 8kg Front Load Washing Machine` | Home Appliances | `35000` | `10-Jan-2026` | Active / Safe |
| `Royal Enfield Classic 350` | Vehicles | `240000` | `05-Mar-2026` | Active / Long Term |
| `Sony WH-1000XM5 Headphones` | Electronics | `29990` | `01-Aug-2026` | Active (No service history) |

### 🛡️ 2. Warranties (Link to the Products above)
| Select Product | Warranty Provider | Start Date | End Date | Status |
| :--- | :--- | :--- | :--- | :--- |
| MacBook Pro M3 | `Apple Care+` | `15-Sep-2025` | `15-Sep-2026` | ACTIVE |
| Samsung Galaxy S24 Ultra | `Samsung India Electronics` | `15-May-2024` | `15-May-2025` | EXPIRED |
| LG 8kg Front Load Washing Machine | `LG Extended Care` | `10-Jan-2026` | `10-Jan-2029` | ACTIVE |
| Royal Enfield Classic 350 | `RE Sure` | `05-Mar-2026` | `05-Mar-2030` | ACTIVE |
| Sony WH-1000XM5 Headphones | `Sony India Guarantee` | `01-Aug-2026` | `01-Aug-2027` | ACTIVE |

### 🔧 3. Service Records (Link to the Products above)
| Select Product | Service Name / Issue | Service Type | Cost | Status | Notes / Work Performed |
| :--- | :--- | :--- | :--- | :--- | :--- |
| MacBook Pro M3 | `Keyboard replaced` | Repair | `0` | COMPLETED | Under Warranty |
| Samsung Galaxy S24 Ultra | `Screen Replacement` | Repair | `15000` | COMPLETED | Replaced display and flashed OS |
| LG 8kg Front Load Washing Machine | `Routine Drum Cleaning` | Maintenance | `500` | COMPLETED | |
| Royal Enfield Classic 350 | `First Free Service` | Maintenance | `800` | COMPLETED | Oil change |
| *(Don't add for Sony Headphones to show empty state!)* | | | | | |

---

## 🔍 8. Eppadi Test Panradhu? (Pro Testing Tips)

Project-a run panni paarkum podhu indha features-a kandippa test panni paarunga:

1. **File Upload Testing (Invoices/Bills):**
   * Products illa Warranty detail page-kku ponga. Anga "Attachments" section-la unga system-la irundhu edhavadhu dummy image (.jpg) illa PDF file-a upload pannunga.
   * *Enna nadakkum?* Backend automatically `uploads` nu oru folder-a root directory-la create panni, andha file-a anga save pannidum. Database-la file-oda name mattum save aagum. Idhu oru super production-ready feature!

2. **Global Search Engine (`Ctrl + K`):**
   * Keyboard-la `Ctrl + K` press pannunga, search box open aagum.
   * Anga "Samsung" illa "Keyboard" nu thedi paarunga. Namma API accurately products, warranties, and service records ellathaiyum thedi filter panni kondu varum.

3. **Dashboard Real-time Metrics:**
   * Mela ulla demo data-va add panni mudichadhum, Dashboard-kku vanga. 
   * "Warranty Health", "Upcoming Expiries" graphs ellam data-kku yetha madhiri dynamically maruradha paarkalaam. Expiry date kitta vandha, red alert kaattum!

---

*Enjoy managing your product warranties effortlessly with WarrantyWise!* 🛡️✨
