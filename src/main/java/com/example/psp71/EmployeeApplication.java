package com.example.psp71;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;
import java.sql.DriverManager;

public class EmployeeApplication extends Application {
    private TableView<Employee> employeeTable;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Employee Management");

        employeeTable = new TableView<>();

        TableColumn<Employee, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(data -> data.getValue().firstNameProperty());

        TableColumn<Employee, String> middleNameColumn = new TableColumn<>("Middle Name");
        middleNameColumn.setCellValueFactory(data -> data.getValue().middleNameProperty());

        TableColumn<Employee, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(data -> data.getValue().lastNameProperty());

        TableColumn<Employee, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(data -> data.getValue().genderProperty());

        TableColumn<Employee, String> dobColumn = new TableColumn<>("Date of Birthday");
        dobColumn.setCellValueFactory(data -> data.getValue().dateOfBirthProperty().asString());

        TableColumn<Employee, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());

        TableColumn<Employee, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(data -> data.getValue().positionProperty());

        employeeTable.getColumns().addAll(
                firstNameColumn, middleNameColumn, lastNameColumn, genderColumn,
                dobColumn, addressColumn, positionColumn
        );

        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        HBox toolbar = new HBox(addButton, editButton, deleteButton);
        toolbar.setSpacing(10);

        VBox root = new VBox(toolbar, employeeTable);
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        Properties props = new Properties();
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        try (InputStream input = new FileInputStream("C:/Users/Lenovo/Desktop/Учеба/СТРВЕБПР/psp71/src" +
                "/main/java/com/example/psp71/db.properties")) {
            props.load(input);

            Connection connection = DriverManager.getConnection(props.getProperty("db.url"),
                    props.getProperty("db.username"), props.getProperty("db.password"));

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM employee");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String lastName = resultSet.getString("last_name");
                String firstName = resultSet.getString("first_name");
                String middleName = resultSet.getString("middle_name");
                String gender = resultSet.getString("gender");
                LocalDate dob = resultSet.getDate("date_of_birth").toLocalDate();
                String address = resultSet.getString("address");
                String position = resultSet.getString("position");

                Employee employee = new Employee(id, lastName, firstName, middleName, gender, dob, address, position);
                employees.add(employee);
            }

            employeeTable.setItems(employees);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addButton.setOnAction(event -> {
            Dialog<Employee> dialog = new Dialog<>();
            dialog.setTitle("Add Employee");

            TextField firstNameField = new TextField();
            TextField middleNameField = new TextField();
            TextField lastNameField = new TextField();
            TextField genderField = new TextField();
            DatePicker dobPicker = new DatePicker();
            dobPicker.getEditor().setDisable(true);
            dobPicker.getEditor().setOpacity(1);
            TextField addressField = new TextField();
            TextField positionField = new TextField();

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.addRow(0, new Label("First Name:"), firstNameField);
            grid.addRow(1, new Label("Middle Name:"), middleNameField);
            grid.addRow(2, new Label("Last Name:"), lastNameField);
            grid.addRow(3, new Label("Gender:"), genderField);
            grid.addRow(4, new Label("Date of Birth:"), dobPicker);
            grid.addRow(5, new Label("Address:"), addressField);
            grid.addRow(6, new Label("Position:"), positionField);

            dialog.getDialogPane().setContent(grid);

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String firstName = firstNameField.getText().trim();
                    String middleName = middleNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    String gender = genderField.getText().trim();
                    LocalDate dob = dobPicker.getValue();
                    String address = addressField.getText().trim();
                    String position = positionField.getText().trim();

                    if (firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || dob == null ||
                            address.isEmpty() || position.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Please fill in all the fields.");
                        alert.showAndWait();
                        return null;
                    }

                    return new Employee(0, firstName, middleName,
                            lastName, gender, dob, address, position);
                }
                return null;
            });

            Optional<Employee> result = dialog.showAndWait();
            result.ifPresent(employee -> {
                try {
                    Connection connection = DriverManager.getConnection(props.getProperty("db.url"),
                            props.getProperty("db.username"), props.getProperty("db.password"));

                    String insertQuery =
                            "INSERT INTO employee (last_name, first_name, middle_name, " +
                                    "gender, date_of_birth, address, position) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement statement = connection.prepareStatement(insertQuery,
                            Statement.RETURN_GENERATED_KEYS);
                    statement.setString(1, employee.getLastName());
                    statement.setString(2, employee.getFirstName());
                    statement.setString(3, employee.getMiddleName());
                    statement.setString(4, employee.getGender());
                    statement.setDate(5, Date.valueOf(employee.getDateOfBirth()));
                    statement.setString(6, employee.getAddress());
                    statement.setString(7, employee.getPosition());

                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected > 0) {
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int generatedId = generatedKeys.getInt(1);
                            employee.setId(generatedId); // Установка сгенерированного значения id
                            employees.add(employee);
                            employeeTable.refresh();
                        }
                    }

                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        });

        deleteButton.setOnAction(event -> {
            Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
            if (selectedEmployee == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an employee to delete.");
                alert.showAndWait();
            } else {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("Are you sure you want to delete the selected employee?");
                ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
                confirmAlert.getButtonTypes().setAll(deleteButtonType, ButtonType.CANCEL);

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == deleteButtonType) {
                    try {
                        Connection connection = DriverManager.getConnection(props.getProperty("db.url"),
                                props.getProperty("db.username"), props.getProperty("db.password"));

                        String deleteQuery = "DELETE FROM employee WHERE id = ?";
                        PreparedStatement statement = connection.prepareStatement(deleteQuery);
                        statement.setInt(1, selectedEmployee.getId());

                        int rowsAffected = statement.executeUpdate();

                        if (rowsAffected > 0) {
                            employees.remove(selectedEmployee);
                            employeeTable.refresh();
                        }

                        statement.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        editButton.setOnAction(event -> {
            Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
            if (selectedEmployee == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an employee to edit.");
                alert.showAndWait();
            } else {
                Dialog<Employee> dialog = new Dialog<>();
                dialog.setTitle("Edit Employee");

                TextField firstNameField = new TextField(selectedEmployee.getFirstName());
                TextField middleNameField = new TextField(selectedEmployee.getMiddleName());
                TextField lastNameField = new TextField(selectedEmployee.getLastName());
                TextField genderField = new TextField(selectedEmployee.getGender());
                DatePicker dobPicker = new DatePicker(selectedEmployee.getDateOfBirth());
                dobPicker.getEditor().setDisable(true);
                dobPicker.getEditor().setOpacity(1);
                TextField addressField = new TextField(selectedEmployee.getAddress());
                TextField positionField = new TextField(selectedEmployee.getPosition());

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.addRow(0, new Label("First Name:"), firstNameField);
                grid.addRow(1, new Label("Middle Name:"), middleNameField);
                grid.addRow(2, new Label("Last Name:"), lastNameField);
                grid.addRow(3, new Label("Gender:"), genderField);
                grid.addRow(4, new Label("Date of Birth:"), dobPicker);
                grid.addRow(5, new Label("Address:"), addressField);
                grid.addRow(6, new Label("Position:"), positionField);

                dialog.getDialogPane().setContent(grid);

                ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        String firstName = firstNameField.getText().trim();
                        String middleName = middleNameField.getText().trim();
                        String lastName = lastNameField.getText().trim();
                        String gender = genderField.getText().trim();
                        LocalDate dob = dobPicker.getValue();
                        String address = addressField.getText().trim();
                        String position = positionField.getText().trim();

                        if (firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || dob == null || address.isEmpty() || position.isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Please fill in all the fields.");
                            alert.showAndWait();
                            return null;
                        }

                        try {
                            Date.valueOf(dob);
                        } catch (IllegalArgumentException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Invalid date format. Please enter a valid date.");
                            alert.showAndWait();
                            return null;
                        }

                        selectedEmployee.setFirstName(firstName);
                        selectedEmployee.setMiddleName(middleName);
                        selectedEmployee.setLastName(lastName);
                        selectedEmployee.setGender(gender);
                        selectedEmployee.setDateOfBirth(dob);
                        selectedEmployee.setAddress(address);
                        selectedEmployee.setPosition(position);

                        return selectedEmployee;
                    }
                    return null;
                });

                Optional<Employee> result = dialog.showAndWait();
                result.ifPresent(employee -> {
                    try {
                        Connection connection = DriverManager.getConnection(props.getProperty("db.url"),
                                props.getProperty("db.username"), props.getProperty("db.password"));

                        String updateQuery = "UPDATE employee SET last_name = ?, first_name = ?, middle_name = ?, gender = ?, " +
                                "date_of_birth = ?, address = ?, position = ? WHERE id = ?";
                        PreparedStatement statement = connection.prepareStatement(updateQuery);
                        statement.setString(1, employee.getLastName());
                        statement.setString(2, employee.getFirstName());
                        statement.setString(3, employee.getMiddleName());
                        statement.setString(4, employee.getGender());
                        statement.setDate(5, Date.valueOf(employee.getDateOfBirth()));
                        statement.setString(6, employee.getAddress());
                        statement.setString(7, employee.getPosition());
                        statement.setInt(8, employee.getId());

                        int rowsAffected = statement.executeUpdate();

                        if (rowsAffected > 0) {
                            employeeTable.refresh();
                        }

                        statement.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}