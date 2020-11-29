package me.wooy.game.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.World

class Background(world: World,batch: SpriteBatch,camera: Camera):Element(world, batch, camera) {
    private val background = Texture(Gdx.files.internal("0.png"))

    override fun render() {
        batch.draw(background,(this.camera.viewportWidth-background.width)/2,0f,background.width.toFloat(),background.height.toFloat())
    }

    override fun dispose() {
        background.dispose()
    }
}