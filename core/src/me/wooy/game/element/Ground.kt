package me.wooy.game.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World

class Ground(world: World, batch: SpriteBatch, camera: Camera) : Element(world, batch, camera) {
    private val debuggerRender = Box2DDebugRenderer()
    private val asset = Texture(Gdx.files.internal("ground.png")).apply {
        this.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    }
    private val ground: TextureRegion = TextureRegion(asset, 0, 0, camera.viewportWidth.toInt(), asset.height)
    private val groundBody = world.createBody(BodyDef().apply {
        this.position.set(0f, 0f)
    }).apply {
        val shape = PolygonShape()
        shape.setAsBox(ground.regionWidth.toFloat(), ground.regionHeight.toFloat())
        this.createFixture(shape, 0f)
        shape.dispose()
    }
    init {
        println(asset.height)
    }
    override fun render() {
        batch.draw(ground, groundBody.position.x, groundBody.position.y)
        //debuggerRender.render(world,camera.combined)
    }

    override fun dispose() {
        asset.dispose()
    }

    companion object{
        const val GROUND_HEIGHT = 36f
    }
}