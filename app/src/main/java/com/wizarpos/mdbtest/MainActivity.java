package com.wizarpos.mdbtest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cloudpos.DeviceException;
import com.cloudpos.OperationListener;
import com.cloudpos.OperationResult;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;
import com.cloudpos.card.Card;
import com.cloudpos.jniinterface.StmGpioInterface;
import com.cloudpos.rfcardreader.RFCardReaderDevice;
import com.cloudpos.rfcardreader.RFCardReaderOperationResult;
import com.cloudpos.sdk.common.SystemProperties;
import com.cloudpos.serialport.SerialPortDevice;
import com.cloudpos.serialport.SerialPortOperationResult;
import com.cloudpos.smartcardreader.SmartCardReaderDevice;
import com.google.android.material.navigation.NavigationView;
import com.wizarpos.util.ByteConvertStringUtil;
import com.wizarpos.util.EnumMDBCommands;
import com.wizarpos.util.LogHelper;
import com.wizarpos.util.MDBUtils;
import com.wizarpos.util.PreferenceHelper;
import com.wizarpos.values.MDBValues;
import com.wizarpos.values.OptionalFeature;
import com.wizarpos.values.PulseValues;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements DataSendListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView textView;
    private LinearLayout llFragmentContainer;
    private String subModel;

    private Handler handler;
    private Handler subThreadHandler;
//    private Thread mdbThread;
    private ExecutorService executorService;
    private Future<?> future;

    private MDBValues mdbValues = new MDBValues();
    private PulseValues pulseValues = new PulseValues();

    private Fragment selectedFragment = null;
    private volatile boolean running = true;
    private volatile boolean open = false;

    public boolean readCardDone = true;
    public Queue<Message> msgQueueReadCard = new LinkedList<>();
    private Context context;

    AlertDialog cardDialog;
    private Card rfCard;
    private RFCardReaderDevice rfCardReaderDevice = null;
    private SmartCardReaderDevice smartCardReaderDevice = null;
    private SerialPortDevice serialPortDevice = null;

    private final String TAG = "MDBTest TAG";
    private final boolean TRIGGER_PULSE_ONE_SHOT = true;
    public static final String SUBMODEL_Q3MINI = "q3mini";
    public static final String SUBMODEL_Q3A7 = "q3a7";
    public static final String SUBMODEL_Q3V = "q3v";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textviewaa);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        context = this;
        mdbValues.setOptionalFeature(new OptionalFeature());

        llFragmentContainer = findViewById(R.id.fragment_container);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        selectedFragment = new HomeFragment();
//        setFragmentContainerHeight(selectedFragment);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getSimpleName());
                    if (selectedFragment == null) {
                        selectedFragment = new HomeFragment();
                    }
                    setFragmentContainerHeight(selectedFragment);
                } else if (item.getItemId() == R.id.nav_pulse) {
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(PulseFragment.class.getSimpleName());
                    if (selectedFragment == null) {
                        selectedFragment = new PulseFragment();
                    }
                    setFragmentContainerHeight(selectedFragment);
                } else if (item.getItemId() == R.id.nav_transaction) {
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(TransactionFragment.class.getSimpleName());
                    if (selectedFragment == null) {
                        selectedFragment = new TransactionFragment();
                    }
                    setFragmentContainerHeight(selectedFragment);
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if(selectedFragment != null){
                    Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(selectedFragment.getClass().getSimpleName());
                    if(existingFragment == null){
                        transaction.add(R.id.fragment_container, selectedFragment, selectedFragment.getClass().getSimpleName());
                    } else {
                        transaction.show(existingFragment);
                    }
                }
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment != selectedFragment) {
                        transaction.hide(fragment);
                    }
                }
                transaction.commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
//                Log.d(TAG, "handleMessage: " + msg.what);
                switch (msg.what) {
                    case MDBUtils.ENABLE_ALL_UI:
                        if(msg.obj != null) {
                            enableAllUiExcept((int) msg.obj);
                        } else {
                            enableAllUiExcept();
                        }
                        break;
                    case MDBUtils.DISABLE_ALL_UI:
                        if(msg.obj != null) {
                            disableAllUiExcept((int) msg.obj);
                        } else {
                            disableAllUiExcept();
                        }
                        break;
                    case MDBUtils.BLACK_LOG:
                        LogHelper.appendBlackMsg((String) msg.obj, textView);
                        break;
                    case MDBUtils.BLUE_LOG:
                        LogHelper.infoAppendForAlert((String) msg.obj, textView);
                        break;
                    case MDBUtils.RED_LOG:
                        LogHelper.appendREDMsg((String) msg.obj, textView);
                        break;
                    case MDBUtils.GREEN_LOG:
                        LogHelper.appendGreenMsg((String) msg.obj, textView);
                        break;
                    case MDBUtils.DIALOG_CONFIRM:
                        Item itemDialog = (Item) msg.obj;
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle("Please confirm item and amount")
                                .setMessage("Vend product\n " + "Amount: $" + itemDialog.getItemPrice() + ",Item: " + itemDialog.getItemId())
                                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendVendDenied();
                                        readCardDone = true;
                                        dialog.dismiss();
                                        processReadcard();
                                    }
                                })
                                .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendVendApproved(itemDialog);
                                        readCardDone = true;
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                readCardDone = true;
                            }
                        });
                        dialog.show();
                        break;
                    case MDBUtils.DIALOG_ITEM:
                        AlertDialog dialog1 = new AlertDialog.Builder(context)
                                .setTitle("Dispense")
                                .setMessage(msg.obj + "")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        processReadcard();
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                readCardDone = true;
                            }
                        });
                        dialog1.show();
                        break;
                    case MDBUtils.DIALOG_READCARD:
                        Message message = new Message();
                        message.what = msg.what;
                        message.obj = msg.obj;
                        msgQueueReadCard.add(message);
                        if(readCardDone) {
                            processReadcard();
                        }
                        break;
                    case MDBUtils.DIALOG_READCARD_CLOSE:
                        closeReadCardDialog();
                        break;
                    case MDBUtils.DIALOG_WAIT:
                        AlertDialog waitDialog = new AlertDialog.Builder(context)
                                .setTitle("Please wait")
                                .setMessage("Canceling...")
                                .setCancelable(false)
                                .create();
                        CountDownTimer timer = new CountDownTimer(6000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                if (waitDialog.isShowing()) {
                                    subThreadHandler.obtainMessage(MDBUtils.SUB_THREAD_CMD_AFTER_WAIT).sendToTarget();
                                    waitDialog.dismiss();
                                }
                            }
                        };
                        waitDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                timer.start();
                            }
                        });
                        waitDialog.show();
                        break;
                    case MDBUtils.TEST_MSG:
                        if (rfCardReaderDevice == null) {
                            rfCardReaderDevice = (RFCardReaderDevice) POSTerminal.getInstance(context)
                                    .getDevice("cloudpos.device.rfcardreader");
                        }
                        try {
                            rfCardReaderDevice.open();
                            SystemClock.sleep(5000);
                            readCardDone = true;
                            rfCardReaderDevice.close();
                        } catch (DeviceException e) {
                            Log.e(TAG, "open rfCardReaderDevice failed !");
//                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });
        if(serialPortDevice == null) {
            serialPortDevice = (SerialPortDevice) POSTerminal.getInstance(context)
                    .getDevice("cloudpos.device.serialport");
        }
        subModel = SystemProperties.get("ro.wp.product.submodel");
        executorService = Executors.newSingleThreadExecutor();
        int selectedSpnPosMdbLevel = PreferenceHelper.getInstance(context).getIntValue("selectedSpnPositonLevel");
        if(selectedSpnPosMdbLevel == 0){
            mdbValues.setMdbLevel(2);
        }else if(selectedSpnPosMdbLevel == 1){
            mdbValues.setMdbLevel(3);
            mdbValues.getOptionalFeature().setAlwaysIdleMdb(true);
        }
//        mdbThread = new Thread(taskMdb);
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
        close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        running = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(executorService != null && !executorService.isShutdown()){
            executorService.shutdownNow();
            executorService = null;
        }
        close();
    }

    public void processReadcard(){
        Item item = new Item();
        if(msgQueueReadCard.isEmpty()){
            readCardDone = true;
            return;
        }
        Log.d(TAG, "processing read card");
        readCardDone = false;
        if(!msgQueueReadCard.isEmpty()) {
            Message msgReadCard = msgQueueReadCard.poll();
            item = (Item) msgReadCard.obj;
        }
        if(cardDialog == null) {
            cardDialog = new AlertDialog.Builder(context)
                    .setTitle("Read card")
                    .setMessage("Please swipe card.")
                    .create();
            cardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    readCardDone = true;
                    if (rfCardReaderDevice != null) {
                        try {
                            rfCardReaderDevice.close();
                        } catch (DeviceException e) {
                            Log.e(TAG, "close rfCardReaderDevice failed ! " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });
            cardDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    sendVendDenied();
                }
            });
        }
