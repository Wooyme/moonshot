package me.wooy.game.misc

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2

data class Position(val x: Int, val y: Int)
data class Force(val force: Float, val vec: Vector2 = Vector2(0f, 1f), val cost: Float = 0f)
data class Fuel(val total: Float)
data class Program(val startTime:Float,val duration:Float = -1f,val rate:Float=1f,val vec:Vector2 = Vector2(0f,1f))
open class Item(val name: String, val texture: Texture, val description: String = "", val weight: Float = 0f) {
    var force: Force? = null
    var fuel: Fuel? = null
    var program:Program? = null
    val hasFuel get() = fuel != null
    val hasForce get() = force != null
}

class Core(texture: Texture):Item("Core",texture,"Core",0.1f)