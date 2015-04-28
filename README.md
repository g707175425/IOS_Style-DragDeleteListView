#DragdeleteListView 
####(仿IOS风格拖拽删除ListView)

###预览效果:


######代码中实现:
	MyDragDeleteListView view = new MyDragDeleteListView(this);
    setContentView(view);
    BaseAdapter adapter = new BaseAdapter();
    view.setAdapter(adapter);
    view.setOnRemoveListener(new MyRemoveListener());//设置拖拽删除监听器
	
#######若想要实现删除后剩余行向删除位置移动的动画,需要在监听器中调用下面的方法,在动画结束的回调中进行adapter.notifydatasetchanged()刷新数据:

    private class MyRemoveListener implements MyDragDeleteListView.OnRemoveListener {
        @Override
        public void onRemoved(final int position, final MyDragDeleteListView.Direction direction) {
            view.removeItemAnim(position, new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(position>lists.size()-1)return;//防止偶尔出现越界现象,最好加入判断.

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




by QQ:707175425