//        Log.d(TAG, "Read item data " + item.getItemPrice() + "," + item.getItemId());
        if (rfCardReaderDevice == null) {
            rfCardReaderDevice = (RFCardReaderDevice) POSTerminal.getInstance(context)
                    .getDevice("cloudpos.device.rfcardreader");
        }
        if (smartCardReaderDevice == null) {
            smartCardReaderDevice = (SmartCardReaderDevice) POSTerminal.getInstance(context)
                    .getDevice("cloudpos.device.smartcardreader");
        }
        try {
            rfCardReaderDevice.open();
            try {
                cardDialog.show();
                Item finalItem = item;
                OperationListener listener = new OperationListener() {
                    @Override
                    public void handleResult(OperationResult arg0) {
                        try {
                            if (arg0.getResultCode() == OperationResult.SUCCESS) {
                                rfCard = ((RFCardReaderOperationResult) arg0).getCard();
                                handler.obtainMessage(MDBUtils.BLACK_LOG, "amount: $" + finalItem.getItemPrice() + ",item: " + finalItem.getItemId()).sendToTarget();
                                handler.obtainMessage(MDBUtils.DIALOG_CONFIRM, finalItem).sendToTarget();
                            } else {
                                printLogAndText('e', "find_card_failed");
                                readCardDone = true;
                            }
                        } catch (Exception e) {
                            readCardDone = true;
                            printLogAndText('e', "find_card_failed");
//                            e.printStackTrace();
                        } finally {
                            try {
                                cardDialog.dismiss();
                                rfCardReaderDevice.close();
                            } catch (DeviceException e) {
                                Log.e(TAG, "close rfCardReaderDevice failed ! " + e.getMessage());
//                                e.printStackTrace();
                            }
                        }
                    }
                };
                rfCardReaderDevice.listenForCardPresent(listener, TimeConstants.FOREVER);
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        } catch (DeviceException e) {
            Log.e(TAG, "open rfCardReaderDevice failed !");
            e.printStackTrace();
        }

    }

    public void closeReadCardDialog(){
        Log.d(TAG, "closeReadCardDialog");
        if(cardDialog != null){
            cardDialog.dismiss();
        }
        if(rfCardReaderDevice != null) {
            try {
                rfCardReaderDevice.close();
            } catch (DeviceException e) {
                Log.w(TAG, "close rfCardReaderDevice failed !");
                e.printStackTrace();
            }
        }
    }

    //time consuming operation, cannot be used in UI thread
    private byte[] readFromSerialPortDevice(int byteLength, int timeout){
        byte[] arryData = new byte[byteLength];
        try {
            SerialPortOperationResult serialPortOperationResult = serialPortDevice.waitForRead(arryData.length, timeout);
            byte[] data = serialPortOperationResult.getData();
            int dataLength = serialPortOperationResult.getDataLength();
            if(data != null) {
                Log.d(TAG, "data: " + ByteConvertStringUtil.buf2StringCompact(data));
                arryData = subByteArray(data, dataLength);
                return arryData;
            } else {
                Log.e(TAG, "readFromSerialPortDevice data is null, byteLength: " + byteLength + ", timeout: " + timeout);
                return null;
            }
        } catch (DeviceException e) {
            e.printStackTrace();
            Log.e(TAG, "readFromSerialPortDevice failed: " + e.getMessage() + ", byteLength: " + byteLength + ", timeout: " + timeout);
            return null;
        }
    }

    private void processData(Item item, byte[] readBytes){
        handler.obtainMessage(MDBUtils.BLUE_LOG, "read: ").sendToTarget();
        handler.obtainMessage(MDBUtils.BLACK_LOG, ByteConvertStringUtil.buf2StringCompact(readBytes)).sendToTarget();
        if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x10}, 2)) {
            sendReset();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x11, 0x00}, 3)) { //Setup config data
            mdbValues.setVmcLevel(readBytes[3]);
            sendSetupConfig(item);
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x11, 0x01}, 3)) { // Setup Max/Min Price
            sendSetupPrice();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x17, 0x00}, 3)) { //Expansion ID
            LogHelper.massiveLog('e', TAG, mdbValues.getOptionalFeature().toString());
            sendExpansionRequestID();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x17, 0x04}, 3)) { //Expansion optional feature bits
            mdbValues.getOptionalFeature().setOptionalFeatureBitsVmc(readBytes[6]);
            LogHelper.massiveLog('d', TAG, "mdbValues.getOptionalFeature().setOptionalFeatureBitsMdb(readBytes[6]): "
                                                 + readBytes[6]);
            mdbValues.getOptionalFeature().setOptionalFeatureBitsMdb(readBytes[6]);
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x14, 0x00}, 3)) {// Disable Reader
            sendDisableReaderAck();
        }else if(compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x14, 0x01}, 3)) {//Enable Reader
            sendEnableReaderAck();
            startTransaction();
        } else if (compareByteArrayHead(readBytes, new byte[]{0x01, 0x00}, 2)) {
            Log.d(TAG, "Ready! Please input item ID");
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "Ready! Please input item ID").sendToTarget();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x15, 0x00}, 3)){ //Revalue approved
            sendRevalueApproved();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x15, 0x01}, 3)){ //Revalue limited
            sendRevalueLimit();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x13, 0x00}, 3)) { //vend request
            //e.g. 00 13 00 00 A0 00 01 B4 98
            //amount: 00 A0
            //id: 00 01
            if(mdbValues.getOptionalFeature().isMonetaryFormat32Vmc() && mdbValues.getOptionalFeature().isMonetaryFormat32Mdb())
            {
                item.setItemAmount(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[3], readBytes[4], readBytes[5], readBytes[6]}).replace(" ", ""));
                item.setItemId(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[7], readBytes[8]}).replace(" ", ""));
                item.calculatePrice();
                BigInteger itemid = new BigInteger(item.getItemId(), 16);
                item.setItemId(itemid.toString());
            } else {
                item.setItemAmount(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[3], readBytes[4]}).replace(" ", ""));
                item.setItemId(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[5], readBytes[6]}).replace(" ", ""));
                item.calculatePrice();
                BigInteger itemid = new BigInteger(item.getItemId(), 16);
                item.setItemId(itemid.toString());
            }
            handler.obtainMessage(MDBUtils.DIALOG_READCARD, item).sendToTarget();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, (byte)0x13, 0x01}, 3)) {  // vend denied/canceled
            handler.obtainMessage(MDBUtils.DIALOG_READCARD_CLOSE).sendToTarget();
            Log.d(TAG, "00 13 01, vending auto cancel, denied, cardDialog closed");
//            sendVendDenied();
            handler.obtainMessage(MDBUtils.DIALOG_WAIT).sendToTarget();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x13, 0x02}, 3)) { //vend success
            sendVendSuccess(readBytes);
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x13, 0x04}, 3)) { //vend session end
            sendSessionEnd();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x13, 0x05}, 3)) { //cash sale
            sendCashSaleAck();
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x13, 0x06}, 3)){ //negative vend request
            if(mdbValues.getMdbLevel() == 3 && mdbValues.getVmcLevel() == 3) {
                Item item1 = new Item();
                if(!mdbValues.getOptionalFeature().isMonetaryFormat32Vmc()) {
                    item1.setItemAmount(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[3], readBytes[4]}).replace(" ", ""));
                    item1.setItemId(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[5], readBytes[6]}).replace(" ", ""));
                    item1.calculatePrice();
                    BigInteger itemid = new BigInteger(item1.getItemId(), 16);
                    item1.setItemId(itemid.toString());
				} else {
                    item1.setItemAmount(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[3], readBytes[4], readBytes[5], readBytes[6]}).replace(" ", ""));
                    item1.setItemId(ByteConvertStringUtil.buf2StringCompact(new byte[]{readBytes[7], readBytes[8]}).replace(" ", ""));
                    item1.calculatePrice();
                    BigInteger itemid = new BigInteger(item1.getItemId(), 16);
                    item1.setItemId(itemid.toString());
				}
                sendNegativeVendApproved(readBytes, item1);
			}
        } else if (compareByteArrayHeadByDevice(readBytes, new byte[]{0x00, 0x14, 0x02}, 3)){ //reader cancel
            handler.obtainMessage(MDBUtils.DIALOG_READCARD_CLOSE).sendToTarget();
            handler.obtainMessage(MDBUtils.DIALOG_WAIT).sendToTarget();
            handler.obtainMessage(MDBUtils.BLACK_LOG, "Reader cancelled").sendToTarget();
        } else if (compareByteArrayHead(readBytes, new byte[]{0x01, (byte)0x94}, 2)) { //diagnose hardware
            if (compareByteArrayHead(readBytes, new byte[]{0x01, (byte)0x94, 0x00}, 3)) {
                handler.obtainMessage(MDBUtils.BLACK_LOG, "hardware diagnose test received").sendToTarget();
            } else {
                //Ps： if ACK after CMDID（0x94）is not 0x00, this means MDB communication timeout
                handler.obtainMessage(MDBUtils.BLACK_LOG, "hardware diagnose test open failed").sendToTarget();
            }
        } else if (compareByteArrayHead(readBytes, new byte[]{0x0d}, 1)){
            Log.d(TAG, "0D, end of data");
        } else {
            Log.d(TAG, "read failed: " + ByteConvertStringUtil.buf2StringCompact(readBytes));
        }
    }

    private boolean compareByteArrayHead(byte[] byteArray, byte[] bytesHead, int nBytes){
        byte[] tmpBytesHead = Arrays.copyOfRange(byteArray, 0, nBytes);
        return Arrays.equals(tmpBytesHead, bytesHead);
    }

    private boolean compareByteArrayHeadByDevice(byte[] byteArray, byte[] bytesHead, int nBytes){
        if(mdbValues.getDeviceType() == 1){
            return compareByteArrayHead(byteArray, bytesHead, nBytes);
        }else {
            byte[] tmpBytesHead = Arrays.copyOfRange(byteArray, 0, nBytes);
            tmpBytesHead[1] = (byte) ((byteArray[1] & 0x0F) | 0x10);
            return Arrays.equals(tmpBytesHead, bytesHead);
        }
    }

    private void sendCommonResponse(){
        byte[] dataBytes = new byte[1];
        byte[] wbytes = MDBUtils.mergePacket(EnumMDBCommands.MDB_CMD_COMMON_RESPONSE, dataBytes);
		try {
			serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "common response");
		} catch (DeviceException e) {
            Log.e(TAG, "sendCommonResponse failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//			throw new RuntimeException(e);
		}
    }
    private void sendReset(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.RESET);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Reset wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Reset", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendReset failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    //readBytes = "00 11 00 02 00 00 01 14"
    private void sendSetupConfig(Item item){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.SETUP_CONFIG, mdbValues, item);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Setup config data. wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Setup config data", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendSetupConfig failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendSetupPrice(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.SETUP_PRICE);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Setup Max/Min Price. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Setup Max/Min Price", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendSetupPrice failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendExpansionRequestID(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.EXPANSION_REQUESTID, mdbValues);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Expansion ID. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Expansion ID", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendExpansionRequestID failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void sendDisableReaderAck(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.COMMON_ACK);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Disable Reader Ack. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Disable Reader", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendDisableReaderAck failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void sendEnableReaderAck(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.COMMON_ACK);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Enable Reader Ack. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Enable Reader", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendEnableReaderAck failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendRevalueApproved(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.REVALUE_APPROVED);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Revalue approved. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Revalue approved", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendRevalueApproved failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendRevalueLimit(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.REVALUE_LIMIT_AMOUNT);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Revalue limit request. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Revalue limit request", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendRevalueLimit failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void sendVendSuccess(byte[] readBytes){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.VEND_SUCCESS);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Vend success! wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            int itemIdHigh = readBytes[3];
            int itemIdLow = readBytes[4];
            int itemId = ((itemIdHigh & 0xFF) << 8) | (itemIdLow & 0xFF);
            handler.obtainMessage(MDBUtils.DIALOG_ITEM, "Vend success! " + ByteConvertStringUtil.buf2StringCompact(wbytes)
                    + " item:" + itemId).sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendVendSuccess failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void sendVendDenied(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.VEND_DENIED);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Vend denied! wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Vend denied", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendVendDenied failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void sendCashSaleAck(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.COMMON_ACK);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "Cash Sale Ack. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("Enable Reader", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendCashSaleAck failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void sendSessionEnd(){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.END_SESSION);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "finish. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("finish", wbytes);
            startTransaction();
        } catch (DeviceException e) {
            Log.e(TAG, "sendSessionEnd failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendVendApproved(Item item){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.VEND_APPROVED, item);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "approved! wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("approved", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendVendApproved failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    //not tested yet
    private void sendNegativeVendApproved(byte[] readBytes, Item item){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.NEGATIVE_VEND_APPROVED, mdbValues, item);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "negative vend approved! wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("negative vend approved", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendNegativeVendApproved failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    //begin session
    private void startTransaction() {
        Log.d(TAG, "mdb: " + mdbValues.getOptionalFeature().isAlwaysIdleMdb() + ", vmc: " + mdbValues.getOptionalFeature().isAlwaysIdleVmc());
        if(mdbValues.getOptionalFeature().isAlwaysIdleMdb() && mdbValues.getOptionalFeature().isAlwaysIdleVmc()){
            Log.d(TAG, "always idle");
            handler.obtainMessage(MDBUtils.BLACK_LOG, "always idle").sendToTarget();
        } else {
            SystemClock.sleep(5000);
            byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.BEGIN_SESSION, mdbValues);
            try {
                serialPortDevice.write(wbytes, 0, wbytes.length);
                Log.d(TAG, "begin session. wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
                printLogWithBlueRead("begin session", wbytes);
            } catch (DeviceException e) {
                Log.e(TAG, "sendBeginSession(startTransaction) failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//                throw new RuntimeException(e);
            }
        }
    }
    private void sendReaderCancelled(String sp){
        byte[] wbytes = EnumMDBCommands.getResponseOfMdbCommands(EnumMDBCommands.MDB_RESPONSE.READER_CANCELLED);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "cancelled. wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
            printLogWithBlueRead("cancelled", wbytes);
        } catch (DeviceException e) {
            Log.e(TAG, "sendReaderCancelled failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendTriggerPulse(){ //09 03 00 20 E0 0D resp: 09 04 01 20 00 DF 0D
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.TRIGGER_PULSE);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send trigger pulse succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
        } catch (DeviceException e) {
            Log.e(TAG, "sendTriggerPulse failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    //09 13 00 21 00 00 00 00 C8 00 00 00 2C 01 00 00 01 00 00 00 E9 0D
    //resp:09 04 01 21 00
    private void sendTriggerPulseOneShot() {
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.TRIGGER_PULSE_ONESHOT, pulseValues);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send trigger pulse one shot succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
        } catch (DeviceException e) {
            Log.e(TAG, "sendTriggerPulseOneShot failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }
    private void sendSetPulseDuration(int duration){ //09 08 00 90 03 C8 00 00 00 A5 0D resp: 09 05 01 90 03 00 6C 0D
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.SET_PARAM_PULSE_DURATION, duration);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send set pulse duration succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
            handler.obtainMessage(MDBUtils.BLACK_LOG, "setting pulse duration").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendSetPulseDuration failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }
    private void sendSetPulseVoltage(int voltage){ //09 08 00 90 04 00 00 00 00 6C 0D resp: 09 05 01 90 04 00 6B 0D
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.SET_PARAM_PULSE_VOLTAGE, voltage);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send set pulse voltage succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
            handler.obtainMessage(MDBUtils.BLACK_LOG, "setting pulse voltage").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendSetPulseVoltage failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }
    private void sendEnterFactoryMode(){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.SET_PARAM_ENTER_FACTORY_MODE);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send enter factory succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "send enter factory succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendEnterFactoryMode failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }
    private void sendQuitFactoryMode(){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.SET_PARAM_QUIT_FACTORY_MODE);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send quit factory succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "send quit factory succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendQuitFactoryMode failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    private void sendActiveCashless(boolean active){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.ACTIVE_CASHLESS, active);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send active cashless succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "send quit factory succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendActiveCashless failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    public void setParamCashlessAddress(){
        Thread thread = new Thread(taskSetParamCashlessAddress);
        thread.start();
    }

    Runnable taskSetParamCashlessAddress = new Runnable() {
        @Override
        public void run() {
            open();
            sendSetParamCashlessAddress();
            getSetParamCashlessAddressResp();
            close();
        }
    };

    private void getSetParamCashlessAddressResp(){
        int cmdLength = readCmdLength(false);
        if(cmdLength > 0){
            byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 2000);// + 0x0D
            if(byteArr != null) {
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x90, 0x06}, 3)) {
                    printLogAndText('d', "set param cashless address succeed");
                } else {
                    printLogAndText('e', "set param cashless address failed!");
                }
            } else {
                printLogAndText('e', "set param cashless address failed, read byte array is null!");
            }
        } else {
            Log.e(TAG, "taskSetParamCashlessAddress: read cmd length failed");
        }
    }

    private void sendSetParamCashlessAddress(){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.SET_PARAM_CASHLESS_ADDRESS, mdbValues.getDeviceType());
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send set cashless address succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "send quit factory succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendSetCashlessAddress failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    private void sendSetParamDefaultActiveStatus(){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.SET_PARAM_DEFAULT_ACTIVE_STATUS, mdbValues.getDefaultActiveStatus());
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send set default active status succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "send quit factory succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendSetDefaultActiveStatus failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    private void sendGetParamDefaultActiveStatus(){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.GET_PARAM_DEFAULT_ACTIVE_STATUS);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send get default active status succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            handler.obtainMessage(MDBUtils.BLACK_LOG, "send quit factory succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendGetCashlessAddress failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    private void sendHardwareDiagnoseTest(){
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.HARDWARE_DIAGNOSE_TEST);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send hardware diagnose test succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
            handler.obtainMessage(MDBUtils.BLACK_LOG, "send hardware diagnose test succeed !").sendToTarget();
        } catch (DeviceException e) {
            Log.e(TAG, "sendHardwareDiagnoseTest failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    //09 03 00 92 6E 0D resp: 09 06 01 92 00 00 0E 5F 0D
    //if version number is 20, resp: 09 06 01 92 00 00 14 59 0D
    private void sendVersionRequest() {
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.VERSION_REQUEST);
        handler.obtainMessage(MDBUtils.BLACK_LOG, "Getting version, please wait... ").sendToTarget();
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send get version request succeed !" + ByteConvertStringUtil.buf2StringCompact(wbytes));
        } catch (DeviceException e) {
            Log.e(TAG, "sendVersionRequest failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
        SystemClock.sleep(200);
    }

    private void sendHardwareVersionRequest(){//09 03 00 93 6D 0D resp: 09 05 01 93 00 04 68 0D
        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.HARDWARE_VERSION_REQUEST);
        try {
            serialPortDevice.write(wbytes, 0, wbytes.length);
            Log.d(TAG, "send get hardware version succeed ! wbytes:" + ByteConvertStringUtil.buf2StringCompact(wbytes));
        } catch (DeviceException e) {
            Log.e(TAG, "sendHardwareVersionRequest failed: " + e.getMessage() + ", wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));
//            throw new RuntimeException(e);
        }
    }

    private void printLogWithBlueRead(String blue, byte[] black){
        handler.obtainMessage(MDBUtils.BLUE_LOG, blue).sendToTarget();
        handler.obtainMessage(MDBUtils.BLACK_LOG, ByteConvertStringUtil.buf2StringCompact(black)).sendToTarget();
    }

    public void startMdb(){
        new Thread(() -> {
            Looper.prepare();
            subThreadHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MDBUtils.SUB_THREAD_CMD_AFTER_WAIT:
                            sendVendDenied();
                            break;
                        default:
                            break;
                    }
                }
            };
            close();
            if(open()){
                handler.obtainMessage(MDBUtils.DISABLE_ALL_UI, R.id.btn_mdb_stop).sendToTarget();
                if(mdbValues.getCurrentVersion() == 0) {
//                    getVersion();
                    sendVersionRequest();
                    readGetVersionResp();
//                    Button button = findViewById(R.id.btn_mdb_start);
//                    if(button != null) {
//                        for(int i = 0; i < 10 || button.isEnabled(); i++) {
//                            SystemClock.sleep(100);
//                            handler.obtainMessage(MDBUtils.DISABLE_ALL_UI).sendToTarget();
//                        }
//                    }
                    Log.d(TAG, "startMdb: mdbValues.getCurrentVersion(): " + mdbValues.getCurrentVersion());
                }
                if((isWhiteDemon() && mdbValues.getCurrentVersion() >=28)
                        || (!isWhiteDemon() && mdbValues.getCurrentVersion() >= 5)) {
                    if(mdbValues.getDefaultActiveStatus() == -1) {
                        getDefaultActiveStatus();
//                        SystemClock.sleep(1000);
                        Log.d(TAG, "startMdb: mdbValues.getDefaultActiveStatus(): " + mdbValues.getDefaultActiveStatus());
                    }
                    if (mdbValues.getDefaultActiveStatus() == 0) {
                        mdbValues.setDefaultActiveStatus(1);
                        setDefaultActiveStatus();
//                        SystemClock.sleep(1000);
                        Log.d(TAG, "startMdb: mdbValues.setDefaultActiveStatus(): " + mdbValues.getDefaultActiveStatus());
                    }
                    activeCashless(true);
                }
                running = true;
//                if(!open){
//                    if(open())
//                        SystemClock.sleep(200);
//                }
//                future = executorService.submit(taskMdb);
                processMdb();
            }
            else {
                printLogAndText('e', "serial port open failed!");
                close();
            }
        }).start();
    }

    public void stopMdb(){
//        mdbThread.interrupt();
        new Thread(() -> {
            handler.obtainMessage(MDBUtils.DISABLE_ALL_UI).sendToTarget();
            running = false;
            SystemClock.sleep(500);
            close();
            if(open()) {
                if (mdbValues.getCurrentVersion() == 0) {
//                getVersion();
                    sendVersionRequest();
                    readGetVersionResp();
//                SystemClock.sleep(1000);
                    Log.d(TAG, "stopMdb: mdbValues.getCurrentVersion(): " + mdbValues.getCurrentVersion());
                }
                if ((isWhiteDemon() && (mdbValues.getCurrentVersion() >= 28))
                        || (!isWhiteDemon() && (mdbValues.getCurrentVersion() >= 5))) {
                    if (mdbValues.getDefaultActiveStatus() == -1) {
                        getDefaultActiveStatus();
//                    SystemClock.sleep(1000);
                        Log.d(TAG, "stopMdb: mdbValues.getDefaultActiveStatus(): " + mdbValues.getDefaultActiveStatus());
                    }
                    activeCashless(false);
//                SystemClock.sleep(1000);
                }
                close();
            }
            handler.obtainMessage(MDBUtils.ENABLE_ALL_UI, R.id.btn_mdb_stop).sendToTarget();
            handler.obtainMessage(MDBUtils.BLACK_LOG, "Mdb stopped").sendToTarget();
        }).start();
    }

    private void clickTriggerPulse(){
        new Thread(() -> {
            if(open()) {
                if (pulseValues.getPulseFrequency() < 0) {
                    return;
                }
                pulseValues.setCurrentSend(pulseValues.getPulseFrequency());
                if (pulseValues.getPulseInterval() < 0) {
                    return;
                }
                handler.obtainMessage(MDBUtils.DISABLE_ALL_UI).sendToTarget();
                if(mdbValues.getCurrentVersion() == 0) {
    //                getVersion();
                    sendVersionRequest();
                    readGetVersionResp();
    //                SystemClock.sleep(500);
                    Log.d(TAG, "mdbValues.getCurrentVersion(): " + mdbValues.getCurrentVersion());
                }
                setPulse();
    //            SystemClock.sleep(500);
                triggerPulse();
                close();
            }
        }).start();
    }

    private void setPulse(){
        if(pulseValues.getPulseDuration() < 0){
            printLogAndText('e', "pulse parameter error!");
            return;
        }
        if(pulseValues.getPulseVoltage() != 0 && pulseValues.getPulseVoltage() != 1){
            printLogAndText('e', "pulse parameter error!");
            return;
        }
        if(pulseValues.getPulseInterval() <= 0){
            printLogAndText('e', "pulse parameter error!");
            return;
        }
        if(pulseValues.getPulseFrequency() <= 0){
            printLogAndText('e', "pulse parameter error!");
            return;
        }
        setPulseVoltage(pulseValues.getPulseVoltage());
        setPulseDuration(pulseValues.getPulseDuration());
//        SystemClock.sleep(1000);
    }

    private void triggerPulse(){
        handler.obtainMessage(MDBUtils.BLACK_LOG, "trigger pulse processing, please wait ...").sendToTarget();
        if (subModel.equalsIgnoreCase(SUBMODEL_Q3MINI)
                || mdbValues.getCurrentVersion() >= 22) {
            int pulseWholeTime = pulseValues.getPulseFrequency() * (pulseValues.getPulseInterval() + pulseValues.getPulseDuration()) + 2000;
            if(pulseWholeTime < 0){
                printLogAndText('e', "pulse parameter out of range!");
                handler.obtainMessage(MDBUtils.ENABLE_ALL_UI).sendToTarget();
                return;
            }
            triggerPulseOneShot();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.obtainMessage(MDBUtils.ENABLE_ALL_UI).sendToTarget();
                }
            }, pulseWholeTime);
        } else if ((subModel.equalsIgnoreCase(SUBMODEL_Q3A7) || subModel.equalsIgnoreCase(SUBMODEL_Q3V))
                    && mdbValues.getCurrentVersion() >= 19){
            sendTriggerPulse();
            for (int i = 1; i < pulseValues.getPulseFrequency(); ++i) {
                readTriggerPulseResp();
                int delayTime = i * (pulseValues.getPulseInterval() + pulseValues.getPulseDuration());
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendTriggerPulse();
                    }
                }, delayTime);
            }
        } else {
            handler.obtainMessage(MDBUtils.RED_LOG, "version is not equal or greater than 22, " +
                    "trigger pulse failed, please update FW version").sendToTarget();
        }
    }

    /**
     * 1. test mib
     * send command to serial "Hardware diagnostic test":
     * Eg：
     * Send Data:090300946c0d
     * Recv Data: 09040194006b0d
     * Ps： ifCMDID（0x94）后面的ACK 不为0x00 ，说明MDB 通讯timeout
     * 2.get MIB version
     * send get version cmd
     * Send Data: 090300926e0d
     * Recv Data: 090601920000 0d 600d
     */

    void testMdb() {
        Log.d(TAG, "test mdb");
    }

    //e.g. version 0019: sp == 01 92 00 00 13 5A, 00 13 decides 19
    public void printVersionName(byte[] readBytes){
        int high = readBytes[3];
        int low = readBytes[4];
        int version = ((high & 0xFF) << 8) | (low & 0xFF);
        mdbValues.setCurrentVersion(version);
        handler.obtainMessage(MDBUtils.GREEN_LOG, "version = " + version).sendToTarget();
    }

    //可以添加参数 byte[] contentBytes
    private byte[] createBytes(){
        int lrc;
        byte[] contentBytes = {0x00, 0x10, 0x10};
        lrc = LRCCheckInt(contentBytes);
        byte[] dataBytes = {0x09, 0x04, 0x00, 0x10, 0x10, (byte) lrc, (byte) 0x0D};
        return dataBytes;
    }

    private byte[] createPollBytes(){
        int lrc;
        byte[] contentBytes = {0x00, 0x12, 0x12};
        lrc = LRCCheckInt(contentBytes);
        byte[] dataBytes = {0x09, 0x04, 0x00, 0x12, 0x12, (byte) lrc, (byte) 0x0D};
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return dataBytes;
    }

    private boolean writeData(byte[] data){
        boolean ret = false;
        try {
            serialPortDevice.write(data, 0, data.length);
            Log.d(TAG, "Setup Max/Min Price. wbytes: " + ByteConvertStringUtil.buf2StringCompact(data));
            printLogWithBlueRead("Setup Max/Min Price", data);
            ret = true;
        } catch (DeviceException e) {
            ret = false;
//            throw new RuntimeException(e);
        }
        if(Arrays.equals(data, createPollBytes())){
            Log.d(TAG, "polling ...");
        } else if(ret){
            Log.d(TAG, "write data success! write length : " + data.length + "data : " + ByteConvertStringUtil.buf2StringCompact(data));
        }else{
            Log.e(TAG, "write data failed!! Data : " + ByteConvertStringUtil.buf2StringCompact(data));
        }
        return ret;
    }

    boolean open() {
        boolean ret = false;
		try {
			serialPortDevice.open(SerialPortDevice.ID_SERIAL_EXT);
            onLogSent('d', TAG, "seriaport open success!");
            ret = true;
            open = true;
		} catch (DeviceException e) {
            onLogSent('e', TAG,"seriaport open failed!");
//			throw new RuntimeException(e);
		}
        if(ret){
            try {
                serialPortDevice.changeSerialPortParams(115200, SerialPortDevice.DATABITS_8, SerialPortDevice.STOPBITS_1, SerialPortDevice.PARITY_NONE);
            } catch (DeviceException e) {
                Log.e(TAG, "change serial port params failed: " + e.getMessage());
//                throw new RuntimeException(e);
            }
            SystemClock.sleep(100);
        }
        return ret;
    }
    void close() {
        running = false;
        if(open) {
            try {
                serialPortDevice.close();
//            onLogSent('d', TAG, "serial port close success!");
                Log.d(TAG, "serial port close success!");
                open = false;
//                return true;
            } catch (DeviceException e) {
//            onLogSent('e', TAG, "serial port close failed!");
                Log.e(TAG, "serial port close failed!" + e.getMessage());
                e.printStackTrace();
//			throw new RuntimeException(e);
//                return false;
            }
        }
        if(rfCardReaderDevice != null) {
            try {
                rfCardReaderDevice.close();
            } catch (DeviceException e) {
                Log.e(TAG, "close rfCardReaderDevice failed ! " + e.getMessage());
                e.printStackTrace();
            }
        }
        if(future != null){
            future.cancel(true);
            future = null;
        }
//        else {
//            return true;
//        }
    }

    public void test(){
        Log.w(TAG, "test mdb");

        byte[] wbytes = EnumMDBCommands.getRequestOfMdbCommands(EnumMDBCommands.MDB_REQUEST.BEGIN_SESSION, mdbValues);
        printLogAndText('i', "test mdb, wbytes: " + ByteConvertStringUtil.buf2StringCompact(wbytes));

        Log.e(TAG, mdbValues.toString());

    }

