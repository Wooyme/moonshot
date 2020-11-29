package me.wooy.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputListener
import me.wooy.game.element.Element
import java.util.*

abstract class BaseScreen(private val moonshot: Moonshot):InputListener(),Screen by ScreenAdapter() {
    val batch get() = moonshot.batch
    private val elements = LinkedList<Element>()
    val camera: OrthographicCamera = OrthographicCamera().apply {
        this.setToOrtho(false, 1280f, 720f)
    }
    val world = World(Vector2(0f,-10f),true)

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f,0f,0f,1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        this.moonshot.batch.projectionMatrix = camera.combined
        this.moonshot.batch.begin()
        elements.forEach(Element::render)
        this.moonshot.batch.end()
        world.step(1/60f,6,2)
    }

    override fun dispose() {
        elements.forEach(Element::dispose)
    }

    protected fun addElement(ele:Element){
        elements.add(ele)
    }
}