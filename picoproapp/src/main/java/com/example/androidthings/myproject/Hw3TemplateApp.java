package com.example.androidthings.myproject;
import com.example.androidthings.myproject.utils.SerialMidi;
import android.os.Handler;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

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
    int channel = 0;
    int velocity = 127;
    int prevNote;
    int curNote;
    String prevLightState = "notCovered";
    NavigableMap noteMap;
    int noteLength = 2000;
    int noteBeingHeld;
    Runnable currNoteOffRunnable;
    List<Integer> notesOnList = new ArrayList<Integer>();

    // vars for sensor readings
    float force, light, flex;

    @Override
    public void setup() {

        // initialize new synthesizer
        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);

        // initialize the analogue readings
        analogInit();

        // create maps from analogue readings to synth note values
        noteMap = createNoteMap();
    }

    @Override
    public void loop() {

        // get analogue readings
        flex = analogRead(A0); // this is timbre
        force = analogRead(A1); // this is pitch
        light = analogRead(A2); // this is note ON/OFF

        delay(450);

        // translate flex sensor input to 0 to 127 range
        int timbreValue = Math.round(127 - 127 * (flex - (float) 2.4));
        timbreValue = timbreValue > 127 ? 127 : timbreValue;
        timbreValue = timbreValue < 0 ? 0 : timbreValue;
        serialMidi.midi_controller_change(channel, 77, timbreValue);

        // convert the force sensor value to a note
        int note = (int) noteMap.floorEntry(force).getValue();

        // cleanup: no note is being held now, so turn off all on notes unless held by light sensor
        if(note == -1 && prevNote != -1) {
            for (Iterator<Integer> i = notesOnList.iterator(); i.hasNext();) {
                currNoteOffRunnable = createNoteOffRunnable(i.next());
                noteOffHandler.postDelayed(currNoteOffRunnable, 2100);
                i.remove();
            }
            prevNote = note;
            return;
        }

        // turn on note (if diff from previous)
        if(note != -1 && note != noteBeingHeld && note != prevNote) {
            curNote = note;
            notesOnList.add(curNote);
            serialMidi.midi_note_on(channel, note, velocity); // turn on new note
            currNoteOffRunnable = createNoteOffRunnable(prevNote); // turn off last note
            noteOffHandler.postDelayed(currNoteOffRunnable, noteLength);
            prevNote = note;
        }

        // detect whether light sensor is covered
        String curLightState = light > 1 ? "covered" : "notCovered";

        // if light sensor is covered hold the note and keep track of the note being held
        if(curLightState == "covered" && prevLightState == "notCovered") {
            noteOffHandler.removeCallbacks(currNoteOffRunnable);
            noteBeingHeld = note;
        }

        // if light sensor cover is released then turn off held note
        if(curLightState == "notCovered" && prevLightState == "covered") {
            Runnable noteOffRunnable = createNoteOffRunnable(noteBeingHeld);
            noteBeingHeld = -1;
            noteOffHandler.postDelayed(noteOffRunnable, noteLength);
        }

        // track light changes
        prevLightState = curLightState;
    }

    // map ranges to values in a classic (abridged) scale
    private NavigableMap<Float, Integer> createNoteMap() {
        NavigableMap<Float, Integer> noteMap = new TreeMap<Float, Integer>();
        noteMap.put((float) 0.0, SerialMidi.MIDI_C4);
        noteMap.put((float) 0.4, SerialMidi.MIDI_E4);
        noteMap.put((float) 0.7, SerialMidi.MIDI_G4);
        noteMap.put((float) 1.0, SerialMidi.MIDI_B5);
        noteMap.put((float) 1.5, SerialMidi.MIDI_A5);
        noteMap.put((float) 3.3, -1);
        return noteMap;
    }

    // handler for turning note off
    Handler noteOffHandler = new Handler();
    private Runnable createNoteOffRunnable(final int note){
        Runnable noteOffRunnable = new Runnable(){
            public void run() {
                if(noteBeingHeld == note) return; // never turn off note that is being held on purpose
                serialMidi.midi_note_off(channel, note, velocity);
            };
        };
        return noteOffRunnable;
    };
}
