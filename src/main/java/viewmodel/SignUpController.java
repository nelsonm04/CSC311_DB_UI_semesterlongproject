package viewmodel;

import dao.DbConnectivityClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Person;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final DbConnectivityClass db = new DbConnectivityClass();

    private final String DB_URL = "jdbc:mysql://csc311mojica04.mysql.database.azure.com/CSC311_BD_TEMP";
    private final String DB_USER = "mojin";
    private final String DB_PASS = "FARM123$";

    public void createNewAccount(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username and Password cannot be empty.");
            return;
        }

        if (!isEmailAvailable(username)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email already registered. Please use a different email.");
            return;
        }

        try {
            // Create a dummy Person object to insert (using last_name as password for now)
            Person newUser = new Person(
                    username,            // firstName
                    password,            // lastName (for now acting as password)
                    "N/A",               // department
                    "CPIS",              // major (default)
                    username,            // email
                    "noimage.png"        // imageURL placeholder
            );
            db.insertUser(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
            goBack(actionEvent);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Signup Failed", "Error occurred during account creation.");
        }
    }

    private boolean isEmailAvailable(String email) {
        boolean available = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                available = false; // Email already exists
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return available;
    }

    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
