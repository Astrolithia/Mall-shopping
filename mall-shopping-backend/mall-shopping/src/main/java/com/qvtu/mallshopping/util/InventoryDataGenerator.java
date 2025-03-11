package com.qvtu.mallshopping.util;

import com.github.javafaker.Faker;
import com.qvtu.mallshopping.model.Inventory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.LocalDateTime;

public class InventoryDataGenerator {
    private final Faker faker;
    private final Random random;

    public InventoryDataGenerator() {
        this.faker = new Faker();
        this.random = new Random();
    }

    public List<Inventory> generateInventories(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> generateInventory())
            .collect(Collectors.toList());
    }

    public Inventory generateInventory() {
        Inventory inventory = new Inventory();
        
        // 设置 SKU
        String adjective = faker.commerce().material().toUpperCase();
        String size = random.nextBoolean() ? "L" : "XL";
        String color = faker.commerce().color().toUpperCase();
        inventory.setSku(String.format("%s-%s-%s", adjective, size, color));
        
        // 设置尺寸和重量
        inventory.setHeight(faker.number().randomDouble(2, 1, 50));
        inventory.setWidth(faker.number().randomDouble(2, 1, 50));
        inventory.setLength(faker.number().randomDouble(2, 1, 50));
        inventory.setWeight(faker.number().randomDouble(2, 100, 2000));
        
        // 设置编码
        inventory.setMidCode("MID" + faker.number().digits(3));
        inventory.setHsCode(String.format("%04d.%02d", 
            faker.number().numberBetween(0, 9999),
            faker.number().numberBetween(0, 99)));
        
        // 设置原产地
        inventory.setOriginCountry(faker.options().option(
            "CN", "US", "JP", "KR", "VN", "IN", "BD", "TR", "IT", "FR"
        ));
        
        // 设置库存数量和管理选项
        inventory.setQuantity(faker.number().numberBetween(50, 1000));
        inventory.setAllowBackorder(faker.bool().bool());
        inventory.setManageInventory(true);
        
        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("supplier", faker.company().name());
        metadata.put("reorder_point", faker.number().numberBetween(10, 50));
        metadata.put("shelf_life_days", faker.number().numberBetween(30, 365));
        inventory.setMetadata(metadata);
        
        return inventory;
    }

    public Map<String, Object> generateLocationLevel() {
        Map<String, Object> locationLevel = new HashMap<>();
        
        // 生成库存数量
        int stockedQuantity = faker.number().numberBetween(100, 1000);
        int reservedQuantity = faker.number().numberBetween(0, stockedQuantity / 4);
        int incomingQuantity = faker.number().numberBetween(0, 200);
        
        locationLevel.put("stocked_quantity", stockedQuantity);
        locationLevel.put("reserved_quantity", reservedQuantity);
        locationLevel.put("available_quantity", stockedQuantity - reservedQuantity);
        locationLevel.put("incoming_quantity", incomingQuantity);
        
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        locationLevel.put("created_at", now.minusDays(faker.number().numberBetween(1, 30)));
        locationLevel.put("updated_at", now);
        locationLevel.put("deleted_at", null);
        
        // 生成元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("last_restock_date", now.minusDays(faker.number().numberBetween(1, 7)).toString());
        metadata.put("next_restock_date", now.plusDays(faker.number().numberBetween(1, 14)).toString());
        metadata.put("restock_quantity", faker.number().numberBetween(100, 500));
        locationLevel.put("metadata", metadata);
        
        return locationLevel;
    }

    public List<Map<String, Object>> generateLocationLevels(int count) {
        List<Map<String, Object>> locationLevels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            locationLevels.add(generateLocationLevel());
        }
        return locationLevels;
    }
} 