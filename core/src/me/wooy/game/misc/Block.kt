package me.wooy.game.misc

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.Fixture

class Block(val fixture: Fixture,val sprite: Sprite,val item:Item,val position: Position){
    var fuel:Float = 0f
    var startTime:Int = 0
}