//    Runnable taskMdb =  new Runnable() {
//        @Override
//        public void run() {
//
//        }
//    };

    private void processMdb(){
        Item item = new Item();
        handler.obtainMessage(MDBUtils.BLACK_LOG, "Init, waiting master command...").sendToTarget();
        try {
//                while (isSlave) {
            while (!Thread.currentThread().isInterrupted() && running) {
                // read start flag 0x09, 1 bit
                byte[] byteArr = readFromSerialPortDevice(1, -1);
                if (byteArr != null) {
                    Log.d(TAG, "read 1 byte success: " + ByteConvertStringUtil.buf2StringCompact(byteArr));
                    if (ByteConvertStringUtil.buf2StringCompact(byteArr).equals("0D")) {
                        continue;
                    }
                    if (ByteConvertStringUtil.buf2StringCompact(byteArr).equals("09")) {
                        // read data length, 1 bit
                        byteArr = readFromSerialPortDevice(1, 200);
                        if (byteArr != null) {
                            Log.d(TAG, "read length success: " + ByteConvertStringUtil.buf2StringCompact(byteArr));
                            String lenthHex = ByteConvertStringUtil.buf2StringCompact(byteArr);
                            BigInteger length = new BigInteger(lenthHex, 16);
                            // read data
                            byteArr = readFromSerialPortDevice(length.intValue(), 200);
                            if (byteArr != null) {
                                Log.i(TAG, "read success: " + ByteConvertStringUtil.buf2StringCompact(byteArr));
                            } else {
                                Log.e(TAG, "read failed");
                            }
                            processData(item, byteArr);
                        } else {
                            Log.e(TAG, "read length failed!");
                        }
                    }
                }
            }
//                close();
        } catch (Exception e) {
            if(Thread.currentThread().isInterrupted()){
                Log.d(TAG, "taskMdb interrupted");
            } else {
                Log.e(TAG, "taskMdb run failed: " + e.getMessage());
            }
        }
    }

    public void getVersion(){
        Thread thread = new Thread(taskGetVersion);
        thread.start();
    }

    //resp: 09 06 01 92 00 00 01 6C 0D
    Runnable taskGetVersion = new Runnable() {
        @Override
        public void run() {
            handler.obtainMessage(MDBUtils.DISABLE_ALL_UI).sendToTarget();
            if(open()) {
                sendVersionRequest();
                readGetVersionResp();
                close();
            }
            handler.obtainMessage(MDBUtils.ENABLE_ALL_UI, R.id.btn_mdb_stop).sendToTarget();
        }
    };

    //time consuming operation, must be used in a new thread
    private void readGetVersionResp(){
        boolean readDone = false;
        int sendCount = 0;
        while (!readDone && sendCount < 3) {
            ++sendCount;
            int cmdLength = readCmdLength(true);
            if(cmdLength > 0){
                byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 5000);// + 0x0D
                if(byteArr != null) {
                    handler.obtainMessage(MDBUtils.BLACK_LOG, "get result:" +
                            ByteConvertStringUtil.buf2StringCompact(byteArr)).sendToTarget();
                    printVersionName(byteArr);
                    break;
                }
            } else {
                if(sendCount < 3) {
                    onLogSent('d', TAG, "get version failed, trying again: " + sendCount);
                    reset();
                    sendVersionRequest();
                } else {
                    onLogSent('e', TAG, "get version failed!");
                }
            }
        }
    }

    public void getHardwareVersion(){
        Thread thread = new Thread(taskGetHardwareVersion);
        thread.start();
    }

    //09 03 00 93 6D 0D resp: 09 05 01 93 00 07 65 0D
    Runnable taskGetHardwareVersion = new Runnable() {
        @Override
        public void run() {
            if(open()) {
                sendHardwareVersionRequest();
                readGetHardwareVersionResp();
                close();
            }
        }
    };

    //time consuming operation, must be used in a new thread
    private void readGetHardwareVersionResp(){
        byte[] byteArr = readFromSerialPortDevice(1, 1000 * 5);
        if(byteArr == null){
            printLogAndText('e', "get hardware version failed!");
        } else {
            byteArr = readFromSerialPortDevice(7, 1000 * 5);
            if(byteArr == null){
                printLogAndText('e', "get hardware version failed!");
            } else {
                byteArr = Arrays.copyOfRange(byteArr, 1, byteArr.length);
                if (compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x93}, 2)){
                    if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x93, 0x00}, 3)) {
                        int hardwareVersionNo = byteArr[3];
                        handler.obtainMessage(MDBUtils.BLACK_LOG, "hardware version: " + hardwareVersionNo).sendToTarget();
                    } else {
                        handler.obtainMessage(MDBUtils.RED_LOG, "get hardware version failed!").sendToTarget();
                    }
                }
            }
        }
    }

    public void enterFactoryMode(){
        Thread thread = new Thread(taskEnterFactoryMode);
        thread.start();
    };

    Runnable taskEnterFactoryMode = new Runnable() {
        @Override
        public void run() {
            if(open()) {
                sendEnterFactoryMode();
                getEnterFactoryModeResp();
                close();
            }
        }
    };

    //time consuming operation, must be used in a new thread
    private void getEnterFactoryModeResp(){
        byte[] byteArr = readFromSerialPortDevice(1, 1000 * 5);
        if(byteArr == null){
            printLogAndText('e', "enter factory mode failed!");
        } else {
            byteArr = readFromSerialPortDevice(4, 1000 * 5);
            if(byteArr == null){
                printLogAndText('e', "enter factory mode failed!");
            } else {
                byteArr = Arrays.copyOfRange(byteArr, 1, byteArr.length);
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x90, 0x05}, 3)) {
//                        if(!mdbValues.isFactoryMode()) {
                    handler.obtainMessage(MDBUtils.BLACK_LOG, "enter factory mode succeed").sendToTarget();
                    mdbValues.setFactoryMode(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = selectedFragment.getView();
                            if(view != null){
                                Button button = view.findViewById(R.id.btn_factory_mode);
                                button.setText("quit factory mode");
                            }
                        }
                    });
