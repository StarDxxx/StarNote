package com.example.donxing.starnote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donxing.starnote.R;
import com.example.donxing.starnote.bean.Note;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static int color;
    private LinkedList<Note> notes;
    private Context mContext;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener;

    private static final int[] colors = new int[]{R.color.color_0,R.color.color_1,
            R.color.color_2,R.color.color_3,R.color.color_4,
            R.color.color_5,R.color.color_6,R.color.color_7,
            R.color.color_8,R.color.color_9,R.color.color_10,};

    public NoteAdapter(LinkedList<Note> notes, Context mContext) {
        this.notes = notes;
        this.mContext = mContext;
    }

    @Override
    public void onClick(View view){
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(view, (Note) view.getTag());
        }
    }
    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemLongClickListener.onItemLongClick(v, (Note) v.getTag());
        }
        return true;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setNotes(LinkedList<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        //注册点击事件
        convertView.setOnClickListener(this);
        convertView.setOnLongClickListener(this);
        ViewHolder viewHolder = new ViewHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        Note note = notes.get(position);

        int id = note.getId();
        viewHolder.cardView.setCardBackgroundColor(mContext.getResources().getColor(colors[id%11]));
        color = color + 1;
        viewHolder.itemView.setTag(note);

        String content = note.getContent();
        String subContent = note.getSubContent();
        String pattern = "<img src='(.*?)'/>";
        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(content);
        if(m.find()){
            try{
                Uri uri = Uri.parse(m.group(1));
                Drawable drawable = Drawable.createFromStream(mContext.getContentResolver().openInputStream(uri),null);
                drawable.setBounds(0,0,2 * drawable.getIntrinsicWidth(),2 * drawable.getIntrinsicHeight());
                viewHolder.imageView.setImageDrawable(drawable);
                subContent = subContent.replace("<img src='" + uri + "'/>", "");
            }catch (Exception FileNotFindException){
                Log.d("找到图片","不能根据当前Uri找到图片");
            }
        }else{
            viewHolder.imageView.setImageDrawable(null);
            Log.d("匹配","没有完成匹配");
        }

        viewHolder.title.setTextColor(Color.rgb(0, 0, 0));   viewHolder.title.setText(note.getTitle());
        viewHolder.subContent.setText(subContent);

        String createTime = note.getCreateTime();

        //如果是今年新建的便签的话，没必要显示年份
        if(createTime.contains("2019年")){
            createTime = createTime.replace("2019年","");
        }
        viewHolder.createTime.setText(createTime);
    }

    public interface OnRecyclerViewItemClickListener{
        void onItemClick(View view,Note note);
    }

    public interface OnRecyclerViewItemLongClickListener{
        void onItemLongClick(View view, Note note);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public ImageView imageView;
        public TextView title;
        public TextView subContent;
        public TextView createTime;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.card_view);
            this.imageView = itemView.findViewById(R.id.note_img);
            this.title = itemView.findViewById(R.id.note_title);
            this.subContent = itemView.findViewById(R.id.note_subContent);
            this.createTime = itemView.findViewById(R.id.note_createTime);
        }

    }

    public void add(Note note){
        if(notes == null){
            notes = new LinkedList<Note>();
        }
        notes.add(note);
        notifyDataSetChanged();
    }
}
