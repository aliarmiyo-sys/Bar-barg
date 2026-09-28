package com.barbarg.robot;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String URL = "https://barname.utcms.ir/";
    WebView web; EditText queue; TextView status; Button start, stop;
    ArrayList<String> rows = new ArrayList<>(); int index = 0; boolean running = false;

    @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(R.layout.activity_main);
        web=findViewById(R.id.web); queue=findViewById(R.id.queue); status=findViewById(R.id.status); start=findViewById(R.id.start); stop=findViewById(R.id.stop);
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setSupportZoom(false); s.setBuiltInZoomControls(false); s.setUserAgentString(s.getUserAgentString()+" BarbargRobot/1.0");
        web.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String u){ status.setText("سامانه باز شد. صف: "+(index+1)+" / "+rows.size()); if(running) new Handler().postDelayed(()->runAutomation(),1200); }});
        start.setOnClickListener(v->startQueue()); stop.setOnClickListener(v->{running=false; status.setText("متوقف شد");});
    }
    void startQueue(){ rows.clear(); for(String x: queue.getText().toString().split("\\n")){ if(!x.trim().isEmpty()) rows.add(x.trim()); } if(rows.isEmpty()){Toast.makeText(this,"صف راننده خالی است",Toast.LENGTH_SHORT).show();return;} index=0; running=true; status.setText("شروع صف، "+rows.size()+" راننده"); web.loadUrl(URL); }
    void runAutomation(){ if(!running || index>=rows.size()){ if(index>=rows.size()) status.setText("صف تمام شد"); return; } final String row=rows.get(index); final String[] p=row.split("\\|",-1); if(p.length<8){ status.setText("خطای فرمت در ردیف "+(index+1)); running=false; return; }
        String js="(function(){"+
          "function all(){return Array.from(document.querySelectorAll('input,textarea,select'));}"+
          "function find(keys){let a=all();for(let e of a){let t=((e.placeholder||'')+' '+(e.name||'')+' '+(e.id||'')+' '+(e.getAttribute('aria-label')||'')).toLowerCase();if(keys.some(k=>t.includes(k)))return e;}return null;}"+
          "function set(e,v){if(!e)return false;e.focus();let s=Object.getOwnPropertyDescriptor(Object.getPrototypeOf(e),'value');if(s&&s.set)s.set.call(e,v);else e.value=v;e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));return true;}"+
          "let vals="+json(p[0])+","+json(p[1])+","+json(p[2])+","+json(p[3])+","+json(p[4])+","+json(p[5])+","+json(p[6])+","+json(p[7])+";"+
          "set(find(['کد ملی','کدملی','national','nationalid']),vals[0]);set(find(['پلاک','plate','pelak']),vals[1]);set(find(['نام راننده','نام','driver']),vals[2]);set(find(['مبدا','مبدأ','origin']),vals[3]);set(find(['مقصد','destination']),vals[4]);set(find(['کالا','cargo','نوع کالا']),vals[5]);set(find(['وزن','weight']),vals[6]);set(find(['کرایه','fare','مبلغ']),vals[7]);"+
          "return 'fields-filled';})()";
        web.evaluateJavascript(js, value->{ status.setText("راننده "+(index+1)+" از "+rows.size()+": اطلاعات اولیه وارد شد؛ بررسی OTP/CAPTCHA و ثبت نهایی با کاربر است."); showOtpDialog(); });
    }
    String json(String s){ return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\""; }
    void showOtpDialog(){ if(!running)return; final EditText in=new EditText(this); in.setHint("کد پیامک را وارد کنید (در صورت درخواست سامانه)"); new AlertDialog.Builder(this).setTitle("تأیید راننده").setMessage("اگر سامانه کد پیامکی خواست، کد را فقط از مسیر مجاز دریافت و اینجا وارد کنید. اگر کدی لازم نیست، ادامه را بزنید.").setView(in).setNegativeButton("توقف",(d,w)->{running=false;status.setText("متوقف شد");}).setPositiveButton("ادامه",(d,w)->{ if(running){ index++; if(index<rows.size()){ status.setText("رفتن به راننده بعدی..."); web.reload(); } else {running=false;status.setText("صف تمام شد");} }}).setCancelable(false).show(); }
}
