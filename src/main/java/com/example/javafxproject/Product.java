package com.example.javafxproject;

public class Product implements Discount, Comparable<Product>{
    private int number;
    private String name;
    private double purchasePrice;
    private double sellPrice;
    private double discountPrice;
    private int stock;
    private String category;

    public Product(String name, double purchasePrice, double sellPrice) {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
        this.discountPrice = 0.0;
        this.stock = 0;
    }

    public Product(int number, String name, double purchasePrice, double sellPrice, double discountPrice, int stock, String category) {
        this.number = number;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
        this.discountPrice = discountPrice;
        this.stock = stock;
        this.category = category;
    }

    @Override
    public int compareTo(Product other) {
        return Double.compare(this.purchasePrice, other.purchasePrice);
    }
    @Override
    public void applyDiscount() {
        System.out.println("Applying discount to " + name);
        switch (category) {
            case "Clothes":
                discountPrice = sellPrice * (1 - CLOTHES_DISCOUNT);
                break;
            case "Shoes":
                discountPrice = sellPrice * (1 - SHOES_DISCOUNT);
                break;
            case "Accessories":
                discountPrice = sellPrice * (1 - ACCESSORY_DISCOUNT);
                break;
            default:
                discountPrice = sellPrice;
        }
    }


    @Override
    public void unApplyDiscount() {

        discountPrice = sellPrice;
    }
    public double getDiscountPercentage() {
        if (category.equalsIgnoreCase("Clothes")) {
            return CLOTHES_DISCOUNT * 100; // Retourne 30
        } else if (category.equalsIgnoreCase("Shoes")) {
            return SHOES_DISCOUNT * 100; // Retourne 20
        } else if (category.equalsIgnoreCase("Accessories")) {
            return ACCESSORY_DISCOUNT * 100; // Retourne 50
        }
        return 0;
    }

    public double getDiscountPrice() {
        return discountPrice == 0 ? sellPrice : discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }
    public double getDisplayPrice() {
        return (discountPrice > 0 && discountPrice < sellPrice) ? discountPrice : sellPrice;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        if (purchasePrice < 0) {
            throw new IllegalArgumentException("Negative price!");
        }
        this.purchasePrice = purchasePrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        if (sellPrice < 0) {
            throw new IllegalArgumentException("Negative price!");
        }
        this.sellPrice = sellPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative!");
        }
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name + " - " + category + " - Stock: " + stock + " - Price: " + sellPrice + "€";
    }

}
