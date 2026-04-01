package io.github.PASAN.modes;

import io.github.PASAN.characters.Character;

/**
 * ENCAPSULATION: CPU decision logic is isolated here.
 *                PVCBattleScreen calls chooseSkill() — it doesn't contain AI logic itself.
 * ABSTRACTION  : The screen just asks "which skill?" — doesn't know or care about the logic.
 *
 * Not a subclass of Mode because PVC is a single match, not a run with progression.
 */
public class PVCMode {

    // Private constructor — this class is a pure utility, never instantiated
    private PVCMode() {}

    /**
     * POLYMORPHISM: Returns the best skill index for the CPU to use.
     * Smart priority: Ultimate > Secondary > Basic.
     * Screen calls this and passes the result to executeSkill().
     */
    public static int chooseSkill(Character enemy, int[] enemyCD) {
        if (enemyCD[2] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(2).getManaCost()) {
            return 2; // Ultimate
        }
        if (enemyCD[1] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(1).getManaCost()) {
            return 1; // Secondary
        }
        return 0; // Basic Attack
    }
}