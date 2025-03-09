package com.qvtu.mallshopping.config;

import com.qvtu.mallshopping.model.Category;
import com.qvtu.mallshopping.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository) {
        return args -> {
            // 只有当数据库为空时才初始化数据
            if (categoryRepository.count() == 0) {
                System.out.println("开始初始化分类数据...");

                // 创建顶级分类：服装
                Category clothing = new Category();
                clothing.setName("服装");
                clothing.setHandle("clothing");
                clothing.setDescription("各类服装");
                clothing.setIsActive(true);
                clothing.setRank(1);
                clothing.setMetadata(new HashMap<>());
                categoryRepository.save(clothing);

                // 创建顶级分类：鞋类
                Category shoes = new Category();
                shoes.setName("鞋类");
                shoes.setHandle("shoes");
                shoes.setDescription("各类鞋子");
                shoes.setIsActive(true);
                shoes.setRank(2);
                shoes.setMetadata(new HashMap<>());
                categoryRepository.save(shoes);

                // 在鞋类下创建子分类：运动鞋
                Category sportsShoes = new Category();
                sportsShoes.setName("运动鞋");
                sportsShoes.setHandle("sports-shoes");
                sportsShoes.setDescription("各类运动鞋");
                sportsShoes.setIsActive(true);
                sportsShoes.setRank(1);
                sportsShoes.setParentCategory(shoes);
                sportsShoes.setMetadata(new HashMap<>());
                categoryRepository.save(sportsShoes);

                // 在服装下创建子分类：上衣
                Category tops = new Category();
                tops.setName("上衣");
                tops.setHandle("tops");
                tops.setDescription("各类上衣");
                tops.setIsActive(true);
                tops.setRank(1);
                tops.setParentCategory(clothing);
                tops.setMetadata(new HashMap<>());
                categoryRepository.save(tops);

                System.out.println("分类数据初始化完成。");
            }
        };
    }
} 