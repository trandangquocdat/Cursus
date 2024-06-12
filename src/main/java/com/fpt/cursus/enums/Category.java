package com.fpt.cursus.enums;

public enum Category {
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    COMPUTER_SCIENCE("Computer Science"),
    INFORMATION_TECHNOLOGY("Information Technology"),
    ENGINEERING("Engineering"),
    FINANCE("Finance"),
    MARKETING("Marketing"),
    HUMAN_RESOURCES("Human Resources"),
    LITERATURE("Literature"),
    HISTORY("History"),
    PHILOSOPHY("Philosophy"),
    VISUAL_ARTS("Visual Arts"),
    MUSIC("Music"),
    PSYCHOLOGY(("Psychology")),
    SOCIOLOGY(("Sociology")),
    ANTHROPOLOGY(("Anthropology")),
    ECONOMICS("Economics"),
    NURSING("Nursing"),
    PUBLIC_HEALTH("Public Health"),
    NUTRITION("Nutrition"),
    LAW("Law"),
    LANGUAGE("Language"),
    GRAPHIC_DESIGN("Graphic Design"),
    PROGRAMMING("Programming"),
    WEB_DEVELOPMENT("Web Development"),
    DATA_SCIENCE("Data Science"),
    AI("Ai"),
    LIFE_COACHING("Life Coaching"),
    STRESS_MANAGEMENT("Stress Management"),;

    private final String cate;

    private Category(String cate) {
        this.cate = cate;
    }

    public static Category getCategory(String category) {
        for (Category element : Category.values()) {
            if (element.cate.equalsIgnoreCase(category)) return element;
        }
        return null;
    }
}
