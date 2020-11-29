package me.wooy.game.element

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import com.badlogic.gdx.utils.TimeUtils
import me.wooy.game.BaseScreen
import me.wooy.game.misc.Block
import me.wooy.game.misc.Item
import me.wooy.game.misc.Position
import java.util.*
import kotlin.collections.HashMap

class LaunchPad(screen: BaseScreen, items: Map<Position, Item>) : Element(screen.world, screen.batch, screen.camera) {
    private val startButton = TextureRegion(asset, 709, 646, 26, 26)
    private val backButton = TextureRegion(asset, 1091, 710, 26, 26)
    private val startButtonVector2 = Vector2(0f, camera.viewportHeight - 26f)
    private val backButtonVector2 = Vector2(32f, camera.viewportHeight - 26f)
    private val blockList = LinkedList<Block>()
    private lateinit var powerMap: MutableMap<Block, MutableList<Block>>
    private val baseX: Float
    private var starting = false
    private var startTime:Long = 0
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
        println(powerMap)
    }

    override fun render() {
        this.batch.draw(startButton, startButtonVector2.x, startButtonVector2.y)
        this.batch.draw(backButton, backButtonVector2.x, backButtonVector2.y)
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
        }
    }

    override fun dispose() {
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