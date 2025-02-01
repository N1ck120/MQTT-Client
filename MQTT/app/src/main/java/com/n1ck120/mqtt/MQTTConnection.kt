package com.n1ck120.mqtt

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck

class MQTTConnection(
    private val clientId: String = "ClientMQTT",
    private val server: String = "broker.hivemq.com",
    private val port: Int = 1883
) {
    private var client: Mqtt3AsyncClient? = null

    // Interface para callbacks (opcional, mas recomendado)
    interface MQTTCallbacks {
        fun onConnected()
        fun onConnectionFailed(throwable: Throwable)
        fun onMessageReceived(topic: String, payload: String)
        fun onSubscribed()
        fun onSubscribeFailed(throwable: Throwable)
        fun onMessagePublished()
        fun onPublishFailed(throwable: Throwable)
    }

    private var callbacks: MQTTCallbacks? = null

    fun setCallbacks(callbacks: MQTTCallbacks) {
        this.callbacks = callbacks
    }

    // Inicializa o cliente uma única vez
    private fun getClient(): Mqtt3AsyncClient {
        if (client == null || client?.state?.isConnected != true) {
            client = Mqtt3Client.builder()
                .identifier(clientId)
                .serverHost(server)
                .serverPort(port)
                .buildAsync()
        }
        return client!!
    }

    fun connect() {
        getClient().connectWith()
            .send()
            .whenComplete { _: Mqtt3ConnAck?, throwable: Throwable? ->
                if (throwable != null) {
                    callbacks?.onConnectionFailed(throwable)
                } else {
                    callbacks?.onConnected()
                }
            }
    }

    fun subscribe(topic: String) {
        getClient().subscribeWith()
            .topicFilter(topic)
            .callback { publish: Mqtt3Publish ->
                val payload = String(publish.payloadAsBytes)
                callbacks?.onMessageReceived(publish.topic.toString(), payload)
            }
            .send()
            .whenComplete { _: Mqtt3SubAck?, throwable: Throwable? ->
                if (throwable != null) {
                    callbacks?.onSubscribeFailed(throwable)
                } else {
                    callbacks?.onSubscribed()
                }
            }
    }

    fun publish(topic: String, payload: String) {
        getClient().publishWith()
            .topic(topic)
            .payload(payload.toByteArray())
            .send()
            .whenComplete { _: Mqtt3Publish?, throwable: Throwable? ->
                if (throwable != null) {
                    callbacks?.onPublishFailed(throwable)
                } else {
                    callbacks?.onMessagePublished()
                }
            }
    }

    fun disconnect() {
        getClient().disconnect()
        client = null
    }
}