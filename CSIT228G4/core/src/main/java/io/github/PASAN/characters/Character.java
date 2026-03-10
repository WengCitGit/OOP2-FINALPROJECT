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
    private Random random;

    protected List<Skill> skills;


    public Character(String name, int maxHp, int maxMana, int regenMana) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.maxMana = maxMana;
        this.currMana = maxMana;
        this.regenMana = regenMana;

        this.random = new Random();
        this.skills = new ArrayList<>(); // Initialize the list
    }

    protected void performAttack(Character target, Skill skill) {
        if (this.currMana < skill.getManaCost()) {
            currMana = 0;
            return;
        }

        this.currMana -= skill.getManaCost();
        int damage = skill.getMinDmg() + this.random.nextInt(skill.getMaxDmg() - skill.getMinDmg() + 1);
        target.takeDamage(damage);

    }


    public abstract void basicAttack(Character target);
    public abstract void secondarySkill(Character target);
    public abstract void ultimateSkill(Character target);

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0) this.hp = 0;
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public void addMana(int amount) {
        this.currMana = Math.min(this.maxMana, this.currMana + amount);
    }

    public void restoreHP() {
        this.hp = this.maxHp;
    }

    public void restoreMana() {
        this.currMana = this.maxMana;
    }

    public void healPercentage(double percent) {
        if (percent <= 0) return;
        int heal = (int) Math.ceil(this.maxHp * percent);
        this.hp = Math.min(this.maxHp, this.hp + heal);
    }

    public java.util.List<Skill> getSkills() {
        return this.skills;
    }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public int getHealth() { return this.hp; }
    public int getMaxHealth() { return this.maxHp; }
    public int getCurrentMana() { return this.currMana; }
    public int getMaxMana() { return this.maxMana; }
    public int getRegenMana() { return this.regenMana; }
}