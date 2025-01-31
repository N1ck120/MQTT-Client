package com.n1ck120.mqtt

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck

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
        val receivedMessages = findViewById<TextView>(R.id.subpayload)
        val fieldTopic = findViewById<EditText>(R.id.topic)
        val fieldMessage = findViewById<EditText>(R.id.msg)
        val btnSettings = findViewById<ImageButton>(R.id.settings)
        val clear = findViewById<FloatingActionButton>(R.id.clearbtn)
        val pause = findViewById<FloatingActionButton>(R.id.pausebtn)

        var isToggled = false

        fun updateButtonAppearance() {
            if (isToggled) {
                pause.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
                pause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.round_play_circle_outline_24))
            } else {
                pause.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF0500"))
                pause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_pause_circle_24))
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
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                val intent = intent // Recupera o Intent atual
                finish() // Finaliza a Activity
                startActivity(intent) // Inicia uma nova instância da mesma Activity
                dialog.dismiss()
            }

            defaultsOption.setOnClickListener {
                Toast.makeText(this, getString(R.string.using_default_values), Toast.LENGTH_SHORT).show()
                SharedPreferencesManager.clearAllData()
                SharedPreferencesManager.saveString("popupAlreadyShown", "1")
                val intent = intent // Recupera o Intent atual
                finish() // Finaliza a Activity
                startActivity(intent) // Inicia uma nova instância da mesma Activity
                dialog.dismiss()
            }
            dialog.show()
        }

        val client = MqttClient.builder()
            .useMqttVersion3()
            .identifier(SharedPreferencesManager.getString("IdClient", "ClientMQTT").toString())
            .serverHost(SharedPreferencesManager.getString("Server", "broker.hivemq.com").toString())
            .serverPort(SharedPreferencesManager.getString("Port", "1883").toString().toInt())
            .buildAsync()

        // Conectando ao broker MQTT
        client.connectWith()
            .send()
            .whenComplete { _: Mqtt3ConnAck?, throwable: Throwable? ->
                if (throwable != null) {
                    // Log de erro ao conectar
                    throwable.printStackTrace()
                } else {
                    // Log de sucesso na conexão
                    println("Connected to the Broker!")
                }
            }

        // Inscrevendo no tópico
            client.subscribeWith()
                .topicFilter(SharedPreferencesManager.getString("Topic", "TestClientMQTT").toString())
                .callback { publish: Mqtt3Publish? ->
                    if (publish != null) {
                        // Capturar a payload da mensagem recebida
                        runOnUiThread {
                            if (isToggled){
                                    receivedMessages.text = String(publish.payloadAsBytes) + "\n" + receivedMessages.text
                            }
                        }
                    }
                }
                .send()
                .whenComplete { _: Mqtt3SubAck?, throwable: Throwable? ->
                    if (throwable != null) {
                        // Handle failure to subscribe
                        throwable.printStackTrace()
                    } else {
                        // Handle successful subscription, e.g. logging or incrementing a metric
                        println("Subscribed to the topic!")
                    }
                }

        // Função para envio de informações
        fun pubmsg(topic : String, payload : String){
            client.publishWith()
                .topic(topic)
                .payload(payload.toByteArray())
                .send()
                .whenComplete { _: Mqtt3Publish?, throwable: Throwable? ->
                    if (throwable != null) {
                        // Log de erro ao publicar
                        throwable.printStackTrace()
                    } else {
                        // Log de sucesso na publicação
                        println("Message published successfully!")
                    }
                }
        }

        if (!SharedPreferencesManager.keyExists("popupAlreadyShown")){
            showInputDialog()
        }

        btnSettings.setOnClickListener {
            showInputDialog()
        }

        clear.setOnClickListener {
            receivedMessages.text = ""

        }

        btnSend.setOnClickListener {
            if (fieldTopic.text.isBlank()){
                fieldTopic.error = getString(R.string.topic_can_t_be_blank)
            }else{
                if (fieldMessage.text.isBlank()){
                    fieldMessage.error = getString(R.string.message_can_t_be_blank)
                }else{
                    pubmsg(fieldTopic.text.toString(), fieldMessage.text.toString())
                }
            }
        }
    }
}





