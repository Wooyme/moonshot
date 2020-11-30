package me.wooy.game.main

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import me.wooy.game.BaseScreen
import me.wooy.game.Moonshot
import me.wooy.game.builder.Builder
import me.wooy.game.element.Element
import kotlin.system.exitProcess

class Startup(private val moonshot: Moonshot,private val isWin:Boolean = false):BaseScreen(moonshot) {
    val stage = Stage()
    private val music = Gdx.audio.newMusic(Gdx.files.internal("music/1.mp3"))
    private val font = BitmapFont()
    private val logo = Texture(Gdx.files.internal("game-off.png"))
    private val moonTexture = Texture(Gdx.files.internal("0.png"))
    private val marsTexture = Texture(Gdx.files.internal("1.png"))
    private val moon = TextureRegion(moonTexture,201,201,200,200)
    private val mars =  TextureRegion(marsTexture,201,201,200,200)
    private val button = TextureRegion(Element.asset,32,462,68,18)
    private val startButtonVec = Vector2(30f,100f)
    private val quitButtonVec = Vector2(30f,60f)
    init {
        stage.addListener(this)
        Gdx.input.inputProcessor = stage
        music.isLooping = true
        music.play()
    }
    override fun render(delta: Float) {
        super.render(delta)
        batch.begin()
        font.data.setScale(1f,1f)
        if(!isWin) {
            batch.draw(logo, (camera.viewportWidth - logo.width) / 2f, (camera.viewportHeight - logo.height) / 2f)
        }
        batch.draw(moon,camera.viewportWidth/3f-100f,camera.viewportHeight/3f*2-60f)
        batch.draw(mars,camera.viewportWidth/3f*2-200f,camera.viewportHeight/3f-140f,400f,400f)
        batch.draw(button,startButtonVec.x,startButtonVec.y,136f,36f)
        font.draw(batch,"START",startButtonVec.x+10f,startButtonVec.y+24f)
        batch.draw(button,quitButtonVec.x,quitButtonVec.y,136f,36f)
        font.draw(batch,"QUIT",quitButtonVec.x+10f,quitButtonVec.y+24f)
        if(isWin){
            font.data.setScale(5f,5f)
            font.draw(batch,"YOU MADE IT!",camera.viewportWidth/2f-10f,camera.viewportHeight/2f-10f)
        }
        batch.end()
    }

    override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if(x>=startButtonVec.x && x<=startButtonVec.x+136f && y>=startButtonVec.y && y<=startButtonVec.y+36f){
            start()
            return true
        }
        if(x>=quitButtonVec.x && x<=quitButtonVec.x+136f && y>=quitButtonVec.y && y<=quitButtonVec.y+36f){
            exitProcess(0)
        }
        return true
    }

    override fun dispose() {
        super.dispose()
        moonTexture.dispose()
        marsTexture.dispose()
        font.dispose()
        stage.dispose()
        music.dispose()
    }
    fun start(){
        moonshot.screen = Builder(moonshot, HashMap())
        dispose()
    }
}