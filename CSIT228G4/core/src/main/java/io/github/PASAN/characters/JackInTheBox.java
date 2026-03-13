package io.github.PASAN.characters;

public class JackInTheBox extends Character {

    public JackInTheBox() {
        super("Jack In The Box", 160, 60, 7);
        skills.add(new Skill("Headbutt Express", 0, 11, 16));
        skills.add(new Skill("Drive-Thru Confusion", 15, 19, 29));
        skills.add(new Skill("Jumbo Jack Combo", 30, 32, 50));
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