package io.github.PASAN.characters;

public class BurgerKing extends Character {

    public BurgerKing() {
        // base Stats: Name, Max HP, Max Mana, Mana Regen
        super("Burger King", 160, 60, 6);

        // skills: Name, Mana Cost, Min Damage, Max Damage

        // basic attack
        skills.add(new Skill("Whopper Flame", 0, 11, 16));

        // secondary
        skills.add(new Skill("Onion Ring Shockwave", 15, 21, 31));

        // ultimate
        skills.add(new Skill("Flame-Grilled Feast", 30, 30, 48));
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