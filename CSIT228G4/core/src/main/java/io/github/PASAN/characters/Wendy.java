package io.github.PASAN.characters;

public class Wendy extends Character {

    public Wendy() {
        //stats: Name, Max HP, Max Mana, Mana Regen
        super("Wendy", 165, 55, 6);

        skills.add(new Skill("Sassy Roast", 0, 12, 17));
        skills.add(new Skill("Vanilla Vengeance", 15, 18, 28));
        skills.add(new Skill("Order Blocker", 30, 30, 48));
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
