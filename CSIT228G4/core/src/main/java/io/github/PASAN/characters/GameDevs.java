package io.github.PASAN.characters;

public class GameDevs extends Character
{
    public GameDevs(String name) {
        // stats: Name, Max HP, Max Mana, Mana Regen
        super(name, 435, 100, 5);
        skills.add(new Skill("Dev Strike", 5, 5, 8));
        skills.add(new Skill("Dev Blast", 10, 7, 12));
        skills.add(new Skill("Dev Ultimate", 30, 12, 15));
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
