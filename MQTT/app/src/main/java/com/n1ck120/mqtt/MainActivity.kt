package com.n1ck120.mqtt

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        val sendmsg = findViewById<Button>(R.id.button)
        val submsg = findViewById<TextView>(R.id.subpayload)
        val ed1 = findViewById<EditText>(R.id.topic)
        val ed2 = findViewById<EditText>(R.id.msg)
        val setting = findViewById<ImageButton>(R.id.settings)

        fun showInputDialog() {
            // Infla o layout do diálogo personalizado
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)
            val builder = AlertDialog.Builder(this)

            // Configura o layout personalizado no diálogo
            builder.setView(dialogView)

            // Configuração do diálogo
            builder.setTitle("Connection Settings")
            builder.setMessage("Leave blank to use default values")

            // Referências às caixas de texto no layout
            val inputField1: EditText = dialogView.findViewById(R.id.inputField1)
            val inputField2: EditText = dialogView.findViewById(R.id.inputField2)
            val inputField3: EditText = dialogView.findViewById(R.id.inputField3)
            val inputField4: EditText = dialogView.findViewById(R.id.inputField4)

            if (SharedPreferencesManager.chaveExiste("IdClient")){
                inputField1.setText(SharedPreferencesManager.obterString("IdClient").toString())
            }
            if (SharedPreferencesManager.chaveExiste("Server")){
                inputField2.setText(SharedPreferencesManager.obterString("Server").toString())
            }
            if (SharedPreferencesManager.chaveExiste("Port")){
                inputField3.setText(SharedPreferencesManager.obterString("Port").toString())
            }
            if (SharedPreferencesManager.chaveExiste("Topic")){
                inputField4.setText(SharedPreferencesManager.obterString("Topic").toString())
            }

            // Botões do diálogo
            builder.setPositiveButton("Save") { _, _ ->
                // Captura os valores digitados
                if (inputField1.text.isNotBlank()){
                    SharedPreferencesManager.salvarString("IdClient", inputField1.text.toString())
                }else{
                    SharedPreferencesManager.removerChave("IdClient")
                }
                if (inputField2.text.isNotBlank()){
                    SharedPreferencesManager.salvarString("Server", inputField2.text.toString())
                }else{
                    SharedPreferencesManager.removerChave("Server")
                }
                if (inputField3.text.isNotBlank()){
                    SharedPreferencesManager.salvarString("Port", inputField3.text.toString())
                }else{
                    SharedPreferencesManager.removerChave("Port")
                }
                if (inputField4.text.isNotBlank()){
                    SharedPreferencesManager.salvarString("Topic", inputField4.text.toString())
                }else{
                    SharedPreferencesManager.removerChave("Topic")
                }
                // Mostra as informações capturadas (ou faça algo com elas)
                SharedPreferencesManager.salvarString("Popup", "1")
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                val intent = intent // Recupera o Intent atual
                finish() // Finaliza a Activity
                startActivity(intent) // Inicia uma nova instância da mesma Activity
            }
            builder.setNegativeButton(getString(R.string.reset_to_defaults)) { dialog, _ ->
                Toast.makeText(this, getString(R.string.using_default_values), Toast.LENGTH_SHORT).show()
                SharedPreferencesManager.limparTudo()
                SharedPreferencesManager.salvarString("Popup", "1")
                dialog.dismiss() // Fecha o diálogo
            }
            // Exibe o diálogo
            builder.create().show()
        }

        val client = MqttClient.builder()
            .useMqttVersion3()
            .identifier(SharedPreferencesManager.obterString("IdClient", "ClientMQTT").toString())
            .serverHost(SharedPreferencesManager.obterString("Server", "broker.hivemq.com").toString())
            .serverPort(SharedPreferencesManager.obterString("Port", "1883").toString().toInt())
            .buildAsync()

        // Conectando ao broker MQTT
        client.connectWith()
            .send()
            .whenComplete { connAck: Mqtt3ConnAck?, throwable: Throwable? ->
                if (throwable != null) {
                    // Log de erro ao conectar
                    throwable.printStackTrace()
                } else {
                    // Log de sucesso na conexão
                    println("Conectado ao broker MQTT!")
                }
            }

        // Inscrevendo no tópico
            client.subscribeWith()
                .topicFilter(SharedPreferencesManager.obterString("Topic", "Test").toString())
                .callback { publish: Mqtt3Publish? ->
                    if (publish != null) {
                        // Capturar a payload da mensagem recebida
                        runOnUiThread {
                            submsg.text = (String(publish.payloadAsBytes)+"\n") + submsg.text
                        }
                    }
                }
                .send()
                .whenComplete { subAck: Mqtt3SubAck?, throwable: Throwable? ->
                    if (throwable != null) {
                        // Handle failure to subscribe
                    } else {
                        // Handle successful subscription, e.g. logging or incrementing a metric
                    }
                }

        // Função para envio de informações
        fun pubmsg(Topico : String = "Test", Payload : String = "Hello World"){
            client.publishWith()
                .topic(Topico)
                .payload(Payload.toByteArray())
                .send()
                .whenComplete { publish: Mqtt3Publish?, throwable: Throwable? ->
                    if (throwable != null) {
                        // Log de erro ao publicar
                        throwable.printStackTrace()
                    } else {
                        // Log de sucesso na publicação
                        println("Mensagem publicada com sucesso!")
                    }
                }
        }

        if (!SharedPreferencesManager.chaveExiste("Popup")){
            showInputDialog()
        }

        setting.setOnClickListener {
            showInputDialog()
        }

        sendmsg.setOnClickListener {
            if (ed1.text.isBlank()){
                ed1.error = getString(R.string.topic_can_t_be_blank)
            }else{
                if (ed2.text.isBlank()){
                    ed2.error = getString(R.string.message_can_t_be_blank)
                }else{
                    pubmsg(ed1.text.toString(), ed2.text.toString())
                }
            }
        }
    }
}





