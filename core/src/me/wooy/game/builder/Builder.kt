package me.wooy.game.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import me.wooy.game.BaseScreen
import me.wooy.game.Moonshot
import me.wooy.game.element.Background
import me.wooy.game.element.Ground
import me.wooy.game.element.Items
import me.wooy.game.element.Staging
import me.wooy.game.main.Main
import me.wooy.game.misc.Item
import me.wooy.game.misc.Position

class Builder(private val moonshot: Moonshot, itemStaging: MutableMap<Position, Item>) : BaseScreen(moonshot) {
    val stage = Stage()
    private val music = Gdx.audio.newMusic(Gdx.files.internal("music/2.wav"))
    private val items = Items(this)
    private val staging = Staging(this, stage,itemStaging)

    init {
        world.gravity.y = 0f
        addElement(Background(world, moonshot.batch, camera))
        addElement(Ground(world, moonshot.batch, camera))
        addElement(items)
        addElement(staging)
        stage.addListener(this)
        Gdx.input.inputProcessor = stage
        music.isLooping = true
        music.play()
    }

    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()
    }


    override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (button == 0) {
            staging.closeAlert(x,y)
            items.getItem(x, y)
            staging.openCore(x, y)
            staging.openHelp(x,y)
        } else {
            items.select = null
            staging.deleteItem(x, y)
        }
        return true
    }

    override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
        if (button == 0) {
            if (staging.finish(x, y)) {
                moonshot.screen = Main(moonshot, staging.items)
                this.dispose()
            }
            items.select?.let {
                staging.setItem(x, y, it)
            }
        }
    }

    override fun mouseMoved(event: InputEvent, x:Float, y:Float): Boolean {
        items.mouse.x = x
        items.mouse.y = y
        return true
    }

    override fun dispose() {
        super.dispose()
        music.dispose()
    }
}