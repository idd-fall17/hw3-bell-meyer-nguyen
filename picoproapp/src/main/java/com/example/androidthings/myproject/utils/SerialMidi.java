package com.example.androidthings.myproject.utils;

import android.util.Log;

import com.google.android.things.pio.UartDevice;

import java.io.IOException;

/**
 * Created by bjoern on 9/12/17.
 * Adapted from Ardumidi
 */

public class SerialMidi {

    // MIDI notes
    public static final int MIDI_OCTAVE =      12;

    public static final int MIDI_C0     =       0;
    public static final int MIDI_D0     =       2;
    public static final int MIDI_E0     =       4;
    public static final int MIDI_F0     =       5;
    public static final int MIDI_G0     =       7;
    public static final int MIDI_A0     =       9;
    public static final int MIDI_B0     =      11;

    public static final int MIDI_C1     =      MIDI_C0+(MIDI_OCTAVE*1);
    public static final int MIDI_D1     =      MIDI_D0+(MIDI_OCTAVE*1);
    public static final int MIDI_E1     =      MIDI_E0+(MIDI_OCTAVE*1);
    public static final int MIDI_F1     =      MIDI_F0+(MIDI_OCTAVE*1);
    public static final int MIDI_G1     =      MIDI_G0+(MIDI_OCTAVE*1);
    public static final int MIDI_A1     =      MIDI_A0+(MIDI_OCTAVE*1);
    public static final int MIDI_B1     =      MIDI_B0+(MIDI_OCTAVE*1);

    public static final int MIDI_C2     =      MIDI_C0+(MIDI_OCTAVE*2);
    public static final int MIDI_D2     =      MIDI_D0+(MIDI_OCTAVE*2);
    public static final int MIDI_E2     =      MIDI_E0+(MIDI_OCTAVE*2);
    public static final int MIDI_F2     =      MIDI_F0+(MIDI_OCTAVE*2);
    public static final int MIDI_G2     =      MIDI_G0+(MIDI_OCTAVE*2);
    public static final int MIDI_A2     =      MIDI_A0+(MIDI_OCTAVE*2);
    public static final int MIDI_B2     =      MIDI_B0+(MIDI_OCTAVE*2);

    public static final int MIDI_C3     =      MIDI_C0+(MIDI_OCTAVE*3);
    public static final int MIDI_D3     =      MIDI_D0+(MIDI_OCTAVE*3);
    public static final int MIDI_E3     =      MIDI_E0+(MIDI_OCTAVE*3);
    public static final int MIDI_F3     =      MIDI_F0+(MIDI_OCTAVE*3);
    public static final int MIDI_G3     =      MIDI_G0+(MIDI_OCTAVE*3);
    public static final int MIDI_A3     =      MIDI_A0+(MIDI_OCTAVE*3);
    public static final int MIDI_B3     =      MIDI_B0+(MIDI_OCTAVE*3);

    public static final int MIDI_C4     =      MIDI_C0+(MIDI_OCTAVE*4);
    public static final int MIDI_D4     =      MIDI_D0+(MIDI_OCTAVE*4);
    public static final int MIDI_E4     =      MIDI_E0+(MIDI_OCTAVE*4);
    public static final int MIDI_F4     =      MIDI_F0+(MIDI_OCTAVE*4);
    public static final int MIDI_G4     =      MIDI_G0+(MIDI_OCTAVE*4);
    public static final int MIDI_A4     =      MIDI_A0+(MIDI_OCTAVE*4);
    public static final int MIDI_B4     =      MIDI_B0+(MIDI_OCTAVE*4);

    public static final int MIDI_C5     =      MIDI_C0+(MIDI_OCTAVE*5);
    public static final int MIDI_D5     =      MIDI_D0+(MIDI_OCTAVE*5);
    public static final int MIDI_E5     =      MIDI_E0+(MIDI_OCTAVE*5);
    public static final int MIDI_F5     =      MIDI_F0+(MIDI_OCTAVE*5);
    public static final int MIDI_G5     =      MIDI_G0+(MIDI_OCTAVE*5);
    public static final int MIDI_A5     =      MIDI_A0+(MIDI_OCTAVE*5);
    public static final int MIDI_B5     =      MIDI_B0+(MIDI_OCTAVE*5);

