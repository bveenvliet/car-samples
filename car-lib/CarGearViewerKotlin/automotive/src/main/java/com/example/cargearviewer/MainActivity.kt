/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cargearviewer

import android.app.Activity
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView

/**
 * A simple activity that demonstrates connecting to car API and processing car property change
 * events.
 *
 * <p>Please see https://developer.android.com/reference/android/car/packages for API documentation.
 */
class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"

        private const val UNKNOWN = "unknown"

        // Values are taken from android.car.hardware.CarSensorEvent class.
        private val VEHICLE_GEARS = mapOf(
            0x0000 to UNKNOWN,
            0x0001 to "GEAR_NEUTRAL",
            0x0002 to "GEAR_REVERSE",
            0x0004 to "GEAR_PARK",
            0x0008 to "GEAR_DRIVE"
        )

        private val IGNITION_STATES = mapOf(
            0 to UNKNOWN,
            1 to "LOCK",
            2 to "OFF",
            3 to "ACC",
            4 to "On",
            5 to "Start"
        )
    }

    private lateinit var currentGearTextView: TextView
    private lateinit var ignitionStateTextView: TextView
    private lateinit var parkingBrakeTextView: TextView
    private lateinit var nightModeTextView: TextView

    /** Car API. */
    private lateinit var car: Car

    /**
     * An API to read VHAL (vehicle hardware access layer) properties. List of vehicle properties
     * can be found in {@link VehiclePropertyIds}.
     *
     * <p>https://developer.android.com/reference/android/car/hardware/property/CarPropertyManager
     */
    private lateinit var carPropertyManager: CarPropertyManager

    private var carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<Any>) {
            Log.d(TAG, "Received on changed car property event")
            // value.value type changes depending on the vehicle property.
            when (value.propertyId) {
                VehiclePropertyIds.GEAR_SELECTION -> currentGearTextView.text =
                    VEHICLE_GEARS.getOrDefault(value.value as Int, UNKNOWN)
                VehiclePropertyIds.IGNITION_STATE -> ignitionStateTextView.text =
                    IGNITION_STATES.getOrDefault(value.value as Int, UNKNOWN)
                VehiclePropertyIds.PARKING_BRAKE_ON -> parkingBrakeTextView.text =
                    value.value.toString()
                VehiclePropertyIds.NIGHT_MODE -> nightModeTextView.text =
                    value.value.toString()
                else ->
                    Log.w(TAG, "unknown id: $value")
            }
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.w(TAG, "Received error car property event, propId=$propId")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentGearTextView = findViewById(R.id.currentGearTextView)
        ignitionStateTextView = findViewById(R.id.ignitionStateTextView)
        parkingBrakeTextView = findViewById(R.id.parkingBrakeTextView)
        nightModeTextView = findViewById(R.id.nightModeTextView)

        // createCar() returns a "Car" object to access car service APIs. It can return null if
        // car service is not yet ready but that is not a common case and can happen on rare cases
        // (for example car service crashes) so the receiver should be ready for a null car object.
        //
        // Other variants of this API allows more control over car service functionality (such as
        // handling car service crashes graciously). Please see the SDK documentation for this.
        car = Car.createCar(this)

        carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager;

        val signalsToMonitor = arrayListOf(
            VehiclePropertyIds.GEAR_SELECTION,
            VehiclePropertyIds.IGNITION_STATE,
            VehiclePropertyIds.PARKING_BRAKE_ON,
            VehiclePropertyIds.NIGHT_MODE
        )

        signalsToMonitor.forEach {
            carPropertyManager.registerCallback(
                carPropertyListener,
                it,
                CarPropertyManager.SENSOR_RATE_ONCHANGE
            )
        }
    }

    private val permissions =
        arrayOf(Car.PERMISSION_ENERGY, Car.PERMISSION_SPEED, Car.PERMISSION_POWERTRAIN)

    override fun onResume() {
        super.onResume()
        when (PackageManager.PERMISSION_GRANTED) {
            checkSelfPermission(permissions[0]) -> {}
            checkSelfPermission(permissions[1]) -> {}
            checkSelfPermission(permissions[2]) -> {}
            else -> {
                requestPermissions(permissions, 0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        car.disconnect()
    }
}
