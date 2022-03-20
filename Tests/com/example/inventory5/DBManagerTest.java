package com.example.inventory5;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DBManagerTest {

    private DBManager dbManager;

    @BeforeAll
    public void setupAll() {
        dbManager = new DBManager();
        dbManager.setupMainTable();

        if (!dbManager.productExists("hello world")) {
            Product testProduct = new Product();

            testProduct.setProductName("hello world");
            testProduct.setProductUnitPrice(10.0);
            testProduct.setImagePath("C:\\Users\\amirh\\IdeaProjects\\Inventory5\\Photos\\noPhoto.png");

            dbManager.recordInMainTable(testProduct);
        }
    }

    @AfterAll
    public void finishAll() throws SQLException {
        dbManager.close();
    }

    @Test
    @Order(1)
    @DisplayName("Should check if the database exists.")
    public void checkDatabaseExistence() {
        File file = new File("C:\\Users\\amirh\\Desktop\\SQL\\inventory\\Inventory3.db");
        Assertions.assertTrue(file.exists());
    }

    @Test
    @Order(2)
    @DisplayName("Should check if a specific table exists.")
    public void shouldCheckForGivenTableExistence() {
        Assertions.assertTrue(dbManager.tableExists("main_table"));
        Assertions.assertFalse(dbManager.tableExists("Test"));
    }

    @Test
    @Order(3)
    @DisplayName("Should check if table has all required columns")
    public void shouldCheckColumnsOfATable() {
        List<String> columnNames = List.of("_id","name","table_name","unit_price","qty","image");
        Assertions.assertIterableEquals(columnNames,dbManager.getListOfColumns("main_table"));
    }

    @Test
    @Order(4)
    @DisplayName("Should check if method can record data")
    public void shouldCheckForRecording() {
        List<Object> expectation = List.of("hello world","HelloWorldTable",10.0);
        Assertions.assertIterableEquals(expectation,dbManager.getProductInfo("hello world"));
    }

    @Test
    @Order(5)
    @DisplayName("Should check updating a table")
    public void shouldCheckTableUpdating() {
        dbManager.updateTable("hello world","unit_price",20.0);
        Assertions.assertTrue(dbManager.getProductInfo("hello world").contains(20.0));
    }

    @Test
    @Order(6)
    @DisplayName("Should check if method can delete specific target")
    public void shouldCheckDeleteMethodFunction() {
        dbManager.deleteFromMain("hello world");
        Assertions.assertFalse(dbManager.productExists("hello world"));
    }

}