package io.github.PASAN;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game
{
    public static Music bgm;
    public static Sound clickSound;

    @Override
    public void create() {
        bgm = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm.wav"));
        bgm.setLooping(true);
        bgm.play();

        clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/button_click.wav"));
        setScreen(new MainMenu(this));
    }

    @Override public void dispose()
    {
        if(bgm != null) bgm.dispose();
        if(clickSound != null) clickSound.dispose();
    }
}