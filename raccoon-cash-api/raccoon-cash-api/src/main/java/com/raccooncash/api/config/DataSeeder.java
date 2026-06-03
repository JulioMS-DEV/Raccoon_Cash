package com.raccooncash.api.config;

import com.raccooncash.api.category.Category;
import com.raccooncash.api.category.CategoryRepository;
import com.raccooncash.api.category.CategoryType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedCategory("Comida", CategoryType.EXPENSE, "#FF7043", "restaurant");
        seedCategory("Transporte", CategoryType.EXPENSE, "#42A5F5", "directions_bus");
        seedCategory("Universidad", CategoryType.EXPENSE, "#7E57C2", "school");
        seedCategory("Gimnasio", CategoryType.EXPENSE, "#66BB6A", "fitness_center");
        seedCategory("Entretenimiento", CategoryType.EXPENSE, "#EC407A", "movie");
        seedCategory("Salud", CategoryType.EXPENSE, "#26A69A", "health_and_safety");
        seedCategory("Servicios", CategoryType.EXPENSE, "#FFA726", "receipt_long");
        seedCategory("Otros", CategoryType.EXPENSE, "#90A4AE", "category");

        seedCategory("Salario", CategoryType.INCOME, "#4CAF50", "payments");
        seedCategory("Regalo", CategoryType.INCOME, "#AB47BC", "redeem");
        seedCategory("Venta", CategoryType.INCOME, "#29B6F6", "sell");
        seedCategory("Reembolso", CategoryType.INCOME, "#26C6DA", "reply");
        seedCategory("Otros", CategoryType.INCOME, "#78909C", "category");
    }

    private void seedCategory(String name, CategoryType type, String color, String icon) {
        if (categoryRepository.existsByNameIgnoreCaseAndTypeAndActiveTrue(name, type)) {
            return;
        }

        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setColor(color);
        category.setIcon(icon);
        category.setActive(true);
        categoryRepository.save(category);
    }
}