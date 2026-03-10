package io.github.PASAN.characters;

public class Skill {
    private String name;
    private int manaCost;
    private int minDmg;
    private int maxDmg;

    public Skill(String name, int manaCost, int minDmg, int maxDmg) {
        this.name = name;
        this.manaCost = manaCost;
        this.minDmg = minDmg;
        this.maxDmg = maxDmg;
    }

    public String getName() { return name; }
    public int getManaCost() { return manaCost; }
    public int getMinDmg() { return minDmg; }
    public int getMaxDmg() { return maxDmg; }
}
