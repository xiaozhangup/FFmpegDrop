package me.xiaozhangup.ffmpegdrop

import java.awt.Point
import java.awt.Shape
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import java.awt.dnd.DnDConstants
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import java.io.File
import javax.swing.JLabel

const val width = 200
const val height = 100

fun main() {
    val frame = JFrame("Drag and Drop File for FFmpeg Processing")
    var initialClick: Point? = null
    var scale = 1.0

    frame.isUndecorated = true
    frame.isAlwaysOnTop = true
    frame.shape = makeShape(width, height)
    frame.setSize(400, 200)

    val panel = JLabel("Drop file to there").apply {
        dropTarget = object : DropTarget() {
            override fun drop(evt: DropTargetDropEvent) {
                var result = 0
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY)
                    val droppedFiles = evt.transferable.getTransferData(evt.currentDataFlavors[0]) as List<*>
                    droppedFiles.forEach {
                        if (it is File) {
                            println("File dropped: ${it.absolutePath}")
                            if (!processFileWithFFmpeg(it)) {
                                result++
                            }
                        }
                    }
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(null, "处理失败，请查看命令行输出获取详情。")
                    e.printStackTrace()
                }
                if (result > 0) {
                    JOptionPane.showMessageDialog(null, "$result 个文件处理失败，请查看命令行输出获取详情。")
                } else {
                    JOptionPane.showMessageDialog(null, "已成功处理所有文件。")
                }
            }
        }
    }

    frame.addKeyListener(object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_ESCAPE) {
                frame.dispose()
            }
        }
    })

    frame.addMouseWheelListener { e ->
        val scrollAmount = e.unitsToScroll

        if (scrollAmount < 0) {
            scale *= 1.1
        } else {
            scale *= 0.9
        }

        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        frame.setSize(scaledWidth, scaledHeight)
        frame.shape = makeShape(scaledWidth, scaledHeight)
    }

    frame.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            initialClick = e.point
        }
    })
    frame.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            val currentLocation = e.locationOnScreen

            val offsetX = currentLocation.x - initialClick!!.x
            val offsetY = currentLocation.y - initialClick!!.y
            frame.setLocation(offsetX, offsetY)
        }
    })

    frame.add(panel)
    frame.isVisible = true
}

fun processFileWithFFmpeg(file: File): Boolean {
    try {
        val inputFile = file.absolutePath
        val outputFile = file.parent + File.separator + "processed_" + file.name

        val command = "ffmpeg -i \"$inputFile\" \"$outputFile\""
        val process = Runtime.getRuntime().exec(command)
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            println("FFmpeg processing ${file.nameWithoutExtension} finished successfully.")
        } else {
            println("FFmpeg processing ${file.nameWithoutExtension} failed.")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return true
}

fun makeShape(width: Int, height: Int): Shape {
    return RoundRectangle2D.Double(
        0.0,
        0.0,
        width.toDouble(),
        height.toDouble(),
        20.0,
        20.0
    )
}
