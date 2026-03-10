package io.github.PASAN.characters;

public class Mcdonalds extends Character {

    public Mcdonalds() {
        //stats: Name, Max HP, Max Mana, Mana Regen
        super("McDonald's", 160, 60, 7);
        skills.add(new Skill("Frosty McFlurry Blast", 0, 11, 16));
        skills.add(new Skill("Crispy BFF Fries Fury", 15, 19, 29));
        skills.add(new Skill("Colossal BIG MAC Slam", 30, 34, 52));
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
