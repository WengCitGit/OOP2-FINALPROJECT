package io.github.PASAN.characters;

/**
 * INHERITANCE  : Extends Character.
 * POLYMORPHISM : Two constructors — default and arcadeOP boosted version.
 *                Both produce a ChiefKhai but with different stats/skills.
 *                The caller doesn't need to know which variant it's getting.
 */

public class ChiefKhai extends Character {

    public ChiefKhai() {
        this(false);
    }

    public ChiefKhai(boolean arcadeOP) {
        super(
                "Chief Khai",
                arcadeOP ? 180 : 165, // Max HP
                arcadeOP ? 85 : 55,   // Max Mana
                arcadeOP ? 8 : 7      // Mana Regen
        );

        if (arcadeOP) {
            // Arcade OP Skills
            skills.add(new Skill("OP Strike", 0, 13, 18));
            skills.add(new Skill("OP Kick", 18, 23, 33));
            skills.add(new Skill("OP Ultimate", 30, 32, 50));
        } else {
            // Standard Skills
            skills.add(new Skill("Batuta Bonk", 0, 11, 16));
            skills.add(new Skill("Pakibukas ng Bag", 15, 21, 31));
            skills.add(new Skill("Whistle Shockwave", 30, 32, 50));
        }
    }

    @Override
    public void basicAttack(Character target) {
        performAttack(target, skills.get(0));
    }

    @Override
    public void secondarySkill(Character target) {
        performAttack(target, skills.get(1));
    }

    @Override
    public void ultimateSkill(Character target) {
        performAttack(target, skills.get(2));
    }
}