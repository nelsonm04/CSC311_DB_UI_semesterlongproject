package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;
import service.UserSession;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    @FXML private TextField first_name, last_name, department, email, imageURL;
    @FXML private ComboBox<Major> majorComboBox;
    @FXML private ImageView img_view;
    @FXML private MenuBar menuBar;
    @FXML private TableView<Person> tv;
    @FXML private TableColumn<Person, Integer> tv_id;
    @FXML private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    @FXML private Button editButton, deleteButton, addButton;
    @FXML private Label statusLabel;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            if (UserSession.getInstance(null, null) == null) {
                addButton.setDisable(true);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }


            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));

            first_name.textProperty().addListener((obs, oldText, newText) -> checkForm());
            last_name.textProperty().addListener((obs, oldText, newText) -> checkForm());
            email.textProperty().addListener((obs, oldText, newText) -> checkForm());
            department.textProperty().addListener((obs, oldText, newText) -> checkForm());
            majorComboBox.valueProperty().addListener((obs, oldValue, newValue) -> checkForm());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void addNewRecord() {
        if (!isFormValid()) {
            statusLabel.setText("Please correct the form before adding.");
            return;
        }
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.insertUser(p);
        cnUtil.retrieveId(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();
        statusLabel.setText("Record added successfully.");
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorComboBox.setValue(null);
        email.setText("");
        imageURL.setText("");

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(true);
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            int index = data.indexOf(p);
            Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                    majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);
            statusLabel.setText("Record updated successfully.");
        }
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            int index = data.indexOf(p);
            cnUtil.deleteRecord(p);
            data.remove(index);
            tv.getSelectionModel().clearSelection();

            editButton.setDisable(true);
            deleteButton.setDisable(true);
            addButton.setDisable(true);
            statusLabel.setText("Record deleted successfully.");
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            majorComboBox.setValue(Major.valueOf(p.getMajor()));
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());

            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options = FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();

        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(), textField2.getText(), comboBox.getValue());
            }
            return null;
        });

        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(results.fname + " " + results.lname + " " + results.major);
        });
    }

    @FXML
    private void importCSV() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open CSV File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showOpenDialog(tv.getScene().getWindow());
            if (file != null) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                data.clear();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length == 6) {
                        Person p = new Person(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]);
                        data.add(p);
                    }
                }
                reader.close();
                statusLabel.setText("Imported CSV successfully.");
            }
        } catch (IOException e) {
            statusLabel.setText("Failed to import CSV.");
            e.printStackTrace();
        }
    }

    @FXML
    private void exportCSV() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CSV File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(tv.getScene().getWindow());
            if (file != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (Person p : data) {
                    writer.write(String.join(",", p.getFirstName(), p.getLastName(), p.getDepartment(), p.getMajor(), p.getEmail(), p.getImageURL()));
                    writer.newLine();
                }
                writer.close();
                statusLabel.setText("Exported CSV successfully.");
            }
        } catch (IOException e) {
            statusLabel.setText("Failed to export CSV.");
            e.printStackTrace();
        }
    }

    private static enum Major {Business, CSC, CPIS}

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    private boolean validateField(TextField field, String regex) {
        return field.getText().matches(regex);
    }

    private boolean isFormValid() {
        boolean firstNameValid = validateField(first_name, "^[A-Za-z]{2,25}$");
        boolean lastNameValid = validateField(last_name, "^[A-Za-z]{2,25}$");
        boolean emailValid = validateField(email, "^[\\w.-]+@farmingdale\\.edu$");
        boolean departmentValid = !department.getText().isEmpty();
        boolean majorValid = (majorComboBox.getValue() != null);
        return firstNameValid && lastNameValid && emailValid && departmentValid && majorValid;
    }

    private void checkForm() {
        addButton.setDisable(!isFormValid());
    }
}
