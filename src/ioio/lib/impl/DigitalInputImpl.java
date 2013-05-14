/*
 * Copyright 2011 Ytai Ben-Tsvi. All rights reserved.
 *  
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ARSHAN POURSOHI OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied.
 */
package ioio.lib.impl;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.impl.IncomingState.InputPinListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

class DigitalInputImpl extends AbstractPin implements DigitalInput,
		InputPinListener {
	private boolean value_;
	private boolean valid_ = false;

	DigitalInputImpl(IOIOImpl ioio, int pin) throws ConnectionLostException {
		super(ioio, pin);
	}

	
	
	@Override
	synchronized public void setValue(int value) {
		// This is step 3 part 2
		firePropertyChanged("value",value_,value);
		//Log.v("DigitalInputImpl", "Pin " + pinNum_ + " value is " + value);
		assert (value == 0 || value == 1);
		value_ = (value == 1);
		if (!valid_) {
			valid_ = true;
		}
		notifyAll();
	}

	@Override
	synchronized public void waitForValue(boolean value)
			throws InterruptedException, ConnectionLostException {
		checkState();
		while ((!valid_ || value_ != value) && state_ != State.DISCONNECTED) {
			wait();
		}
		checkState();
	}

	@Override
	synchronized public void close() {
		super.close();
		try {
			ioio_.protocol_.setChangeNotify(pinNum_, false);
		} catch (IOException e) {
		}
	}

	@Override
	synchronized public boolean read() throws InterruptedException,
			ConnectionLostException {
		checkState();
		while (!valid_ && state_ != State.DISCONNECTED) {
			wait();
		}
		checkState();
		return value_;
	}

	@Override
	public synchronized void disconnected() {
		super.disconnected();
		notifyAll();
	}
	
	
	
	/*
	 * This is the start of step 3
	 * 
	 */
	private final List<Listener> listeners = new LinkedList<Listener>();

    protected final <T> void firePropertyChanged(final String property,
            final T oldValue, final T newValue) {
        assert(property != null);
        if((oldValue != null && oldValue.equals(newValue))
                || (oldValue == null && newValue == null))
            return;
        for(final Listener listener : this.listeners) {
            try {
                if(listener.getProperties().contains(property))
                	//System.out.println("property changed");
                    listener.propertyChanged(property, oldValue, newValue);
            } catch(Exception ex) {
             System.out.println(ex.getMessage());
                // log these, to help debugging
                ex.printStackTrace();
            }
        }
    }
    @Override
    synchronized public final boolean addListener(final Listener x) {
        if(x == null) return false;
        return this.listeners.add(x);
    }
    @Override
    synchronized public final boolean removeListener(final Listener x) {
        return this.listeners.remove(x);
    }
    
    /*
     * This is the end of step 3
     * 
     */
}
