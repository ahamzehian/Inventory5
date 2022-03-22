package com.example.inventory5;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class MainController implements Initializable {

    // Main product table editing tools
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button undoButton;

    // Main table factors
    @FXML
    private TableView<MainTableInfo> mainTable;
    @FXML
    private TableColumn<MainTableInfo, String> idColumn;
    @FXML
    private TableColumn<MainTableInfo, String> nameColumn;

    private ObservableList<MainTableInfo> mainObservableList = FXCollections.observableArrayList();

    // Edit text fields and related buttons
    @FXML
    private VBox mainEditVBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField priceTextField;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button imageButton;
    @FXML
    private Button confirmEditButton;

    // Transaction recording textfield and button
    @FXML
    private TextField transQtyTextField;
    @FXML
    private Button recordButton;

    // Product table elements
    @FXML
    private TableView<ProductTableInfo> productTable;
    @FXML
    private TableColumn<ProductTableInfo,String> indexColumn;
    @FXML
    private TableColumn<ProductTableInfo,String> dateColumn;
    @FXML
    private TableColumn<ProductTableInfo,String> transactionQtyColumn;
    @FXML
    private TableColumn<ProductTableInfo,String> unitValueColumn;
    @FXML
    private TableColumn<ProductTableInfo,String> remainderQtyColumn;
    @FXML
    private TableColumn<ProductTableInfo,String> remainderValueColumn;

    private ObservableList<ProductTableInfo> productTableObservableList = FXCollections.observableArrayList();

    @FXML
    private ImageView imageView;

    @FXML
    private BarChart<String,Number> barChart;

    private DBManager dbManager;

    private String targetProduct;
    private final String emptyImagePath = "C:\\Users\\amirh\\IdeaProjects\\Inventory5\\Photos\\noPhoto.png";
    private String imagePath = emptyImagePath;

    private XYChart.Series<String, Number> series1 = new XYChart.Series<>();

    private Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        mainEditVBox.setVisible(false);
        editButton.setVisible(false);
        confirmEditButton.setVisible(false);
        transQtyTextField.setDisable(true);

        dbManager = new DBManager();
        mainObservableList = dbManager.getMainTableObservable();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        mainTable.setItems(mainObservableList);
        
        for (String productName:dbManager.getProductNameList()) {
            initializeChart(productName);
        }

        barChart.setAnimated(false);

    }

    public void updateMainTable() {
        mainObservableList = dbManager.getMainTableObservable();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        mainTable.setItems(mainObservableList);

        imageView.setImage(dbManager.getImage(targetProduct));
    }

    // Populate product table base on selected row from main table
    public void updateProductTable(String productName) {

        productTableObservableList = dbManager.getProductTableObservable(productName);

        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transaction_date"));
        transactionQtyColumn.setCellValueFactory(new PropertyValueFactory<>("transaction_qty"));
        unitValueColumn.setCellValueFactory(new PropertyValueFactory<>("value_per_unit"));
        remainderQtyColumn.setCellValueFactory(new PropertyValueFactory<>("remainder_qty"));
        remainderValueColumn.setCellValueFactory(new PropertyValueFactory<>("remainder_value"));

        productTable.setItems(productTableObservableList);
    }

    public void initializeChart(String productName) {

        barChart.getData().clear();
//        if (!seriesMap.containsKey(productName)) {

            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            CategoryAxis xAxis = new CategoryAxis();

            List<String> dates = dbManager.getTransactionDate(productName);
            List<Integer> qtys = dbManager.getTransactionQty(productName);

            xAxis.setLabel("date");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("score");

            barChart.setTitle("Comparison between various cars");

            series1.setName("date");
            for (int i = 0; i < dates.size(); i++) {
                series1.getData().add(new XYChart.Data<>(dates.get(i), qtys.get(i)));
            }

            seriesMap.put(productName,series1);

//        }

    }

    public void updateChart(XYChart.Series<String, Number> series) {
        barChart.getData().clear();
        barChart.getData().add(series);
    }

    @FXML
    public void onAddButtonClick() {
        mainEditVBox.setVisible(true);
        nameTextField.setDisable(false);
        confirmEditButton.setVisible(false);
        okButton.setVisible(true);
        imageView.setImage(null);
    }

    @FXML
    public void onAddingToMain() {

        okButton.setVisible(true);
        confirmEditButton.setVisible(false);

        Product product = new Product();

        targetProduct = nameTextField.getText();
        product.setProductName(targetProduct);

        seriesMap.put(targetProduct, new XYChart.Series<>());

        if (!priceTextField.getText().isEmpty()) {
            product.setProductUnitPrice(Double.parseDouble(priceTextField.getText()));
        } else {
            product.setProductUnitPrice(0.0);
        }

        product.setImagePath(imagePath);

        if (dbManager.productExists(targetProduct)) {
            Alert nameExistsAlert = new Alert(Alert.AlertType.NONE);

            nameExistsAlert.setAlertType(Alert.AlertType.ERROR);
            nameExistsAlert.setContentText("Product Already exists! ");
            nameExistsAlert.show();

            System.out.println("Product Already exists! ");

        } else {

            dbManager.recordInMainTable(product);
            dbManager.setupProductTable(product.getTableName());

            dbManager.recordInProductTable(targetProduct,0);
            updateProductTable(targetProduct);

            updateMainTable();
            imageView.setImage(null);

            hideInputFields();
        }
    }

    @FXML
    public void events() {
        barChart.getData().clear();
        targetProduct = mainTable.getFocusModel().getFocusedItem().getName();
        imageView.setImage(dbManager.getImage(targetProduct));
        updateProductTable(targetProduct);
        editButton.setVisible(true);
        transQtyTextField.setDisable(false);
        updateChart(seriesMap.get(targetProduct));
    }

    @FXML
    public void onDeleteButtonClick() throws SQLException {
        dbManager.close();
        dbManager = new DBManager();
        dbManager.deleteTable(targetProduct);
        updateProductTable(targetProduct);
        dbManager.deleteFromMain(targetProduct);
        updateMainTable();
        imageView.setImage(null);
        targetProduct = "";
        updateChart(new XYChart.Series<>());
    }

    @FXML
    public void onCancelButtonClick() {
        mainEditVBox.setVisible(false);
        imageView.setImage(null);
    }

    @FXML
    public void onImageButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(imageButton.getScene().getWindow());
            imagePath = file.getPath();
        } catch (Exception e) {
            System.out.println("onImageButtonClick exception! " + e.getMessage());
        }
    }

    @FXML
    public void onEditClick() {
        mainEditVBox.setVisible(true);
        nameTextField.setDisable(true);

        okButton.setVisible(false);
        confirmEditButton.setVisible(true);
    }

    @FXML
    public void onEditConfirmation() {
        String name = nameTextField.getText();

        if (!priceTextField.getText().isEmpty()) {
            Double price = Double.parseDouble(priceTextField.getText());
            dbManager.updateTable(targetProduct,"unit_price",price);
        }

        if (!imagePath.equals(emptyImagePath)) {
            dbManager.updateImage(targetProduct,imagePath);
        } else {
            imagePath = emptyImagePath;
        }

        if (!name.isEmpty()) {
            dbManager.updateTable(targetProduct,"name",name);
        }
        updateMainTable();

        hideInputFields();
    }

    public void hideInputFields() {
        mainEditVBox.setVisible(false);
    }

    @FXML
    public void onRecordButtonClick() {

        barChart.getData().clear();

        int qty = Integer.parseInt(transQtyTextField.getText());
        dbManager.recordInProductTable(targetProduct,qty);
        updateProductTable(targetProduct);

        transQtyTextField.clear();

        int inventoryQty = dbManager.getQty(targetProduct);
        seriesMap.get(targetProduct).getData().add(new XYChart.Data<>(dbManager.getCurrentTime(),inventoryQty));
        XYChart.Series<String, Number> series = seriesMap.get(targetProduct);
        updateChart(series);
    }

    @FXML
    public void exitApplication(ActionEvent event) throws SQLException{
        Platform.exit();
        dbManager.close();
    }

}
