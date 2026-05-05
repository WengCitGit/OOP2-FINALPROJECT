package io.github.PASAN.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ABSTRACTION  : Hides all combat math behind clean public methods.
 *                Subclasses only define WHAT their skills do, not HOW damage is calculated.
 * ENCAPSULATION: All fields are private. HP/Mana can only be changed through
 *                controlled methods (takeDamage, addMana, etc.) — never set directly.
 * INHERITANCE  : Every character (Jollibee, McDonald, etc.) extends this class
 *                and inherits all shared battle behavior for free.
 */
public abstract class Character {


    private final String name;
    private int hp;
    private final int maxHp;
    private int currMana;
    private final int maxMana;
    private final int regenMana;
    private final Random random;

    // protected so subclasses can add skills in their constructors
    protected final List<Skill> skills;

    // ---------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------

    public Character(String name, int maxHp, int maxMana, int regenMana) {
        this.name      = name;
        this.maxHp     = maxHp;
        this.hp        = maxHp;
        this.maxMana   = maxMana;
        this.currMana  = maxMana;
        this.regenMana = regenMana;
        this.random    = new Random();
        this.skills    = new ArrayList<>();
    }

    // ---------------------------------------------------------------
    // ABSTRACTION: Shared combat logic — subclasses call this, never reimplement it.
    // ENCAPSULATION: Mana is deducted and damage is calculated in ONE place only.
    // ---------------------------------------------------------------

    protected void performAttack(Character target, Skill skill) {
        if (currMana < skill.getManaCost()) return;
        currMana -= skill.getManaCost();
        int damage = skill.getMinDmg() + random.nextInt(skill.getMaxDmg() - skill.getMinDmg() + 1);
        target.takeDamage(damage);
    }

    // ---------------------------------------------------------------
    // ABSTRACTION + POLYMORPHISM: Each subclass defines its own skill behavior.
    // The caller just says character.basicAttack(target) — doesn't care which character it is.
    // ---------------------------------------------------------------

    public abstract void basicAttack(Character target);
    public abstract void secondarySkill(Character target);
    public abstract void ultimateSkill(Character target);

    // ---------------------------------------------------------------
    // ENCAPSULATION: HP and Mana are mutated ONLY through these methods.
    // This prevents invalid states like negative HP or mana above max.
    // ---------------------------------------------------------------

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);         // can never go below 0
    }

    public void addMana(int amount) {
        currMana = Math.min(maxMana, currMana + amount);   // can never exceed max
    }

    public void regenerateMana() {
        addMana(regenMana);
    }

    public void healPercentage(double percent) {
        if (percent <= 0) return;
        int heal = (int) Math.ceil(maxHp * percent);
        hp = Math.min(maxHp, hp + heal);
    }

    public void restoreHP()   { hp = maxHp; }
    public void restoreMana() { currMana = maxMana; }

    public boolean isAlive()  { return hp > 0; }

    // ---------------------------------------------------------------
    // ENCAPSULATION: Read-only getters — callers can READ but never WRITE directly
    // ---------------------------------------------------------------

    public String     getName()        { return name; }
    public int        getHealth()      { return hp; }
    public int        getMaxHealth()   { return maxHp; }
    public int        getCurrentMana() { return currMana; }
    public int        getMaxMana()     { return maxMana; }
    public int        getRegenMana()   { return regenMana; }
    public List<Skill> getSkills()     { return skills; }
}