package io.github.PASAN.characters;

// ENCAPSULATION: All fields private, exposed only through getters.
// Skill is immutable — no setters needed since stats never change after creation.
public class Skill {

    private final String name;
    private final int manaCost;
    private final int minDmg;
    private final int maxDmg;

    public Skill(String name, int manaCost, int minDmg, int maxDmg) {
        this.name     = name;
        this.manaCost = manaCost;
        this.minDmg   = minDmg;
        this.maxDmg   = maxDmg;
    }

    public String getName()    { return name; }
    public int getManaCost()   { return manaCost; }
    public int getMinDmg()     { return minDmg; }
    public int getMaxDmg()     { return maxDmg; }
}