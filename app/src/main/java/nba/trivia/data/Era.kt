package nba.trivia.data

enum class Era(val title: String, val years: String) {
    EARLY_YEARS("The Early Years", "1946–1979"),
    MAGIC_AND_BIRD("Magic & Bird", "1980s"),
    JORDAN("The Jordan Era", "1990s"),
    KOBE_AND_SHAQ("Kobe, Shaq & Duncan", "2000s"),
    MODERN("The Modern Game", "2010s–today");

    val label: String get() = "$title ($years)"

    val startYear: String get() = years.take(4)

    val previous: Era? get() = entries.getOrNull(ordinal - 1)
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
}
