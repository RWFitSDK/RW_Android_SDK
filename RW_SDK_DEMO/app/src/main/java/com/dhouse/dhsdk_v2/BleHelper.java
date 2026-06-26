package com.dhouse.dhsdk_v2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.os.PowerManager;

import java.lang.reflect.Method;
import java.util.List;

public class BleHelper {

    @SuppressLint("MissingPermission")
    public static boolean isBluetoothOpen(Context context) {

        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothManager != null) {
//            mBluetoothAdapter = mBluetoothManager.getAdapter();
//        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            return true;
        }
        return false;
    }

    public static void refreshDeviceCache(BluetoothGatt gatt) {
        try {
            Method refresh = gatt.getClass().getMethod("refresh");
            if (refresh != null) {
                refresh.invoke(gatt);
            }
        } catch (Exception var3) {
//            BleClient.showMessage(var3.getMessage());
        }

    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return true;
        } else {
            boolean isScreenOn;
            if (Build.VERSION.SDK_INT >= 20) {
                isScreenOn = powerManager.isInteractive();
            } else {
                isScreenOn = powerManager.isScreenOn();
            }

            return isScreenOn;
        }
    }

//    public static boolean isInDfuMode(BluetoothGatt gatt) {
//        return gatt.getService(BleManager.RX_UPDATE_UUID) != null;
//    }

    /**
     * 打开蓝牙
     *
     * @return
     */
    public static boolean openBLE(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return mBluetoothAdapter.enable();
    }

    /**
     * 判断蓝牙开关是否打开
     *
     * @return
     */
    public static boolean isBleOpen(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

}
