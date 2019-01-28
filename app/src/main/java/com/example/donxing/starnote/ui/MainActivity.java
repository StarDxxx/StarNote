package com.example.donxing.starnote.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.donxing.starnote.R;
import com.example.donxing.starnote.adapter.NoteAdapter;
import com.example.donxing.starnote.bean.Note;
import com.example.donxing.starnote.db.NoteDbHelpBusiness;
import com.example.donxing.starnote.db.NotesDatabaseHelper;
import com.example.donxing.starnote.view.SpacesItemDecoration;
import com.simple.spiderman.CrashModel;
import com.simple.spiderman.SpiderMan;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,View.OnClickListener {

    NoteAdapter noteAdapter;
    NoteDbHelpBusiness dbBus;
    private LinkedList<Note> notes;
    private String groupName = "全部";
    String showNotesModel;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);

        //用户喜好  首页面 notes布局显示  第一次默认 宫格模式
        SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
        showNotesModel = prefs.getString("ShowNotesModel","宫格模式");


        //弹出崩溃信息展示界面
        SpiderMan.getInstance()
                .init(this)
                //设置是否捕获异常，不弹出崩溃框
                .setEnable(true)
                //设置是否显示崩溃信息展示页面
                .showCrashMessage(true)
                //是否回调异常信息，友盟等第三方崩溃信息收集平台会用到,
                .setOnCrashListener(new SpiderMan.OnCrashListener() {
                    @Override
                    public void onCrash(Thread t, Throwable ex, CrashModel model) {
                        //CrashModel 崩溃信息记录，包含设备信息
                    }
                });

        SearchView searchView =  findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                notes = filter(notes,s);
                refreshAdapter();
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                refreshNotes();
                refreshAdapter();
                return false;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(MainActivity.this, NoteNewActivity.class);
                    intent.putExtra("groupName",groupName);
                    intent.putExtra("NewOrEdit","New");
                    startActivity(intent);
            }
        });

        FloatingActionButton fabVoice = (FloatingActionButton) findViewById(R.id.fab_voice);
        fabVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteVoiceActivity.class);
                startActivity(intent);
            }
        });

        dbBus = NoteDbHelpBusiness.getInstance(this);
        refreshNotes();

        noteAdapter = new NoteAdapter(notes,this);
        noteAdapter.setOnItemClickListener(new NoteAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                Intent intent = new Intent(MainActivity.this,NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Note",note);
                intent.putExtra("data",bundle);
                startActivity(intent);
            }
        });
        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                //弹出一个dialog，用用户选择是否删除
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.setTitle("系统提示：")
                                            .setMessage("确定要删除该便签吗？")
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dbBus.deleteNote(note);
                                                    refreshNotes();
                                                    refreshAdapter();
                                                }
                                            }).create();
                alertDialog.show();
            }
        });

        recyclerView = findViewById(R.id.recycle_view);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new SpacesItemDecoration(3));   //用了card view之后不用再设置分隔线了 不知道上面那句要不要去掉
        recyclerView.setAdapter(noteAdapter);

        //根据用户的喜好设置 来刷新 LayoutManager，完成实时刷新
        refreshLayoutManager();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar_main, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // 根据组名groupName 刷新数据  notes 对象 ，由于groupName的变化，或者其他增删导致 数据变化 ,合理并不会对搜索框的过滤刷新
    private void refreshNotes(){
        if(groupName.equals("全部")){
            notes = dbBus.getAll();
        }else{
            notes = dbBus.getNotesByGroup(groupName);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.list_mode) {
            showNotesModel = "列表模式";
            SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ShowNotesModel",showNotesModel);
            editor.apply();         //editor.commit();
        }else if(id == R.id.grid_model){
            showNotesModel = "宫格模式";
            SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ShowNotesModel",showNotesModel);
            editor.apply();         //editor.commit();
        }

        refreshLayoutManager();

        return super.onOptionsItemSelected(item);
    }

    public void refreshLayoutManager(){
        if(showNotesModel.equals("列表模式")){
            LinearLayoutManager linerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linerLayoutManager);
        }else if(showNotesModel.equals("宫格模式")){
            //GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
            //recyclerView.setLayoutManager(gridLayoutManager);
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);  //两列，纵向排列
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            // Handle the camera action
            groupName = "全部";
        } else if (id == R.id.nav_unGrouped) {
            groupName = "未分组";
        } else if (id == R.id.nav_life) {
            groupName = "生活";
        } else if (id == R.id.nav_work) {
            groupName = "工作";
        } else if (id == R.id.nav_recycle) {
            groupName = "回收站";
        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        changedGroup();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes();
        refreshAdapter();
    }

    private void refreshAdapter(){
        //这里应该是 输入一个组名 刷新这个组名下的 notes
        //刷新 adapter中的数据
        noteAdapter.setNotes(notes);
    }

    private void changedGroup(){
        //select 语句
        //刷新数据
        refreshNotes();
        refreshAdapter();
        //notifacation
    }

    private LinkedList<Note> filter(LinkedList<Note> noteList, String text){
        LinkedList<Note> filterString = new LinkedList<>();
        for(Note note:noteList){
            if(note.getContent().contains(text)){
                filterString.add(note);
            }
        }
        return filterString;
    }
}
