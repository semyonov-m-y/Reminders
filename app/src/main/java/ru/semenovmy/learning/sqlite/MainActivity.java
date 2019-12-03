package ru.semenovmy.learning.sqlite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final LinkedHashMap<Integer, Integer> mIDmap = new LinkedHashMap<>();
    private final MultiSelector mMultiSelector = new MultiSelector();

    private FloatingActionButton mAddReminderButton;
    private RecyclerView mList;
    private ReminderDatabase mReminderDatabase;
    private RecyclerViewAdapter mAdapter;
    private int mTempPost;
    private String mTitle;
    private int mID;
    private List<TitleSorter> TitleSortList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReminderDatabase = new ReminderDatabase(getApplicationContext());
        mAddReminderButton = findViewById(R.id.add_reminder);
        mList = findViewById(R.id.reminder_list);

        EditText editText = new EditText(this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitle = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAddReminderButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Добавьте напоминание:");
            builder.setView(editText);
            builder.setNegativeButton("Подтвердить",
                    (dialog, which) -> {
                        mID = mReminderDatabase.addReminder(new Reminder(mTitle, "false"));
                        createRecyclerView();
                    });
            builder.setNeutralButton("Отмена",
                    (dialog, which) -> doNothing());
            builder.show();
        });

        createRecyclerView();
    }

    public void doNothing() {

    }

    private void createRecyclerView() {
        mList.setLayoutManager(getLayoutManager());
        registerForContextMenu(mList);
        mAdapter = new RecyclerViewAdapter();
        mAdapter.setItemCount(getDefaultItemCount());
        mList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }

    private int getDefaultItemCount() {
        return 100;
    }

    private final androidx.appcompat.view.ActionMode.Callback mDeleteMode = new ModalMultiSelectorCallback(mMultiSelector) {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode actionMode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_reminder, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                case R.id.discard_reminder:
                    // Закрыть меню
                    actionMode.finish();

                    // Получить id напоминания равному элементу Recycler View
                    for (int i = mIDmap.size(); i >= 0; i--) {
                        if (mMultiSelector.isSelected(i, 0)) {
                            int id = mIDmap.get(i);

                            Reminder temp = mReminderDatabase.getReminder(id);
                            mReminderDatabase.deleteReminder(temp);
                            mAdapter.removeItemSelected(i);
                        }
                    }

                    mMultiSelector.clearSelections();

                    // Пересоздать Recycler View, чтобы переопределить элементы
                    mAdapter.onDeleteItem(getDefaultItemCount());

                    return true;

                default:
                    break;
            }
            return false;
        }
    };

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VerticalItemHolder> {

        private final ArrayList<ReminderItem> mItems;

        RecyclerViewAdapter() {
            mItems = new ArrayList<>();
        }

        void setItemCount(int count) {
            mItems.clear();
            mItems.addAll(generateListData(count));
            notifyDataSetChanged();
        }

        void onDeleteItem(int count) {
            mItems.clear();
            mItems.addAll(generateListData(count));
        }

        void removeItemSelected(int selected) {
            if (mItems.isEmpty()) return;
            mItems.remove(selected);
            notifyItemRemoved(selected);
        }

        @NonNull
        @Override
        public VerticalItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            View root = inflater.inflate(R.layout.recycle_items, container, false);

            return new VerticalItemHolder(root, this);
        }

        @Override
        public void onBindViewHolder(VerticalItemHolder itemHolder, int position) {
            ReminderItem item = mItems.get(position);
            itemHolder.setReminderTitle(item.mTitle);
            itemHolder.setActive(item.mActive, position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        /**
         * Класс для UI и данных для Recycler View
         */
        public class VerticalItemHolder extends SwappingHolder
                implements View.OnClickListener, View.OnLongClickListener {

            public final TextView mTitleText;
            public final CheckBox mActive;
            private final RecyclerViewAdapter mAdapter;

            VerticalItemHolder(View itemView, RecyclerViewAdapter adapter) {
                super(itemView, mMultiSelector);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                itemView.setLongClickable(true);

                mAdapter = adapter;

                mTitleText = itemView.findViewById(R.id.recycle_title);
                mActive = itemView.findViewById(R.id.active);
                mActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        for (int i = mIDmap.size(); i >= 0; i--) {
                            if ( mIDmap.get(i) != null && b) {
                                int id = mIDmap.get(i);

                                Reminder temp = mReminderDatabase.getReminder(id);
                                temp.setActive("true");
                                mReminderDatabase.updateReminder(temp);
                            } else if ( mIDmap.get(i) != null && !b) {
                                int id = mIDmap.get(i);

                                Reminder temp = mReminderDatabase.getReminder(id);
                                temp.setActive("false");
                                mReminderDatabase.updateReminder(temp);
                            }
                        }
                    }
                });
            }

            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(this)) {
                    mTempPost = mList.getChildAdapterPosition(v);

                } else if (mMultiSelector.getSelectedPositions().isEmpty()) {
                    mAdapter.setItemCount(getDefaultItemCount());
                }
            }

            @Override
            public boolean onLongClick(View v) {
                AppCompatActivity activity = MainActivity.this;
                activity.startSupportActionMode(mDeleteMode);
                mMultiSelector.setSelected(this, true);
                v.setBackgroundColor(Color.DKGRAY);
                return true;
            }

            void setReminderTitle(String title) {
                mTitleText.setText(title);
                String letter = "A";

                if (title != null && !title.isEmpty()) {
                    letter = title.substring(0, 1);
                }
            }

            /**
             * Метод для установки картинки для элемента списка
             */
            void setActive(String active, int id) {
                if (active.equals("true")) {
                    mActive.setChecked(true);
                    Reminder temp = mReminderDatabase.getAllReminders().get(id);
                    temp.setActive("true");
                    mReminderDatabase.updateReminder(temp);
                } else if (active.equals("false")) {
                    mActive.setChecked(false);
                    Reminder temp = mReminderDatabase.getAllReminders().get(id);
                    temp.setActive("true");
                    mReminderDatabase.updateReminder(temp);
                }
            }
        }

        /**
         * Метод для генерации данных списка
         */
        List<ReminderItem> generateListData(int count) {
            ArrayList<ReminderItem> items = new ArrayList<>();

            List<Reminder> reminders = mReminderDatabase.getAllReminders();
            List<String> Titles = new ArrayList<>();
            List<String> Actives = new ArrayList<>();
            List<Integer> IDList = new ArrayList<>();
            TitleSortList = new ArrayList<>();

            for (Reminder r : reminders) {
                Titles.add(r.getTitle());
                Actives.add(r.getActive());
                IDList.add(r.getID());
            }

            int key = 0;

            for (int k = 0; k < Titles.size(); k++) {
                TitleSortList.add(new TitleSorter(key, Titles.get(k)));
                key++;
            }

            int k = 0;

            // Добавляем данные в каждый элемент списка
            for (TitleSorter item : TitleSortList) {
                int i = item.getIndex();

                items.add(new ReminderItem(Titles.get(i), Actives.get(i)));

                mIDmap.put(k, IDList.get(i));
                k++;
            }

            return items;
        }
    }
}
