package com.example.inventory5;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private final String dbName = "inventory3.db";
    private final String path = "C:\\Users\\amirh\\Desktop\\SQL\\inventory";
    private final String DB_URL = "jdbc:sqlite:" + path + "\\" + dbName;

    private Connection connection;

    private final String MAIN_TABLE = "main_table";

    private final String ID_COLUMN = "_id";
    private final String NAME_COLUMN = "name";
    private final String TABLE_NAME_COLUMN = "table_name";
    private final String UNIT_PRICE_COLUMN = "unit_price";
    private final String QTY_COLUMN = "qty";
    private final String IMAGE_COLUMN = "image";

//    private String INDEX_COLUMN = "index";
    private final String TRANSACTION_DATE_COLUMN = "transaction_date";
    private final String TRANSACTION_QTY_COLUMN = "transaction_qty";
    private final String VALUE_PER_UNIT_COLUMN = "value_per_unit";
    private final String REMAINDER_QTY_COLUMN = "remainder_qty";
    private final String REMAINDER_VALUE_COLUMN = "remainder_value";

    public DBManager() {
        try {

            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connection established.");

        } catch (SQLException e) {
            System.out.println("Failed to create the connection! " + e.getMessage());
        }
    }

    public void setupMainTable() {
        try {

            Statement statement = connection.createStatement();
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS " + MAIN_TABLE + "(" +
                            ID_COLUMN + " INT, " +
                            NAME_COLUMN + " TEXT, " +
                            TABLE_NAME_COLUMN + " TEXT, " +
                            UNIT_PRICE_COLUMN + " INT, " +
                            QTY_COLUMN + " INT, " +
                            IMAGE_COLUMN + " BLOB)"
            );
            statement.close();

        } catch (SQLException e) {
            System.out.println("Failed to create the main table! " + e.getMessage());
        }
    }

    public void setupProductTable(String tableName) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
                            ID_COLUMN + " INT, " +
                            TRANSACTION_DATE_COLUMN + " TEXT, " +
                            TRANSACTION_QTY_COLUMN + " TEXT, " +
                            VALUE_PER_UNIT_COLUMN + " INT, " +
                            REMAINDER_QTY_COLUMN + " INT, " +
                            REMAINDER_VALUE_COLUMN + " INT)"
            );
            statement.close();
        } catch (SQLException e) {
            System.out.println("Failed to create product table! " + e.getMessage());
        }
    }

    public void recordInMainTable(Product product) {
        try {

            String query = "INSERT INTO " + MAIN_TABLE + "(" +
                    ID_COLUMN + ", " +
                    NAME_COLUMN + ", " +
                    TABLE_NAME_COLUMN + ", " +
                    UNIT_PRICE_COLUMN + ", " +
                    QTY_COLUMN + ", " +
                    IMAGE_COLUMN + ") VALUES(?,?,?,?,?,?)";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1,getLastIndex(MAIN_TABLE)+1);
            ps.setString(2,product.getProductName());
            ps.setString(3,product.getTableName());
            ps.setDouble(4,product.getProductUnitPrice());
            ps.setInt(5,1);
            FileInputStream fis = new FileInputStream(product.getImagePath());
            ps.setBytes(6,fis.readAllBytes());

            ps.execute();
            ps.close();

            System.out.println("Product " + product.getProductName() + " has been recorded.");

        } catch (SQLException e) {
            System.out.println("Failed to record in main table! " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to access the image! " + e.getMessage());
        }
    }

    public void deleteFromMain(String productName) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "DELETE FROM " + MAIN_TABLE + " WHERE " + NAME_COLUMN + "='" + productName + "'"
            );
            statement.close();
        } catch (SQLException e) {
            System.out.println("Failed to delete the " + productName + " from " + MAIN_TABLE + "! " + e.getMessage());
        }
    }

    public void deleteTable(String productName) {
        String tableName = getTableName(productName);
        try {
            Statement statement = connection.createStatement();
            String query = "DROP TABLE " + tableName;
            statement.execute(query);
            System.out.println("Table deleted!");
            statement.close();
        } catch (SQLException e) {
            System.out.println("Failed to delete the product table! " + e.getMessage());
        }
    }

    public void recordInProductTable(String productName, int transactionQty) {

        try {

            String tableName = getTableName(productName);

            String query = "INSERT INTO " + tableName + "(" +
                    ID_COLUMN + "," +
                    TRANSACTION_DATE_COLUMN + "," +
                    TRANSACTION_QTY_COLUMN + "," +
                    VALUE_PER_UNIT_COLUMN + "," +
                    REMAINDER_QTY_COLUMN + "," +
                    REMAINDER_VALUE_COLUMN +
                    ") VALUES(?,?,?,?,?,?)";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1,getLastIndex(tableName)+1);
            ps.setString(2,getCurrentTime());
            ps.setInt(3,transactionQty);
            ps.setDouble(4,getUnitPrice(productName));
            ps.setInt(5,getQty(productName) + transactionQty);
            ps.setDouble(6,(getQty(productName) + transactionQty)*getUnitPrice(productName));

            updateQty(productName,(getQty(productName) + transactionQty));

            ps.execute();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Failed to record the transaction into product table! " + e.getMessage());
        }

    }

    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    // Return products table name
    public String getTableName(String productName) {
        productName = productName.toLowerCase();
        String result = "";
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT " + TABLE_NAME_COLUMN + " FROM " + MAIN_TABLE + " WHERE " + NAME_COLUMN + "='" +
                    productName + "'"
            );
            result = resultSet.getString(TABLE_NAME_COLUMN);
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Failed to get product table name! " + e.getMessage());
        }
        return result;
    }

    // Return unit price of the given product name from main_table
    public double getUnitPrice(String productName) {
        productName = productName.toLowerCase();
        double result = 0.0;

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT " + UNIT_PRICE_COLUMN + " FROM " + MAIN_TABLE + " WHERE " + NAME_COLUMN + "='" +
                            productName + "'"
            );
            result = resultSet.getDouble(UNIT_PRICE_COLUMN);
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Failed to get the unit price! " + e.getMessage());
        }

        return result;
    }

    // Return remainder quantity of given product name as stocked quantity of the product
    public int getQty(String productName) {
        int result = 0;
        productName = productName.toLowerCase();

        try {
            String query = "SELECT " + QTY_COLUMN + " FROM " + MAIN_TABLE + " WHERE " + NAME_COLUMN +
                    "='" + productName + "'";
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            result = resultSet.getInt(QTY_COLUMN);
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Failed to get the quantity of the " + productName + "! " + e.getMessage());
        }

        return result;
    }

    public void updateQty(String productName, int newQty) {

        productName = productName.toLowerCase();

        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "UPDATE " + MAIN_TABLE + " SET " + QTY_COLUMN + "=" + newQty + " WHERE " +
                    NAME_COLUMN + "='" + productName + "'"
                    );
            statement.close();
        }catch (SQLException e) {
            System.out.println("Failed to update the qty of " + productName + "! " + e.getMessage());
        }

    }

    // Update the main table. Use productName to find your target
    public void updateTable(String productName,String columnName,Object newValue) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "UPDATE " + MAIN_TABLE + " SET " + columnName + "=" +
                            (columnName instanceof String?"'" + newValue + "'":newValue) +
                            " WHERE " + NAME_COLUMN + "='" + productName + "'"
            );
            statement.close();
        } catch (SQLException e) {
            System.out.println("Failed to update the main table! " + e.getMessage());
        }
    }

    // Return transaction quantity of a given product in a list
    public List<Integer> getTransactionQty(String productName) {
        List<Integer> result = new ArrayList<>();
        productName = productName.toLowerCase();
        try {

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT " + TRANSACTION_QTY_COLUMN + " FROM " + getTableName(productName)
            );

            while (resultSet.next()) {
                result.add(resultSet.getInt(TRANSACTION_QTY_COLUMN));
            }

        } catch (SQLException e) {
            System.out.println("Failed to get list of transaction qty! " + e.getMessage());
        }
