package io.github.PASAN;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * SkillEffect — Manages a single skill projectile animation.
 *
 * Handles:
 * - Animation state tracking
 * - Position interpolation from start to target
 * - Frame selection and flipping
 * - Active state management
 */
public class SkillEffect {

    public Animation<TextureRegion> animation;
    public float stateTime;
    public Vector2 position;

    private float startX, startY;
    private float targetX, targetY;

    public boolean active = true;
    public boolean hasHit = false;
    private boolean flipX;

    public SkillEffect(Animation<TextureRegion> animation,
                       float startX, float startY,
                       float targetX, float targetY,
                       boolean flipX) {

        this.animation = animation;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.flipX = flipX;

        this.position = new Vector2(startX, startY);
        this.stateTime = 0f;
    }

    public void update(float delta) {
        if (!active) return;

        stateTime += delta;

        float duration = animation.getAnimationDuration();
        float progress = stateTime / duration;

        if (progress >= 1f) {
            progress = 1f;
            active = false;

            // SNAP EXACTLY TO TARGET ON LAST FRAME
            position.set(targetX, targetY);
        } else {
            // Smooth interpolation
            position.x = startX + (targetX - startX) * progress;
            position.y = startY + (targetY - startY) * progress;
        }
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animation.getKeyFrame(stateTime, false);

        if (frame != null && frame.isFlipX() != flipX) {
            frame.flip(true, false);
        }

        return frame;
    }

    public boolean isLastFrame() {
        return animation.isAnimationFinished(stateTime);
    }

    public boolean isFlipX() {
        return flipX;
    }
}