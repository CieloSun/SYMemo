package com.symemo;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import com.baoyz.swipemenulistview.BaseSwipListAdapter;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import java.util.List;

public class SimpleActivity extends Activity {
    private List<BookItem> mList;
    private BaseSwipListAdapter mAdapter;
    private SwipeMenuListView mListView;
    private BookItemHelper bookItemHelper;
    public FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        floatingActionButton=(FloatingActionButton)findViewById(R.id.fab_upload);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SimpleActivity.this,SyncActivity.class));
            }
        });
    }
    @Override
    public void onStart(){
        super.onStart();
        bookItemHelper = new BookItemHelper(SimpleActivity.this);
        mList = bookItemHelper.getMyList();
        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        mAdapter = new BaseSwipListAdapter() {
            @Override
            public int getCount() {
                return mList.size();
            }
            @Override
            public BookItem getItem(int position) {
                return mList.get(position);
            }
            @Override
            public long getItemId(int position) {
                return position;
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                class ViewHolder {
                    TextView tx_clock;
                    TextView tv_name;
                    public ViewHolder(View view) {
                        tx_clock=(TextView) view.findViewById(R.id.tx_clock);
                        tv_name = (TextView) view.findViewById(R.id.tv_name);
                        view.setTag(this);
                    }
                }
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(),
                            R.layout.item_list_app, null);
                    new ViewHolder(convertView);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                final BookItem item = getItem(position);
                holder.tv_name.setText(item.getContent());
                holder.tx_clock.setText(item.getTime());
                holder.tv_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        open(item);
                    }
                });
                return convertView;
            }

        };
        mListView.setAdapter(mAdapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        mListView.setMenuCreator(creator);
        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                                                 @Override
                                                 public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                                                     BookItem item = mList.get(position);
                                                     switch (index) {
                                                         case 0:
                                                             // open
                                                             open(item);
                                                             break;
                                                         case 1:
                                                             // delete
                                                             bookItemHelper.deleteItem(item);
                                                             mList = bookItemHelper.getMyList();
                                                             mAdapter.notifyDataSetChanged();
                                                             break;
                                                     }
                                                     return false;
                                                 }
                                             }

        );

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener(){
            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }

        });
        // set MenuStateChangeListener
        mListView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });
        //set Item Click Long
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }
    private void open(BookItem item){
        Intent intent=new Intent();
        intent.putExtra("id",item.getId());
        intent.putExtra("content",item.getContent());
        intent.putExtra("time",item.getTime());
        setResult(RESULT_OK, intent);
        finish();
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_left) {
            mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
            return true;
        }
        if (id == R.id.action_right) {
            mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}