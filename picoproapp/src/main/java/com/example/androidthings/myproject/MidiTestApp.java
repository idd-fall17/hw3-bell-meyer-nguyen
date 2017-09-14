package com.example.androidthings.myproject;

import com.example.androidthings.myproject.utils.SerialMidi;
import com.google.android.things.contrib.driver.mma8451q.Mma8451Q;

import android.util.FloatProperty;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

/**
 * Demo of the SerialMidi class
 * Created by bjoern on 9/12/17.
 */

public class MidiTestApp extends SimplePicoPro {
    SerialMidi serialMidi;
    int channel = 0;
    int velocity = 127; //0..127
    int timbre_value = 0;
    final int timbre_controller = 0x47;
    float velocityFloat;
    int curNote;
    int prevNote;

    Mma8451Q accelerometer;
    float[] xyz = {0.f,0.f,0.f};

    float force, light;

    @Override
    public void setup() {
        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);

        analogInit();

        createNoteHMap();


        try {
            accelerometer = new Mma8451Q("I2C1");
            accelerometer.setMode(Mma8451Q.MODE_ACTIVE);
        } catch (IOException e) {
            Log.e("MMA8451App","setup",e);
        }

    }

    private void createNoteHMap() {
        HashMap<Integer, Integer> noteMap = new HashMap<Integer, Integer>();
    }

    @Override
    public void loop() {

        force = analogRead(A0);
        light = analogRead(A1);
        int note;
        println("" + force + "");

        if(force < .5) {
            note = SerialMidi.MIDI_A4;
        } else if(force < 1){
            note = SerialMidi.MIDI_B4;
        } else if (force < 1.5) {
            note = SerialMidi.MIDI_C4;
        } else if (force < 2) {
            note = SerialMidi.MIDI_D4;
        } else if (force < 2.5) {
            note = SerialMidi.MIDI_E4;
        } else {
            //note = SerialMidi.MIDI_F4;
            note = -1;
        }

        //delay(50);
        if(note != prevNote) {
            serialMidi.midi_note_off(channel, note, velocity);
        }

        prevNote = note;
        //serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

        if(note > 0) {
            serialMidi.midi_note_on(channel, note, velocity);
        }
    }

    private void scratch() {
        try {
            xyz = accelerometer.readSample();
            //println("X: "+xyz[0]+"   Y: "+xyz[1]+"   Z: "+xyz[2]);
        } catch (IOException e) {
            Log.e("MMA8451App","loop",e);
        }


        velocityFloat = xyz[0] * 100;
        velocity = ((int) velocityFloat);
        println("   X: "+xyz[0]);

        delay(200);

        serialMidi.midi_note_on(channel,SerialMidi.MIDI_E4,velocity);
        delay(200);
        serialMidi.midi_note_off(channel,SerialMidi.MIDI_E4,velocity);
        delay(200);
        serialMidi.midi_note_on(channel,SerialMidi.MIDI_G4,velocity);
        delay(200);
        serialMidi.midi_note_off(channel,SerialMidi.MIDI_G4,velocity);
        delay(200);

        //
        timbre_value+=5;
        if(timbre_value>=127)
            timbre_value=0;
        serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

    }
}