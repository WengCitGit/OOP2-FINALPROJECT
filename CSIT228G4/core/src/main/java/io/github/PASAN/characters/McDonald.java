package io.github.PASAN.characters;

/**
 * INHERITANCE  : Extends Character — gets all shared behavior for free.
 * POLYMORPHISM : Overrides the three skill methods with BurgerKing-specific behavior.
 */

public class McDonald extends Character {

    public McDonald() {
        //stats: Name, Max HP, Max Mana, Mana Regen
        super("McDonald", 160, 60, 7);
        skills.add(new Skill("McFlurry Blast", 0, 11, 16));
        skills.add(new Skill("BFF Fries Fury", 15, 19, 29));
        skills.add(new Skill("BIG MAC Slam", 30, 34, 52));
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
