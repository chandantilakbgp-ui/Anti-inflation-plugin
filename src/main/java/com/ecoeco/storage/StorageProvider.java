package com.ecoeco.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {
    void init();
    void close();
    CompletableFuture<Double> loadPlayerTax(UUID uuid);
    CompletableFuture<Void> savePlayerTax(UUID uuid, double taxRate);
}
