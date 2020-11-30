package me.wooy.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.wooy.game.builder.Builder;
import me.wooy.game.main.Main;
import me.wooy.game.main.Startup;
import me.wooy.game.misc.Item;
import me.wooy.game.misc.Position;

import java.util.HashMap;

public class Moonshot extends Game {
	public SpriteBatch batch;
	@Override
	public void create () {
		this.batch = new SpriteBatch();
		this.setScreen(new Startup(this,false));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
