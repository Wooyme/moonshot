package me.wooy.game.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import com.badlogic.gdx.utils.TimeUtils
import me.wooy.game.main.Main
import me.wooy.game.misc.Block
import me.wooy.game.misc.Core
import me.wooy.game.misc.Item
import me.wooy.game.misc.Position
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class LaunchPad(private val screen: Main, items: Map<Position, Item>) : Element(screen.world, screen.batch, screen.camera) {
    private val moonTexture = Texture(Gdx.files.internal("0.png"))
    private val moon = TextureRegion(moonTexture,201,201,200,200)
    private lateinit var moonBody:Body
    private val font = BitmapFont()
    private val infoPanelTexture = TextureRegion(asset,32,2720,161,96)
    private val startButton = TextureRegion(asset, 709, 646, 26, 26)
    private val backButton = TextureRegion(asset, 1091, 710, 26, 26)
    private val startButtonVector2 = Vector2(0f, camera.viewportHeight - 26f)
    private val backButtonVector2 = Vector2(32f, camera.viewportHeight - 26f)
    private val blockList = LinkedList<Block>()
    private lateinit var powerMap: MutableMap<Block, MutableList<Block>>
    private val baseX: Float
    private var starting = false
    private var startTime:Long = 0
    private lateinit var core: Block
    private val lastCoreVector2 = Vector2(0f,0f)
    private var lastCheckTime = 0L
    private var win = false
    init {
        val minX = items.minBy { it.key.x }?.key?.x
        val maxX = items.maxBy { it.key.x }?.key?.x
        baseX = if (minX == null || maxX == null) {
            camera.viewportWidth / 2f
        } else {
            (camera.viewportWidth) / 2f - (maxX - minX) * 32f
        }
        items.forEach { (position, item) ->
            blockList.add(createBlock(position, item))
        }
        joinBlocks()
        initPower()
        theMoon()
    }

    fun theMoon(){
        val bodyDef = BodyDef()
        bodyDef.position.set(0f,camera.viewportHeight*3f)
        val moon = world.createBody(bodyDef)
        val circle = CircleShape()
        circle.radius = camera.viewportWidth/2f
        moon.createFixture(circle,1000f)
        moonBody = moon
    }

    fun checkWin(){
        if(TimeUtils.millis()-lastCheckTime>3000 && !win) {
            if(lastCoreVector2.x == core.fixture.body.worldCenter.x && lastCoreVector2.y == core.fixture.body.worldCenter.y
                    && lastCoreVector2.x>moonBody.worldCenter.x-camera.viewportWidth/2f && lastCoreVector2.x<moonBody.worldCenter.x +camera.viewportWidth/2f
                    && lastCoreVector2.y>moonBody.worldCenter.y){
                screen.win()
            }
            lastCheckTime = TimeUtils.millis()
            lastCoreVector2.set(core.fixture.body.worldCenter.x,core.fixture.body.worldCenter.y)
        }
    }

    override fun uiRender() {
        this.batch.draw(startButton, startButtonVector2.x, startButtonVector2.y)
        this.batch.draw(backButton, backButtonVector2.x, backButtonVector2.y)
        this.batch.draw(infoPanelTexture,0f,32f)
        if(this::core.isInitialized){
            font.draw(batch,"Height:${(core.fixture.body.position.y*100).roundToInt()/100f}",10f,110f)
            font.draw(batch,"Gravity:${(world.gravity.y * 100).roundToInt() /100f}",10f,110f-font.lineHeight)
            font.draw(batch,"SpeedX:${(core.fixture.body.linearVelocity.x*100).roundToInt()/100f}",10f,110f-font.lineHeight*2)
            font.draw(batch,"SpeedY:${(core.fixture.body.linearVelocity.y*100).roundToInt()/100f}",10f,110f-font.lineHeight*3)
        }
    }
    override fun render() {
        checkWin()
        batch.draw(moon,moonBody.worldCenter.x-camera.viewportWidth/2f,moonBody.worldCenter.y - camera.viewportWidth/2f,camera.viewportWidth,camera.viewportWidth)
        blockList.forEach {
            it.sprite.rotation = it.fixture.body.angle * MathUtils.radiansToDegrees
            it.sprite.setPosition(it.fixture.body.position.x - 16, it.fixture.body.position.y - 16)
            it.sprite.draw(batch)
        }
        if (starting) {
            blockList.filter { it.item.hasForce }.forEach {
                val canStart=it.item.powerProgram?.let {
                    TimeUtils.millis() - startTime>it.startTime*1000 && if(it.duration<0) true else TimeUtils.millis()-(startTime+it.startTime)<it.duration*1000
                }?:true
                val force = it.item.force!!
                val fuelList = powerMap[it] ?: emptyList<Block>()
                val sum = fuelList.filter { it.fuel > 0 }.sumByDouble { it.fuel.toDouble() }.toFloat()

                if (sum > force.cost && canStart) {
                    val programX = it.item.powerProgram?.vec?.let { it.x/Math.sqrt(Math.pow(it.x.toDouble(),2.toDouble())+Math.pow(it.y.toDouble(),2.toDouble())) }?.toFloat()?:1f
                    val programY = it.item.powerProgram?.vec?.let { it.y/Math.sqrt(Math.pow(it.x.toDouble(),2.toDouble())+Math.pow(it.y.toDouble(),2.toDouble())) }?.toFloat()?:1f
                    it.fixture.body.applyForceToCenter(force.force * force.vec.x * (it.item.powerProgram?.rate?:1f)*programX, force.force * force.vec.y * (it.item.powerProgram?.rate?:1f)*programY, true)
                    val notEmptyFuel = fuelList.filter { it.fuel > 0 }
                    val ava = force.cost / notEmptyFuel.size *  (it.item.powerProgram?.rate?:1f)
                    notEmptyFuel.forEach {
                        it.fuel -= ava
                        if (it.fuel < 0) it.fuel = 0f
                        it.fixture.density = it.item.weight+(it.fuel/it.item.fuel!!.total)*it.item.fuel!!.weight
                        it.fixture.body.resetMassData()
                    }
                }
            }
            blockList.filter { it.item.hasJoint && it.fixture.body.jointList.notEmpty() }.forEach {
                val canStart=it.item.jointProgram?.let {
                    TimeUtils.millis() - startTime>it.startTime*1000
                }?:true
                if(canStart){
                    it.jointList.forEach {
                        world.destroyJoint(it)
                    }
                }
            }
            camera.position.y = core.fixture.body.position.y
            if(core.fixture.body.position.y>camera.viewportHeight/2f && world.gravity.y<0){
                if(world.gravity.y+0.01f<0)
                    world.gravity = Vector2(0f,world.gravity.y+0.01f)
                else
                    world.gravity.y = 0f
            }
        }
    }

    override fun dispose() {
        moonTexture.dispose()
    }

    private fun createBlock(position: Position, item: Item): Block {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(baseX + position.x * 32f, Ground.GROUND_HEIGHT + 16f + position.y * 32f)
        val body = world.createBody(bodyDef)
        val shape = PolygonShape()
        shape.setAsBox(16f, 16f)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = shape
        fixtureDef.density = item.weight+(item.fuel?.weight?:0f)
        fixtureDef.friction = 0f
        fixtureDef.restitution = 0f
        val fixture = body.createFixture(fixtureDef)
        val sprite = Sprite(item.texture)
        shape.dispose()
        return Block(fixture, sprite, item, position).apply {
            item.fuel?.let {
                this.fuel = it.total
            }
            if(item is Core) core = this
        }
    }

    private fun joinBlocks() {
        val map = HashMap<Position, Block>()
        blockList.forEach {
            map[it.position] = it
        }
        blockList.forEach {
            listOfNotNull(map[Position(it.position.x - 1, it.position.y)],
                    map[Position(it.position.x + 1, it.position.y)],
                    map[Position(it.position.x, it.position.y - 1)],
                    map[Position(it.position.x, it.position.y + 1)]).forEach { n ->
                val jointDef = WeldJointDef()
                jointDef.initialize(it.fixture.body, n.fixture.body, Vector2((it.fixture.body.worldCenter.x + n.fixture.body.worldCenter.x) / 2
                        , (it.fixture.body.worldCenter.y + n.fixture.body.worldCenter.y) / 2))
                val joint =  world.createJoint(jointDef)
                it.jointList.add(joint)
                n.jointList.add(joint)
            }
            map.remove(it.position)
        }
    }

    private fun initPower() {
        val map = HashMap<Position, Block>()
        blockList.forEach {
            map[it.position] = it
        }
        fun findPower(block: Block, fuelSet: MutableSet<Block>) {
            listOfNotNull(map[Position(block.position.x - 1, block.position.y)],
                    map[Position(block.position.x + 1, block.position.y)],
                    map[Position(block.position.x, block.position.y - 1)],
                    map[Position(block.position.x, block.position.y + 1)]).forEach { n ->
                if (n.item.hasFuel && !fuelSet.contains(n)) {
                    fuelSet.add(n)
                    findPower(n, fuelSet)
                }
            }
        }
        this.powerMap = mutableMapOf(*blockList.filter { it.item.hasForce }.map {
            val set = mutableSetOf<Block>()
            findPower(it, set)
            it to set.toMutableList().apply {
                if(it.item.hasFuel){
                    this.add(0,it)
                }
            }
        }.toTypedArray())
    }


    fun start(x: Float, y: Float) {
        if (x >= startButtonVector2.x && x <= startButtonVector2.x + startButton.regionWidth && y >= startButtonVector2.y && y <= startButtonVector2.y + startButton.regionHeight) {
            starting = true
            startTime = TimeUtils.millis()
        }
    }

    fun back(x: Float, y: Float): Boolean {
        if (x >= backButtonVector2.x && x <= backButtonVector2.x + backButton.regionWidth && y >= backButtonVector2.y && y <= backButtonVector2.y + backButton.regionHeight) {
            return true
        }
        return false
    }

}