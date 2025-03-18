package com.qvtu.mallshopping.util;

import com.github.javafaker.Faker;
import com.qvtu.mallshopping.model.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class LocationDataGenerator {
    private final Faker faker;

    public LocationDataGenerator() {
        this.faker = new Faker(new Locale("zh_CN"));
    }

    public List<Location> generateLocations(int count) {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            locations.add(generateLocation());
        }
        return locations;
    }

    private Location generateLocation() {
        Location location = new Location();
        location.setName(generateWarehouseName());
        location.setAddress(faker.address().fullAddress());
        location.setCity(faker.address().city());
        location.setCountryCode("CN");
        location.setPostalCode(faker.address().zipCode());
        location.setProvince(faker.address().state());
        location.setPhone(faker.phoneNumber().cellPhone());
        location.setMetadata(generateMetadata());
        return location;
    }

    private String generateWarehouseName() {
        String[] prefixes = {"中央", "区域", "城市", "快递", "电商", "跨境"};
        String[] types = {"仓库", "物流中心", "配送中心", "中转站"};
        return faker.options().option(prefixes) + faker.address().city() + faker.options().option(types);
    }

    private Map<String, Object> generateMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("warehouse_type", faker.options().option("main", "backup", "transit", "distribution"));
        metadata.put("capacity", faker.number().numberBetween(1000, 10000));
        metadata.put("temperature_controlled", faker.bool().bool());
        metadata.put("security_level", faker.options().option("high", "medium", "standard"));
        metadata.put("operating_hours", "24/7");
        return metadata;
    }
} 