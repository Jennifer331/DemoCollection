package cn.leixiaoyue.lhlearningdemo;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leixiaoyue.lhlearningdemo.draganddrop.DragAndDropActivity;
import cn.leixiaoyue.lhlearningdemo.graffiti.GraffitiActivity;
import cn.leixiaoyue.lhlearningdemo.networkconnection.NetworkActivity;
import cn.leixiaoyue.lhlearningdemo.recycleviewalbum.AlbumActivity;
import cn.leixiaoyue.lhlearningdemo.rotate.RotateActivity;
import cn.leixiaoyue.lhlearningdemo.translateandhide.TranslateViewAcitivity;

/**
 * Created by 80119424 on 2016/2/14.
 */
public class TableOfContent extends ListActivity {
    private static final String ITEM_IMAGE = "item_image";
    private static final String ITEM_TITLE = "item_title";
    private static final String ITEM_DESP = "item_description";
    //Initial Data
    final List<Map<String, Object>> data =new ArrayList<>();
    final SparseArray<Class<? extends Activity>> activityMapping = new SparseArray<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.table_of_contents);
        initData();
        final SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.toc_item,
                new String[]{ITEM_IMAGE, ITEM_TITLE, ITEM_DESP},
                new int[]{R.id.Image, R.id.Title, R.id.SubTitle});
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Class<? extends Activity> activityToLaunch = activityMapping.get(position);
                if (null != activityToLaunch) {
                    final Intent launchIntent = new Intent(TableOfContent.this, activityToLaunch);
                    startActivity(launchIntent);
                }
            }
        });
    }

    private void initData() {
        addActivity(R.drawable.effect_drag_drop,
                getText(R.string.drag_drop).toString(),
                getText(R.string.drag_drop_desp).toString(),
                DragAndDropActivity.class);
        addActivity(R.drawable.effect_rotate,
                getText(R.string.rotate).toString(),
                getText(R.string.rotate_desp).toString(),
                RotateActivity.class);
        addActivity(R.drawable.effect_hide,
                getText(R.string.translate_hide).toString(),
                getText(R.string.translate_hide_desp).toString(),
                TranslateViewAcitivity.class);
        addActivity(R.drawable.effect_network_connection,
                getText(R.string.network_connection).toString(),
                getText(R.string.network_connection_desp).toString(),
                NetworkActivity.class);
        addActivity(R.drawable.effect_graffiti,
                getText(R.string.graffiti).toString(),
                getText(R.string.graffiti_desp).toString(),
                GraffitiActivity.class);
        addActivity(R.drawable.effect_recycleview_album,
                getText(R.string.recycleviewalbum).toString(),
                getText(R.string.recycleviewalbum).toString(),
                AlbumActivity.class);
    }

    private void addActivity(int imageId, String title, String description, Class<? extends Activity> activity) {
        final Map<String, Object> item = new HashMap<>();
        item.put(ITEM_IMAGE, imageId);
        item.put(ITEM_TITLE, title);
        item.put(ITEM_DESP, description);
        data.add(item);
        activityMapping.put(activityMapping.size(), activity);
    }
}
