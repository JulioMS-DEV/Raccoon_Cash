package com.raccooncash.api.category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class CategoryRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    @NotNull(message = "El tipo de categoria es obligatorio")
    private CategoryType type;
    private String color;
    private String icon;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public CategoryType getType() {
        return type;
    }
    public void setType(CategoryType type) {
        this.type = type;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
