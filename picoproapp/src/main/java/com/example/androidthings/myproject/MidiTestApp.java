package com.example.androidthings.myproject;

import com.example.androidthings.myproject.utils.SerialMidi;
import com.google.android.things.contrib.driver.mma8451q.Mma8451Q;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Demo of the SerialMidi class
 * Created by bjoern on 9/12/17.
 */

public class MidiTestApp extends SimplePicoPro {
    SerialMidi serialMidi;

    // synth controls
    int channel = 0;
    int velocity = 127;
    int timbre_value = 0;
    final int timbre_controller = 0x47;
    boolean instrumentState = false;
    int prevNote;
    String prevLightState;
    int prevTimbre;
    NavigableMap noteMap;
    NavigableMap timbreMap;
    int curNote;

    // instruments
    int[] instruments = {1, 25, 59, 67, 53, 55};
    int instrumentCounter = 0;

    // vars for sensor readings
    Mma8451Q accelerometer;
    float[] xyz = {0.f,0.f,0.f};
    float force, light;

    @Override
    public void setup() {

        // initialize new synthesizer
        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);

        // initialize the analogue readings
        analogInit();

        // get new accelerometer
        /*
        try {
            accelerometer = new Mma8451Q("I2C1");
            accelerometer.setMode(Mma8451Q.MODE_ACTIVE);
        } catch (IOException e) {
            Log.e("MMA8451App","setup",e);
        }
        */

        // create maps from analogue readings to synth values
        noteMap = createNoteMap();
        timbreMap = createTimbreMap();
    }

    private NavigableMap<Float, Integer> createNoteMap() {
        NavigableMap<Float, Integer> noteMap = new TreeMap<Float, Integer>();
        noteMap.put((float) 0.0, SerialMidi.MIDI_C4);
        noteMap.put((float) 0.4, SerialMidi.MIDI_D4);
        noteMap.put((float) 0.6, SerialMidi.MIDI_E4);
        noteMap.put((float) 0.8, SerialMidi.MIDI_F4);
        noteMap.put((float) 1.0, SerialMidi.MIDI_G4);
        noteMap.put((float) 2.0, SerialMidi.MIDI_A5);
        noteMap.put((float) 3.0, SerialMidi.MIDI_B5);
        noteMap.put((float) 3.3, -1);

        return noteMap;
    }

    private NavigableMap<Float, Integer> createTimbreMap() {
        NavigableMap<Float, Integer> timbreMap = new TreeMap<Float, Integer>();
        timbreMap.put((float) 0.0, 0);
        timbreMap.put((float) 1.0, 33);
        timbreMap.put((float) 2.0, 66);
        timbreMap.put((float) 3.0, 100);
        timbreMap.put((float) 4.0, 127);
        return timbreMap;
    }

    // handler + runnable for toggling instrument state
    Handler toggleInstrumentHandler = new Handler();
    Runnable toggleInstrumentRunnable = new Runnable() {
        @Override
        public void run() {
            noteOffHandler.removeCallbacks(noteOffRunnable);
            noteOffHandler.postDelayed(noteOffRunnable, 2000);
        }
    };

    // handler for turning off note
    Handler noteOffHandler = new Handler();
    Runnable noteOffRunnable = new Runnable() {
        @Override
        public void run() {
            print("CUR NOTE " + curNote);
            serialMidi.midi_note_off(channel, curNote, velocity);
        }
    };

    @Override
    public void loop() {
        // get analogue readings
        force = analogRead(A0); // this is pitch
        light = analogRead(A1); // this is note ON/OFF
        float acceleration = (float) 0.0; // this is timbre
        //print("FORCE: " + force);
        //print("LIGHT: " + light);

        delay(300);

        /*
        try {
            xyz = accelerometer.readSample();
            println("X: "+xyz[0]+"   Y: "+xyz[1]+"   Z: "+xyz[2]);
            acceleration = xyz[0];
        } catch (IOException e) {
            Log.e("MMA8451App","loop",e);
        }
        */

        // if light sensor is covered for at least 1 second, then hold note
        String curLightState = light > .5 ? "covered" : "notCovered";
        if(curLightState == "covered") {
            toggleInstrumentHandler.postDelayed(toggleInstrumentRunnable, 1000);
        } else {
            toggleInstrumentHandler.removeCallbacks(toggleInstrumentRunnable);
        }

        // save light state so we only act on changes
        prevLightState = curLightState;

        // convert the force sensor value to a note
        int note = (int) noteMap.floorEntry(force).getValue();

        // turn on note for 1.5s then turn off
        if(note != prevNote && note != -1) {
            curNote = note;
            serialMidi.midi_note_on(channel, note, velocity);
            noteOffHandler.postDelayed(noteOffRunnable, 2000);
            prevNote = note;
        }

        /*
        // convert acceleration to timbre
        int timbreValue = (int) timbreMap.floorEntry(acceleration).getValue();

        // check for timbre change
        if(prevTimbre != timbreValue) {
            serialMidi.midi_controller_change(channel, timbre_controller, timbre_value);
            prevTimbre = timbreValue;
        }
        */
    }
}