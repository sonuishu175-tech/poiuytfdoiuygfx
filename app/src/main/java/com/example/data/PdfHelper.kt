package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfHelper {
    const val MAX_PDF_BYTES = 10L * 1024L * 1024L // 10 MB security limit

    data class PdfInspectionResult(
        val fileName: String,
        val sizeBytes: Long,
        val pageCount: Int,
        val pageBitmaps: List<Bitmap>
    )

    fun inspectAndRenderPdf(
        context: Context,
        uri: Uri,
        maxPages: Int = 5
    ): Result<PdfInspectionResult> = runCatching {
        var displayName = "document.pdf"
        var fileSize = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
            }
        }

        if (fileSize > MAX_PDF_BYTES) {
            error("PDF exceeds maximum allowed size of 10 MB.")
        }

        val tempFile = File(context.cacheDir, "inspect_${System.currentTimeMillis()}.pdf")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val copied = input.copyTo(output)
                    if (copied > MAX_PDF_BYTES) {
                        error("PDF exceeds maximum allowed size of 10 MB.")
                    }
                    if (fileSize <= 0L) fileSize = copied
                }
            } ?: error("Unable to open selected PDF.")

            val descriptor = android.os.ParcelFileDescriptor.open(
                tempFile,
                android.os.ParcelFileDescriptor.MODE_READ_ONLY
            )
            descriptor.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val count = renderer.pageCount
                    val pagesToRender = minOf(count, maxPages)
                    val bitmaps = mutableListOf<Bitmap>()
                    for (i in 0 until pagesToRender) {
                        renderer.openPage(i).use { page ->
                            val scale = minOf(1024f / page.width.coerceAtLeast(1), 1.5f)
                            val width = (page.width * scale).toInt().coerceAtLeast(1)
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            canvas.drawColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmaps.add(bitmap)
                        }
                    }
                    PdfInspectionResult(
                        fileName = displayName,
                        sizeBytes = fileSize,
                        pageCount = count,
                        pageBitmaps = bitmaps
                    )
                }
            }
        } finally {
            tempFile.delete()
        }
    }

    fun exportResumeToPdf(
        context: Context,
        fullName: String,
        resumeContent: String
    ): Result<Uri> = runCatching {
        val dir = File(context.cacheDir, "resumes").apply { mkdirs() }
        val safeName = fullName.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Resume" }
        val outFile = File(dir, "${safeName}_AI_Resume.pdf")

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 44f
        val maxTextWidth = pageWidth - margin * 2

        val titlePaint = Paint().apply {
            color = Color.rgb(22, 13, 43)
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(35, 35, 45)
            textSize = 11.5f
            isAntiAlias = true
        }

        val wrappedLines = wrapLines(resumeContent, bodyPaint, maxTextWidth)
        var lineIndex = 0
        var pageNumber = 1

        while (lineIndex < wrappedLines.size || pageNumber == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            var y = margin + 12f

            if (pageNumber == 1) {
                canvas.drawText(fullName.ifBlank { "Professional Resume" }, margin, y, titlePaint)
                y += 28f
            }

            while (lineIndex < wrappedLines.size && y < pageHeight - margin) {
                canvas.drawText(wrappedLines[lineIndex], margin, y, bodyPaint)
                y += 16f
                lineIndex++
            }
            document.finishPage(page)
            pageNumber++
        }

        FileOutputStream(outFile).use { fos -> document.writeTo(fos) }
        document.close()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }

    private fun wrapLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        text.lines().forEach { paragraph ->
            if (paragraph.isBlank()) {
                result.add("")
            } else {
                var current = ""
                paragraph.split(" ").forEach { word ->
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (paint.measureText(candidate) <= maxWidth) {
                        current = candidate
                    } else {
                        if (current.isNotEmpty()) result.add(current)
                        current = word
                    }
                }
                if (current.isNotEmpty()) result.add(current)
            }
        }
        return result
    }
}