//        System.out.println(result);
        return result;
    }

    // Return remainder quantity list. This is coming from product table
    public List<Integer> getRemainderQty(String productName) {

        List<Integer> result = new ArrayList<>();

        try {

            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT " + REMAINDER_QTY_COLUMN + " FROM " + getTableName(productName)
            );

            while (resultSet.next()) {
                result.add(resultSet.getInt(REMAINDER_QTY_COLUMN));
            }

        } catch (SQLException e) {
            System.out.println("Failed to get remainder qty list! " + e.getMessage());
        }

        return result;

    }

    // Return transaction date of a given product in a list
    public List<String> getTransactionDate(String productName) {
        List<String> result = new ArrayList<>();
        productName = productName.toLowerCase();

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT " + TRANSACTION_DATE_COLUMN + " FROM " + getTableName(productName)
            );

            while (resultSet.next()) {
                result.add(resultSet.getString(TRANSACTION_DATE_COLUMN));
            }
        } catch (SQLException e) {
            System.out.println("Failed to get the list of transaction date! " + e.getMessage());
        }
//        System.out.println(result);
        return result;
    }

    public List<String> getProductNameList() {
        List<String> productNameList = new ArrayList<>();

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT " + NAME_COLUMN + " FROM " + MAIN_TABLE
            );

            while (resultSet.next()) {
                productNameList.add(resultSet.getString(NAME_COLUMN));
            }
        } catch (SQLException e) {
            System.out.println("Failed to get the list of products name! " + e.getMessage());
        }

        return productNameList;
    }

    // Update image of the given product
    public void updateImage(String productName, String imagePath) {
        try {

            String query = "UPDATE " + MAIN_TABLE + " SET " + IMAGE_COLUMN + "= ? WHERE " + NAME_COLUMN + "= ?";

            PreparedStatement ps = connection.prepareStatement(query);
            FileInputStream fis = new FileInputStream(imagePath);
            ps.setBytes(1, fis.readAllBytes());
            ps.setString(2,productName);

            ps.executeUpdate();

            ps.close();
            fis.close();

        } catch (SQLException e) {
            System.out.println("Failed to access image path! " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to read the new image path! " + e.getMessage());
        }
    }

    // Return id and name from main table in am observableList
    public ObservableList<MainTableInfo> getMainTableObservable() {
        ObservableList result = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT * FROM " + MAIN_TABLE
            );
            while (resultSet.next()) {
                MainTableInfo mti = new MainTableInfo(
                        Integer.toString(resultSet.getInt(ID_COLUMN)),
                        resultSet.getString(NAME_COLUMN)
                );
                result.add(mti);
            }
            resultSet.close();
        }catch (SQLException e) {
            System.out.println("Failed to retrieve data! " + e.getMessage());
        }
        return result;
    }

    // Populate the product table view base on given product name
    public ObservableList<ProductTableInfo> getProductTableObservable(String productName) {
        ObservableList<ProductTableInfo> result = FXCollections.observableArrayList();
        String productTableName = getTableName(productName);
        try {

            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT * FROM " + productTableName
            );

            while (resultSet.next()) {
                ProductTableInfo pti = new ProductTableInfo(
                        Integer.toString(resultSet.getInt(ID_COLUMN)),
                        resultSet.getString(TRANSACTION_DATE_COLUMN),
                        Integer.toString(resultSet.getInt(TRANSACTION_QTY_COLUMN)),
                        Double.toString(resultSet.getDouble(VALUE_PER_UNIT_COLUMN)),
                        Integer.toString(resultSet.getInt(REMAINDER_QTY_COLUMN)),
                        Double.toString(resultSet.getDouble(REMAINDER_VALUE_COLUMN))
                );
                result.add(pti);
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Failed to get product table observable list! " + e.getMessage());
        }
        return result;
    }

    // The method is designed for testing the function of recordInMainTable
    public List<Object> getProductInfo(String productName) {
        List<Object> result = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + MAIN_TABLE + " WHERE " + NAME_COLUMN + "='" + productName + "'"
            );

            result.add(resultSet.getString(NAME_COLUMN));
            result.add(resultSet.getString(TABLE_NAME_COLUMN));
            result.add(resultSet.getDouble(UNIT_PRICE_COLUMN));

            statement.close();
            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Failed to retrieve info for " + productName + " from main_table! " + e.getMessage());
        }
        return result;
    }

    // return image of a given product from main table
    public Image getImage(String productName) {
        if (productExists(productName)) {
            try {
                ResultSet resultSet = connection.createStatement().executeQuery(
                        "SELECT * FROM " + MAIN_TABLE + " WHERE " + NAME_COLUMN + "='" + productName + "'"
                );
                return new Image(resultSet.getBinaryStream(IMAGE_COLUMN));
            } catch (SQLException e) {
                System.out.println("Failed to access the image for " + productName + "! " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean tableExists(String tableName) {

        boolean result = false;
        try {

            DatabaseMetaData dmd = connection.getMetaData();
            ResultSet resultSet = dmd.getTables(null, null, tableName, new String[]{"TABLE"});
            result = resultSet.next();

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Failed to get the list of tables! " + e.getMessage());
        }
        return result;

    }

    // Return a list of columns of a given table name
    public List<String> getListOfColumns(String tableName) {
        List<String> result = new ArrayList<>();

        try {

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData rsmd = resultSet.getMetaData();

            for (int i = 1; i < rsmd.getColumnCount()+1; i++) {
                result.add(rsmd.getColumnName(i));
            }

            statement.close();
            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Failed to get list of columns! " + e.getMessage());
        }

        return result;

    }

    // Search through main_table to see if the given product name exists
    public boolean productExists(String productName) {
        productName = productName.toLowerCase();
        boolean result = false;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT COUNT(*) AS prCount FROM " + MAIN_TABLE + " WHERE " +
                            NAME_COLUMN + "='" + productName + "'"
            );
            result = resultSet.getInt("prCount")!=0;
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Failed to check for " + productName + "! " + e.getMessage());
        }
        return result;
    }

    // Return the maximum value of the _id column
    public int getLastIndex(String tableName) {
        int result = 0;
        if (!tableIsEmpty(tableName)) {
            try {

                ResultSet resultSet = connection.createStatement().executeQuery(
                        "SELECT MAX(" + ID_COLUMN + ") AS max_index FROM " + tableName
                );
                result = resultSet.getInt("max_index");
                resultSet.close();

            } catch (SQLException e) {
                System.out.println("Failed to get the last index from " + tableName + "! " + e.getMessage());
            }
        }
        return result;
    }

    // uses the input table name to check if the table is empty or not
    public boolean tableIsEmpty(String tableName) {
        boolean result = false;
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT COUNT(*) AS row_count FROM " + tableName
            );
            result = resultSet.getInt("row_count")==0;
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Failed to get count of the table " + tableName + "! " + e.getMessage());
        }
        return result;
    }

    public void close() throws SQLException {
        try {
            connection.close();
            System.out.println("Connection closed!");
        } catch (SQLException e) {
            System.out.println("Failed to close the connection! " + e.getMessage());
        } finally {
            connection.close();
        }
    }

}
