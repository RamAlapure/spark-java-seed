package com.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StockPrice implements Serializable {
    private String symbol;
    private LocalDate date;
    private double price;
}