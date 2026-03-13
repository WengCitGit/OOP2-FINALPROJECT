package io.github.PASAN.characters;

public class LittleCaesar extends Character {

    public LittleCaesar() {
        //stats: Name, Max HP, Max Mana, Mana Regen
        super("Little Caesars", 165, 55, 6);

        skills.add(new Skill("Hot-N-Ready Slam", 0, 12, 17));
        skills.add(new Skill("Crazy Bread Barrage", 15, 20, 30));
        skills.add(new Skill("Deep! Dish Catastrophe", 30, 34, 52));
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
