package io.github.PASAN;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game
{
    private Music bgm;

    @Override
    public void create() {
        bgm = Gdx.audio.newMusic(Gdx.files.internal("musics/bgm.wav"));
        bgm.setLooping(true);
        bgm.play();
        setScreen(new MainMenu());
    }

    @Override public void dispose()
    {
        if(bgm != null) bgm.dispose();
    }
}