//                        }
                } else {
                    handler.obtainMessage(MDBUtils.RED_LOG, "enter factory mode failed!").sendToTarget();
                }
            }
        }
    }

    public void quitFactoryMode(){
        Thread thread = new Thread(taskQuitFactoryMode);
        thread.start();
    }

    Runnable taskQuitFactoryMode = new Runnable() {
        @Override
        public void run() {
            if(open()){
                sendQuitFactoryMode();
                getQuitFactoryModeResp();
                close();
            }
        }
    };

    //time consuming operation, cannot be used in UI thread
    private void getQuitFactoryModeResp(){
        byte[] byteArr = readFromSerialPortDevice(1, 1000 * 5);
        if(byteArr == null){
            printLogAndText('e', "quit factory mode failed!");
        } else {
            byteArr = readFromSerialPortDevice(4, 1000 * 5);
            if(byteArr == null){
                printLogAndText('e', "quit factory mode failed!");
            } else {
                byteArr = Arrays.copyOfRange(byteArr, 1, byteArr.length);
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x90, 0x05}, 3)) {
                    if(!mdbValues.isFactoryMode()) {
                        handler.obtainMessage(MDBUtils.BLACK_LOG, "quit factory mode succeed").sendToTarget();
                        mdbValues.setFactoryMode(false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                View view = selectedFragment.getView();
                                if(view != null){
                                    Button button = view.findViewById(R.id.btn_factory_mode);
                                    button.setText("enter factory mode");
                                }
                            }
                        });
                    }
                } else {
                    handler.obtainMessage(MDBUtils.RED_LOG, "quit factory mode failed!").sendToTarget();
                }
            }
        }
    }

    private void setPulseDuration(int duration){
        sendSetPulseDuration(duration);
        readSetPulseDurationResp();
    }
    //time consuming operation, must be used in a new thread
    private void readSetPulseDurationResp(){
        int cmdLength = readCmdLength(false);
        if(cmdLength > 0){
            byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 5000);// + 0x0D
            if(byteArr != null) {
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x90, 0x03}, 3)) {
                    handler.obtainMessage(MDBUtils.BLACK_LOG, "set pulse duration succeed").sendToTarget();
                } else {
                    handler.obtainMessage(MDBUtils.RED_LOG, "set pulse duration failed!").sendToTarget();
                    close();
                }
            } else {
                printLogAndText('e', "set pulse duration failed, read byte array is null!");
            }
        } else {
            Log.e(TAG, "readSetPulseDurationResp: read cmd length failed");
        }
    }

    private void setPulseVoltage(int voltage){
        sendSetPulseVoltage(voltage);
        readSetPulseVoltageResp();
    }

    //time consuming operation, cannot be used in UI thread
    private void readSetPulseVoltageResp(){
        int cmdLength = readCmdLength(false);
        if(cmdLength > 0){
            byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 5000);// + 0x0D
            if(byteArr != null) {
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x90, 0x04}, 3)) {
                    printLogAndText('d', "set pulse voltage succeed");
                } else {
                    printLogAndText('e', "set pulse voltage failed!");
                }
            } else {
                printLogAndText('e', "set pulse voltage failed, read byte array is null!");
            }
        } else {
            Log.e(TAG, "taskSetPulseVoltage: read cmd length failed");
        }
    }

    //time consuming operation, cannot be used in UI thread
    private void readTriggerPulseResp(){
        byte[] byteArr = readFromSerialPortDevice(1, 1000 * 5);
        if(byteArr == null){
            printLogAndText('e', "trigger pulse failed!");
        } else {
            byteArr = readFromSerialPortDevice(4, 1000 * 5);
            if(byteArr == null){
                printLogAndText('e', "trigger pulse failed!");
            } else {
                byteArr = Arrays.copyOfRange(byteArr, 1, byteArr.length);
                if (compareByteArrayHead(byteArr, new byte[]{0x01, 0x20, 0x00}, 3)) { //trigger pulse
                    //receive: 09 04 01 20 00 DF 0D
                    pulseValues.setCurrentSend(pulseValues.getCurrentSend() - 1);
                    if (pulseValues.getCurrentSend() == 0) {
                        handler.obtainMessage(MDBUtils.ENABLE_ALL_UI).sendToTarget();
                        handler.obtainMessage(MDBUtils.BLACK_LOG, "trigger pulse " + pulseValues.getPulseFrequency() + " times succeed").sendToTarget();
                    } else {
                        handler.obtainMessage(MDBUtils.BLACK_LOG, "trigger pulse " + (pulseValues.getPulseFrequency() - pulseValues.getCurrentSend())).sendToTarget();
                    }
                } else {
                    printLogAndText('e', "trigger pulse failed!");
                }
            }
        }
    }

    private void triggerPulseOneShot(){
        sendTriggerPulseOneShot();
        readTriggerPulseOneShotResp();
    }

