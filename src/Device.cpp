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

#include <assert.h>
#include <errno.h>


#include <zmq.h>

#include "jzmq.hpp"
#include "util.hpp"
#include "org_zeromq_ZMQ_Device.h"


static void *fetch_socket (JNIEnv *env,
                           jobject socket);


/**
 * Called to construct a Java Device object.
 */
JNIEXPORT void JNICALL Java_org_zeromq_ZMQ_00024Device_construct (JNIEnv *env,
                                                                   jobject obj,
                                                                   jint type,
                                                                   jobject insocket_,
                                                                   jobject outsocket_)
{
    switch (type) {
    case ZMQ_STREAMER:
    case ZMQ_FORWARDER:
    case ZMQ_QUEUE:
        {          
            void *i = fetch_socket (env, insocket_);
                if (i == NULL) {
                    raise_exception (env, EINVAL);
                    return;
                }
                
            void *o = fetch_socket (env, outsocket_);
                if (o == NULL) {
                    raise_exception (env, EINVAL);
                    return;
                }

            int rc = zmq_device (type, i, o);
            int err = errno;
            if (rc != 0) {
                raise_exception (env, err);
                return;
            }

            return;
        }
    default:
        raise_exception (env, EINVAL);
        return;
    }
}


/**
 * Get the value of socketHandle for the specified Java Socket.
 */
static void *fetch_socket (JNIEnv *env,
                           jobject socket)
{
    static jmethodID get_socket_handle_mid = NULL;

    if (get_socket_handle_mid == NULL) {
        jclass cls = env->GetObjectClass (socket);
        assert (cls);
        get_socket_handle_mid = env->GetMethodID (cls,
            "getSocketHandle", "()J");
        env->DeleteLocalRef (cls);
        assert (get_socket_handle_mid);
    }
  
    void *s = (void*) env->CallLongMethod (socket, get_socket_handle_mid);
    if (env->ExceptionCheck ()) {
        s = NULL;
    }
  
    return s;
}
