package com.example.androidthings.myproject;
import com.example.androidthings.myproject.utils.SerialMidi;

import android.util.Log;

import java.io.IOException;

import com.google.android.things.contrib.driver.mma8451q.Mma8451Q;

import android.os.Handler;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * HW3 Template
 * Created by bjoern on 9/12/17.
 * Wiring:
 * USB-Serial Cable:
 *   GND to GND on IDD Hat
 *   Orange (Tx) to UART6 RXD on IDD Hat
 *   Yellow (Rx) to UART6 TXD on IDD Hat
 * Accelerometer:
 *   Vin to 3V3 on IDD Hat
 *   GND to GND on IDD Hat
 *   SCL to SCL on IDD Hat
 *   SDA to SDA on IDD Hat
 * Analog sensors:
 *   Middle of voltage divider to Analog A0..A3 on IDD Hat
 */

public class Hw3TemplateApp extends SimplePicoPro {
    SerialMidi serialMidi;

    // synth controls
    int channel = 1;
    int velocity = 127;
    int timbreValue = 0;
    int prevTimbre;
    final int timbre_controller = 0x47;
    int prevNote;
    String prevLightState = "notCovered";
    NavigableMap noteMap;
    NavigableMap timbreMap;
    int noteLength = 2000;
    int noteToHold;
    Runnable currNoteOffRunnable;

    // vars for sensor readings
    float force, light, flex;

    @Override
    public void setup() {

        // initialize new synthesizer
        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);

        // initialize the analogue readings
        analogInit();

        // create maps from analogue readings to synth values
        noteMap = createNoteMap();
        timbreMap = createTimbreMap();
    }

    @Override
    public void loop() {

        // get analogue readings
        flex = analogRead(A0); // this is timbre
        force = analogRead(A1); // this is pitch
        light = analogRead(A2); // this is note ON/OFF

        //print("FORCE: " + force);
        //print("LIGHT: " + light);

        delay(1000); // is this the right delay?


        // translate flex sensor input to 0 to 127 range
        int timbreValue = Math.round(127 * (flex - (float) 2.2));
        timbreValue = timbreValue > 127 ? 127 : timbreValue;

        // do something with the flex
        //int timbreValue = (int) timbreMap.floorEntry(flex).getValue(); // timbre value goes from 0 to 127

        print("FLEX " + timbreValue);

        // check for timbre change
        if(prevTimbre != timbreValue) {
            serialMidi.midi_controller_change(channel, timbre_controller, timbreValue);
            prevTimbre = timbreValue;
        }

        // convert the force sensor value to a note
        int note = (int) noteMap.floorEntry(force).getValue();

        //print("NOTE " + note);
        //print("FORCE " + force);
        // turn on note for 2s (if diff from previous)
        if(note != prevNote && note != -1) {
            serialMidi.midi_note_on(channel, note, velocity);
            currNoteOffRunnable = createNoteOffRunnable(note);
            noteOffHandler.postDelayed(currNoteOffRunnable, noteLength);
            prevNote = note;
        }

        // detect whether light sensor is covered
        String curLightState = light > .5 ? "covered" : "notCovered";
        print("LIGHT " + light);

        print(curLightState);


        // if light sensor is covered hold the note and keep track of the note being held
        if(curLightState == "covered" && prevLightState == "notCovered") {
            //print("HOLDING NOTE " + note);
            noteOffHandler.removeCallbacks(currNoteOffRunnable);
            noteToHold = note;
        }

        // if light sensor cover is released then turn off held note
        if(curLightState == "notCovered" && prevLightState == "covered") {
            //print("releasing NOTE " + noteToHold);
            Runnable noteOffRunnable = createNoteOffRunnable(noteToHold);
            noteOffHandler.postDelayed(noteOffRunnable, 1000);
        }

        // track light changes
        prevLightState = curLightState;
    }

    private NavigableMap<Float, Integer> createNoteMap() {
        NavigableMap<Float, Integer> noteMap = new TreeMap<Float, Integer>();
        noteMap.put((float) 0.0, SerialMidi.MIDI_C4);
        noteMap.put((float) 0.3, SerialMidi.MIDI_D4);
        noteMap.put((float) 0.5, SerialMidi.MIDI_E4);
        noteMap.put((float) 0.8, SerialMidi.MIDI_F4);
        noteMap.put((float) 1.0, SerialMidi.MIDI_G4);
        noteMap.put((float) 1.3, SerialMidi.MIDI_A5);
        noteMap.put((float) 1.6, SerialMidi.MIDI_B5);
        noteMap.put((float) 2.1, -1);

        return noteMap;
    }

    private NavigableMap<Float, Integer> createTimbreMap() {
        NavigableMap<Float, Integer> timbreMap = new TreeMap<Float, Integer>();
        timbreMap.put((float) 1.0, 100);
        timbreMap.put((float) 2.0, 66);
        timbreMap.put((float) 2.5, 0);
        timbreMap.put((float) 2.8, 66);
        timbreMap.put((float) 3.1, 100);
        timbreMap.put((float) 3.4, 127);

        return timbreMap;
    }

    // handler for turning note off
    Handler noteOffHandler = new Handler();
    private Runnable createNoteOffRunnable(final int note){
        Runnable noteOffRunnable = new Runnable(){
            public void run() {
                serialMidi.midi_note_off(channel, note, velocity);
            };
        };
        return noteOffRunnable;
    };
}
