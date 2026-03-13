package io.github.PASAN.characters;

public class Jollibee extends Character {

    public Jollibee() {
        // stats: Name, Max HP, Max Mana, Mana Regen
        super("Jollibee", 165, 55, 6);
        skills.add(new Skill("Juicylicious Slam", 0, 12, 17));
        skills.add(new Skill("Langhap Sarap Aura", 15, 18, 28));
        skills.add(new Skill("Super Meal Domination", 30, 32, 50));
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