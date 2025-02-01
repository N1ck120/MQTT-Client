package com.n1ck120.mqtt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SharedPreferencesManager.init(this)
        val btnSend = findViewById<Button>(R.id.button)
        val btnSettings = findViewById<ImageButton>(R.id.settings)
        val btnRefresh = findViewById<ImageButton>(R.id.refresh)
        val clear = findViewById<FloatingActionButton>(R.id.clearbtn)
        val fieldMessage = findViewById<EditText>(R.id.msg)
        val fieldTopic = findViewById<EditText>(R.id.topic)
        val pause = findViewById<FloatingActionButton>(R.id.pausebtn)
        val receivedMessages = findViewById<TextView>(R.id.subpayload)
        val wait = findViewById<TextView>(R.id.wait)
        val progress = findViewById<ProgressBar>(R.id.progressBar)
        var isToggled = false

        val mqtt = MQTTConnection(SharedPreferencesManager.getString("IdClient", "ClientMQTT").toString(), SharedPreferencesManager.getString("Server", "broker.hivemq.com").toString(), SharedPreferencesManager.getString("Port", "1883").toString().toInt())
        mqtt.setCallbacks(object : MQTTConnection.MQTTCallbacks {
            override fun onConnected() {
                runOnUiThread {
                    btnSend.isEnabled = true
                    fieldTopic.isEnabled = true
                    fieldMessage.isEnabled = true
                    wait.visibility = View.GONE
                    progress.visibility = View.GONE

                    Toast.makeText(this@MainActivity,
                        getString(R.string.connectedtoast)+" "+SharedPreferencesManager.getString("Server", "broker.hivemq.com").toString(), Toast.LENGTH_SHORT).show()
                    mqtt.subscribe(SharedPreferencesManager.getString("Topic", "TestClientMQTT").toString())

                    btnSend.setOnClickListener {
                        if (fieldTopic.text.isBlank()){
                            fieldTopic.error = getString(R.string.topic_can_t_be_blank)
                        }else{
                            if (fieldMessage.text.isBlank()){
                                fieldMessage.error = getString(R.string.message_can_t_be_blank)
                            }else{
                                mqtt.publish(fieldTopic.text.toString(), fieldMessage.text.toString())
                            }
                        }
                    }
                }
            }

            override fun onConnectionFailed(throwable: Throwable) {
                Toast.makeText(this@MainActivity, "Falha na conexão!", Toast.LENGTH_SHORT).show()
            }

            override fun onMessageReceived(topic: String, payload: String) {
                runOnUiThread {
                    if (isToggled){
                        receivedMessages.text = payload + "\n" + receivedMessages.text
                    }
                }
            }

            override fun onSubscribed() {
                //Toast.makeText(this@MainActivity, "Inscrito com sucesso!", Toast.LENGTH_SHORT).show()
            }

            override fun onSubscribeFailed(throwable: Throwable) {
                //Toast.makeText(this@MainActivity, "Falha na inscrição!", Toast.LENGTH_SHORT).show()
            }

            override fun onMessagePublished() {
                //Toast.makeText(this@MainActivity, "Mensagem enviada!", Toast.LENGTH_SHORT).show()
            }

            override fun onPublishFailed(throwable: Throwable) {
                //Toast.makeText(this@MainActivity, "Falha no envio da mensagem!", Toast.LENGTH_SHORT).show()
            }
        })

        mqtt.connect()

        fun refrsh() = runBlocking{
            mqtt.disconnect()
            runOnUiThread{
                btnSend.isEnabled = false
                fieldTopic.isEnabled = false
                fieldMessage.isEnabled = false
                wait.visibility = View.VISIBLE
                progress.visibility = View.VISIBLE
            }
            mqtt.connect()
        }

        //Lógica do Botão pause
        fun updateButtonAppearance() {
            if (isToggled) {
                pause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_pause_circle_24))
            } else {
                pause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.round_play_circle_outline_24))
            }
        }

        pause.setOnClickListener {
            isToggled = !isToggled
            updateButtonAppearance()
        }

        fun showInputDialog() {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create() // Cria o diálogo sem botões padrão

            // Referências às caixas de texto no layout
            val inputField1: EditText = dialogView.findViewById(R.id.inputField1)
            val inputField2: EditText = dialogView.findViewById(R.id.inputField2)
            val inputField3: EditText = dialogView.findViewById(R.id.inputField3)
            val inputField4: EditText = dialogView.findViewById(R.id.inputField4)

            // Referências aos botões do layout
            val defaultsOption: Button = dialogView.findViewById(R.id.btnDefaults)
            val saveOption: Button = dialogView.findViewById(R.id.btnSave)
            var changed : Boolean = false

            if (SharedPreferencesManager.keyExists("IdClient")){
                inputField1.setText(SharedPreferencesManager.getString("IdClient").toString())
            }
            if (SharedPreferencesManager.keyExists("Server")){
                inputField2.setText(SharedPreferencesManager.getString("Server").toString())
            }
            if (SharedPreferencesManager.keyExists("Port")){
                inputField3.setText(SharedPreferencesManager.getString("Port").toString())
            }
            if (SharedPreferencesManager.keyExists("Topic")){
                inputField4.setText(SharedPreferencesManager.getString("Topic").toString())
            }
            inputField1.doAfterTextChanged {
                changed = true
            }
            inputField2.doAfterTextChanged {
                changed = true
            }
            inputField3.doAfterTextChanged {
                changed = true
            }
            inputField4.doAfterTextChanged {
                changed = true
            }

            // Configura os listeners dos botões
            saveOption.setOnClickListener {
                // Captura os valores digitados
                if (inputField1.text.isNotBlank()){

                    SharedPreferencesManager.saveString("IdClient", inputField1.text.toString())
                }else{
                    SharedPreferencesManager.removeKey("IdClient")
                }
                if (inputField2.text.isNotBlank()){

                    SharedPreferencesManager.saveString("Server", inputField2.text.toString())
                }else{
                    SharedPreferencesManager.removeKey("Server")
                }
                if (inputField3.text.isNotBlank()){

                    SharedPreferencesManager.saveString("Port", inputField3.text.toString())
                }else{
                    SharedPreferencesManager.removeKey("Port")
                }
                if (inputField4.text.isNotBlank()){

                    SharedPreferencesManager.saveString("Topic", inputField4.text.toString())
                }else{
                    SharedPreferencesManager.removeKey("Topic")
                }
                // Mostra as informações capturadas (ou faça algo com elas)
                SharedPreferencesManager.saveString("popupAlreadyShown", "1")
                if (changed == true){
                    refrsh()
                }
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            defaultsOption.setOnClickListener {
                Toast.makeText(this, getString(R.string.using_default_values), Toast.LENGTH_SHORT).show()
                SharedPreferencesManager.clearAllData()
                SharedPreferencesManager.saveString("popupAlreadyShown", "1")
                refrsh()
                dialog.dismiss()
            }
            dialog.show()
        }

        if (!SharedPreferencesManager.keyExists("popupAlreadyShown")){
            showInputDialog()
        }

        btnSettings.setOnClickListener {
            showInputDialog()
        }

        btnRefresh.setOnClickListener {
            refrsh()
        }

        clear.setOnClickListener {
            receivedMessages.text = ""

        }
    }
}





