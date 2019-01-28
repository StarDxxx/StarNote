package com.example.donxing.starnote.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.donxing.starnote.R;
import com.example.donxing.starnote.bean.Note;
import com.example.donxing.starnote.db.NoteDbHelpBusiness;
import com.example.donxing.starnote.db.NotesDatabaseHelper;
import com.example.donxing.starnote.util.CommonUtil;
import com.example.donxing.starnote.util.ContentToSpannableString;
import com.example.donxing.starnote.util.GlideImageEngine;
import com.example.donxing.starnote.util.UriToPathUtil;
import com.simple.spiderman.CrashModel;
import com.simple.spiderman.SpiderMan;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoteNewActivity extends AppCompatActivity {
    private String NewOrEdit;
    Note oldNote = null;
    private String groupName;
    private EditText editText;
    private List<Uri> mSelected;
    private GlideImageEngine glideImageEngine;
    private int REQUEST_CODE_CHOOSE = 23;
    private boolean isStart = false;      //判断是否开始录音
    private MediaRecorder mediaRecorder = null;
    private int REQUEST_PERMISSION_CODE;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_new);

        ActivityCompat.requestPermissions(NoteNewActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION_CODE);


        //初始化一些变量
        initData();

        Toolbar toolbar_note_new = (Toolbar)findViewById(R.id.toolbar_note_new);
        setSupportActionBar(toolbar_note_new);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar_note_new.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        editText = findViewById(R.id.note_new_editText);
        if(NewOrEdit.equals("New")){

        }else{
            Bundle bundle = getIntent().getBundleExtra("data");
            oldNote = (Note)bundle.getSerializable("OldNote");
            SpannableString spannableString = ContentToSpannableString.Content2SpanStr(NoteNewActivity.this, oldNote.getContent());
            editText.append(spannableString);

            /*
            此时如果用户只是进来看一眼，就不应该删除。
            NoteDbHelpBusiness dbHelpBusiness = NoteDbHelpBusiness.getInstance(this);
            //编辑意味着将旧的便签删除
            dbHelpBusiness.deleteNote(oldNote);
            */
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        //打开这个activity的时候自动获得焦点 + 自动打开软键盘  当editText获得焦点的时候，软键盘就会打开，相当于你点了一下屏幕
        editGetFocus();


        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    Log.d("焦点","获得焦点");
                }else{
                    Log.d("焦点","失去焦点");
                }
            }
        });

        FloatingActionButton btn_note_complete = findViewById(R.id.button_note_new_complete);
        btn_note_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //向数据库中新增一条Note数据
                closeSoftKeyInput();
                editLoseFocus();
                AddNote();
            }
        });

        FloatingActionButton addPic = findViewById(R.id.button_note_new_picture);
        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到 图片选择界面 向当前editText中插入图片
                callGallery();
            }
        });

        final Button addVoice = findViewById(R.id.button_note_new_voice);
        addVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isStart){
                    startRecord();
                    addVoice.setText("停止录音");
                    isStart = true;
                    editText.append("\n");
                }else{
                    stopRecord();
                    addVoice.setText("开始录音");
                    isStart = false;
                    //这是手机emoji上的一个图标
                    editText.append("\uD83C\uDFA4");
                    editText.append("\n");
                }
            }
        });
    }

    private void initData(){
        groupName = getIntent().getStringExtra("groupName");
        Log.d("groupName","传过来的组名是" + groupName);
        if(groupName.equals("全部")){
            groupName = "未分组";
        }

        NewOrEdit = getIntent().getStringExtra("NewOrEdit");
    }

    private void AddNote(){
        String mContent = editText.getText().toString();
        int i = 0;
        String title;
        for(i = 0;i < mContent.length();i++){
            if (mContent.charAt(i) == '\n'){
                break;
            }
        }
        title = mContent.substring(0,i);

        String subContent;
        if(i < mContent.length()){
            int j = 0;
            for(j = i + 1;j < mContent.length();j++){
                if(mContent.charAt(j) == '\n'){
                    break;
                }
            }
            subContent = mContent.substring(i+1,j);
        }else{
            subContent = "";

        }

        Log.d("mContent:", "用户输入的内容是" + mContent);
        Note note = new Note();
        note.setTitle(title);
        note.setSubContent(subContent);
        note.setContent(mContent);
        note.setCreateTime(CommonUtil.date2string(new Date()));
        note.setGroupName(groupName);

        NoteDbHelpBusiness dbBus = NoteDbHelpBusiness.getInstance(this);

        //当用户确定完成编辑之后， 意味着将旧的便签删除
        if(oldNote != null){
            dbBus.deleteNote(oldNote);
        }

        dbBus.addNote(note);

    }

    //关闭软键盘
    private void closeSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void editLoseFocus(){
        editText.clearFocus();
    }

    private void editGetFocus(){
        editText.requestFocus();
    }

    private void callGallery(){
        glideImageEngine = new GlideImageEngine();

        Matisse.from(NoteNewActivity.this)
                .choose(MimeType.ofAll())
                .countable(true)
                .maxSelectable(9)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(glideImageEngine)
                .forResult(REQUEST_CODE_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            if(data != null){
                if(requestCode == 1){

                }else if(requestCode == REQUEST_CODE_CHOOSE){
                    mSelected = Matisse.obtainResult(data);
                    Uri nSelected = mSelected.get(0);

                    //用Uri的string来构造spanStr，不知道能不能获得图片
                    //  ## +  string +  ##  来标识图片  <img src=''>

                    //SpannableString spanStr = new SpannableString(nSelected.toString());
                    SpannableString spanStr = new SpannableString("<img src='" + nSelected.toString() + "'/>");
                    Log.d("图片Uri",nSelected.toString());
                    String path = UriToPathUtil.getRealFilePath(this,nSelected);
                    Log.d("图片Path",path);

                    try{

                        //根据Uri 获得 drawable资源
                        Drawable drawable = Drawable.createFromStream(this.getContentResolver().openInputStream(nSelected),null);
                        drawable.setBounds(0,0,2 * drawable.getIntrinsicWidth(),2 * drawable.getIntrinsicHeight());
                        //BitmapDrawable bd = (BitmapDrawable) drawable;
                        //Bitmap bp = bd.getBitmap();
                        //bp.setDensity(160);
                        ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BASELINE);
                        spanStr.setSpan(span,0,spanStr.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.d("spanString：",spanStr.toString());
                        int cursor = editText.getSelectionStart();
                        editText.getText().insert(cursor, spanStr);
                    }catch (Exception FileNotFoundException){
                        Log.d("异常","无法根据Uri找到图片资源");
                    }
                    //Drawable drawable = NoteNewActivity.this.getResources().getDrawable(nSelected);
                }
            }
        }
    }

    private void startRecord(){
        if(mediaRecorder == null){
            File dir = new File(Environment.getExternalStorageDirectory(),"sounds");
            if (!dir.exists()){
                dir.mkdir();
            }
            File soundFile = new File(dir, System.currentTimeMillis() + ".amr");
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mediaRecorder.setOutputFile(soundFile.getAbsolutePath());

            editText.append("<voice src='" + soundFile.getAbsolutePath() + "'/>");

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void stopRecord(){
        if (mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

}
