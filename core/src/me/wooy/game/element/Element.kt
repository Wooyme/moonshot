package me.wooy.game.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.World

abstract class Element(protected val world: World, protected val batch: SpriteBatch, protected val camera: Camera) {
    companion object {
        val asset by lazy { Texture(Gdx.files.internal("scifi_ui_nyknck.png")) }
    }

    abstract fun render()
    abstract fun dispose()
}