import hype.H
import hype.HDrawable3D
import hype.HDrawablePool
import hype.HIcosahedron
import hype.extended.colorist.HPixelColorist
import hype.extended.layout.HGridLayout
import processing.core.PApplet
import java.awt.Color


class Facelizer : PApplet() {
    val stageW = 892 // should match resolution of image and mask files
    val stageH = 892
    val bgColor = Color(0x000000).rgb

    val imgPrefix: String = "el"

    val PATH_DATA = "data/"
    val PATH_RENDER = "render/"

    // PRIMARY SETTINGS
    val fullscreen = false
    val recordOutput = false
    val streamAudio = true
    val showVisualizer = false

    // AUDIO DATA
    val audioFile = "shelera.aiff"
    val audioPath = PATH_DATA + audioFile

    // AUDIO SETTINGS
    val transformSettings = TransformSettings(
        100, 25f, 0.10f, 0.05f
    )

    lateinit var audio: AudioProcessor

    // RENDERING
    val renderFileName = "el-01/el-"
    val outputLength = 60 // seconds

    val recordTimeDelay = 100
    val renderFrameRate = 30
    val cellSize = 10
    val sphereDetail = 20
    var renderFrameCount = 0

    lateinit var pool: HDrawablePool

    override fun settings() {
        size(stageW, stageH, P3D)
        if (fullscreen) fullScreen()
    }

    override fun setup() {
        frameRate(renderFrameRate.toFloat())
        audio = AudioProcessor(
            this,
            streamAudio,
            recordOutput,
            audioPath,
            transformSettings = transformSettings
        )
        H.init(this)
        H.background(bgColor)
        H.autoClears(true)
        H.use3D(true)

        val img1 = loadImage("$PATH_DATA$imgPrefix.png");
        val img2 = loadImage("$PATH_DATA$imgPrefix-mask.png");
        val colors = HPixelColorist(img1)
        val mask = HPixelColorist(img2)
        pool = HDrawablePool((width / cellSize) * (width / cellSize))
            .add(HIcosahedron())
            .autoAddToStage()
            .layout(
                HGridLayout()
                    .startX(cellSize / 2f)
                    .startY(cellSize / 2f)
                    .spacing(cellSize + 2f, cellSize + 2f)
                    .cols(height / cellSize)
            )
            .onCreate { obj ->

                val d = obj as HDrawable3D
                d
                    .depth(-100f)
                    .anchorAt(H.CENTER)
                    .size(cellSize - 4f)
                    .noStroke()
                mask.applyColor(d)
            }
            .requestAll()
        for (d in pool) {
            if (d.fill() <= color(1)) {
                colors.applyColor(d)
                d.bool("active", true)
            } else {
                d.bool("active", false)
                d.hide()
            }
        }
    }

    override fun draw() {
        if (recordOutput) {
            delay(recordTimeDelay)
        }  else {
            surface.setTitle( "FPS : " + frameRate.toInt() )
        }
        lights()
        sphereDetail(sphereDetail)
        audio.process()
        var count = 0
        for (d in pool) {
            if (d.bool("active")) {
                val s = d as HDrawable3D
                val fftData: Float = audio.fftData[count]
                val depth = map(fftData, 0f, 100f, 8f, 30f)
                val z = map(fftData, 0f, 100f, -100f, 100f)
                s.z(z)
                s.depth(depth.toInt().toFloat())
//                if (count == 10) {
//                    println(depth)
//                }
                ++count
                if (count == audio.fftData.size) count = 0
            }
        }

        H.drawStage()
        pointLight(255f, 255f, 255f, width/2f, height/3f, 50f);
        if (showVisualizer) audio.drawWidget()
        if (recordOutput) renderOutput()
    }

    fun renderOutput() {
        ++renderFrameCount
        saveFrame("$PATH_RENDER$renderFileName#########.tif")
        if (renderFrameCount == outputLength * renderFrameRate) exit()
        println("Remaining frames: " + (outputLength * renderFrameRate - renderFrameCount))
    }

    override fun stop() {
        audio.stop()
        super.stop()
    }
}