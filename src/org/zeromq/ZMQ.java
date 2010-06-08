/*
    Copyright (c) 2007-2010 iMatix Corporation

    This file is part of 0MQ.

    0MQ is free software; you can redistribute it and/or modify it under
    the terms of the Lesser GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    0MQ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Lesser GNU General Public License for more details.

    You should have received a copy of the Lesser GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.zeromq;

public class ZMQ {
    static {
        System.loadLibrary("jzmq");
    }

    // Values for flags in Socket's send and recv functions.
    public static final int NOBLOCK = 1;

    // Socket types, used when creating a Socket.
    public static final int PAIR = 0;
    public static final int PUB = 1;
    public static final int SUB = 2;
    public static final int REQ = 3;
    public static final int REP = 4;
    public static final int XREQ = 5;
    public static final int XREP = 6;
    public static final int UPSTREAM = 7;
    public static final int DOWNSTREAM = 8;

    // Values for setsockopt.
    public static final int HWM = 1;
    public static final int SWAP = 3;
    public static final int AFFINITY = 4;
    public static final int IDENTITY = 5;
    public static final int SUBSCRIBE = 6;
    public static final int UNSUBSCRIBE = 7;
    public static final int RATE = 8;
    public static final int RECOVERY_IVL = 9;
    public static final int MCAST_LOOP = 10;
    public static final int SNDBUF = 11;
    public static final int RCVBUF = 12;

    // Send/Recv
    public static final int NOBLOCK = 1;
    public static final int SNDMORE = 2;

    // Polling types for a Poller.
    public static final int POLLIN = 1;
    public static final int POLLOUT = 2;
    public static final int POLLERR = 4;

	// Devices - Experimental.                                                   

	public static final int STREAMER = 1;
	public static final int FORWARDER = 2;
	public static final int QUEUE = 3;
	
    public static Context context(int ioThreads) {
        return new Context (ioThreads);
    }
    
    public static Device device(int type,
                                Socket in_socket,
                                Socket out_socket) {
        return new Device (type, in_socket, out_socket);
    }

    /**
     * Inner class: Context.
     */
    public static class Context {

        /**
         * This is an explicit "destructor".  It can be called to ensure
         * the corresponding 0MQ Context has been disposed of.
         */
        public void term () {
            finalize ();
        }

        /**
         * Create a new Socket within this context.
         *
         * @param type the socket type.
         * @return the newly created Socket.
         */
        public Socket socket(int type) {
            return new Socket (this, type);
        }

        /**
         * Create a new Poller within this context.
         *
         * @param size the poller size.
         * @return the newly created Poller.
         */
        public Poller poller(int size) {
            return new Poller (this, size);
        }


        /**
         * Class constructor.
         *
         * @param ioThreads size of the threads pool to handle I/O
         * operations.
         */
        protected Context (int ioThreads) {
            construct (ioThreads);
        }

        /** Initialize the JNI interface */
        protected native void construct (int ioThreads);

        /** Free all resources used by JNI interface. */
        protected native void finalize ();

        /**
         * Get the underlying context handle.
         * This is private because it is only accessed from JNI, where
         * Java access controls are ignored.
         *
         * @return the internal 0MQ context handle.
         */
        private long getContextHandle () {
            return contextHandle;
        }

        /** Opaque data used by JNI driver. */
        private long contextHandle;
    }



    /**
     * Inner class: Socket.
     */
    public static class Socket {
        
        /**
         * This is an explicit "destructor".  It can be called to
         * ensure the corresponding 0MQ Socket has been disposed of.
         */
        public void close () {
            finalize ();
        }
        
        /**
         * Set the socket option value, given as a long.
         *
         * @param option ID of the option to set.
         * @param optval value (as a long) to set the option to.
         */
        public native void setsockopt (int option,
                                       long optval);

        /**
         * Set the socket option value, given as a String.
         *
         * @param option ID of the option to set.
         * @param optval value (as a String) to set the option to.
         */
        public native void setsockopt (int option,
                                       String optval);
        
        /**
         * Bind to network interface. Start listening for new
         * connections.
         *
         * @param addr the endpoint to bind to.
         */
        public native void bind (String addr);

        /**
         * Connect to remote application.
         *
         * @param addr the endpoint to connect to.
         */
        public native void connect (String addr);
        
        /**
         * Send a message.
         *
         * @param msg the message to send, as an array of bytes.
         * @param flags the flags to apply to the send operation.
         * @return true if send was successful, false otherwise.
         */
        public native boolean send (byte [] msg,
                                    long flags);
        
        /**
         * Receive a message.
         *
         * @param flags the flags to apply to the receive operation.
         * @return the message received, as an array of bytes; null on error.
         */
        public native byte [] recv (long flags);


        /**
         * Class constructor.
         *
         * @param context a 0MQ context previously created.
         * @param type the socket type.
         */
        protected Socket (Context context,
                          int type) {
            construct (context, type);
        }

        /** Initialize the JNI interface */
        protected native void construct (Context context,
                                         int type);

        /** Free all resources used by JNI interface. */
        protected native void finalize ();

        /**
         * Get the underlying socket handle.
         * This is private because it is only accessed from JNI, where
         * Java access controls are ignored.
         *
         * @return the internal 0MQ socket handle.
         */
        private long getSocketHandle () {
            return socketHandle;
        }

        /** Opaque data used by JNI driver. */
        private long socketHandle;
    }


    /**
     * Inner class: Poller.
     */
    public static class Poller {

        /**
         * Register a Socket for polling on all events.
         *
         * @param socket the Socket we are registering.
         * @return the index identifying this Socket in the poll set.
         */
        public int register (Socket socket) {
            return register(socket, POLLIN | POLLOUT | POLLERR);
        }
    
        /**
         * Register a Socket for polling on the specified events.
         *
         * @param socket the Socket we are registering.
         * @param events a mask composed by XORing POLLIN, POLLOUT and
         * POLLERR.
         * @return the index identifying this Socket in the poll set.
         */
        public int register (Socket socket,
                             int events) {
            if (next >= size)
                return -1;
            this.socket[next] = socket;
            this.event[next] = (short) events;
            return next++;
        }
    
        /**
         * Get the current poll timeout.
         *
         * @return the current poll timeout in ms.
         */
        public long getTimeout () {
            return this.timeout;
        }
    
        /**
         * Set the poll timeout.
         *
         * @param timeout the desired poll timeout in ms.
         */
        public void setTimeout (long timeout) {
            this.timeout = timeout;
        }

        /**
         * Get the current poll set size.
         *
         * @return the current poll set size.
         */
        public int getSize () {
            return this.size;
        }

        /**
         * Get the index for the next position in the poll set size.
         *
         * @return the index for the next position in the poll set
         * size.
         */
        public int getNext () {
            return this.next;
        }

        /**
         * Issue a poll call.
         * @return how many objects where signalled by poll().
         */
        public long poll () {
            if (size <= 0 || next <= 0)
                return 0;

            for (int i = 0; i < next; ++i) {
                revent[i] = 0;
            }

            return run_poll(next, socket, event, revent, timeout);
        }

        /**
         * Check whether the specified element in the poll set was
         * signalled for input.
         *
         * @return true if the element was signalled.
         */
        public boolean pollin(int index) {
            return poll_mask(index, POLLIN);
        }

        /**
         * Check whether the specified element in the poll set was
         * signalled for output.
         *
         * @return true if the element was signalled.
         */
        public boolean pollout(int index) {
            return poll_mask(index, POLLOUT);
        }

        /**
         * Check whether the specified element in the poll set was
         * signalled for error.
         *
         * @return true if the element was signalled.
         */
        public boolean pollerr(int index) {
            return poll_mask(index, POLLERR);
        }


        /**
         * Class constructor.
         *
         * @param context a 0MQ context previously created.
         * @param size the number of Sockets this poller will contain.
         */
        protected Poller (Context context,
                          int size) {
            this.context = context;
            this.size = size;
            this.next = 0;
            
            this.socket = new Socket[size];
            this.event = new short[size];
            this.revent = new short[size];
        }

        /**
         * Issue a poll call on the specified 0MQ sockets.
         *
         * @param socket an array of 0MQ Socket objects to poll.
         * @param event an array of short values specifying what to
         * poll for.
         * @param revent an array of short values with the results.
         * @param timeout the maximum timeout in microseconds.
         * @return how many objects where signalled by poll().
         */
        private native long run_poll(int count,
                                     Socket[] socket,
                                     short[] event,
                                     short[] revent,
                                     long timeout);

        /**
         * Check whether a specific mask was signalled by latest poll
         * call.
         *
         * @param index the index indicating the socket.
         * @param mask a combination of POLLIN, POLLOUT and POLLERR.
         * @return true if specific socket was signalled as specified.
         */
        private boolean poll_mask(int index,
                                  int mask) {
            if (mask <= 0 || index < 0 || index >= next)
                return false;
            return (revent[index] & mask) > 0;
        }

        private Context context = null;
        private long timeout = 0;
        private int size = 0;
        private int next = 0;
        private Socket[] socket = null;
        private short[] event = null;
        private short[] revent = null;
    }
    
        /**
     * Inner class: Device.
     */
    public static class Device {
    
        /**
         * Class constructor.
         *
         * @param type a Device type .
         * @param in_socket a 0MQ Socket previously created.
         * @param out_socket a 0MQ Socket previously created.
         */
        protected Device (int type,
                          Socket in_socket,
                          Socket out_socket) {
            construct (type, in_socket, out_socket);
        }

        /** Initialize the JNI interface */
        protected native void construct (int type,
                                         Socket in_socket, 
                                         Socket out_socket);
    }
}
