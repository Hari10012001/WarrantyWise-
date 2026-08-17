# WarrantyWise User & Developer Guide

Welcome to WarrantyWise!

Please see the comprehensive ELI5 Tanglish Guide here:
👉 **[GUIDE_TANGLISH.md](GUIDE_TANGLISH.md)**

### Quick Start:
- Double-click **`start.bat`** to start the application and launch your browser.
- Double-click **`stop.bat`** to stop the application.

## Demo / Presentation Data Guide

When presenting the project, use the following dummy data to populate the fields accurately and make the dashboard look comprehensive. It covers all lifecycle states (Active, Expiring Soon, Expired).

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
