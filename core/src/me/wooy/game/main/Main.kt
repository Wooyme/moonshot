package me.wooy.game.main

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import me.wooy.game.BaseScreen
import me.wooy.game.Moonshot
import me.wooy.game.builder.Builder
import me.wooy.game.element.Background
import me.wooy.game.element.Ground
import me.wooy.game.element.LaunchPad
import me.wooy.game.misc.Item
import me.wooy.game.misc.Position


class Main(private val moonshot: Moonshot, private val items:MutableMap<Position, Item>):BaseScreen(moonshot), InputProcessor by InputAdapter() {
    private val launchPad = LaunchPad(this,items)
    init {
        world.gravity.y = -10f
        addElement(Background(world,moonshot.batch,camera))
        addElement(Ground(world,this.moonshot.batch,camera))
        addElement(launchPad)
        Gdx.input.inputProcessor = this
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val x = screenX.toFloat()
        val y = camera.viewportHeight - screenY
        if(button==0){
            launchPad.start(x,y)
            if(launchPad.back(x,y)){
                moonshot.screen = Builder(moonshot,items)
                this.dispose()
            }
        }
        return true
    }
}