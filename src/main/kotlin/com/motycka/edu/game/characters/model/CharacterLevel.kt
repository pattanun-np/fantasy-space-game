package com.motycka.edu.game.characters.model


enum class CharacterLevel(val level: Int, val requireExp: Int) {
    LEVEL_1(1, 0),
    LEVEL_2(2, 300),
    LEVEL_3(3, 600),
    LEVEL_4(4, 900),
    LEVEL_5(5, 1200),
    LEVEL_6(6, 1500),
    LEVEL_7(7, 1800),
    LEVEL_8(8, 2100),
    LEVEL_9(9, 2400),
    LEVEL_10(10, 2700);

    companion object {
        fun fromLevel(level: Int): CharacterLevel {
            return values().find { it.level == level } ?: LEVEL_1
        }
    }

    // Get the next level (returns null if already at max level)
    fun getNextLevel(): CharacterLevel? {
        val levels = values()
        val currentIndex = levels.indexOf(this)
        return if (currentIndex < levels.size - 1) levels[currentIndex + 1] else null
    }

    // Check if character has enough experience to level up
    open fun shouldLevelUp(experience: Int): Boolean {
        val nextLevel = getNextLevel() ?: return false
        return experience >= nextLevel.requireExp
    }


    /*
     * Updates the level of a character based on their total points.
     *
     * @param character The character whose level is to be updated. Can be null if otherPoints is provided.
     * @param otherPoints A list of points to be used for level calculation if character is null. Must contain exactly 4 points.
     * @return The updated CharacterLevel based on the total points.
     * @throws IllegalArgumentException if neither character nor otherPoints are provided, or if otherPoints does not contain exactly 4 points.
     */
    fun upLevel(character: Character?, otherPoints: List<Int>?): CharacterLevel {
        val totalPoints: Int
        if (character != null) {
            totalPoints = character.health + when (character) {
                is Warrior -> character.attack + character.stamina + character.defense
                is Sorcerer -> character.attack + character.mana + character.healing
                else -> error("Require Character")
            }
        } else if (otherPoints != null) {
            require(otherPoints.size == 4) {
                "Require 4 points"
            }
            totalPoints = otherPoints.sum()
        } else {
            error("Require Character or List<Int>")
        }

        return entries.toTypedArray().findLast { it.requireExp <= totalPoints } ?: LEVEL_1
    }

}