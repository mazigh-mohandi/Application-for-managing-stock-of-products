package com.example.javafxproject;

public interface Discount {
    double CLOTHES_DISCOUNT = 0.3;
    double SHOES_DISCOUNT = 0.2;
    double ACCESSORY_DISCOUNT = 0.5;

    void applyDiscount();
    void unApplyDiscount();
}
