package me.wooy.game.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import me.wooy.game.BaseScreen
import me.wooy.game.misc.*
import java.util.*

class Staging(screen: BaseScreen, private val stage: Stage, val items: MutableMap<Position, Item>) : Element(screen.world, screen.batch, screen.camera) {
    private val buttonFont = BitmapFont()
    private val button = TextureRegion(asset, 32, 204, 64, 20)
    private val finishButtonVector2 = Vector2((camera.viewportWidth) / 2f - button.regionWidth, camera.viewportHeight - button.regionHeight - 10f)
    private val helpButtonVector2 = Vector2((camera.viewportWidth) / 2f - button.regionWidth*2-10f,camera.viewportHeight - button.regionHeight - 10f)
    private val alertTexture = TextureRegion(asset, 700, 1879, 68, 24)
    private val alertVector2 = Vector2(camera.viewportWidth / 3f, camera.viewportHeight / 2f)
    private val skin = Skin(Gdx.files.internal("default/skin/uiskin.json"))
    private val font = BitmapFont()
    private val itemPanel = TextureRegion(asset, 256, 2720, 160, 96)
    private val hLine = TextureRegion(asset, 865, 60, 65, 10)
    private val vLine = TextureRegion(asset, 1000, 30, 10, 60)
    private val stagingVector = Vector2(camera.viewportWidth / 2f, 32f)
    private val height = camera.viewportHeight - 32f
    private val width = camera.viewportWidth / 2f
    private val cols = LinkedList<Float>()
    private val rows = LinkedList<Float>()
    private var showItemPanel = false
    private var panelDrawer: (() -> Any?)? = null
    private var panelCallback: (() -> Any?)? = null
    private var drawAlert: (() -> Any?)? = null

    init {
        (0 until (width / 32).toInt()).forEach {
            cols.add(stagingVector.x + it * 32f)
        }
        (0 until (height / 32).toInt()).forEach {
            rows.add(stagingVector.y + it * 32f)
        }

    }

    override fun render() {
        this.batch.draw(button, finishButtonVector2.x, finishButtonVector2.y)
        buttonFont.draw(this.batch, "Finish!", finishButtonVector2.x + 14, finishButtonVector2.y + button.regionHeight - 4)
        this.batch.draw(button,helpButtonVector2.x,helpButtonVector2.y)
        buttonFont.draw(this.batch, "Help!", helpButtonVector2.x + 14, helpButtonVector2.y + button.regionHeight - 4)
        cols.forEach {
            batch.draw(vLine, it, stagingVector.y, vLine.regionWidth.toFloat(), height)
        }
        rows.forEach {
            batch.draw(hLine, stagingVector.x, it, width, hLine.regionHeight.toFloat())
        }
        items.forEach { (position, item) ->
            this.batch.draw(item.texture, cols[position.x] + 6, rows[position.y] + 6)
        }
        panelDrawer?.invoke()
        drawAlert?.invoke()
    }

    override fun dispose() {
        font.dispose()
        skin.dispose()
        buttonFont.dispose()
    }

    fun openHelp(x:Float,y:Float){
        if (helpButtonVector2.x <= x
                && helpButtonVector2.x + button.regionWidth >= x
                && helpButtonVector2.y <= y
                && helpButtonVector2.y + button.regionHeight >= y
        ){
            drawAlert = {
                batch.draw(alertTexture, alertVector2.x, alertVector2.y, camera.viewportWidth / 3f, 200f)
                font.draw(batch, "Build a rocket,\n and let it land on the moon", alertVector2.x + 40f, alertVector2.y + 160f)
            }
        }
    }
    
    fun closeAlert(x: Float, y: Float) {
        if (x < alertVector2.x || x > alertVector2.x + camera.viewportWidth / 3f || y < alertVector2.y || y > alertVector2.y + 200f) drawAlert = null
    }

    fun setItem(x: Float, y: Float, item: Item) {
        val position = findItem(x, y)
        position?.let {
            items[it] = item.copy()
        }
    }

    fun deleteItem(x: Float, y: Float) {
        val position = findItem(x, y)
        position?.let {
            items.remove(it)
        }
    }

    fun openCore(x: Float, y: Float) {
        if (showItemPanel) {
            if (x < camera.viewportWidth / 3f || x > camera.viewportWidth / 3f * 2f || y < 100f || y > camera.viewportHeight - 100f) {
                showItemPanel = false
                panelDrawer = null
                panelCallback?.invoke()
            }
        } else {
            val position = findItem(x, y)
            position?.let {
                items[it]?.let {
                    showItemPanel = true
                    when {
                        it is Core -> {
                            showCorePanel()
                        }
                        it.hasForce -> {
                            showThrusterPanel(it)
                        }
                        it.hasJoint -> {
                            showJointPanel(it)
                        }
                    }
                }
            }
        }
    }

    private fun findItem(x: Float, y: Float): Position? {
        var col = -1
        var row = -1
        cols.forEachIndexed { index, fl ->
            if (x > fl && x <= fl + 32) {
                col = index
                return@forEachIndexed
            }
        }
        rows.forEachIndexed { index, fl ->
            if (y > fl && y <= fl + 32f) {
                row = index
                return@forEachIndexed
            }
        }
        return if (col >= 0 && row >= 0) Position(col, row) else null
    }

    private fun showCorePanel() {
        val eleVector2 = Vector2(camera.viewportWidth / 3f + 10f, camera.viewportHeight - 140f)
        panelDrawer = {
            val totalWeight = items.map { it.value.weight }.reduce { acc, item -> acc + item }
            this.batch.draw(itemPanel, camera.viewportWidth / 3f, 100f, camera.viewportWidth / 3f, camera.viewportHeight - 200f)
            font.draw(batch, "Total Weight:${totalWeight}", eleVector2.x, eleVector2.y)
        }
    }

