package com.example.javafxproject;

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

public class ProductController {
    private boolean isEditing = false;

    private double capital = 1000.0; // Exemple de capital initial
    private double globalIncome = 0.0; // Revenus globaux
    private double globalCost = 0.0; // Coûts globaux



    @FXML
    private TableView<Product> tblProducts; // Lien avec la TableView dans SceneBuilder
    @FXML
    private TableColumn<Product, String> colName;
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

    // Liste observable pour stocker les produits
    private ObservableList<Product> productList;

    @FXML
    public void initialize() {
        // Initialiser les colonnes de la TableView
        lblCapital.setText(String.format("%.2f €", capital));
        lblIncome.setText(String.format("%.2f €", globalIncome));
        lblCost.setText(String.format("%.2f €", globalCost));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPurchasePrice.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        colSellPrice.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Ajouter des catégories dans le ComboBox
        cmbCategory.getItems().addAll("Vêtements", "Chaussures", "Accessoires");
        productList = FXCollections.observableArrayList(
                new Product("Robe", 50.0, 100.0, 10, "Vêtements"),
                new Product("Chaussures", 30.0, 60.0, 20, "Chaussures"),
                new Product("Sac à main", 20.0, 40.0, 15, "Accessoires")
        );
        tblProducts.setItems(productList);

        // Désactiver le formulaire au départ
        disableForm();
    }

    private void disableForm() {
        txtName.setDisable(true);
        txtPurchasePrice.setDisable(true);
        txtSellPrice.setDisable(true);
        txtStock.setDisable(true);
        cmbCategory.setDisable(true);
        btnValider.setDisable(true); // Assurez-vous d’avoir déclaré btnValidate
    }


    private void enableForm() {
        txtName.setDisable(false);
        txtPurchasePrice.setDisable(false);
        txtSellPrice.setDisable(false);
        txtStock.setDisable(false);
        cmbCategory.setDisable(false);
        btnValider.setDisable(false); // Activer le bouton Valider
    }

    // Charger une liste simulée de produits
    private void loadProducts() {
        productList = FXCollections.observableArrayList(
                new Product("T-shirt", 10.0, 15.0, 50, "Vêtements"),
                new Product("Sneakers", 50.0, 80.0, 20, "Chaussures"),
                new Product("Sac à main", 30.0, 60.0, 10, "Accessoires")
        );
    }


    @FXML
    private void onValidateProduct() {
        // Valider le formulaire
        if (!validateForm()) {
            return; // Ne pas continuer si le formulaire est invalide
        }

        if (!isEditing) { // Mode ajout
            Product newProduct = new Product(
                    txtName.getText(),
                    Double.parseDouble(txtPurchasePrice.getText()),
                    Double.parseDouble(txtSellPrice.getText()),
                    Integer.parseInt(txtStock.getText()),
                    cmbCategory.getValue()
            );

            // Calcul des coûts et mise à jour du capital
            double cost = newProduct.getPurchasePrice() * newProduct.getStock();
            if (capital >= cost) {
                globalCost += cost;
                capital -= cost;

                // Ajouter le produit à la liste
                productList.add(newProduct);

                showAlert("Succès", "Le produit a été ajouté avec succès !", Alert.AlertType.INFORMATION);
                updateStats(); // Mettre à jour les statistiques
            } else {
                showAlert("Erreur", "Capital insuffisant pour acheter ces produits.", Alert.AlertType.ERROR);
            }

            resetForm();
        } else { // Mode modification
            Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

            if (selectedProduct != null) {
                selectedProduct.setName(txtName.getText());
                selectedProduct.setPurchasePrice(Double.parseDouble(txtPurchasePrice.getText()));
                selectedProduct.setSellPrice(Double.parseDouble(txtSellPrice.getText()));
                selectedProduct.setStock(Integer.parseInt(txtStock.getText()));
                selectedProduct.setCategory(cmbCategory.getValue());

                tblProducts.refresh();

                showAlert("Succès", "Le produit a été modifié avec succès !", Alert.AlertType.INFORMATION);

                resetForm();
                isEditing = false;
            }
        }
    }





    // Méthode pour réinitialiser et désactiver le formulaire
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
    private void onAddProduct() {
        // Activer le formulaire pour ajout
        enableForm();
        isEditing = false; // On sort du mode modification, car on ajoute un nouveau produit
        txtName.clear();
        txtPurchasePrice.clear();
        txtSellPrice.clear();
        txtStock.clear();
        cmbCategory.getSelectionModel().clearSelection();
        // Alerte pour guider l'utilisateur
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajout d'un produit");
        alert.setHeaderText(null);
        alert.setContentText("Veuillez remplir tous les champs du formulaire, puis cliquez sur 'Valider' pour ajouter le produit.");
        alert.showAndWait();
    }


