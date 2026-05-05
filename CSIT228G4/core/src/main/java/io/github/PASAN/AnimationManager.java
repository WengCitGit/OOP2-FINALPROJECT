package io.github.PASAN;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * ENCAPSULATION : All animation loading and caching lives here.
 * ABSTRACTION   : Callers ask for poses or skill animations — never
 *                 touch file paths or Texture objects directly.
 *
 * Naming convention expected on disk:
 *
 *   POSES (character sprite swaps):
 *   Idle  : assets/characters/<n>/<n>_pose1.png  (normal/idle state — 1 frame)
 *   Skill : assets/characters/<n>/<n>_pose2.png  (casting stance — 1 frame)
 *
 *   SKILL ANIMATIONS (projectile animations):
 *   Skill1: assets/characters/<n>/<n>_skill1_frame1.png ... _frame5.png (5 frames)
 *   Skill2: assets/characters/<n>/<n>_skill2_frame1.png ... _frame5.png (5 frames)
 *   Skill3: assets/characters/<n>/<n>_skill3_frame1.png ... _frame5.png (5 frames)
 *
 * Workflow:
 *   1. Character stands in pose1 (idle)
 *   2. Skill pressed → character sprite swaps to pose2 (casting stance)
 *   3. Skill animation (multi-frame projectile) launches separately
 *   4. Projectile lands → character returns to pose1
 *
 * Characters with missing assets fall back to a 1×1 placeholder texture.
 */
public class AnimationManager {

    // ── constants ──────────────────────────────────────────────────────────
    public static final int SKILL_FRAMES     = 5;
    public static final float SKILL_FRAME_DUR = 0.10f; // seconds per skill frame

    // ── internal cache ─────────────────────────────────────────────────────

    private final Map<String, TextureRegion> poses = new HashMap<>();

    private final Map<String, Animation<TextureRegion>> skillAnimations = new HashMap<>();


    private final java.util.List<Texture> ownedTextures = new java.util.ArrayList<>();

    // ── placeholder (shared) ───────────────────────────────────────────────
    private Texture       placeholderTex;
    private TextureRegion placeholderRegion;
    private Animation<TextureRegion> placeholderAnimation;

    // ── constructor ────────────────────────────────────────────────────────
    public AnimationManager() {
        buildPlaceholder();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PUBLIC API — POSES
    // ══════════════════════════════════════════════════════════════════════

    /** Pre-load both poses and all skill animations for one character. Safe to call for unfinished chars. */
    public void loadCharacter(String charName) {
        Gdx.app.log("AnimationManager", "Loading character: " + charName);

        getPose1(charName); // loads pose1 if not cached
        getPose2(charName); // loads pose2 if not cached

        // Load all skill animations
        for (int i = 1; i <= 3; i++) {
            getSkillAnimation(charName, i);
        }

        Gdx.app.log("AnimationManager", "✓ Character loaded: " + charName);
    }

    /**
     * Returns the idle pose (pose1) as a TextureRegion.
     * Never null — falls back to a placeholder.
     * Use this for drawing the character in normal state.
     */
    public TextureRegion getPose1(String charName) {
        String key = charName + "_pose1";
        if (!poses.containsKey(key)) {
            TextureRegion region = safeLoad("characters/" + charName + "/" + charName + "_pose1.png");
            if (region == null) {
                Gdx.app.log("AnimationManager", "⚠ Pose1 missing for '" + charName + "' — using placeholder.");
                region = placeholderRegion;
            }
            poses.put(key, region);
        }
        return poses.get(key);
    }

    /**
     * Returns the skill casting pose (pose2) as a TextureRegion.
     * Never null — falls back to a placeholder.
     * Use this for drawing the character while skill is active (for 0.5s).
     */
    public TextureRegion getPose2(String charName) {
        String key = charName + "_pose2";
        if (!poses.containsKey(key)) {
            TextureRegion region = safeLoad("characters/" + charName + "/" + charName + "_pose2.png");
            if (region == null) {
                Gdx.app.log("AnimationManager", "⚠ Pose2 missing for '" + charName + "' — using placeholder.");
                region = placeholderRegion;
            }
            poses.put(key, region);
        }
        return poses.get(key);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PUBLIC API — SKILL ANIMATIONS
    // ══════════════════════════════════════════════════════════════════════


    public Animation<TextureRegion> getSkillAnimation(String charName, int skillNum) {
        if (skillNum < 1 || skillNum > 3) skillNum = 1;
        String key = charName + "_skill" + skillNum;

        if (!skillAnimations.containsKey(key)) {
            Animation<TextureRegion> anim = loadSkillAnimation(charName, skillNum);
            skillAnimations.put(key, anim);
        }
        return skillAnimations.get(key);
    }

    /** Release every texture this manager owns. Call from screen's dispose(). */
    public void dispose() {
        for (Texture t : ownedTextures) {
            if (t != null) {
                try {
                    t.dispose();
                } catch (Exception e) {
                    Gdx.app.log("AnimationManager", "Error disposing texture: " + e.getMessage());
                }
            }
        }
        ownedTextures.clear();
        poses.clear();
        skillAnimations.clear();
        if (placeholderTex != null) {
            try {
                placeholderTex.dispose();
            } catch (Exception e) {
                Gdx.app.log("AnimationManager", "Error disposing placeholder: " + e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════


    private Animation<TextureRegion> loadSkillAnimation(String charName, int skillNum) {
        TextureRegion[] frames = new TextureRegion[SKILL_FRAMES];
        boolean allLoaded = true;

        for (int i = 1; i <= SKILL_FRAMES; i++) {
            String path = "characters/" + charName + "/" + charName
                    + "_skill" + skillNum + "_frame" + i + ".png";
            TextureRegion region = safeLoad(path);
            if (region == null) {
                Gdx.app.log("AnimationManager", "⚠ Missing frame: " + path);
                allLoaded = false;
                break;
            }
            frames[i - 1] = region;
        }

        if (!allLoaded) {
            Gdx.app.log("AnimationManager",
                    "⚠ Skill" + skillNum + " frames incomplete for '" + charName + "' — using placeholder animation.");


            return placeholderAnimation;
        }

        Animation<TextureRegion> anim = new Animation<>(SKILL_FRAME_DUR, frames);
        anim.setPlayMode(Animation.PlayMode.NORMAL); // play once then hold last frame
        return anim;
    }


    private TextureRegion safeLoad(String internalPath) {
        try {
            if (!Gdx.files.internal(internalPath).exists()) {
                Gdx.app.log("AnimationManager", "File not found: " + internalPath);
                return null;
            }
            Texture tex = new Texture(Gdx.files.internal(internalPath));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            ownedTextures.add(tex);
            return new TextureRegion(tex);
        } catch (Exception e) {
            Gdx.app.error("AnimationManager", "Failed to load: " + internalPath, e);
            return null;
        }
    }


    private void buildPlaceholder() {
        com.badlogic.gdx.graphics.Pixmap pm =
                new com.badlogic.gdx.graphics.Pixmap(1, 1,
                        com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(1f, 0f, 1f, 1f); // magenta = obvious missing asset
        pm.fill();
        placeholderTex    = new Texture(pm);
        pm.dispose();
        placeholderRegion = new TextureRegion(placeholderTex);

        // Create placeholder animation to avoid null returns
        placeholderAnimation = new Animation<>(SKILL_FRAME_DUR, placeholderRegion);
        placeholderAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }
}