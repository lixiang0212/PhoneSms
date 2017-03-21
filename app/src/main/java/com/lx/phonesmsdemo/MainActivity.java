package com.lx.phonesmsdemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import static cn.smssdk.SMSSDK.getSupportedCountries;
import static cn.smssdk.SMSSDK.getVerificationCode;
import static cn.smssdk.SMSSDK.submitUserInfo;
import static cn.smssdk.SMSSDK.submitVerificationCode;

public class MainActivity extends AppCompatActivity {
    private EditText et_Number,et_Code;
    private Button btn_getCode,btn_login;
    //手机号和验证码
    private String phoneNumber;
    private String SmsCode;
    //控制按钮样式是否改变
    private boolean tag = true;
    //每次验证请求需要间隔60S
    private int i=60;
    //app key和app secret 需要填自己应用的对应的！
    private final String AppKey = "1b6d84c4f0816";
    private final String AppSecret = "8f7e44affc77ee222a17617c33c59d60";
    private EventHandler eventHandler;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case 0:
                    //客户端验证成功，可以进行注册,返回校验的手机和国家代码phone/country
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    //获取验证码成功
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    //返回支持发送验证码的国家列表
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        SMSSDK.initSDK(this,AppKey,AppSecret);
        initEventHandler();
        initView();
    }

    private void initEventHandler() {
        eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                super.afterEvent(event, result, data);
                if(result == SMSSDK.RESULT_COMPLETE){
                    //回调完成
                    if(event==SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
                        //提交验证码成功
                        Message msg = new Message();
                        msg.arg1 = 0;
                        msg.obj = data;
                        handler.sendMessage(msg);
                        Log.i("AAA","提交验证码成功");
                    }else if(event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                        Message msg = new Message();
                        msg.arg1 = 1;
                        msg.obj = "获取验证码成功";
                        handler.sendMessage(msg);
                        Log.i("AAA","获取验证码成功");
                    }else if(event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        //返回支持发送验证码的国家列表
                        Message msg = new Message();
                        msg.arg1 = 2;
                        msg.obj = "返回支持发送验证码的国家列表";
                        handler.sendMessage(msg);
                        Log.i("AAA", "返回支持发送验证码的国家列表");
                    }
                }else {
                        //返回支持发送验证码的国家列表
                        Message msg = new Message();
                        msg.arg1 = 3;
                        msg.obj = "验证失败";
                        handler.sendMessage(msg);
                        Log.d("AAA", "验证失败");
                        ((Throwable) data).printStackTrace();
                    }
                }
        };
        SMSSDK.registerEventHandler(eventHandler);
    }

    private void initView() {
        et_Number = (EditText) findViewById(R.id.et_getNumber);
        et_Code = (EditText) findViewById(R.id.et_writeCode);
        btn_getCode = (Button) findViewById(R.id.btn_getCode);
        btn_getCode.setClickable(false);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber=et_Number.getText().toString();
                if(phoneNumber.equals("")){
                    Toast.makeText(MainActivity.this,"手机号不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    //填写了手机号码
                    if(isMobileNO(phoneNumber)){
                        //如果手机号码无误，则发送验证请求
                        btn_getCode.setClickable(true);
                        changeBtnGetCode();
                        getSupportedCountries();
                        getVerificationCode("86",phoneNumber);
                    }else{
                        //手机号格式有误
                        Toast.makeText(MainActivity.this,"手机号格式错误，请检查",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsCode = et_Code.getText().toString();
                if (SmsCode.equals("")){
                    Toast.makeText(MainActivity.this,"验证码不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    //填写了验证码，进行验证
                    submitVerificationCode("86", phoneNumber, SmsCode);
                    Toast.makeText(MainActivity.this,"正在检查 验证码",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /*
 * 改变按钮样式
 * */
    private void changeBtnGetCode() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (tag) {
                    while (i > 0) {
                        i--;
                        //如果活动为空
                        if (MainActivity.this == null) {
                            break;
                        }
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_getCode.setText("获取验证码(" + i + ")");
                                btn_getCode.setClickable(false);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    tag = false;
                }
                i = 60;
                tag = true;
                if (MainActivity.this!= null) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_getCode.setText("获取验证码");
                            btn_getCode.setClickable(true);
                        }
                    });
                }
            }
        };
        thread.start();
    }

    //验证输入的手机号是否有效
    private boolean isMobileNO(String phone) {
       /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        String telRegex = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(phone)) return false;
        else return phone.matches(telRegex);
    }

}
