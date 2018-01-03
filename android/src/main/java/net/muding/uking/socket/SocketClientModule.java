package net.muding.uking.socket;

import android.util.Log;
import android.support.annotation.Nullable;
//import java.io.IOException;

import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.NetworkInterface;
//import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.GuardedAsyncTask;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.bmdelacruz.socketify.client.Client;

/**
 * Created by uking 2017-12-26
 */
public class SocketClientModule extends ReactContextBaseJavaModule {
    private final String eTag = "NIOSocket";
    private final String EVENT_DISCONNECT = "NOISOCKET_CLIENT_DISCONNECT";
    private final String EVENT_RECIVE = "NOISOCKET_CLIENT_RECIVE";
    private final String EVENT_CONNECT = "NOISOCKET_CLIENT_CONNECT";
    private ReactContext mReactContext;

    Client client;

    public SocketClientModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
    }
    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
    @Override
    public void onCatalystInstanceDestroy() {
        try {
            new GuardedAsyncTask<Void, Void>(getReactApplicationContext()) {
                @Override
                protected void doInBackgroundGuarded(Void... params) {
                    try{
                        if (client != null) {
                            client.disconnect();
                            client=null;
                        }
                    }catch( IOException ee){
                        ee.printStackTrace();
                    }
                }
            }.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException es){
            es.printStackTrace();
        }
    }


    @ReactMethod
    public void connect(ReadableMap config) {
        try{
            if(config.hasKey("host")&&config.hasKey("port")){
                client =new Client(config.getInt("port"),config.getString("host"));
                client.setReconnect(config.hasKey("reconnect")&&config.getBoolean("reconnect"));
                client.setListener(new Client.Listener() {
                    @Override
                    public void onDataReceived(byte[] data) {
                        WritableMap eventParams = Arguments.createMap();
                        eventParams.putString("data", new String(data));
                        sendEvent(mReactContext,EVENT_RECIVE,eventParams);
                    }

                    @Override
                    public void onServerDisconnect() {
                        WritableMap eventParams = Arguments.createMap();
                        sendEvent(mReactContext,EVENT_DISCONNECT,eventParams);
                    }
                    @Override
                    public void onServerConnect(String msg){
                        WritableMap eventParams = Arguments.createMap();
                        eventParams.putString("data", msg);
                        sendEvent(mReactContext,EVENT_CONNECT,eventParams);
                    }
                });
                client.connect();
            }else{
                WritableMap eventParams = Arguments.createMap();
                eventParams.putString("data", "invalid parameter");
                sendEvent(mReactContext,EVENT_CONNECT,eventParams);
            }
        }catch(IOException e){
            //e.printStackTrace();
        }
    }

    @ReactMethod
    public void disconnect() {
        try{
            if (client != null) {
                client.disconnect();
                client = null;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void send(String msg) {
        try{
            if (client != null&&msg!="") {
                client.sendBytes(msg.getBytes());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public String getName() {
        return eTag;
    }

    public void onDestroy() {
        try{
            if (client != null) {
                client.disconnect();
                client=null;
            }
        }catch( IOException e){
            e.printStackTrace();
        }
    }
}