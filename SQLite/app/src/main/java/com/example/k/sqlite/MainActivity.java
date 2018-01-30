package com.example.k.sqlite;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Bluetooth On/Off
    Boolean bt = false;

    //Spinner value
    String spinner_author ="";

    //Uri
    TextView tvuri;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;

    //create dbhelper
    DBHelper dbHelper;

    public static Context mContext;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    //데이터 주고받기 위한 어레이 리스트
    ArrayList<Item> tmp_item = new ArrayList<>();

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = MainActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            bt=true;
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            bt = false;
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf);
                    dbHelper.checkBoxing(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    //save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private void setStatus(int resId) {
        Activity activity = this;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        Activity activity = this;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //Bluetooth Adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //BluetoothService
        mBluetoothService = new BluetoothService(this,mHandler);

        dbHelper = new DBHelper(getApplicationContext(), "BOOK.db", null, 2);

        //connect listview and listviewadapter
        final ArrayList<Item> items = new ArrayList<>();
        final ListViewAdapter adapter = new ListViewAdapter(items, getApplicationContext());
        final ListView listview = (ListView) findViewById(R.id.list_db);
        listview.setAdapter(adapter);

        final TextView tvdate = (TextView)findViewById(R.id.time_text);
        final EditText ettitle = (EditText)findViewById(R.id.title);
        final EditText etcontnet  = (EditText)findViewById(R.id.content);
        tvuri = (TextView)findViewById(R.id.uri);

        //time to set now
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //time format
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/DD HH시mm분");
        tvdate.setText(simpleDateFormat.format(date));

        //Spinner
        // String get value about position
        Spinner s = (Spinner)findViewById(R.id.spinner);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner_author = ""+parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /*
        * create button
        * */
        //time update button
        Button btn_tupdate = (Button)findViewById(R.id.time);
        btn_tupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                final SimpleDateFormat simpleDate = new SimpleDateFormat("MM/DD HH시mm분");
                tvdate.setText(simpleDate.format(date));
            }
        });

        // add button (add to DB)
        Button btn_add = (Button)findViewById(R.id.add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = tvdate.getText().toString();
                String title = ettitle.getText().toString();
                String content = etcontnet.getText().toString();
                String uri = tvuri.getText().toString();
                dbHelper.insert(date,title,content,spinner_author,uri);
                dbHelper.getResult(items,adapter);
                //reset all text
                ettitle.setText(" ");
                etcontnet.setText(" ");
                tvuri.setText(" ");
            }
        });

        //check button (check to DB)
        Button btn_check = (Button)findViewById(R.id.check);
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.getResult(items, adapter);
                tmp_item.clear();
            }
        });

        //find file uri button
        Button btn_uplord = (Button)findViewById(R.id.uplord);
        btn_uplord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                startActivityForResult(intent,0);
            }
        });

        //data sync
        Button btn_sync = (Button)findViewById(R.id.sync);
        btn_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.sendBoxing("start");
            }
        });

        //delete All DB
        Button btn_dAll = (Button)findViewById(R.id.delAll);
        btn_dAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteAll();
            }
        });

        //아이템 수정/삭제 List목록에서 해당 아이템을 클릭하면 반응 할 수 있게 만듬
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setMessage("어떤 작업을 진행하시겠습니까?");
                alert.setPositiveButton("데이터 수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.update(items.get(position).date,items.get(position).title,items.get(position).content,items.get(position).author,tvuri.getText().toString());
                        dbHelper.getResult(items,adapter);
                    }
                });
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alert.setNeutralButton("데이터 삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.delete(items.get(position).date,items.get(position).title,items.get(position).content,items.get(position).author,tvuri.getText().toString());
                        dbHelper.getResult(items,adapter);
                        return;
                    }
                });
                AlertDialog a = alert.create();
                a.show();
            }
        });
    }

    /*
    * if start app off the bluetooth
    *  turn on the bluetooth
    *  */
    public void onStart(){
        super.onStart();

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        } else {
            if(mBluetoothService == null){

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothService.start();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 0:
                if(resultCode == Activity.RESULT_OK){
                    tvuri.setText(data.getData().toString());
                } else {}
                break;
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    connectDevice(data,true);
                } break;
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(this,"블루투스를 켰습니다.",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"블루투스가 연결되지 않았습니다.",Toast.LENGTH_SHORT).show();
                    return;
                } break;
        }
    }

    //디바이스 연결하기
    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothService.connect(device,secure);
    }

    //디바이스를 찾기 가능한 상태로 만들기기
    private void ensureDiscoverable() {
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);
        }
    }

    //아이템 보내기
    public void sendMessage (String message){
        if(mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this,"블루투스가 연결되어 있지 않습니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!message.equals(null)){
            try {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mBluetoothService.write(send);
            } catch (Exception e) {
                Toast.makeText(this,"동기화에 실패했습니다. 다시 시도해주세요",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //make menu in Actionbar
    //액션버튼 메뉴 액션바에 집어 넣기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //액션버튼을 클릭했을때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        //메뉴의 디바이스 찾기 버튼
        if (id == R.id.action_find) {
            Intent serverIntent = new Intent(MainActivity.this,DeviceListActivity.class);
            startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
            return true;
        }
        //메뉴의 디바이스 활성화 버튼
        if (id == R.id.action_stay) {
            ensureDiscoverable();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
