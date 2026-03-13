package io.github.PASAN.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Character {

    private String name;
    private int hp;
    private int maxHp;
    private int currMana;
    private int maxMana;
    private int regenMana;
    protected List<Skill> skills;
    private Random random;

    public Character(String name, int maxHp, int maxMana, int regenMana) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.maxMana = maxMana;
        this.currMana = maxMana;
        this.regenMana = regenMana;

        this.random = new Random();
        this.skills = new ArrayList<>();
    }

    protected void performAttack(Character target, Skill skill) {
        if (currMana < skill.getManaCost()) return;
        currMana -= skill.getManaCost();

        int damage = skill.getMinDmg() + random.nextInt(skill.getMaxDmg() - skill.getMinDmg() + 1);
        target.takeDamage(damage);
    }

    public abstract void basicAttack(Character target);
    public abstract void secondarySkill(Character target);
    public abstract void ultimateSkill(Character target);

    // ---- NEW METHODS ----
    public void skill(Character target) {
        secondarySkill(target);
    }

    public void ultimate(Character target) {
        ultimateSkill(target);
    }

    // ----------------------
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void addMana(int amount) {
        currMana = Math.min(maxMana, currMana + amount);
    }

    public void restoreHP() {
        hp = maxHp;
    }

    public void restoreMana() {
        currMana = maxMana;
    }

    public void regenerateMana() {
        addMana(regenMana);
    }

    public void healPercentage(double percent) {
        if (percent <= 0) return;
        int heal = (int) Math.ceil(maxHp * percent);
        hp = Math.min(maxHp, hp + heal);
    }

    public List<Skill> getSkills() { return skills; }
    public String getName() { return name; }
    public int getHealth() { return hp; }
    public int getMaxHealth() { return maxHp; }
    public int getCurrentMana() { return currMana; }
    public int getMaxMana() { return maxMana; }
    public int getRegenMana() { return regenMana; }
}