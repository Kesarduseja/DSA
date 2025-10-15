import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
/**
* A Java Swing GUI application to manage tables from a Quiz database.
*/
public class QuizDBViewer extends JFrame {

   // --- GUI Components ---
   private JComboBox<String> tableSelector;
   private JButton loadButton, addButton, updateButton, deleteButton;
   private JTable dataTable;
   private JScrollPane tableScrollPane;

   //
   // !!! IMPORTANT: CHANGE THIS SECTION FOR YOUR DATABASE !!!
   // Using the corrected URL from previous steps.
   private static final String DB_URL = "jdbc:mysql://localhost:3307/quizdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
   private static final String DB_USER = "root";
   private static final String DB_PASSWORD = "KESAR@0508DUSEJA";
   //
   // -----------------------------------------------------------

   // The list of tables the user is allowed to view.
   private static final String[] ALLOWED_TABLES = {
           "Student", "Admin", "Quiz", "Attempts", "Question", "Options"
   };


   /**
    * Constructor to set up the GUI.
    */
   public QuizDBViewer() {
       // --- Frame Setup (UNCHANGED) ---
       super("Quiz Database Manager");
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setSize(900, 700);
       setLayout(new BorderLayout(10, 10));

       // ... [Remaining GUI setup code is omitted for brevity, it remains unchanged]

       Font labelFont = new Font("Times New Roman", Font.BOLD, 20);
       Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
       Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);
       Font headerFont = new Font("Segoe UI", Font.BOLD, 15);

       Color primaryColor = new Color(0, 123, 255); // A modern blue
       Color backgroundColor = new Color(248, 249, 250); // Light grey
       Color buttonTextColor = Color.WHITE;
     
       // --- Top Panel for Table Selection ---
       JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
       topPanel.setBackground(backgroundColor);
       JLabel selectLabel = new JLabel("Select Table:");
       selectLabel.setFont(labelFont);
       topPanel.add(selectLabel);

       tableSelector = new JComboBox<>(ALLOWED_TABLES);
       tableSelector.setFont(labelFont);
       topPanel.add(tableSelector);

       loadButton = new JButton("Load Data");
       styleButton(loadButton, buttonFont, primaryColor, buttonTextColor);
       topPanel.add(loadButton);

