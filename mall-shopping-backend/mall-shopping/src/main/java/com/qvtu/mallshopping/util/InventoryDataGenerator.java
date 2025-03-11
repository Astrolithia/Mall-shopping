package com.qvtu.mallshopping.util;

import com.github.javafaker.Faker;
import com.qvtu.mallshopping.model.Inventory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InventoryDataGenerator {
    private final Faker faker = new Faker();
    
    public Inventory generateInventory() {
        Inventory inventory = new Inventory();
        
        // 生成基本信息
        String productType = faker.commerce().productName().split(" ")[0].toUpperCase();
        String size = faker.options().option("S", "M", "L", "XL", "XXL");
        String color = faker.commerce().color().toUpperCase();
        inventory.setSku(String.format("%s-%s-%s", productType, size, color));
        
        // 设置库存数量和管理选项
        inventory.setQuantity(faker.number().numberBetween(10, 1000));
        inventory.setAllowBackorder(faker.bool().bool());
        inventory.setManageInventory(true);
        
        // 设置物品尺寸和重量
        inventory.setHeight(faker.number().randomDouble(2, 1, 50));
        inventory.setWidth(faker.number().randomDouble(2, 1, 50));
        inventory.setLength(faker.number().randomDouble(2, 1, 50));
        inventory.setWeight(faker.number().randomDouble(2, 100, 2000));
        
        // 设置商品编码
        inventory.setMidCode("MID" + faker.number().digits(3));
        inventory.setHsCode(faker.number().digits(4) + "." + faker.number().digits(2));
        
        // 设置原产地
        inventory.setOriginCountry(faker.options().option(
            "CN", "US", "JP", "KR", "VN", "IN", "BD", "TR", "IT", "FR"
        ));
        
        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("color", color.toLowerCase());
        metadata.put("size", size);
        metadata.put("material", faker.commerce().material());
        metadata.put("department", faker.commerce().department());
        inventory.setMetadata(metadata);
        
        return inventory;
    }
    
    public List<Inventory> generateInventories(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> generateInventory())
            .collect(Collectors.toList());
    }
} 