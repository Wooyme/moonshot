package me.wooy.game.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import me.wooy.game.BaseScreen
import me.wooy.game.misc.*
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.HashMap

class Items(screen: BaseScreen) : Element(screen.world, screen.batch, screen.camera) {
    private val font = BitmapFont()
    private val buttonFont = BitmapFont()
    private val hLine = TextureRegion(asset, 865, 60, 65, 10)
    private val vLine = TextureRegion(asset, 1000, 30, 10, 60)
    private val uiTop = TextureRegion(asset, 830, 1117, 200, 35)
    private val uiBottom = TextureRegion(asset, 830, 1185, 200, 35)
    private val information = TextureRegion(asset,32,2720,161,96)

    private val topVector2 = Vector2(0f,camera.viewportHeight-uiTop.regionHeight-10)
    private val bottomVector2 = Vector2(0f,200f)
    private val informationVector2 = Vector2(0f,32f)
    private val cols = LinkedList<Float>()
    private val rows = LinkedList<Float>()
    private val items = HashMap<Position,Item>()
    var select:Item? = null
    val mouse = Vector2(0f,0f)
    init {
        (0 until (camera.viewportWidth / (3 * 32)).toInt()).forEach {
            cols.add(bottomVector2.x+it * 32f+12)
        }
        (1 .. ((topVector2.y-bottomVector2.y)/32).toInt()).forEach {
            rows.add(it*32f+bottomVector2.y+6)
        }
        items[Position(0,0)] = Item("Thruster",Texture(Gdx.files.internal("items/rocket_fins.png")),"Generate a strong vertical downward thrust",2f).apply {
            this.force = Force(500000f,Vector2(0f,1f),0.1f)
        }
        items[Position(1,0)] = Item("Fuel Tank", Texture(Gdx.files.internal("items/fuel_tank.png")),"Tank full of fuel",1f).apply {
            this.fuel = Fuel(30f,15f)
        }
        items[Position(2,0)] = Item("Structure", Texture(Gdx.files.internal("items/simple.png")),"Connect each part",1f)
        items[Position(3,0)] = Item("Spinner", Texture(Gdx.files.internal("items/spinner.png")),"Generate small thrust in any direction",0.5f).apply {
            this.force = Force(200000f, Vector2(1f,1f),0.1f)
            this.fuel = Fuel(1f,0.5f)
        }
        items[Position(4,0)] = Item("Joint",Texture(Gdx.files.internal("items/joint.png")),"It can disconnect from the surroundings at a certain time",0.1f).apply {
            this.jointProgram = JointProgram(1f)
        }
        items[Position(0,1)] = Core(Texture(Gdx.files.internal("items/core.png")))
    }
    override fun render() {

        this.batch.draw(information,informationVector2.x,informationVector2.y,camera.viewportWidth/3f,information.regionHeight.toFloat())
        this.batch.draw(uiBottom, bottomVector2.x, bottomVector2.y, camera.viewportWidth / 3f, uiBottom.regionHeight.toFloat())
        val vSize = (camera.viewportWidth / (3 * 32)).toInt()*32
        items.forEach { (position, item) ->
            this.batch.draw(item.texture,cols[position.x]+6,rows[position.y]+6)
        }
        cols.forEach {
            this.batch.draw(vLine,it,bottomVector2.y+6,vLine.regionWidth.toFloat(), topVector2.y-bottomVector2.y+uiTop.regionHeight)
        }
        rows.forEach {
            this.batch.draw(hLine,bottomVector2.x+10f,it,vSize.toFloat(),hLine.regionHeight.toFloat())
        }
        this.batch.draw(uiTop, topVector2.x, topVector2.y, camera.viewportWidth / 3f, uiTop.regionHeight.toFloat())
        select?.let {
            this.batch.draw(it.texture,mouse.x-16,mouse.y-16)
            drawInformation(it)
        }
    }

    override fun dispose() {
        font.dispose()
        buttonFont.dispose()
    }

    fun getItem(x:Float,y:Float){
        var col = -1
        var row = -1
        cols.forEachIndexed { index, fl ->
            if(x>fl && x<=fl+32) {
                col = index
                return@forEachIndexed
            }
        }
        rows.forEachIndexed{ index,fl->
            if(y>fl && y<=fl+32f){
                row = index
                return@forEachIndexed
            }
        }
        println("Col:$col,Row:$row")
        if(col>=0 && row>=0){
            this.select = items[Position(col,row)]
        }
    }

    private fun drawInformation(item:Item){
        val x = informationVector2.x+10f
        val y =informationVector2.y+information.regionHeight.toFloat()-10f
        font.setColor(1f,1f,1f,1f)
        font.draw(batch,"Name:${item.name}",x,y)
        font.setColor(0.8f,0.8f,0.8f,1f)
        font.draw(batch,"Description:${item.description}",x,y-font.lineHeight)
        font.draw(batch,"Weight:${item.weight}",x,y-font.lineHeight*2)
    }


}