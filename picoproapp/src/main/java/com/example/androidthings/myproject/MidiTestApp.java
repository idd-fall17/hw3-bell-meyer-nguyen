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
    int timbreValue = 0;
    int prevTimbre;
    final int timbre_controller = 0x47;
    int prevNote;
    String prevLightState;
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
        force = analogRead(A0); // this is pitch
        light = analogRead(A1); // this is note ON/OFF
        flex = analogRead(A3); // this is timbre
        //print("FORCE: " + force);
        //print("LIGHT: " + light);

        delay(300); // is this the right delay?

        // do something with the flex
        /*
        int timbreValue = (int) timbreMap.floorEntry(flex).getValue();
        // timbre value goes from 0 to 127

        // check for timbre change
        if(prevTimbre != timbreValue) {
            serialMidi.midi_controller_change(channel, timbre_controller, timbreValue);
            prevTimbre = timbreValue;
        }
        */

        // convert the force sensor value to a note
        int note = (int) noteMap.floorEntry(force).getValue();

        // turn on note for 2s (if diff from previous)
        if(note != prevNote && note != -1) {
            serialMidi.midi_note_on(channel, note, velocity);
            currNoteOffRunnable = createNoteOffRunnable(note);
            noteOffHandler.postDelayed(currNoteOffRunnable, noteLength);
            prevNote = note;
        }

        // detect whether light sensor is covered
        String curLightState = light > .5 ? "covered" : "notCovered";

        // if light sensor is covered hold the note and keep track of the note being held
        if(curLightState == "covered" && prevLightState == "notCovered") {
            noteOffHandler.removeCallbacks(currNoteOffRunnable);
            noteToHold = note;
        }

        // if light sensor cover is released then turn off held note
        if(curLightState == "Notcovered") {
            Runnable noteOffRunnable = createNoteOffRunnable(noteToHold);
            noteOffHandler.postDelayed(noteOffRunnable, noteLength);
        }
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