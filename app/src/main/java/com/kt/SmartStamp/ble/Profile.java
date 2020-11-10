package com.kt.SmartStamp.ble;

import java.util.UUID;

public class Profile {
    public static UUID UUID_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    public static UUID CHARACTERISTIC_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID CHARACTERISTIC_NOTI = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID CHARACTERISTIC_BATTERY = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

}
