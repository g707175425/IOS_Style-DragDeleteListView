package com.example.stronghope.dragdeletelistview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private ArrayList<String> lists = new ArrayList<String>();
    private MyDragDeleteListView view;
    private MyAdapter adapter;

    {
        for (int i = 0;i < 100 ; i++){
            lists.add("我是第:"+i+"行");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new MyDragDeleteListView(this);
        setContentView(view);
        adapter = new MyAdapter();
        view.setAdapter(adapter);
        view.setOnRemoveListener(new MyRemoveListener());
    }

    private class MyRemoveListener implements MyDragDeleteListView.OnRemoveListener {
        @Override
        public void onRemoved(final int position, final MyDragDeleteListView.Direction direction) {
            view.removeItemAnim(position, new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(position>lists.size()-1)return;

                    if(direction == MyDragDeleteListView.Direction.LEFT){
                        System.out.println(lists.get(position) + "驳回");
                    }else{
                        System.out.println(lists.get(position) + "通过");
                    }

                    lists.remove(position);
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

        }
    }

    private class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
            textView.setText(lists.get(position));
            textView.setTextSize(20);
            textView.setBackgroundColor(Color.WHITE);
            return textView;
        }
    }
}