//    class TriggerPulseOneShotTask implements Runnable {
//        private int pulseTime;
//        public TriggerPulseOneShotTask(int pulseTime) {
//            this.pulseTime = pulseTime;
//        }
//        @Override
//        public void run() {
//            pulseTime = Math.max(pulseTime, 5000);
//            readTriggerPulseOneShotResp();
//            close();
//        }
//    }

    //time consuming operation, cannot be used in UI thread
    private void readTriggerPulseOneShotResp(){
        int cmdLength = readCmdLength(false);
        if(cmdLength > 0){
            byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 5000);// + 0x0D
            if(byteArr != null) {
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x21, 0x00}, 3)) {
                    handler.obtainMessage(MDBUtils.BLACK_LOG, "trigger pulse succeed").sendToTarget();
                }else {
                    printLogAndText('e', "trigger pulse one shot failed!");
                }
            } else {
                printLogAndText('e', "trigger pulse one shot failed, read byte array is null!");
            }
        } else {
            Log.e(TAG, "TriggerPulseOneShotTask: read cmd length failed");
        }
    }

    public void getDefaultActiveStatus(){
        sendGetParamDefaultActiveStatus();
        readGetParamDefaultActiveStatus();
    }

    //time consuming operation, cannot be used in UI thread
    private void readGetParamDefaultActiveStatus(){
        int cmdLength = readCmdLength(false);
        if(cmdLength > 0){
            byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 300);// + 0x0D
            if(byteArr != null) {
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x91, 0x07, 0x00}, 4)) {
                    printLogAndText('d', "get default active status succeed");
                    byte[] baActiveStatus = subByteArrayIgnore(byteArr, 4, 4);
                    int nActiveStatus = MDBUtils.byteArrayToIntLittleEndian(baActiveStatus);
                    mdbValues.setDefaultActiveStatus(nActiveStatus);
                } else {
                    printLogAndText('e', "get default active status failed!");
                }
            } else {
                printLogAndText('e', "get default active status failed, read byte array is null!");
            }
        } else {
            Log.e(TAG, "taskGetDefaultActiveStatus: read cmd length failed");
        }
    }

    public void setDefaultActiveStatus() {
        sendSetParamDefaultActiveStatus();
        readSetParamDefaultStatusResp();
    }

    //time consuming operation, cannot be used in UI thread
    private void readSetParamDefaultStatusResp(){
        int cmdLength = readCmdLength(false);
        if(cmdLength > 0){
            byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 5000);// + 0x0D
            if(byteArr != null) {
                if(compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x90, 0x07, 0x00}, 4)) {
                    printLogAndText('d', "set param default status succeed");
                } else {
                    printLogAndText('e', "set param default status failed!");
                }
            } else {
                printLogAndText('e', "set param default status failed, read byte array is null!");
            }
        } else {
            Log.e(TAG, "taskSetParamDefaultStatus: read cmd length failed");
        }
    }

    public void activeCashless(boolean active) {
        sendActiveCashless(active);
        readActiveCashlessResp();
    }

    //time consuming operation, cannot be used in UI thread
    private void readActiveCashlessResp(){
        boolean activeSucceed = false;
        long totalWaitTime = 0;
        do{
            long startTime = System.currentTimeMillis();
            int cmdLength = readCmdLength(false);
            if(cmdLength > 0){
                byte[] byteArr = readFromSerialPortDevice(cmdLength + 1, 1000);// + 0x0D 100ms should be enough
                if(byteArr != null) {
                    if(byteArr[0] == 0x01 && compareByteArrayHead(byteArr, new byte[]{0x01, (byte)0x96}, 2)) {
                        if (compareByteArrayHead(byteArr, new byte[]{0x01, (byte) 0x96, 0x00}, 3)) {
                            printLogAndText('d', "active cashless succeed");
                        } else {
                            printLogAndText('e', "active cashless failed!");
                        }
                        activeSucceed = true;
                    }
                } else {
                    printLogAndText('e', "active cashless failed, read byte array is null!");
                }
            } else {
                Log.e(TAG, "ActiveCashlessTask: read cmd length failed");
            }
            long endTime = System.currentTimeMillis();
            totalWaitTime += (endTime - startTime);
        } while(!activeSucceed && totalWaitTime < 3000);
    }

    //read only once: readDone is true
    //keep reading: readDone is false
    public int readCmdLength(boolean readDone) {
        int ret = -1;
         do{
            byte[] byteArr = readFromSerialPortDevice(1, 5000);
            if(byteArr == null){
                Log.e(TAG, "read cmd length failed, read byte array is null");
                return ret;
            }
            if (Arrays.equals(byteArr, new byte[]{0x09})) {
                byteArr = readFromSerialPortDevice(1, 5000);
                if (byteArr != null && byteArr.length == 1) {
                    ret = byteArr[0];
                    Log.d(TAG, "cmd length: " + ret);
                    readDone = true;
                } else {
                    Log.e(TAG, "read cmd length failed, read byte array is null or length is not 1");
                }
            } else {
                Log.e(TAG, "read cmd length failed, read byte array is not start with 0x09");
            }
        } while(!readDone);
        return ret;
    }

    public void reset(){
        int nResult = -1;
        nResult = StmGpioInterface.ispReset();
        if(nResult < 0)
            onLogSent('e', TAG, "reset failed: " + nResult);
        else
            onLogSent('d', TAG, "reset success");
    }

    public void setBalance(){
        try {
            int balance = MDBUtils.getBalanceFromBigDecimal(mdbValues.getActualPrice(), mdbValues.getX(), mdbValues.getY());
            if(balance > 65534){
                Log.e(TAG, "balance: " + balance);
                printLogAndText('e', "set balance failed, balance out of range");
            } else {
                mdbValues.setBalance(balance);
                printLogAndText('d', "balance value changed: " + mdbValues.getActualPrice());
                Log.d(TAG, mdbValues.toString());
            }
        } catch (ArithmeticException e) {
            printLogAndText('e', "set balance failed, balance/scale factor is illegal!");
        }
    }
    public void setBalance(BigDecimal bigDecimal){
        mdbValues.setActualPrice(bigDecimal);
        setBalance();
    }

    public void setDeviceType(int value){
        if(mdbValues.getDeviceType() != value) {
            mdbValues.setDeviceType(value);
            Log.d(TAG, "setDeviceType: mdbValues.getDeviceType(): " + mdbValues.getDeviceType());
            setParamCashlessAddress();
        }
    }

    public void setCheckboxStatus(CheckBox cb){
        new Thread(()->{
            if(open()) {
                if(mdbValues.getCurrentVersion() == 0) {
                    sendVersionRequest();
                    readGetVersionResp();
                }
                if((isWhiteDemon() && mdbValues.getCurrentVersion() >=28)
                || (!isWhiteDemon() && mdbValues.getCurrentVersion() >= 5)) {
                    getDefaultActiveStatus();
                    Log.d(TAG, "setCheckboxStatus: mdbValues.getDefaultActiveStatus(): " + mdbValues.getDefaultActiveStatus());
                    if (mdbValues.getDefaultActiveStatus() == 1) {
                        runOnUiThread(() -> {
                            cb.setChecked(false);
                        });
                    } else {
                        runOnUiThread(() -> {
                            cb.setChecked(true);
                        });
                    }
                    close();
                }
            }
        }).start();
    }

    public void changeCashlessDefaultActiveStatus(boolean active){
        mdbValues.setDefaultActiveStatus(active ? 0 : 1);
        new Thread(() -> {
            if(open()) {
                setDefaultActiveStatus();
                Log.d(TAG, "changeCashlessDefaultActiveStatus: setDefaultActiveStatus: " + mdbValues.getDefaultActiveStatus());
                getDefaultActiveStatus();
                Log.d(TAG, "changeCashlessDefaultActiveStatus: getDefaultActiveStatus: " + mdbValues.getDefaultActiveStatus());
                close();
            }
        }).start();
    }

    public boolean isWhiteDemon(){
        if((subModel.equalsIgnoreCase(SUBMODEL_Q3A7) || subModel.equalsIgnoreCase(SUBMODEL_Q3V))){
            return true;
        } else if(subModel.equalsIgnoreCase(SUBMODEL_Q3MINI)){
            return false;
        } else {
            Log.e(TAG, "isWhiteDemon: unknown subModel: " + subModel);
            return false;
        }
    }

    public static byte[] subByteArray(byte[] byteArray, int length) {
        byte[] arrySub = new byte[length];
        if (length >= 0) System.arraycopy(byteArray, 0, arrySub, 0, length);
        return arrySub;
    }

    public static byte[] subByteArrayIgnore(byte[] byteArray, int startIndex, int length) {
        byte[] arrySub = new byte[length];
        if (length >= 0) System.arraycopy(byteArray, startIndex, arrySub, 0, length);
        return arrySub;
    }

    private byte[] createBytes(String edit) {
        ArrayList<Byte> as = new ArrayList<>();

        int s1 = 9;
        int e1 = 13;

        int len;
        int lrc;
        String[] split = edit.replace(",", " ").split(" ");
        len = split.length + 1;
        for (String spli : split) {
            as.add((byte) Integer.parseInt(spli, 16));
        }
        byte[] by = new byte[as.size()];
        for (int i = 0; i < as.size(); i++) {
            by[i] = as.get(i);
        }
        lrc = LRCCheckInt(by);
        ArrayList<Byte> as2 = new ArrayList<>();

        as2.add((byte) s1);
        as2.add((byte) len);

        for (byte b : by) {
            as2.add(b);
        }

        as2.add((byte) lrc);
        as2.add((byte) e1);

        byte[] by2 = new byte[as2.size()];
        for (int i = 0; i < as2.size(); i++) {
//            Log.d("ByteConvertStringUtil.buf2StringCompact by2","as2.get(i)" +as2.get(i));
            by2[i] = as2.get(i);
        }

        String s = ByteConvertStringUtil.buf2StringCompact(by2);
        Log.d(TAG, "ss = " + s);
        return by2;
    }

    private int LRCCheckInt(byte[] bytes) {
        int l1 = 0;
        for (byte b : bytes) {
            l1 = l1 + (int) b;
        }
        return ~l1 + 1;
    }

    private String LRCCheck(byte[] bytes) {
        int l1 = 0;
        for (byte b : bytes) {
            l1 = l1 + (int) b;
        }
        return Integer.toHexString(~l1 + 1);
    }

    public static String getTimeWithFormat() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdr.format(time);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLogSent(char c, String tag, String log) {
        switch (c){
            case 'd':
                handler.obtainMessage(MDBUtils.BLACK_LOG, log).sendToTarget();
                Log.d(tag, log);
                break;
            case 'i':
                handler.obtainMessage(MDBUtils.BLACK_LOG, log).sendToTarget();
                Log.i(tag, log);
                break;
            case 'e':
                handler.obtainMessage(MDBUtils.RED_LOG, log).sendToTarget();
                Log.e(tag, log);
                break;
        }
    }

    @Override
    public void onIntValueSent(int type, int value) {
        switch(type){
            case MDBUtils.TYPE_PULSE_INTERVAL:
                pulseValues.setPulseInterval(value);
                break;
            case MDBUtils.TYPE_PULSE_FREQUENCY:
                pulseValues.setPulseFrequency(value);
                break;
            case MDBUtils.TYPE_PULSE_DURATION:
                pulseValues.setPulseDuration(value);
                break;
            case MDBUtils.TYPE_PULSE_VOLTAGE:
                pulseValues.setPulseVoltage(value);
                break;
            case MDBUtils.TYPE_SPN_MDB_LEVEL:
                mdbValues.setMdbLevel(value);
                break;
            case MDBUtils.ENABLE_ALL_UI:
                handler.obtainMessage(MDBUtils.ENABLE_ALL_UI).sendToTarget();
                break;
            case MDBUtils.DISABLE_ALL_UI:
                handler.obtainMessage(MDBUtils.DISABLE_ALL_UI).sendToTarget();
                break;
            case MDBUtils.TYPE_X:
                mdbValues.setX(value);
                break;
            case MDBUtils.TYPE_Y:
                mdbValues.setY(value);
                break;
            case MDBUtils.TYPE_SPN_DEVICE_TYPE:
                setDeviceType(value);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStringValueSent(int type, String value) {
        switch (type){
            case MDBUtils.TYPE_SPN_FIRMWARE_ITEM:
                mdbValues.setFirmwareName(value);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBooleanValueSent(int type, boolean value) {
        switch (type){
            case MDBUtils.TYPE_CB_CHECK_ALWAYS_IDLE:
                LogHelper.massiveLog('d', TAG,
                        "onBooleanValueSent: TYPE_CB_CHECK_ALWAYS_IDLE: mdbValues.getOptionalFeature().setAlwaysIdleMdb: " + value);
                mdbValues.getOptionalFeature().setAlwaysIdleMdb(value);
                LogHelper.massiveLog('e', TAG, mdbValues.getOptionalFeature().toString());
                break;
            case MDBUtils.TYPE_CB_CHECK_32BIT_MONETARY:
                mdbValues.getOptionalFeature().setMonetaryFormat32Mdb(value);
                break;
            case MDBUtils.TYPE_CB_CHECK_NEGATIVE_VEND:
                mdbValues.getOptionalFeature().setNegativeVendAllowedMdb(value);
                break;
            case MDBUtils.TYPE_CB_CHECK_DEFAULT_ACTIVE:
                changeCashlessDefaultActiveStatus(value);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBigDecimalValueSent(int type, BigDecimal value) {
        switch (type){
            case MDBUtils.TYPE_BALANCE:
                setBalance(value);
                break;
            default:
                break;
        }
    }

    @Override
    public void onOpenClicked() {
        open();
    }

    @Override
    public void onCloseClicked() {
        close();
    }

    @Override
    public void onGetVersionClicked() {
        getVersion();
    }

    @Override
    public void onMdbStartClicked() {
        startMdb();
    }

    @Override
    public void onMdbStopClicked() {
        stopMdb();
    }

    @Override
    public void onTestClicked() {
        test();
    }

    Handler delayHandler = new Handler();

    @Override
    public void onTriggerPulseClicked() {
        clickTriggerPulse();
    }

    @Override
    public void onSetPulseClicked() {
        setPulse();
    }

    @Override
    public void onFactoryModeClicked() {
        View view = selectedFragment.getView();
        if(view != null){
            Button button = view.findViewById(R.id.btn_factory_mode);
            String buttonText = button.getText().toString();
            if(buttonText.equalsIgnoreCase("enter factory mode")){
                enterFactoryMode();
            } else if (buttonText.equalsIgnoreCase("quit factory mode")) {
                quitFactoryMode();
            }
        }
    }

    @Override
    public void onGetHardwareVersionClicked() {
        getHardwareVersion();
    }

    @Override
    public void onDiagnoseHardwareClicked() {
        sendHardwareDiagnoseTest();
    }

    @Override
    public void onTransactionFragmentViewCreated(CheckBox cb) {
        setCheckboxStatus(cb);
    }

    public void printLogAndText(char c, String s){
        onLogSent(c, TAG, s);
    }

    public void setFragmentContainerHeight(Fragment fragment){
//        float density = getResources().getDisplayMetrics().density;
        int widthInPx = llFragmentContainer.getWidth();
        if(fragment instanceof HomeFragment){
            int heightInPx = getResources().getDimensionPixelSize(R.dimen.fragment_height_home);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
            llFragmentContainer.setLayoutParams(llParams);
        } else if (fragment instanceof PulseFragment) {
            int heightInPx = getResources().getDimensionPixelSize(R.dimen.fragment_height_pulse);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
            llFragmentContainer.setLayoutParams(llParams);
        } else if (fragment instanceof TransactionFragment) {
            int heightInPx = getResources().getDimensionPixelSize(R.dimen.fragment_height_transaction);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
            llFragmentContainer.setLayoutParams(llParams);
        }
    }

    private void disableAllUiExcept(int... exceptIds) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        MenuItem menuItem = navigationView.getCheckedItem();
        if(menuItem != null){
            int itemId = menuItem.getItemId();
            if(itemId == R.id.nav_home){
                disableHomeUiExcept(exceptIds);
            } else if(itemId == R.id.nav_pulse){
                disablePulseUiExcept(exceptIds);
            } else if(itemId == R.id.nav_transaction){
                disableTransactionUiExcept(exceptIds);
            }
        }
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
    }

    private void enableAllUiExcept(int... exceptIds){
        NavigationView navigationView = findViewById(R.id.nav_view);
        MenuItem menuItem = navigationView.getCheckedItem();
        if(menuItem != null){
            int itemId = menuItem.getItemId();
            if(itemId == R.id.nav_home){
                enableHomeUiExcept(exceptIds);
            } else if(itemId == R.id.nav_pulse){
                enablePulseUiExcept(exceptIds);
            } else if(itemId == R.id.nav_transaction){
                enableTransactionUiExcept(exceptIds);
            }
        }
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
    }

    private void enableAllUi(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        MenuItem menuItem = navigationView.getCheckedItem();
        if(menuItem != null){
            int itemId = menuItem.getItemId();
            if(itemId == R.id.nav_home){
                enableHomeUi();
            } else if(itemId == R.id.nav_pulse){
                enablePulseUi();
            } else if(itemId == R.id.nav_transaction){
                enableTransactionUi();
            }
        }
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
    }

    private void disablePulseUiExcept(int... exceptIds) {
        disablePulseUi();
        for (int id : exceptIds) {
            switch (id) {
                case R.id.et_duration:
                    EditText etDuration = selectedFragment.getView().findViewById(R.id.et_duration);
                    etDuration.setEnabled(true);
                    break;
                case R.id.et_frequency:
                    EditText etFrequency = selectedFragment.getView().findViewById(R.id.et_frequency);
                    etFrequency.setEnabled(true);
                    break;
                case R.id.et_latency:
                    EditText etLatency = selectedFragment.getView().findViewById(R.id.et_latency);
                    etLatency.setEnabled(true);
                    break;
                case R.id.btn_trigger_pulse:
                    Button btnTriggerPulse = selectedFragment.getView().findViewById(R.id.btn_trigger_pulse);
                    btnTriggerPulse.setEnabled(true);
                    break;
                case R.id.btn_set_pulse:
                    Button btnSetPulse = selectedFragment.getView().findViewById(R.id.btn_set_pulse);
                    btnSetPulse.setEnabled(true);
                    break;
                case R.id.spn_voltage:
                    Spinner spnVoltage = selectedFragment.getView().findViewById(R.id.spn_voltage);
                    spnVoltage.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    }
    private void disablePulseUi(){
        EditText etDuration = selectedFragment.getView().findViewById(R.id.et_duration);
        etDuration.setEnabled(false);
        EditText etFrequency = selectedFragment.getView().findViewById(R.id.et_frequency);
        etFrequency.setEnabled(false);
        EditText etLatency = selectedFragment.getView().findViewById(R.id.et_latency);
        etLatency.setEnabled(false);

        Button btnTriggerPulse = selectedFragment.getView().findViewById(R.id.btn_trigger_pulse);
        btnTriggerPulse.setEnabled(false);
        Button btnSetPulse = selectedFragment.getView().findViewById(R.id.btn_set_pulse);
        btnSetPulse.setEnabled(false);

        Spinner spnVoltage = selectedFragment.getView().findViewById(R.id.spn_voltage);
        spnVoltage.setEnabled(false);
    }

    private void enablePulseUiExcept(int... exceptIds){
        enablePulseUi();
        for (int id : exceptIds) {
            switch (id) {
                case R.id.et_duration:
                    EditText etDuration = selectedFragment.getView().findViewById(R.id.et_duration);
                    etDuration.setEnabled(false);
                    break;
                case R.id.et_frequency:
                    EditText etFrequency = selectedFragment.getView().findViewById(R.id.et_frequency);
                    etFrequency.setEnabled(false);
                    break;
                case R.id.et_latency:
                    EditText etLatency = selectedFragment.getView().findViewById(R.id.et_latency);
                    etLatency.setEnabled(false);
                    break;
                case R.id.btn_trigger_pulse:
                    Button btnTriggerPulse = selectedFragment.getView().findViewById(R.id.btn_trigger_pulse);
                    btnTriggerPulse.setEnabled(false);
                    break;
                case R.id.btn_set_pulse:
                    Button btnSetPulse = selectedFragment.getView().findViewById(R.id.btn_set_pulse);
                    btnSetPulse.setEnabled(false);
                    break;
                case R.id.spn_voltage:
                    Spinner spnVoltage = selectedFragment.getView().findViewById(R.id.spn_voltage);
                    spnVoltage.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    }

    private void enablePulseUi(){
        EditText etDuration = selectedFragment.getView().findViewById(R.id.et_duration);
        etDuration.setEnabled(true);
        EditText etFrequency = selectedFragment.getView().findViewById(R.id.et_frequency);
        etFrequency.setEnabled(true);
        EditText etLatency = selectedFragment.getView().findViewById(R.id.et_latency);
        etLatency.setEnabled(true);

        Button btnTriggerPulse = selectedFragment.getView().findViewById(R.id.btn_trigger_pulse);
        btnTriggerPulse.setEnabled(true);
        Button btnSetPulse = selectedFragment.getView().findViewById(R.id.btn_set_pulse);
        btnSetPulse.setEnabled(true);

        Spinner spnVoltage = selectedFragment.getView().findViewById(R.id.spn_voltage);
        spnVoltage.setEnabled(true);
    }

    private void disableTransactionUiExcept(int... exceptIds) {
        disableTransactionUi();
        for (int id : exceptIds) {
            switch (id) {
                case R.id.btn_set_balance:
                    Button btnSetBalance = selectedFragment.getView().findViewById(R.id.btn_set_balance);
                    btnSetBalance.setEnabled(true);
                    break;
                case R.id.et_balance:
                    EditText etBalance = selectedFragment.getView().findViewById(R.id.et_balance);
                    etBalance.setEnabled(true);
                    break;
                case R.id.spn_mdb_level:
                    Spinner spnMdbLevel = selectedFragment.getView().findViewById(R.id.spn_mdb_level);
                    spnMdbLevel.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    }
    private void disableTransactionUi(){
        Button btnSetBalance = selectedFragment.getView().findViewById(R.id.btn_set_balance);
        btnSetBalance.setEnabled(false);
        EditText etBalance = selectedFragment.getView().findViewById(R.id.et_balance);
        etBalance.setEnabled(false);
        Spinner spnMdbLevel = selectedFragment.getView().findViewById(R.id.spn_mdb_level);
        spnMdbLevel.setEnabled(false);
    }

    private void enableTransactionUiExcept(int... exceptIds){
        enableTransactionUi();
        for (int id : exceptIds) {
            switch (id) {
                case R.id.btn_set_balance:
                    Button btnSetBalance = selectedFragment.getView().findViewById(R.id.btn_set_balance);
                    btnSetBalance.setEnabled(false);
                    break;
                case R.id.et_balance:
                    EditText etBalance = selectedFragment.getView().findViewById(R.id.et_balance);
                    etBalance.setEnabled(false);
                    break;
                case R.id.spn_mdb_level:
                    Spinner spnMdbLevel = selectedFragment.getView().findViewById(R.id.spn_mdb_level);
                    spnMdbLevel.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    }

    private void enableTransactionUi(){
        Button btnSetBalance = selectedFragment.getView().findViewById(R.id.btn_set_balance);
        btnSetBalance.setEnabled(true);
        EditText etBalance = selectedFragment.getView().findViewById(R.id.et_balance);
        etBalance.setEnabled(true);
        Spinner spnMdbLevel = selectedFragment.getView().findViewById(R.id.spn_mdb_level);
        spnMdbLevel.setEnabled(true);
    }

    private void disableHomeUiExcept(int... exceptIds) {
        disableHomeUi();
        for (int ids : exceptIds) {
            switch (ids) {
                case R.id.btn_get_version:
                    Button btnGetVersion = selectedFragment.getView().findViewById(R.id.btn_get_version);
                    btnGetVersion.setEnabled(true);
                    break;
                case R.id.btn_factory_mode:
                    Button btnFactoryMode = selectedFragment.getView().findViewById(R.id.btn_factory_mode);
                    btnFactoryMode.setEnabled(true);
                    break;
                case R.id.btn_get_hardware_version:
                    Button btnGetHardwareVersion = selectedFragment.getView().findViewById(R.id.btn_get_hardware_version);
                    btnGetHardwareVersion.setEnabled(true);
                    break;
                case R.id.btn_mdb_start:
                    Button btnStartMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_start);
                    btnStartMdb.setEnabled(true);
                    break;
                case R.id.btn_mdb_stop:
                    Button btnStopMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_stop);
                    btnStopMdb.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    }

    private void disableHomeUi(){
        Button btnGetVersion = selectedFragment.getView().findViewById(R.id.btn_get_version);
        btnGetVersion.setEnabled(false);
        Button btnFactoryMode = selectedFragment.getView().findViewById(R.id.btn_factory_mode);
        btnFactoryMode.setEnabled(false);
        Button btnGetHardwareVersion = selectedFragment.getView().findViewById(R.id.btn_get_hardware_version);
        btnGetHardwareVersion.setEnabled(false);
        Button btnStartMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_start);
        btnStartMdb.setEnabled(false);
        Button btnStopMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_stop);
        btnStopMdb.setEnabled(false);
    }

    private void enableHomeUiExcept(int... exceptIds){
        enableHomeUi();
        for (int id : exceptIds) {
            switch (id) {
                case R.id.btn_get_version:
                    Button btnGetVersion = selectedFragment.getView().findViewById(R.id.btn_get_version);
                    btnGetVersion.setEnabled(false);
                    break;
                case R.id.btn_factory_mode:
                    Button btnFactoryMode = selectedFragment.getView().findViewById(R.id.btn_factory_mode);
                    btnFactoryMode.setEnabled(false);
                    break;
                case R.id.btn_get_hardware_version:
                    Button btnGetHardwareVersion = selectedFragment.getView().findViewById(R.id.btn_get_hardware_version);
                    btnGetHardwareVersion.setEnabled(false);
                    break;
                case R.id.btn_mdb_start:
                    Button btnStartMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_start);
                    btnStartMdb.setEnabled(false);
                    break;
                case R.id.btn_mdb_stop:
                    Button btnStopMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_stop);
                    btnStopMdb.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    }

    private void enableHomeUi(){
        Button btnGetVersion = selectedFragment.getView().findViewById(R.id.btn_get_version);
        btnGetVersion.setEnabled(true);
        Button btnFactoryMode = selectedFragment.getView().findViewById(R.id.btn_factory_mode);
        btnFactoryMode.setEnabled(true);
        Button btnGetHardwareVersion = selectedFragment.getView().findViewById(R.id.btn_get_hardware_version);
        btnGetHardwareVersion.setEnabled(true);
        Button btnStartMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_start);
        btnStartMdb.setEnabled(true);
        Button btnStopMdb = selectedFragment.getView().findViewById(R.id.btn_mdb_stop);
        btnStopMdb.setEnabled(true);
    }

    private void testMdbMasterSend(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                handler.obtainMessage(MDBUtils.BLACK_LOG, "Init, sending reset request").sendToTarget();
                Log.d(TAG, "Init, sending reset request");
                boolean flagEnd = true;
//                int write =  .write(writeData, 0, writeData.length);
                boolean isPolled = false;
                byte[] resetRequest = createBytes();
                if(writeData(resetRequest)) {
                    boolean isReadReset = false;
                    boolean isReadSetupPrice = false;
                    boolean isReadEnableReader = false;
                    boolean isReadBeginSession = false;
                    boolean isReadVendDispatch = false;
                    boolean isReadConfig = false;
                    boolean isReadExpansionId = false;
                    boolean isReadVendApproved = false;
                    boolean isReadEnd = false;
                    boolean isWriteReset = false;
                    boolean isWriteSetupPrice = false;
                    boolean isWriteEnableReader = false;
                    boolean isWriteBeginSession = false;
                    boolean isWriteVendDispatch = false;
                    boolean isWriteConfig = false;
                    boolean isWriteExpansionId = false;
                    boolean isWriteVendApproved = false;
                    boolean isWriteEnd = false;
                    while (flagEnd) {
                        isPolled = false;
                        byte[] readResult = readFromSerialPortDevice(1, 1000);
                        String sp = "";
                        if (readResult != null) {
                            if (ByteConvertStringUtil.buf2StringCompact(readResult).equals("09")) {
                                readResult = readFromSerialPortDevice(1, 500);
                                if (readResult != null) {
                                    String lenthHex = ByteConvertStringUtil.buf2StringCompact(readResult);
                                    BigInteger item = new BigInteger(lenthHex, 16);
                                    readResult = new byte[item.intValue() + 1];
                                    readResult = readFromSerialPortDevice(item.intValue() + 1, 500);
                                    String stringCompact = ByteConvertStringUtil.buf2StringCompact(readResult);
                                    Log.d(TAG, "read success !!! read result : " + stringCompact);
                                    sp = stringCompact;
                                }
                            } else {
                                sp = "";
                                flagEnd = false;
                            }
                            if (sp.startsWith("01 00")) {
                                if(sp.equals("01 00 00 FF 0D")){
                                    isReadReset = true;
                                }
                                if (!isReadConfig && isReadReset) {
                                    //0011000200000114
                                    byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x11, 0x00, 0x02, 0x00, 0x00, 0x01, 0x14});
                                    if(isWriteConfig){
                                        writeData(createPollBytes());
                                    }else if (writeData(wbytes)) {
                                        handler.obtainMessage(MDBUtils.BLACK_LOG, "sending setup config request").sendToTarget();
                                        isWriteConfig = true;
                                    }
                                }else if (!isReadExpansionId && isReadSetupPrice) {
                                    //0017004944533030303031303030363630324944534d4442564d43434f4e303221
                                    byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x17, 0x00, 0x49, 0x44, 0x53, 0x30, 0x30, 0x30, 0x30, 0x31, 0x30, 0x30, 0x30, 0x36, 0x36, 0x30,
                                            0x32, 0x49, 0x44, 0x53, 0x4D, 0x44, 0x42, 0x56, 0x4D, 0x43, 0x43, 0x4F, 0x4E, 0x30, 0x32, 0x21});
                                    if(isWriteExpansionId){
                                        writeData(createPollBytes());
                                    }else if (writeData(wbytes)) {
                                        handler.obtainMessage(MDBUtils.BLACK_LOG, "sending Expansion ID request").sendToTarget();
                                        isWriteExpansionId = true;
                                    }
                                }else if (!isReadBeginSession && isReadEnableReader) {
                                    //00030064ffffffff00000063
//                                    byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x03, 0x00, 0x64, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
//                                            (byte) 0xFF, 0x00, 0x00, 0x00, 0x63});
//                                    if(isWriteBeginSession){
//                                        writeData(createPollBytes());
//                                    }else if (writeData(wbytes)) {
//                                        handler.obtainMessage(MDBUtils.BLACK_LOG, "sending begin session with balance 0064 request").sendToTarget();
                                    isReadBeginSession = true;
                                    isWriteBeginSession = true;
//                                    }
                                }else if (!isReadVendApproved && isReadBeginSession) {
                                    //001300006400e158
                                    byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x13, 0x00, 0x00, 0x64, 0x00, (byte) 0xE1, 0x58});
                                    if(isWriteVendApproved){
                                        writeData(createPollBytes());
                                    }else if (writeData(wbytes)) {
                                        handler.obtainMessage(MDBUtils.BLACK_LOG, "sending vend request with amount 0064").sendToTarget();
                                        isWriteVendApproved = true;
                                    }
                                }else if (!isReadEnd && isReadVendDispatch) {
                                    //00130417
                                    byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x13, 0x04, 0x17});
                                    if(isWriteEnd){
                                        writeData(createPollBytes());
                                    }else if (writeData(wbytes)) {
                                        handler.obtainMessage(MDBUtils.BLACK_LOG, "sending vend session end request").sendToTarget();
                                        isReadEnd = true;
                                        isWriteEnd = true;
                                    }
                                }
                            }
                            if (sp.startsWith("01 01") && !isReadSetupPrice) {
                                isReadConfig = true;
                                //0011012b5c000aa3
                                byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x11, 0x01, 0x2B, 0x5C, 0x00, 0x0A, (byte) 0xA3});
                                if(isWriteSetupPrice){
                                    writeData(createPollBytes());
                                }else if (writeData(wbytes)) {
                                    handler.obtainMessage(MDBUtils.BLACK_LOG, "sending setup max/mix price request").sendToTarget();
                                    isReadSetupPrice = true;
                                    isWriteSetupPrice = true;
                                }
                            }
                            if (sp.startsWith("01 09") && !isReadEnableReader) {
                                isReadExpansionId = true;
                                //00140115
                                byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x14, 0x01, 0x15});
                                if(isWriteEnableReader){
                                    writeData(createPollBytes());
                                }else if (writeData(wbytes)) {
                                    handler.obtainMessage(MDBUtils.BLACK_LOG, "sending enable Reader request").sendToTarget();
                                    isReadEnableReader = true;
                                    isWriteEnableReader = true;
                                }
                            }
                            if (sp.startsWith("01 05") && !isReadVendDispatch) {
                                isReadVendApproved = true;
                                //00130200e1f6
                                byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x13, 0x02, 0x00, (byte) 0xE1, (byte) 0xF6});
                                if(isWriteVendDispatch){
                                    writeData(createPollBytes());
                                }else if (writeData(wbytes)) {
                                    handler.obtainMessage(MDBUtils.BLACK_LOG, "sending vend dispatch request with item 00e1").sendToTarget();
                                    isReadVendDispatch = true;
                                    isWriteVendDispatch = true;
                                }
                            }
                            if (sp.startsWith("01 06")) {
                                byte[] wbytes = MDBUtils.mergePacket(0, new byte[]{0x13, 0x04, 0x17});
                                if(isWriteEnd){
                                    writeData(createPollBytes());
                                }else if (writeData(wbytes)) {
                                    handler.obtainMessage(MDBUtils.BLACK_LOG, "vend denied!! sending vend session end request").sendToTarget();
                                    isReadEnd = true;
                                    isWriteEnd = true;
                                }
                            }
                            if (sp.startsWith("01 07")) {
                                flagEnd = false;
                            }
                        }else if(!isPolled){
                            if(writeData(createPollBytes())){
                                isPolled = true;
                            }
                        }
//                        else if (read == -262254) {
//                            handler.obtainMessage(MDBUtils.RED_LOG, "serial port read timeout").sendToTarget();
//                        } else {
//                            handler.obtainMessage(MDBUtils.RED_LOG, "serial port read failed = " + read).sendToTarget();
//                            Log.d(TAG, "read fail!!");
//                        }
                    }
                }
                else {
                    Log.e(TAG, "write failed!! write data = " + ByteConvertStringUtil.buf2StringCompact(resetRequest));
                }
            }
        }).start();
    }




}