    public static final int MIDI_C6     =      MIDI_C0+(MIDI_OCTAVE*6);
    public static final int MIDI_D6     =      MIDI_D0+(MIDI_OCTAVE*6);
    public static final int MIDI_E6     =      MIDI_E0+(MIDI_OCTAVE*6);
    public static final int MIDI_F6     =      MIDI_F0+(MIDI_OCTAVE*6);
    public static final int MIDI_G6     =      MIDI_G0+(MIDI_OCTAVE*6);
    public static final int MIDI_A6     =      MIDI_A0+(MIDI_OCTAVE*6);
    public static final int MIDI_B6     =      MIDI_B0+(MIDI_OCTAVE*6);

    public static final int MIDI_SHARP  =       1;
    public static final int MIDI_FLAT   =      -1;

    // MIDI out
    public static final int MIDI_NOTE_OFF  =        0x80;
    public static final int MIDI_NOTE_ON   =        0x90;
    public static final int MIDI_PRESSURE  =        0xA0;
    public static final int MIDI_CONTROLLER_CHANGE = 0xB0;
    public static final int MIDI_PROGRAM_CHANGE    = 0xC0;
    public static final int MIDI_CHANNEL_PRESSURE  = 0xD0;
    public static final int MIDI_PITCH_BEND        = 0xE0;

    private UartDevice mPort;

    public SerialMidi(UartDevice port) {
        mPort = port;
    }

    public void midi_note_off(int channel, int key, int velocity)
    {
        midi_command(MIDI_NOTE_OFF, channel, key, velocity);
    }

    public void midi_note_on(int channel, int key, int velocity)
    {
        midi_command(MIDI_NOTE_ON, channel, key, velocity);
    }

    public void midi_key_pressure(int channel, int key, int value)
    {
        midi_command(MIDI_PRESSURE, channel, key, value);
    }

    public void midi_controller_change(int channel, int control, int value)
    {
        midi_command(MIDI_CONTROLLER_CHANGE, channel, control, value);
    }

    public void midi_program_change(int channel, int program)
    {
        midi_command_short(MIDI_PROGRAM_CHANGE, channel, program);
    }

    public void midi_channel_pressure(int channel, int value)
    {
        midi_command_short(MIDI_CHANNEL_PRESSURE, channel, value);
    }

    public void midi_pitch_bend(int channel, int value)
    {
        midi_command(MIDI_PITCH_BEND, channel, value & 0x7F, value >> 7);
    }

    public void midi_command(int command, int channel, int param1, int param2)
    {
        assert(0<=command && 255>=command);
        assert(0<=channel && 15>=channel);
        assert(0<=param1 && 127>=param1);
        assert(0<=param2 && 127>=param2);
        byte[] message = {(byte)(command | (channel & 0x0F)),(byte)(param1 & 0x7F),(byte)(param2 & 0x7F) };
        try {
            mPort.write(message,message.length);
            /*Log.i("SerialMidi","C:"+command+" Ch:"+channel+" P1:"+param1+"P2:"+param2);
            Log.i("SerialMidi",String.valueOf((int)message[0]));
            Log.i("SerialMidi",String.valueOf((int)message[1]));
            Log.i("SerialMidi",String.valueOf((int)message[2]));

            */
        } catch (IOException e) {
            Log.e("SerialMidi","midi_command",e);
        }

    }

    public void midi_command_short(int command, int channel, int param1)
    {
        assert(0<=command && 255>=command);
        assert(0<=channel && 15>=channel);
        assert(0<=param1 && 127>=param1);
        byte[] message = {(byte)(command | (channel & 0x0F)),(byte)(param1 & 0x7F)};
        try {
            mPort.write(message,message.length);
        } catch (IOException e) {
            Log.e("SerialMidi","midi_command_short",e);
        }
    }



}