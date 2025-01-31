package com.example.javafxproject;
import java.sql.PreparedStatement;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import com.example.javafxproject.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Collections;


public class ProductController {
    private boolean isEditing = false;

    private double capital = 1000.0;
    private double globalIncome = 0.0;
    private double globalCost = 0.0;

    @FXML
    private TableView<Product> tblProducts;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, Double> colPurchasePrice;
    @FXML
    private TableColumn<Product, Double> colSellPrice;
    @FXML
    private TableColumn<Product, Integer> colStock;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPurchasePrice;
    @FXML
    private TextField txtSellPrice;
    @FXML
    private TextField txtStock;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private Button btnValider;
    @FXML
    private Label lblCapital;
    @FXML
    private Label lblIncome;
    @FXML
    private Label lblCost;
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/WomenStoreDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Mazigh_2002";

    @FXML
    public void initialize() {
        loadFinanceFromDatabase();
        updateStats();

        System.out.println("Initialisation du contrôleur...");
        productList = FXCollections.observableArrayList();
        tblProducts.setItems(productList);
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        colPurchasePrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPurchasePrice()));
        colSellPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDisplayPrice()));
        colStock.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStock()));
        cmbCategory.setItems(FXCollections.observableArrayList("Clothes", "Shoes", "Accessories"));

        loadProductsFromDatabase();


        disableForm();
    }

    private void loadFinanceFromDatabase() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT capital, income, cost FROM finance LIMIT 1")) {

            if (rs.next()) {
                capital = rs.getDouble("capital");
                globalIncome = rs.getDouble("income");
                globalCost = rs.getDouble("cost");
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL (loadFinance) : " + e.getMessage());
        }
    }


    @FXML
    private void onShowProducts() {
        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        productList.clear();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("number"),
                        rs.getString("name"),
                        rs.getDouble("purchase_price"),
                        rs.getDouble("sell_price"),
                        rs.getDouble("discount_price"),
                        rs.getInt("stock"),
                        rs.getString("category")
                ));
            }

            Collections.sort(productList);

            tblProducts.setItems(FXCollections.observableArrayList(productList));
            tblProducts.refresh();

        } catch (SQLException e) {
            showAlert("Erreur SQL", "Impossible de charger les produits : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onAddProduct() {
        enableForm();
        isEditing = false;
        txtName.clear();
        txtPurchasePrice.clear();
        txtSellPrice.clear();
        txtStock.clear();
        cmbCategory.getSelectionModel().clearSelection();

        showAlert("Ajout d'un produit", "Remplissez le formulaire et cliquez sur 'Valider'.", Alert.AlertType.INFORMATION);
    }



    private void disableForm() {
        txtName.setDisable(true);
        txtPurchasePrice.setDisable(true);
        txtSellPrice.setDisable(true);
        txtStock.setDisable(true);
        cmbCategory.setDisable(true);
        btnValider.setDisable(true);
    }


    private void enableForm() {
        txtName.setDisable(false);
        txtPurchasePrice.setDisable(false);
        txtSellPrice.setDisable(false);
        txtStock.setDisable(false);
        cmbCategory.setDisable(false);
        btnValider.setDisable(false);
    }


    @FXML
    private void onValidateProduct() {
        if (!validateForm()) {
            return;
        }

        if (!isEditing) {
            try (Connection conn = Database.getConnection()) {
                Product newProduct = new Product(
                        txtName.getText(),
                        Double.parseDouble(txtPurchasePrice.getText()),
                        Double.parseDouble(txtSellPrice.getText())
                );
                newProduct.setStock(Integer.parseInt(txtStock.getText()));
                newProduct.setCategory(cmbCategory.getValue());

                double cost = newProduct.getPurchasePrice() * newProduct.getStock();
                if (capital >= cost) {
                    globalCost += cost;
                    capital -= cost;

                    String query = "INSERT INTO products (name, purchase_price, sell_price, stock, category) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, newProduct.getName());
                        stmt.setDouble(2, newProduct.getPurchasePrice());
                        stmt.setDouble(3, newProduct.getSellPrice());
                        stmt.setInt(4, newProduct.getStock());
                        stmt.setString(5, newProduct.getCategory());

                        int rowsInserted = stmt.executeUpdate();
                        if (rowsInserted > 0) {
                            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    newProduct.setNumber(generatedKeys.getInt(1));
                                }
                            }
                            showAlert("Succès", "Le produit a été ajouté avec succès !", Alert.AlertType.INFORMATION);
                            loadProductsFromDatabase();
                            refreshTable();
                            updateStats();
                        }
                    }
                } else {
                    showAlert("❌ Erreur", "Capital insuffisant pour acheter ces produits.", Alert.AlertType.ERROR);
                }

                resetForm();
            } catch (SQLException e) {
                showAlert("Erreur SQL", "Impossible d'ajouter le produit : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                try (Connection conn = Database.getConnection()) {
                    String query = "UPDATE products SET name=?, purchase_price=?, sell_price=?, stock=?, category=? WHERE number=?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, txtName.getText());
                        stmt.setDouble(2, Double.parseDouble(txtPurchasePrice.getText()));
                        stmt.setDouble(3, Double.parseDouble(txtSellPrice.getText()));
                        stmt.setInt(4, Integer.parseInt(txtStock.getText()));
                        stmt.setString(5, cmbCategory.getValue());
                        stmt.setInt(6, selectedProduct.getNumber());

                        int rowsUpdated = stmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            selectedProduct.setName(txtName.getText());
                            selectedProduct.setPurchasePrice(Double.parseDouble(txtPurchasePrice.getText()));
                            selectedProduct.setSellPrice(Double.parseDouble(txtSellPrice.getText()));
                            selectedProduct.setStock(Integer.parseInt(txtStock.getText()));
                            selectedProduct.setCategory(cmbCategory.getValue());

                            showAlert("Succès", "Le produit a été modifié avec succès !", Alert.AlertType.INFORMATION);
                            tblProducts.refresh();
                            updateStats();
                            resetForm();
                            isEditing = false;
                        } else {
                            showAlert("Erreur", "Aucune modification apportée.", Alert.AlertType.ERROR);
                        }
                    }
                } catch (SQLException e) {
                    showAlert("Erreur SQL", "Impossible de modifier le produit : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Erreur", "Veuillez sélectionner un produit à modifier.", Alert.AlertType.ERROR);
            }
        }
    }





    private void refreshTable() {
        tblProducts.setItems(FXCollections.observableArrayList(productList));
        tblProducts.refresh();
    }








    private void resetForm() {
        txtName.clear();
        txtPurchasePrice.clear();
        txtSellPrice.clear();
        txtStock.clear();
        cmbCategory.getSelectionModel().clearSelection();

        txtName.setDisable(true);
        txtPurchasePrice.setDisable(true);
        txtSellPrice.setDisable(true);
        txtStock.setDisable(true);
        cmbCategory.setDisable(true);
    }





    @FXML
    private void onEditProduct() {
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            enableForm();
            isEditing = true;

            txtName.setText(selectedProduct.getName());
            txtPurchasePrice.setText(String.valueOf(selectedProduct.getPurchasePrice()));
            txtSellPrice.setText(String.valueOf(selectedProduct.getSellPrice()));
            txtStock.setText(String.valueOf(selectedProduct.getStock()));
            cmbCategory.setValue(selectedProduct.getCategory());

            showAlert("Modification en cours", "Modifiez les informations puis cliquez sur 'Valider' pour enregistrer.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Erreur", "Veuillez sélectionner un produit à modifier.", Alert.AlertType.WARNING);
        }
    }




    @FXML
    private void onProductSelected() {
        if (isEditing) {
            Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

            if (selectedProduct != null) {
                txtName.setText(selectedProduct.getName());
                txtPurchasePrice.setText(String.valueOf(selectedProduct.getPurchasePrice()));
                txtSellPrice.setText(String.valueOf(selectedProduct.getSellPrice()));
                txtStock.setText(String.valueOf(selectedProduct.getStock()));
                cmbCategory.setValue(selectedProduct.getCategory());
            }
        }
    }

    @FXML
    private void onDeleteProduct() {
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le produit");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le produit sélectionné : " + selectedProduct.getName() + " ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = Database.getConnection()) {
                        String query = "DELETE FROM products WHERE number=?";
                        try (PreparedStatement stmt = conn.prepareStatement(query)) {
                            stmt.setInt(1, selectedProduct.getNumber());

                            int rowsDeleted = stmt.executeUpdate();
                            if (rowsDeleted > 0) {
                                productList.remove(selectedProduct);
                                loadProductsFromDatabase();
                                tblProducts.refresh();
                                updateStats();
                                showAlert("Succès", "Produit supprimé avec succès !", Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Erreur", "Impossible de supprimer le produit.", Alert.AlertType.ERROR);
                            }
                        }
                    } catch (SQLException e) {
                        showAlert("Erreur SQL", "Impossible de supprimer le produit : " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Erreur", "Veuillez sélectionner un produit à supprimer.", Alert.AlertType.ERROR);
        }
    }




    private boolean isValidName(String name) {
        return name.matches("[a-zA-Z0-9À-ÖØ-öø-ÿ ]+");
    }

    private boolean validateForm() {
        if (txtName.getText().isEmpty()) {
            showAlert("Erreur", "Le champ 'Nom' est vide. Veuillez le remplir.", Alert.AlertType.ERROR);
            return false;
        }
        if (!isValidName(txtName.getText())) {
            showAlert("Erreur", "Le champ 'Nom' contient des caractères invalides. Veuillez entrer un texte valide (seulement des lettres).", Alert.AlertType.ERROR);
            return false;
        }

        if (!isDouble(txtPurchasePrice.getText()) || Double.parseDouble(txtPurchasePrice.getText()) <= 0) {
            showAlert("Erreur", "Le champ 'Prix d'achat' doit être un nombre positif.", Alert.AlertType.ERROR);
            return false;
        }
        if (!isDouble(txtSellPrice.getText()) || Double.parseDouble(txtSellPrice.getText()) <= 0) {
            showAlert("Erreur", "Le champ 'Prix de vente' doit être un nombre positif.", Alert.AlertType.ERROR);
            return false;
        }

        if (!isInteger(txtStock.getText()) || Integer.parseInt(txtStock.getText()) < 0) {
            showAlert("Erreur", "Le champ 'Stock' doit être un entier positif.", Alert.AlertType.ERROR);
            return false;
        }

        if (cmbCategory.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une catégorie.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }


    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStats() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT capital, income, cost FROM finance")) {

            if (rs.next()) {
                capital = rs.getDouble("capital");
                globalIncome = rs.getDouble("income");
                globalCost = rs.getDouble("cost");

                lblCapital.setText(String.format("%.2f €", capital));
                lblIncome.setText(String.format("%.2f €", globalIncome));
                lblCost.setText(String.format("%.2f €", globalCost));
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Impossible de récupérer les statistiques financières.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onSellProduct() {
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Vendre un produit");
            dialog.setHeaderText("Vendre " + selectedProduct.getName());
            dialog.setContentText("Quantité à vendre :");

            dialog.showAndWait().ifPresent(quantityText -> {
                try {
                    int quantity = Integer.parseInt(quantityText);
                    if (quantity > 0 && quantity <= selectedProduct.getStock()) {
                        double revenue = quantity * selectedProduct.getSellPrice();
                        selectedProduct.setStock(selectedProduct.getStock() - quantity);
                        globalIncome += revenue;
                        capital += revenue;

                        try (Connection conn = Database.getConnection()) {
                            conn.setAutoCommit(false);

                            try (PreparedStatement stmt1 = conn.prepareStatement(
                                    "UPDATE products SET stock = ? WHERE number = ?")) {
                                stmt1.setInt(1, selectedProduct.getStock());
                                stmt1.setInt(2, selectedProduct.getNumber());
                                stmt1.executeUpdate();
                            }

                            try (PreparedStatement stmt2 = conn.prepareStatement(
                                    "UPDATE finance SET capital = capital + ?, income = income + ?")) {
                                stmt2.setDouble(1, revenue);
                                stmt2.setDouble(2, revenue);
                                stmt2.executeUpdate();
                            }

                            conn.commit();

                            tblProducts.refresh();
                            updateStats();

                            showAlert("Succès", "Vente réussie !", Alert.AlertType.INFORMATION);
                        } catch (SQLException e) {
                            showAlert("Erreur SQL", "Erreur lors de la vente du produit : " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Erreur", "Stock insuffisant ou quantité invalide.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez entrer un nombre valide.", Alert.AlertType.ERROR);
                }
            });
        } else {
            showAlert("Erreur", "Veuillez sélectionner un produit à vendre.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onBuyProduct() {
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Acheter un produit");
            dialog.setHeaderText("Acheter " + selectedProduct.getName());
            dialog.setContentText("Quantité à acheter :");

            dialog.showAndWait().ifPresent(quantityText -> {
                try {
                    int quantity = Integer.parseInt(quantityText);
                    if (quantity <= 0) {
                        showAlert("Erreur", "Veuillez entrer une quantité valide.", Alert.AlertType.ERROR);
                        return;
                    }

                    double totalCost = quantity * selectedProduct.getPurchasePrice();

                    if (capital >= totalCost) {
                        try (Connection conn = Database.getConnection()) {
                            conn.setAutoCommit(false);

                            try (PreparedStatement stmt1 = conn.prepareStatement(
                                    "UPDATE products SET stock = ? WHERE number = ?")) {
                                stmt1.setInt(1, selectedProduct.getStock() + quantity);
                                stmt1.setInt(2, selectedProduct.getNumber());
                                stmt1.executeUpdate();
                            }

                            try (PreparedStatement stmt2 = conn.prepareStatement(
                                    "UPDATE finance SET capital = capital - ?, cost = cost + ?")) {
                                stmt2.setDouble(1, totalCost);
                                stmt2.setDouble(2, totalCost);
                                stmt2.executeUpdate();
                            }

                            conn.commit();

                            selectedProduct.setStock(selectedProduct.getStock() + quantity);
                            globalCost += totalCost;
                            capital -= totalCost;

                            tblProducts.refresh();
                            updateStats();

                            showAlert("Succès", "Achat réussi !", Alert.AlertType.INFORMATION);
                        } catch (SQLException e) {
                            showAlert("Erreur SQL", "Erreur lors de l'achat : " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Erreur", "Capital insuffisant pour cet achat.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez entrer un nombre valide.", Alert.AlertType.ERROR);
                }
            });
        } else {
            showAlert("Erreur", "Veuillez sélectionner un produit à acheter.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onApplyDiscount() {
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            selectedProduct.applyDiscount();

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE products SET discount_price = ? WHERE number = ?")) {

                stmt.setDouble(1, selectedProduct.getDiscountPrice());
                stmt.setInt(2, selectedProduct.getNumber());
                stmt.executeUpdate();
            } catch (SQLException e) {
                showAlert("Erreur SQL", "Impossible d'appliquer la remise : " + e.getMessage(), Alert.AlertType.ERROR);
            }
            double discountPercentage = selectedProduct.getDiscountPercentage();
            tblProducts.refresh();
            showAlert("Remise appliquée", "Une remise de " + discountPercentage + "% a été appliquée sur " + selectedProduct.getName(),Alert.AlertType.INFORMATION);}
        else {
            showAlert("Erreur", "Veuillez sélectionner un produit.", Alert.AlertType.ERROR);
        }
        tblProducts.refresh();

    }

    @FXML
    private void onRemoveDiscount() {
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            selectedProduct.unApplyDiscount();

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE products SET discount_price = ? WHERE number = ?")) {

                stmt.setDouble(1, selectedProduct.getSellPrice());
                stmt.setInt(2, selectedProduct.getNumber());
                stmt.executeUpdate();
            } catch (SQLException e) {
                showAlert("Erreur SQL", "Impossible de retirer la remise : " + e.getMessage(), Alert.AlertType.ERROR);
            }

            tblProducts.refresh();
            showAlert("Succès", "Remise retirée !", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Erreur", "Veuillez sélectionner un produit.", Alert.AlertType.ERROR);
        }
        tblProducts.refresh();

    }




}