    private fun showThrusterPanel(item: Item) {
        val eleVector2 = Vector2(camera.viewportWidth / 3f + 10f, camera.viewportHeight - 140f)
        val programVector2 = Vector2(eleVector2.x, eleVector2.y - font.lineHeight - 80f)
        panelDrawer = {
            this.batch.draw(itemPanel, camera.viewportWidth / 3f, 100f, camera.viewportWidth / 3f, camera.viewportHeight - 200f)
            font.draw(batch, item.name, eleVector2.x, eleVector2.y)
            this.batch.draw(item.texture, eleVector2.x, eleVector2.y - font.lineHeight - 42f)

            font.draw(batch, "Program:", programVector2.x, programVector2.y)
            font.draw(batch, "Start Time:", programVector2.x, programVector2.y - font.lineHeight)
            font.draw(batch, "Duration:", programVector2.x, programVector2.y - font.lineHeight * 2)
            font.draw(batch, "Power Rate:", programVector2.x, programVector2.y - font.lineHeight * 3)
            font.draw(batch, "X:", programVector2.x, programVector2.y - font.lineHeight * 4)
            font.draw(batch, "Y:", programVector2.x, programVector2.y - font.lineHeight * 5)
        }
        val startTimeField = TextField(item.powerProgram?.startTime?.toString()?:"0", skin)
        startTimeField.setPosition(programVector2.x + 100f, programVector2.y - font.lineHeight * 2)
        startTimeField.setSize(100f, font.lineHeight)
        val durationField = TextField(item.powerProgram?.duration?.let { if(it<0) "" else it.toString() }?:"", skin)
        durationField.messageText = "Optional"
        durationField.setPosition(programVector2.x + 100f, programVector2.y - font.lineHeight * 3)
        durationField.setSize(camera.viewportWidth / 3f - 150f, font.lineHeight)
        val powerRateField = TextField(item.powerProgram?.rate?.toString()?:"1.0", skin)
        powerRateField.setPosition(programVector2.x + 100f, programVector2.y - font.lineHeight * 4)
        powerRateField.setSize(camera.viewportWidth / 3f - 150f, font.lineHeight)
        val xField = TextField(item.powerProgram?.vec?.x?.toString()?:"0.0", skin)
        xField.setPosition(programVector2.x + 100f, programVector2.y - font.lineHeight * 5)
        xField.setSize(camera.viewportWidth / 3f - 150f, font.lineHeight)
        val yField = TextField(item.powerProgram?.vec?.y?.toString()?:"1.0", skin)
        yField.setPosition(programVector2.x + 100f, programVector2.y - font.lineHeight * 6)
        yField.setSize(camera.viewportWidth / 3f - 150f, font.lineHeight)
        stage.addActor(startTimeField)
        stage.addActor(durationField)
        stage.addActor(powerRateField)
        stage.addActor(xField)
        stage.addActor(yField)
        panelCallback = {
            item.powerProgram = PowerProgram(startTimeField.text.toFloatOrNull()?:0f,
                    if (durationField.text.isNotEmpty()) durationField.text.toFloatOrNull()?:-1f else -1f,
                    powerRateField.text.toFloatOrNull()?:0f,
                    Vector2(xField.text.toFloatOrNull()?:0f, yField.text.toFloatOrNull()?:0f))
            startTimeField.remove()
            durationField.remove()
            powerRateField.remove()
            xField.remove()
            yField.remove()
        }
    }

    private fun showJointPanel(item: Item) {
        val eleVector2 = Vector2(camera.viewportWidth / 3f + 10f, camera.viewportHeight - 140f)
        val programVector2 = Vector2(eleVector2.x, eleVector2.y - font.lineHeight - 80f)
        panelDrawer = {
            this.batch.draw(itemPanel, camera.viewportWidth / 3f, 100f, camera.viewportWidth / 3f, camera.viewportHeight - 200f)
            font.draw(batch, item.name, eleVector2.x, eleVector2.y)
            this.batch.draw(item.texture, eleVector2.x, eleVector2.y - font.lineHeight - 42f)
            font.draw(batch, "Program:", programVector2.x, programVector2.y)
            font.draw(batch, "Start Time:", programVector2.x, programVector2.y - font.lineHeight)
        }
        val startTimeField = TextField(item.jointProgram?.startTime?.toString()?:"0", skin)
        startTimeField.setPosition(programVector2.x + 100f, programVector2.y - font.lineHeight * 2)
        startTimeField.setSize(100f, font.lineHeight)
        stage.addActor(startTimeField)
        panelCallback = {
            item.jointProgram = JointProgram(startTimeField.text.toFloatOrNull()?:0f)
            startTimeField.remove()
        }
    }

    fun finish(x: Float, y: Float): Boolean {
        if (finishButtonVector2.x <= x
                && finishButtonVector2.x + button.regionWidth >= x
                && finishButtonVector2.y <= y
                && finishButtonVector2.y + button.regionHeight >= y
        ) {
            if (items.any { it.value is Core })
                return true
            else {
                drawAlert = {
                    batch.draw(alertTexture, alertVector2.x, alertVector2.y, camera.viewportWidth / 3f, 200f)
                    font.draw(batch, "Core Missing!", alertVector2.x + 40f, alertVector2.y + 160f)
                }
            }
        }
        return false
    }
}