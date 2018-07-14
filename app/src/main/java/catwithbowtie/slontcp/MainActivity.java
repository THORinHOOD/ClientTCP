package catwithbowtie.slontcp;

import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private PrintWriter out;
    private DataInputStream in;

    private static final int SERVERPORT = 8093;
    private static final String SERVER_IP = "92.63.105.60";

    private TextView chat;

    private EditText login;
    private EditText password;
    private EditText name;

    private EditText message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chat = (TextView) findViewById(R.id.textView);
        login = (EditText) findViewById(R.id.lgn);
        password = (EditText) findViewById(R.id.pswd);
        name = (EditText) findViewById(R.id.name);
        message = (EditText) findViewById(R.id.msg);

        new Thread(new ClientThread()).start();
    }

    public void addMessage(final String login, final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                chat.setText(chat.getText() + "\n" + login + " : " + msg);
            }
        });
    }

    public void send_msg(View view) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (out != null) {
                        Log.d("MyDebug", "send msg");

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", "mes");
                        jsonObject.put("login", login.getText().toString());
                        jsonObject.put("recipient", "all");
                        jsonObject.put("message", message.getText().toString());

                        out.println(jsonObject);
                        out.flush();
                    }
                } catch(Exception ex) {
                    Log.d("MyDebug", ex.getMessage());
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void registration(View view) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (out != null) {
                        Log.d("MyDebug", "registration");

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", "reg");
                        jsonObject.put("login", login.getText().toString());
                        jsonObject.put("password", password.getText().toString());
                        jsonObject.put("name", name.getText().toString());

                        out.println(jsonObject);
                        out.flush();
                    }
                } catch(Exception ex) {
                    Log.d("MyDebug", ex.getMessage());
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("MyDebug", "connected");
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), false);
                in = new DataInputStream(socket.getInputStream());

                while (true) {
                    byte[] fromServer = new byte[1024];
                    in.read(fromServer);
                    JSONObject msg = new JSONObject(convertFromByteToString(fromServer));
                    addMessage(msg.getString("sender"), msg.getString("message"));
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertFromByteToString(byte[] bytes) throws UnsupportedEncodingException {
        int size = 0;
        while (size < bytes.length)
        {
            if (bytes[size] == 0)
                break;
            ++size;
        }
        return new String(bytes, 0, size, "UTF-8");
    }
}
