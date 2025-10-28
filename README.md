# 📞 Modern Phone Book Application (Java Swing & Basic Data Structures)

This project is a simple, modern phone book application built using **Java Swing** for the Graphical User Interface (GUI). It demonstrates fundamental GUI design principles, event handling, and basic data structure operations (List manipulation) commonly used in small desktop applications.

## ✨ Features Implemented

* **Add Contact:** Users can input a name and phone number to add a new contact to the master list.
* **Search Functionality:** Filter the contact list instantly by searching for keywords in both the **name** and **phone number** fields.
* **Delete Contact:** Allows users to select a row in the table and delete the corresponding contact after a confirmation prompt.
* **Zebra-Striped Table UI:** Implements a custom `TableCellRenderer` to create an **alternating row color (zebra-striping)** effect for improved readability and user experience.
* **Data Storage:** Contacts are managed using an `ArrayList<Contact>`, demonstrating list manipulation for core operations (add, filter, remove).

## 🚀 How to Run the Application

This is a standalone Java Swing application.

### Prerequisites

* **Java Development Kit (JDK)** installed (Java 8 or later recommended).

### Steps

1.  **Save the Code:** Save the provided Java code as a file named `PhoneBookApp.java`.
2.  **Compile:** Open your terminal or command prompt, navigate to the directory where you saved the file, and compile the code. (If you use emojis in the code, you may need to specify the encoding).

    ```bash
    # Standard compilation
    javac PhoneBookApp.java

    # If you encounter "unmappable character" errors (due to emojis)
    javac -encoding UTF-8 PhoneBookApp.java
    ```
3.  **Run:** Execute the compiled class file.

    ```bash
    java PhoneBookApp
    ```

## 🛠 Technologies Used

* **Language:** Java
* **GUI Toolkit:** Java Swing
* **Data Structure:** `java.util.ArrayList` (for contact storage)
* **Design Pattern:** Custom `DefaultTableCellRenderer` (for the Zebra UI effect)
