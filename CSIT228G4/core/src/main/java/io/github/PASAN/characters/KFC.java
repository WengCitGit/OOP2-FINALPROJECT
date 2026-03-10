package io.github.PASAN.characters;

public class KFC extends Character {

    public KFC() {
        //stats: Name, Max HP, Max Mana, Mana Regen
        super("Colonel Sanders", 165, 60, 7);

        skills.add(new Skill("DrumStick Smash", 0, 12, 17));
        skills.add(new Skill("Shooting Flaming Hotshot", 15, 20, 30));
        skills.add(new Skill("Secret Sauce Overload", 30, 32, 50));
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

