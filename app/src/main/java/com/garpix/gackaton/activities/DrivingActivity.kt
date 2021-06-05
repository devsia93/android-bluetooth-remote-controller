package com.garpix.gackaton.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.garpix.gackaton.R
import com.garpix.gackaton.utils.Const
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_driving.*
import java.io.IOException
import java.util.*


class DrivingActivity : AppCompatActivity() {
    private var address: String? = ""
    private var btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var device: BluetoothDevice? = null

    inner class ConnectThread(device: BluetoothDevice, message: String) : Thread() {
        init {
            var tmp: BluetoothSocket? = null
            this@DrivingActivity.device = device
            try {
                val uuid = UUID.fromString(Const.UUID)
                tmp = this@DrivingActivity.device!!.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                Logger.e("Created socket: ${e.message}")
                e.printStackTrace()
            }
            socket = tmp
            btAdapter?.cancelDiscovery()
            try {
                socket!!.connect()
            } catch (e: IOException) {
                Logger.e("Connected socket: ${e.message}")
                try {
                    socket!!.close()
                } catch (e: IOException) {
                    Logger.e("Closed socket: ${e.message}")
                    e.printStackTrace()
                }
            }
            send(message)
            Logger.i("Send message ${message}.")
        }

        @Throws(IOException::class)
        fun send(message: String) {
            val outputStream = socket!!.outputStream
            outputStream.write(message.toByteArray())
            receive()
        }

        @Throws(IOException::class)
        fun receive() {
            val inputStream = socket!!.inputStream
            val buffer = ByteArray(256)
            val bytes: Int
            try {
                bytes = inputStream.read(buffer)
                val readMessage = String(buffer, 0, bytes)
                Logger.d("Received: $readMessage")
                socket!!.close()
            } catch (e: IOException) {
                Logger.e("Received: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving)

        val intent = intent
        address = intent.getStringExtra(Const.EXTRA_ADDRESS)

        if (!btAdapter!!.isEnabled) {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetooth, Const.ENABLE_BT)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            socket!!.close()
        } catch (e: IOException) {
            Logger.e("Closed socket: ${e.message}")
            e.printStackTrace()
        }
    }

    fun onClickButtonDrive(view: View) {
        when (view.id) {
            R.id.btnForward -> sendMessageToDevice(Const.Driver.FORWARD)
            R.id.btnLeft -> sendMessageToDevice(Const.Driver.LEFT)
            R.id.btnRight -> sendMessageToDevice(Const.Driver.RIGHT)
            R.id.btnBackward -> sendMessageToDevice(Const.Driver.BACKWARD)
        }
    }

    private fun sendMessageToDevice(message: String) {
        val device = this.btAdapter?.getRemoteDevice(address)
        try {
            if (device != null) {
                ConnectThread(device, message).start()
                Logger.i("Started thread to ${device}.")
            }
        } catch (e: IOException) {
            Logger.e("Started thread to ${device}: ${e.message}")
            e.printStackTrace()
        }
    }
}