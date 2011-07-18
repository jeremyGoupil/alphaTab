/*
 * This file is part of alphaTab.
 *
 *  alphaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  alphaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with alphaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alphatab.midi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.IllegalStateException;
import java.io.*;

import javax.swing.JOptionPane;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;

import netscape.javascript.JSObject;

public class MidiPlayer extends JApplet
{
    private Sequence          _sequence;
    private Sequencer         _sequencer;
    private long              _lastTick;
    private String            _updateFunction;
    private String            _jsInitFunction;
	private int 			  _metronomeTrack;
	private TickNotifierReceiver _tickReceiver;

    @Override
    public void init()
    {
        super.init();
        _updateFunction = getParameter("onTickChanged");
        _jsInitFunction = getParameter("onAppletLoaded");

        try
        {
            _sequencer = MidiSystem.getSequencer();
            _sequencer.open();

            Transmitter tickTransmitter = _sequencer.getTransmitter();
            _tickReceiver = new TickNotifierReceiver(
            tickTransmitter.getReceiver());
            tickTransmitter.setReceiver(_tickReceiver);

            _tickReceiver
                    .addControllerEventListener(new ControllerEventListener()
                    {
                        @Override
                        public void controlChange(ShortMessage event)
                        {
                            if (_sequencer.isRunning())
                            {
                                switch (event.getCommand())
                                {
                                    case 0x80:// Noteon
                                    case 0x90:// noteof
                                        notifyPosition(_sequencer
                                                .getTickPosition());
                                    break;
                                }
                            }
                        }
                    });
        	//This pulls up the javascript player overlay
        	JSObject.getWindow(this).call(_jsInitFunction, new String[0]);
        }
        catch (MidiUnavailableException e)
        {
            e.printStackTrace();
        }
    }

    private synchronized void notifyPosition(long tickPosition)
    {
        if (_lastTick == tickPosition || _updateFunction == null) return;
        try {
	        JSObject.getWindow(this).call(_updateFunction,
	                new String[] { new Long(tickPosition).toString() });
	       	_lastTick = tickPosition;
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
    }

    public synchronized void updateSongData(String commands)
    {
        try
        {
            _sequence = MidiSequenceParser.parse(commands);
            _sequencer.setSequence(_sequence);
			_metronomeTrack = MidiSequenceParser.getMetronomeTrack();
        }
        catch (Throwable e)
        {
            //JOptionPane.showMessageDialog(null,"MidiPlayer Error: \n" + e.toString());
			//e.printStackTrace();
        }
    }

	public synchronized void setMetronomeEnabled(boolean enabled)
	{
		_sequencer.setTrackMute(_metronomeTrack, !enabled);
	}

	public synchronized void isMetronomeEnabled()
	{
		_sequencer.getTrackMute(_metronomeTrack);
	}

    public synchronized void play()
    {
    	try {
        	_sequencer.start();
    	}
    	catch (IllegalStateException e) {
    		e.printStackTrace();
    	}
    }

    public synchronized void pause()
    {
    	try {
    		//Not sure if this helps or is neccesscary, sends the allSoundsOff command
	    	for(int i=0; i<15; i++) {
	    		ShortMessage allNotesOff = new ShortMessage();
	    		allNotesOff.setMessage(176+i,120,0);
				_tickReceiver.send(allNotesOff,-1);
	    	}
        	_sequencer.stop();
    	}
    	catch (IllegalStateException e) {
    		e.printStackTrace();
    	}
    	catch (InvalidMidiDataException ex) {
    		ex.printStackTrace();
    	}
    	catch (Exception ep) {
    		ep.printStackTrace();
    	}
    }

    public void stop()
    {
    	try {
    		this.pause();
			_sequencer.setTickPosition(0);
    	}
    	catch (IllegalStateException e) {
    		e.printStackTrace();
    	}
    }

	public synchronized boolean isRunning()
	{
		return _sequencer.isRunning();
	}

    public synchronized void goTo(int tickPosition) {
        _sequencer.stop();
        _sequencer.setTickPosition(tickPosition);
    }
}