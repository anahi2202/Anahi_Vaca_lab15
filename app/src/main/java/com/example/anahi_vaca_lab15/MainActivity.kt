package com.example.imageclassifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar ImageView, TextView y Button
        val img: ImageView = findViewById(R.id.imageToLabel)
        val txtOutput: TextView = findViewById(R.id.txtOutput)
        val btn: Button = findViewById(R.id.btnTest)

        // Nombre del archivo de imagen en assets
        val fileName = "meningioma4.jpg"
        // Obtener bitmap desde assets
        val bitmap: Bitmap? = assetsToBitmap(fileName)
        bitmap?.apply {
            img.setImageBitmap(this)
        }

        // Cargar el modelo personalizado desde los assets
        val localModel = LocalModel.Builder()
            .setAssetFilePath("moodel_int8.tflite") // Asegúrate de que el archivo se llame así en la carpeta assets
            .build()

        // Configurar onClickListener para el botón
        btn.setOnClickListener {
            // Configurar las opciones del etiquetador para el modelo personalizado
            val options = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.6f) // Umbral de confianza
                .setMaxResultCount(4)         // Máximo número de resultados
                .build()

            // Crear el etiquetador con las opciones personalizadas
            val labeler = ImageLabeling.getClient(options)
            val image = InputImage.fromBitmap(bitmap!!, 0)
            var outputText = ""

            // Procesar la imagen
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // La tarea se completó con éxito
                    for (label in labels) {
                        val text = label.text  // Texto de la etiqueta (por ejemplo, "flower")
                        val confidence = label.confidence  // Nivel de confianza
                        outputText += "$text : ${"%.2f".format(confidence * 100)}%\n"
                    }
                    txtOutput.text = outputText  // Mostrar el resultado en el TextView
                }
                .addOnFailureListener { e ->
                    // La tarea falló con una excepción
                    txtOutput.text = "Error: ${e.message}"
                }
        }
    }

    // Función para obtener un Bitmap desde la carpeta assets
    fun Context.assetsToBitmap(fileName: String): Bitmap? {
        return try {
            with(assets.open(fileName)) {
                BitmapFactory.decodeStream(this)
            }
        } catch (e: IOException) {
            null
        }
    }
}