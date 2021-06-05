package com.garpix.gackaton.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

import android.content.Intent
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.activity_main.*

import android.widget.*
import com.garpix.gackaton.R
import com.garpix.gackaton.utils.Const
import java.util.*


class MainActivity : AppCompatActivity() {

    private var btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var btSocket: BluetoothSocket? = null
    private var pairedDevices: Set<BluetoothDevice>? = null
//    private var adapter: ArrayAdapter<String>? = null

//    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//            if (BluetoothDevice.ACTION_FOUND == action) {
//                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                Log.d("F00001", device?.name as String)
//                adapter?.add(device.name + '\n' + device.address)
//            }
//            lvDevicesBT.adapter = adapter
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        adapter = ArrayAdapter(applicationContext, android.R.layout.simple_expandable_list_item_1)

//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(receiver, filter)

        if (btAdapter != null) {
            Toast.makeText(applicationContext, "Bluetooth adapter has found.", Toast.LENGTH_SHORT)
                .show()
            if (btAdapter!!.isEnabled) {
                buttonsVisibilityController(true)
                lvDevicesBT.visibility = View.VISIBLE
            } else {
                buttonsVisibilityController(false)
                lvDevicesBT.visibility = View.GONE
            }
        } else {
            Toast.makeText(
                applicationContext, "Bluetooth adapter has not found:(",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val myListClickListener = OnItemClickListener { _, view, _, _ ->
        val info = (view as TextView).text.toString()
        val address = info.substring(info.length - 17)
        Toast.makeText(applicationContext, info, Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, DrivingActivity::class.java)
        intent.putExtra(Const.EXTRA_ADDRESS, address)
        startActivity(intent)
    }

    fun onClickEnableBluetooth(view: View) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, Const.ENABLE_BT)
        recreate()
    }

    fun onClickDisableBluetooth(view: View) {
        btAdapter?.disable()
        recreate()
    }

    fun onClickScanDevices(view: View) {
        val deviceList: ArrayList<String> = ArrayList<String>()
        pairedDevices = btAdapter?.bondedDevices
        if (pairedDevices?.size!! < 1) {
            Toast.makeText(applicationContext, "No paired devices found.", Toast.LENGTH_SHORT)
                .show()
        } else {
            for (bt in pairedDevices!!)
                deviceList.add(bt.name + " " + bt.address)
            Toast.makeText(applicationContext, "Showing paired devices", Toast.LENGTH_SHORT).show()
            val adapter: ArrayAdapter<*> =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
            lvDevicesBT.adapter = adapter
            lvDevicesBT.onItemClickListener = myListClickListener
        }
    }

    private fun buttonsVisibilityController(bluetoothStatus: Boolean) {
        if (bluetoothStatus) {
            btnScanBt.visibility = View.VISIBLE
            btnEnableBT.visibility = View.GONE
            btnDisableBT.visibility = View.VISIBLE
        } else {
            btnScanBt.visibility = View.GONE
            btnEnableBT.visibility = View.VISIBLE
            btnDisableBT.visibility = View.GONE
        }
    }

    override fun onDestroy() {
//        unregisterReceiver(receiver)
        super.onDestroy()
    }
}