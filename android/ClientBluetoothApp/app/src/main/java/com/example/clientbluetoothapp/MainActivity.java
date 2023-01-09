package com.example.clientbluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientbluetoothapp.btlib.BluetoothChannel;
import com.example.clientbluetoothapp.btlib.BluetoothUtils;
import com.example.clientbluetoothapp.btlib.ConnectToBluetoothServerTask;
import com.example.clientbluetoothapp.btlib.ConnectionTask;
import com.example.clientbluetoothapp.btlib.RealBluetoothChannel;
import com.example.clientbluetoothapp.btlib.exceptions.BluetoothDeviceNotFound;
import com.example.clientbluetoothapp.utils.ClientConfig;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothChannel btChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter != null && !btAdapter.isEnabled()){
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ClientConfig.bluetooth.ENABLE_BT_REQUEST);
        }
        initUI();

    }

    private void initUI() {

        ((TextView)findViewById(R.id.lblStato)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(((TextView)findViewById(R.id.lblStato)).getText().toString().equals("ALLARME")){
                    findViewById(R.id.btnManual).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.btnManual).setVisibility(View.INVISIBLE);
                    findViewById(R.id.openBar).setVisibility(View.INVISIBLE);
                    findViewById(R.id.lblOpenBar).setVisibility(View.INVISIBLE);
                }
            }
        });


        findViewById(R.id.btnBluetooth).setOnClickListener(v -> {
            try {
                connectToServer();
            }catch (BluetoothDeviceNotFound bluetoothDeviceNotFound) {
                bluetoothDeviceNotFound.printStackTrace();
            }
        });

        findViewById(R.id.btnManual).setOnClickListener(v -> {
            findViewById(R.id.openBar).setVisibility(View.VISIBLE);
            findViewById(R.id.lblOpenBar).setVisibility(View.VISIBLE);
            btChannel.sendMessage("M");
        });

        final SeekBar sk= findViewById(R.id.openBar);

        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int val = seekBar.getProgress();
                val=val*20;
                ((TextView)findViewById(R.id.lblOpenBar)).setText(val+"%");
                btChannel.sendMessage("m ".concat(Integer.toString(val)));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(btChannel!=null){
            btChannel.close();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ClientConfig.bluetooth.ENABLE_BT_REQUEST && resultCode == RESULT_OK) {
            Log.d(ClientConfig.APP_LOG_TAG, "Bluetooth enabled!");
        }

        if (requestCode == ClientConfig.bluetooth.ENABLE_BT_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(ClientConfig.APP_LOG_TAG, "Bluetooth not enabled!");
        }
    }

    private void connectToServer() throws BluetoothDeviceNotFound{
        final BluetoothDevice serverDevice = BluetoothUtils.getPairedDeviceByName(ClientConfig.bluetooth.BT_DEVICE_ACTING_AS_SERVER_NAME);

        // !!! UTILIZZARE IL CORRETTO VALORE DI UUID
        final UUID uuid = BluetoothUtils.getEmbeddedDeviceDefaultUuid();
        //final UUID uuid = BluetoothUtils.generateUuidFromString(C.bluetooth.BT_SERVER_UUID);

        new ConnectToBluetoothServerTask(serverDevice, uuid, new ConnectionTask.EventListener() {
            @Override
            public void onConnectionActive(final BluetoothChannel channel) {

                ((TextView) findViewById(R.id.statusLabel)).setText(String.format("Status : connected to server on device %s",
                        serverDevice.getName()));

                findViewById(R.id.btnBluetooth).setEnabled(false);

                btChannel = channel;
                btChannel.registerListener(new RealBluetoothChannel.Listener() {
                    @Override
                    public void onMessageReceived(String receivedMessage) {
                        String[] parts=receivedMessage.split(" ");
                        ((TextView)findViewById(R.id.lblStato)).setText(parts[0]);
                        ((TextView)findViewById(R.id.lblLivello)).setText(parts[1]+"cm");
                        ((TextView)findViewById(R.id.lblApertura)).setText(parts[2]+"%");
                    }

                    @Override
                    public void onMessageSent(String sentMessage) {
                    }
                });
            }

            @Override
            public void onConnectionCanceled() {
                ((TextView) findViewById(R.id.statusLabel)).setText(String.format("Status : unable to connect, device %s not found!",
                        ClientConfig.bluetooth.BT_DEVICE_ACTING_AS_SERVER_NAME));
            }
        }).execute();
    }


}