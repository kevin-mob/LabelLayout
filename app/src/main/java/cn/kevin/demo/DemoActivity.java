package cn.kevin.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.kevin.labellayout.LabelAdapter;
import cn.kevin.labellayout.LabelLayout;

public class DemoActivity extends AppCompatActivity {
    LabelLayout labelLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labelLayout = (LabelLayout) findViewById(R.id.label_layout);
        List<String> list = new ArrayList<>();
        list.add("年轻有为");
        list.add("先挣一个亿");
        list.add("钻石王老五");
        list.add("呵呵");
        list.add("玉树临风");
        list.add("风流倜傥人见人爱花见花开车见车爆胎");
        list.add("人中吕布马中赤兔");
        list.add("升职加薪");
        list.add("当上总经理");
        list.add("出任ceo");
        list.add("迎娶白富美");
        list.add("走上人生巅峰，想想还有点小激动");
        TextLabelAdapter adapter = new TextLabelAdapter(list);
        labelLayout.setAdapter(adapter);
    }

    class TextLabelAdapter extends LabelAdapter<String> {
        public TextLabelAdapter(List<String> models) {
            super(models);
        }
        @Override
        public void onDataSet(View labelView, String item) {
            ((TextView)labelView).setText(item);
        }

        @Override
        public View getLabelView(ViewGroup parent) {
            return LayoutInflater.from(DemoActivity.this).inflate(R.layout.label, parent, false);
        }
    }
}