       // --- Data Table ---
       dataTable = new JTable();
       dataTable.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());
       dataTable.setFont(tableFont);
       dataTable.setRowHeight(28);
       dataTable.setSelectionBackground(new Color(13, 202, 240));
       dataTable.setSelectionForeground(Color.BLACK);


       JTableHeader header = dataTable.getTableHeader();
       header.setFont(headerFont);
       header.setBackground(new Color(52, 58, 64)); // Dark grey header
       header.setForeground(Color.WHITE);

       tableScrollPane = new JScrollPane(dataTable);
       tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

       // --- Bottom Panel for CRUD Operations ---
       JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
       bottomPanel.setBackground(backgroundColor);

       addButton = new JButton("Add New Record");
       styleButton(addButton, buttonFont, primaryColor, buttonTextColor);
       bottomPanel.add(addButton);

       updateButton = new JButton("Update Selected Record");
       styleButton(updateButton, buttonFont, primaryColor, buttonTextColor);
       bottomPanel.add(updateButton);

       deleteButton = new JButton("Delete Selected Record");
       styleButton(deleteButton, buttonFont, new Color(220, 53, 69), buttonTextColor); // Red for delete
       bottomPanel.add(deleteButton);

       JButton reportButton = new JButton("Generate Report");
       styleButton(reportButton, buttonFont, new Color(25, 135, 84), buttonTextColor); // Green for report
       bottomPanel.add(reportButton);

       // Add panels to the frame
       add(topPanel, BorderLayout.NORTH);
       add(tableScrollPane, BorderLayout.CENTER);
       add(bottomPanel, BorderLayout.SOUTH);

       // --- Action Listeners ---
       loadButton.addActionListener(e -> loadTableData());
       addButton.addActionListener(e -> showAddDialog());
       updateButton.addActionListener(e -> showUpdateDialog());
       deleteButton.addActionListener(e -> deleteSelectedRecord());
       reportButton.addActionListener(e -> showReportSelectionDialog());
     
       setLocationRelativeTo(null);
   }


   /**
    * Helper method to style buttons uniformly. (UNCHANGED)
    */
   private void styleButton(JButton button, Font font, Color bgColor, Color fgColor) {
       button.setFont(font);
       button.setBackground(bgColor);
       button.setForeground(fgColor);
       button.setFocusPainted(false);
       button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
   }


   /**
    * Fetches and displays data from the selected table. (UNCHANGED)
    */
   private void loadTableData() {
       String selectedTable = (String) tableSelector.getSelectedItem();
       if (selectedTable == null) return;

       try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + selectedTable)) {

           dataTable.setModel(buildTableModel(rs));
       } catch (SQLException ex) {
           showError("Database Error: " + ex.getMessage());
       }
   }


   // ====================================================================
   // --- NEW HELPER METHOD FOR DYNAMIC CRUD ---
   // ====================================================================




   /**
    * Retrieves the column names for the currently selected table.
    * @return A Vector of Strings containing the column names.
    */
   private Vector<String> getColumnNames() throws SQLException {
       String selectedTable = (String) tableSelector.getSelectedItem();
       Vector<String> columnNames = new Vector<>();
       if (selectedTable == null) return columnNames;

       // Use LIMIT 1 (or equivalent for other DBs) to minimize data transfer, we only need metadata
       String sql = "SELECT * FROM " + selectedTable + " LIMIT 1";


       try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

           ResultSetMetaData metaData = rs.getMetaData();
           int columnCount = metaData.getColumnCount();
           for (int column = 1; column <= columnCount; column++) {
               columnNames.add(metaData.getColumnName(column));
           }
       }
       return columnNames;
   }


   // ====================================================================
   // --- CRUD METHODS IMPLEMENTATION ---
   // ====================================================================


   /**
    * Shows a dialog to insert a new record into the selected table.
    * Skips the first column (assumed to be an auto-incremented Primary Key).
    */
   private void showAddDialog() {
       String selectedTable = (String) tableSelector.getSelectedItem();
       if (selectedTable == null) return;

       try {
           Vector<String> columnNames = getColumnNames();
           // Panel to hold the labels and text fields
           JPanel panel = new JPanel(new GridLayout(columnNames.size(), 2, 5, 5));
           List<JTextField> fields = new ArrayList<>();

           // Start from index 1 to skip the assumed Primary Key (first column)
           for (int i = 1; i < columnNames.size(); i++) {
               String colName = columnNames.get(i);
               panel.add(new JLabel(colName + ":"));
               JTextField field = new JTextField(15);
               fields.add(field);
               panel.add(field);
           }

           int result = JOptionPane.showConfirmDialog(this, panel,
                   "Add New Record to " + selectedTable, JOptionPane.OK_CANCEL_OPTION);


           if (result == JOptionPane.OK_OPTION) {
               // 1. Build the SQL INSERT statement
               StringBuilder columns = new StringBuilder();
               StringBuilder values = new StringBuilder();


               for (int i = 1; i < columnNames.size(); i++) {
                   columns.append(columnNames.get(i));
                   values.append("?");
                   if (i < columnNames.size() - 1) {
                       columns.append(", ");
                       values.append(", ");
                   }
               }


               String sql = "INSERT INTO " + selectedTable + " (" + columns.toString() + ") VALUES (" + values.toString() + ")";


               // 2. Execute the INSERT
               try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {


                   // Bind values: Simple String binding for all input fields
                   for (int i = 0; i < fields.size(); i++) {
                       // NOTE: For production use, you'd need type-checking (setInt, setDouble, etc.) here.
                       pstmt.setString(i + 1, fields.get(i).getText());
                   }


                   int rowsAffected = pstmt.executeUpdate();
                   if (rowsAffected > 0) {
                       JOptionPane.showMessageDialog(this, "Record added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                       loadTableData(); // Refresh the table
                   } else {
                       showError("Failed to add new record.");
                   }


               } catch (SQLException ex) {
                   showError("Database Error during INSERT: " + ex.getMessage() + "\nCheck data types and constraints.");
               }
           }
       } catch (SQLException ex) {
           showError("Error preparing Add dialog: " + ex.getMessage());
       }
   }

   /**
    * Shows a dialog pre-filled with the selected record's data to update it.
    */
   private void showUpdateDialog() {
       int selectedRow = dataTable.getSelectedRow();
       String selectedTable = (String) tableSelector.getSelectedItem();
       if (selectedRow == -1 || selectedTable == null) {
           showError("Please select a record to update.");
           return;
       }

       try {
           Vector<String> columnNames = getColumnNames();
           JPanel panel = new JPanel(new GridLayout(columnNames.size() + 1, 2, 5, 5));
           List<JTextField> fields = new ArrayList<>();


           // Primary Key is the first column, used for the WHERE clause
           Object primaryKeyValue = dataTable.getValueAt(selectedRow, 0);
           String pkColumnName = columnNames.get(0);


           // Display the PK, but make it uneditable for safety
           panel.add(new JLabel(pkColumnName + " (PK):"));
           JTextField pkField = new JTextField(primaryKeyValue.toString());
           pkField.setEditable(false);
           panel.add(pkField);




           // Populate the dialog fields for editable columns (starting from index 1)
           for (int i = 1; i < columnNames.size(); i++) {
               String colName = columnNames.get(i);
               Object currentValue = dataTable.getValueAt(selectedRow, i);


               panel.add(new JLabel(colName + ":"));
               JTextField field = new JTextField(15);
               field.setText(currentValue != null ? currentValue.toString() : "");
               fields.add(field);
               panel.add(field);
           }

           int result = JOptionPane.showConfirmDialog(this, panel,
                   "Update Record in " + selectedTable, JOptionPane.OK_CANCEL_OPTION);


           if (result == JOptionPane.OK_OPTION) {
               // 1. Build the SQL UPDATE statement
               // e.g., UPDATE Student SET Name=?, Email=?, Phone=? WHERE Student_ID=?
               StringBuilder setClause = new StringBuilder();
               for (int i = 1; i < columnNames.size(); i++) {
                   setClause.append(columnNames.get(i)).append(" = ?");
                   if (i < columnNames.size() - 1) {
                       setClause.append(", ");
                   }
               }
               String sql = "UPDATE " + selectedTable + " SET " + setClause.toString() + " WHERE " + pkColumnName + " = ?";


               // 2. Execute the UPDATE
               try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {


                   // Bind values for the SET clause (starting at index 1 in pstmt)
                   for (int i = 0; i < fields.size(); i++) {
                       pstmt.setString(i + 1, fields.get(i).getText());
                   }


                   // Bind the Primary Key for the WHERE clause (last index in pstmt)
                   int pkIndex = fields.size() + 1;
                   // Check if PK is an Integer (best practice)
                   if (primaryKeyValue instanceof Integer) {
                       pstmt.setInt(pkIndex, (Integer) primaryKeyValue);
                   } else {
                       pstmt.setString(pkIndex, primaryKeyValue.toString());
                   }




                   int rowsAffected = pstmt.executeUpdate();
                   if (rowsAffected > 0) {
                       JOptionPane.showMessageDialog(this, "Record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                       loadTableData(); // Refresh the table
                   } else {
                       showError("No record was updated. Check if data was changed.");
                   }

               } catch (SQLException ex) {
                   showError("Database Error during UPDATE: " + ex.getMessage() + "\nCheck data types and constraints.");
               }
           }
       } catch (SQLException ex) {
           showError("Error preparing Update dialog: " + ex.getMessage());
       }
   }

   /**
    * Deletes the selected record using its Primary Key.
    * Assumes the Primary Key is the first column in the result set.
    */
   private void deleteSelectedRecord() {
       int selectedRow = dataTable.getSelectedRow();
       String selectedTable = (String) tableSelector.getSelectedItem();
       if (selectedRow == -1 || selectedTable == null) {
           showError("Please select a record to delete.");
           return;
       }

       int confirm = JOptionPane.showConfirmDialog(this,
               "Are you sure you want to delete the selected record from the " + selectedTable + " table?",
               "Confirm Delete", JOptionPane.YES_NO_OPTION);

       if (confirm == JOptionPane.YES_OPTION) {
           try {
               // Get the value of the primary key (assumed to be the first column)
               Object pkValue = dataTable.getValueAt(selectedRow, 0);
               String pkColumnName = dataTable.getColumnName(0);

               String sql = "DELETE FROM " + selectedTable + " WHERE " + pkColumnName + " = ?";

               try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {


                   // Assuming Primary Key is an Integer or String for binding
                   if (pkValue instanceof Integer) {
                       pstmt.setInt(1, (Integer) pkValue);
                   } else {
                       pstmt.setString(1, pkValue.toString());
                   }


                   int rowsAffected = pstmt.executeUpdate();
                   if (rowsAffected > 0) {
                       JOptionPane.showMessageDialog(this, "Record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                       loadTableData(); // Refresh the table
                   } else {
                       showError("Failed to delete record. It may no longer exist.");
                   }
               }
           } catch (SQLException ex) {
               showError("Database Error during DELETE: " + ex.getMessage());
           } catch (Exception ex) {
               showError("Error processing selected record: " + ex.getMessage());
           }
       }
   }

   // ====================================================================
   // --- REPORTING SECTION (UNCHANGED) ---
   // ====================================================================

   private void showReportSelectionDialog() {
       String[] reportOptions = {
               "Student Performance Report",
               "Quiz Summary Report",
               "High Scorers Report",
               "Admins and Quizzes Created"
       };

       String selectedReport = (String) JOptionPane.showInputDialog(this, "Choose a report to generate:", "Generate Report", JOptionPane.PLAIN_MESSAGE, null, reportOptions, reportOptions[0]);


       if (selectedReport != null) {
           switch (selectedReport) {
               case "Student Performance Report":
                   generateStudentPerformanceReport();
                   break;
               case "Quiz Summary Report":
                   generateQuizSummaryReport();
                   break;
               case "High Scorers Report":
                   generateHighScorersReport();
                   break;
               case "Admins and Quizzes Created":
                   generateAdminQuizCountReport();
                   break;
           }
       }
   }

   private void generateStudentPerformanceReport() {
       String studentIdStr = JOptionPane.showInputDialog(this, "Enter Student ID:", "Input Required", JOptionPane.QUESTION_MESSAGE);
       if (studentIdStr == null || studentIdStr.trim().isEmpty()) return;


       try {
           int studentId = Integer.parseInt(studentIdStr);
           String sql = "SELECT s.Name, a.Quiz_ID, a.Marks_Obtained, q.Total_Marks " +
                   "FROM Attempts a " +
                   "JOIN Student s ON a.Student_ID = s.Student_ID " +
                   "JOIN Quiz q ON a.Quiz_ID = q.Quiz_ID " +
                   "WHERE a.Student_ID = ?";

           try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setInt(1, studentId);
               try (ResultSet rs = pstmt.executeQuery()) {
                   dataTable.setModel(buildTableModel(rs));
               }
           } catch (SQLException ex) {
               showError("Database Report Error: " + ex.getMessage());
           }
       } catch (NumberFormatException ex) {
           showError("Invalid input. Please enter a valid number for the Student ID.");
       }
   }


   private void generateQuizSummaryReport() {
       String quizIdStr = JOptionPane.showInputDialog(this, "Enter Quiz ID:", "Input Required", JOptionPane.QUESTION_MESSAGE);
       if (quizIdStr == null || quizIdStr.trim().isEmpty()) return;

       try {
           int quizId = Integer.parseInt(quizIdStr);
           String sql = "SELECT Quiz_ID, " +
                   "AVG(Marks_Obtained) AS Average_Score, " +
                   "MIN(Marks_Obtained) AS Min_Score, " +
                   "MAX(Marks_Obtained) AS Max_Score, " +
                   "COUNT(Student_ID) AS Number_of_Attempts " +
                   "FROM Attempts " +
                   "WHERE Quiz_ID = ? " +
                   "GROUP BY Quiz_ID";

           try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setInt(1, quizId);
               try (ResultSet rs = pstmt.executeQuery()) {
                   dataTable.setModel(buildTableModel(rs));
               }
           } catch (SQLException ex) {
               showError("Database Report Error: " + ex.getMessage());
           }
       } catch (NumberFormatException ex) {
           showError("Invalid input. Please enter a valid number for the Quiz ID.");
       }
   }


   private void generateHighScorersReport() {
       String percentageStr = JOptionPane.showInputDialog(this, "Show students with scores above (%):", "Input Required", JOptionPane.QUESTION_MESSAGE);
       if (percentageStr == null || percentageStr.trim().isEmpty()) return;


       try {
           double percentage = Double.parseDouble(percentageStr);
           String sql = "SELECT s.Name, s.Email, a.Quiz_ID, a.Marks_Obtained, q.Total_Marks " +
                   "FROM Attempts a " +
                   "JOIN Student s ON a.Student_ID = s.Student_ID " +
                   "JOIN Quiz q ON a.Quiz_ID = q.Quiz_ID " +
                   "WHERE (CAST(a.Marks_Obtained AS FLOAT) / q.Total_Marks) * 100 > ?";


           try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setDouble(1, percentage);
               try (ResultSet rs = pstmt.executeQuery()) {
                   dataTable.setModel(buildTableModel(rs));
               }
           } catch (SQLException ex) {
               showError("Database Report Error: " + ex.getMessage());
           }
       } catch (NumberFormatException ex) {
           showError("Invalid input. Please enter a valid number for the percentage.");
       }
   }


   private void generateAdminQuizCountReport() {
       String sql = "SELECT adm.Name, adm.Email, COUNT(q.Quiz_ID) AS Quizzes_Created " +
               "FROM Admin adm " +
               "LEFT JOIN Quiz q ON adm.Admin_ID = q.Admin_ID " +
               "GROUP BY adm.Admin_ID, adm.Name, adm.Email " +
               "ORDER BY Quizzes_Created DESC";

       try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
           dataTable.setModel(buildTableModel(rs));
       } catch (SQLException ex) {
           showError("Database Report Error: " + ex.getMessage());
       }
   }


   // --- HELPER AND UTILITY METHODS (UNCHANGED) ---

   private static class ZebraTableCellRenderer extends DefaultTableCellRenderer {
       private static final Color EVEN_ROW_COLOR = new Color(240, 248, 255);
       private static final Color ODD_ROW_COLOR = Color.WHITE;


       @Override
       public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
           if (!isSelected) {
               component.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
           }
           return component;
       }
   }

   private void showError(String message) {
       JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
   }

   public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
       ResultSetMetaData metaData = rs.getMetaData();
       Vector<String> columnNames = new Vector<>();
       int columnCount = metaData.getColumnCount();
       for (int column = 1; column <= columnCount; column++) {
           columnNames.add(metaData.getColumnName(column));
       }

       Vector<Vector<Object>> data = new Vector<>();
       while (rs.next()) {
           Vector<Object> vector = new Vector<>();
           for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
               vector.add(rs.getObject(columnIndex));
           }
           data.add(vector);
       }
       return new DefaultTableModel(data, columnNames);
   }

   public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> new QuizDBViewer().setVisible(true));
   }
}