    @FXML
    private void onEditProduct() {
        // Récupérer le produit sélectionné
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Activer le formulaire pour modification
            enableForm();
            isEditing = true;

            // Pré-remplir le formulaire avec les données du produit sélectionné
            txtName.setText(selectedProduct.getName());
            txtPurchasePrice.setText(String.valueOf(selectedProduct.getPurchasePrice()));
            txtSellPrice.setText(String.valueOf(selectedProduct.getSellPrice()));
            txtStock.setText(String.valueOf(selectedProduct.getStock()));
            cmbCategory.setValue(selectedProduct.getCategory());
        } else {
            // Alerte : aucun produit sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun produit sélectionné");
            alert.setHeaderText("Impossible de modifier un produit");
            alert.setContentText("Veuillez sélectionner un produit dans la liste avant de cliquer sur 'Modifier un produit'.");
            alert.showAndWait();
        }
    }



    @FXML
    private void onProductSelected() {
        if (isEditing) { // Vérifier que l'on est bien en mode modification
            Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

            if (selectedProduct != null) {
                // Pré-remplir le formulaire avec les données du produit sélectionné
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
        // Récupérer le produit sélectionné
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Confirmation avant suppression
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le produit");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le produit sélectionné : " + selectedProduct.getName() + " ?");

            // Gestion de la réponse de l'utilisateur
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Supprimer le produit de la liste
                    productList.remove(selectedProduct);

                    // Alerte de confirmation
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Produit supprimé");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Le produit a été supprimé avec succès !");
                    successAlert.showAndWait();
                }
            });
        } else {
            // Alerte : aucun produit sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun produit sélectionné");
            alert.setHeaderText("Impossible de supprimer un produit");
            alert.setContentText("Veuillez sélectionner un produit dans la liste avant de cliquer sur 'Supprimer un produit'.");
            alert.showAndWait();
        }
    }

    private boolean isValidName(String name) {
        // Autoriser les lettres, les chiffres et les espaces uniquement
        return name.matches("[a-zA-Z0-9À-ÖØ-öø-ÿ ]+");
    }

    private boolean validateForm() {
        // Vérifier si le nom est vide
        if (txtName.getText().isEmpty()) {
            showAlert("Erreur", "Le champ 'Nom' est vide. Veuillez le remplir.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier si le nom contient uniquement des caractères valides
        if (!isValidName(txtName.getText())) {
            showAlert("Erreur", "Le champ 'Nom' contient des caractères invalides. Veuillez entrer un texte valide (seulement des lettres).", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier si le prix d'achat est un nombre positif
        if (!isDouble(txtPurchasePrice.getText()) || Double.parseDouble(txtPurchasePrice.getText()) <= 0) {
            showAlert("Erreur", "Le champ 'Prix d'achat' doit être un nombre positif.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier si le prix de vente est un nombre positif
        if (!isDouble(txtSellPrice.getText()) || Double.parseDouble(txtSellPrice.getText()) <= 0) {
            showAlert("Erreur", "Le champ 'Prix de vente' doit être un nombre positif.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier si le stock est un entier positif
        if (!isInteger(txtStock.getText()) || Integer.parseInt(txtStock.getText()) < 0) {
            showAlert("Erreur", "Le champ 'Stock' doit être un entier positif.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier si une catégorie est sélectionnée
        if (cmbCategory.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une catégorie.", Alert.AlertType.ERROR);
            return false;
        }

        return true; // Tout est valide
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

    // Met à jour les labels avec les dernières valeurs
    private void updateStats() {
        lblCapital.setText(String.format("%.2f €", capital));
        lblIncome.setText(String.format("%.2f €", globalIncome));
        lblCost.setText(String.format("%.2f €", globalCost));
    }

    @FXML
    private void onSellProduct() {
        // Vérifier si un produit est sélectionné
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Boîte de dialogue pour entrer la quantité
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Vendre un produit");
            dialog.setHeaderText("Vendre " + selectedProduct.getName());
            dialog.setContentText("Quantité à vendre :");

            dialog.showAndWait().ifPresent(quantityText -> {
                try {
                    int quantity = Integer.parseInt(quantityText);

                    if (quantity > 0 && quantity <= selectedProduct.getStock()) {
                        // Calcul des revenus
                        double income = quantity * selectedProduct.getSellPrice();
                        globalIncome += income;
                        capital += income;

                        // Mise à jour du stock
                        selectedProduct.setStock(selectedProduct.getStock() - quantity);

                        // Rafraîchir la TableView et les stats
                        tblProducts.refresh();
                        updateStats();

                        // Message de succès
                        showAlert("Succès", "Vente réussie ! Vous avez gagné " + income + " €.", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Erreur", "Quantité invalide ou stock insuffisant.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez entrer un nombre valide.", Alert.AlertType.ERROR);
                }
            });
        } else {
            // Alerte si aucun produit n'est sélectionné
            showAlert("Erreur", "Veuillez sélectionner un produit à vendre.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBuyProduct() {
        // Vérifier si un produit est sélectionné
        Product selectedProduct = tblProducts.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Boîte de dialogue pour entrer la quantité à acheter
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Acheter un produit");
            dialog.setHeaderText("Acheter " + selectedProduct.getName());
            dialog.setContentText("Quantité à acheter :");

            dialog.showAndWait().ifPresent(quantityText -> {
                try {
                    int quantity = Integer.parseInt(quantityText);

                    if (quantity > 0) {
                        // Calcul du coût total de l'achat
                        double totalCost = quantity * selectedProduct.getPurchasePrice();

                        if (capital >= totalCost) {
                            // Mettre à jour le stock et les statistiques
                            selectedProduct.setStock(selectedProduct.getStock() + quantity);
                            globalCost += totalCost;
                            capital -= totalCost;

                            // Rafraîchir la TableView et les statistiques
                            tblProducts.refresh();
                            updateStats();

                            // Message de succès
                            showAlert("Succès", "Achat réussi ! Vous avez acheté " + quantity + " " + selectedProduct.getName() + ".", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Erreur", "Capital insuffisant pour cet achat.", Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Erreur", "Veuillez entrer une quantité valide.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez entrer un nombre valide.", Alert.AlertType.ERROR);
                }
            });
        } else {
            // Alerte si aucun produit n'est sélectionné
            showAlert("Erreur", "Veuillez sélectionner un produit à acheter.", Alert.AlertType.ERROR);
        }
    }








}

