package net.muding.uking.socket;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.GuardedAsyncTask;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.bmdelacruz.socketify.client.Client;

/**
 * Created by uking 2017-12-26
 */
public class SocekClientModule extends ReactContextBaseJavaModule {
    private final String eTag = "NIOSocket";
    private final String EVENT_DISCONNECT = "NOISOCKET_CLIENT_DISCONNECT";
    private final String EVENT_RECIVE = "NOISOCKET_CLIENT_RECIVE";
    private final String EVENT_CONNECT = "NOISOCKET_CLIENT_CONNECT";
    private ReactContext mReactContext;

    Client client;

    public SocekClientModule(ReactApplicationContext reactContext) {
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
                    if (client != null) {
                        client.disconnect(false);
                    }
                }
            }.execute().get();
        } catch (InterruptedException ioe) {
            Log.e(eTag, "onCatalystInstanceDestroy", ioe);
        } catch (ExecutionException ee) {
            Log.e(eTag, "onCatalystInstanceDestroy", ee);
        }
    }


    @ReactMethod
    public void connect(String ip,String port) {
        client =new Client(port,ip);
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
        });
        client.connect();
        if(client&&client.isConnected()){
            WritableMap eventParams = Arguments.createMap();
            sendEvent(mReactContext,EVENT_CONNECT,eventParams);
        }
    }

    @ReactMethod
    public void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
    }

    @ReactMethod
    public void send(String msg) {
        if (client != null&&msg!="") {
            client.sendBytes(msg.getBytes());
        }
    }

    @Override
    public String getName() {
        return eTag;
    }
}