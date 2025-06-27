# 📱 Transaction Manager App

A powerful Android application built with Android Studio that helps users track and manage **expenses and income**, visualize data, collaborate with others, and even calculate academic results from PDF files.

---

## 🚀 Features

### 💰 Transaction Management
- Add and manage **Income** and **Expense** entries.
- View all transactions in a structured **Records** tab.

### 📊 Data Visualization
- Generate **Pie Charts** to analyze spending and income trends.
- Create and manage **Budgets** for effective financial planning.

### 🔐 Authentication
- **Login with Gmail** using Firebase Authentication.
- User data is securely stored and synced using **Cloud Firestore**.

### 📄 Report Generation
- Generate **PDF reports** of your financial activity:
  - **Daily**
  - **Monthly**
  - **Yearly**

### 👥 Shared Records
- Create shared transaction records.
- **Grant access to multiple users via email**.
- Collaborators can **add, edit, or delete** shared transactions.

### 📚 Result Calculator
- Upload **PDF result files** for individual courses.
- Automatically **merge and calculate final results** into a **comprehensive PDF**.

---

## 🛠 Tech Stack

- **Android Studio**
- **Kotlin / Java**
- **Firebase Authentication**
- **Cloud Firestore**
- **MPAndroidChart** (for pie charts)
- **PDFBox / iText** (for PDF generation and parsing)
- **Material Design Components**

---

## 🔐 Permissions Required

- Internet (for Firebase sync and login)
- Storage (for reading/writing PDF files)
