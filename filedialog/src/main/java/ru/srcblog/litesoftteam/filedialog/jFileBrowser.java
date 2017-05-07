/*
        Copyright 2017 Vitaliy Sychov

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */

package ru.srcblog.litesoftteam.filedialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class jFileBrowser extends Activity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,View.OnClickListener{

    public static final int MODE_OPEN = 111;
    public static final int MODE_SAVE = 112;

    private static String ROOT_DIR = "/";

    private jFileBrowser main;

    private String path = "";
    MyAdapter adapter = null;

    private int mode = 0; // диалог открытия или сохранения файла см. константы MODE_*
    private String sFilter = null;

    private AlertDialog errorAlert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        main = this;

        Intent intent = getIntent();
        if(intent != null)
        {
            mode = intent.getIntExtra("mode",0);
            sFilter = intent.getStringExtra("filter");
        }

        setContentView(R.layout.activity_j_file_browser);

        initComponents();
    }

    public void initComponents()
    {
        if(mode == MODE_SAVE) {
            findViewById(R.id.save_panel).setVisibility(View.VISIBLE);
            Button bSave = (Button) findViewById(R.id.save_button);
            bSave.setOnClickListener(this);
        } else
            findViewById(R.id.save_panel).setVisibility(View.GONE);

        GridView list = (GridView) findViewById(R.id.list_view);
        list.setOnItemClickListener(this);
        //list.setOnItemLongClickListener(this);

        ImageButton imgButton = (ImageButton) findViewById(R.id.jump_button);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editPath = (EditText) main.findViewById(R.id.path_edit);
                String temp = editPath.getText().toString();
                File f = new File(temp);
                if(f.exists() && f.isDirectory()) {
                    path = temp;
                    jumpToDir();
                } else
                {
                    // TODO Изменить иконку
                    AlertDialog.Builder builder = new AlertDialog.Builder(main);
                    builder.setTitle(R.string.file_dialog_no_dir)
                            .setMessage(R.string.file_dialog_check_path)
                            .setCancelable(true)
                            .setIcon(R.mipmap.ic_launcher_round)
                            .setPositiveButton(R.string.file_dialog_ok, new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    if(errorAlert != null)
                                        errorAlert.dismiss();
                                }
                            });
                    errorAlert = builder.show();

                }
            }
        });

        adapter = new MyAdapter(this);
        list.setAdapter(adapter);

        String[] arr = listRoot(sFilter);
        refreshList(arr);
    }

    public static String[] listRoot(String filter) {
        return native_list(ROOT_DIR,filter,false);
    }

    public static String[] list(String path,String filter)
    {
        ArrayList<String> list = new ArrayList<>();
        list.add("...");
        String[] arr = native_list(path,filter,false);
        if(arr != null) {
            for (String text : arr)
                list.add(text);
        }
        arr = new String[list.size()];
        for(int i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }

    private static String[] native_list(String path, final String filter,boolean sort)
    {
        File fRoot = new File(path);
        final FilenameFilter fileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                System.out.println("Dir: " + dir.getAbsolutePath() + " name: "+ name);
                if(filter == null)
                    return true;
                else if(name.equalsIgnoreCase(filter) || (new File(dir,name)).isDirectory())
                    return true;

                return false;
            }
        };
        String[] arr = fRoot.list(fileFilter);
        if(arr != null && sort) {
            Arrays.sort(arr, 0, arr.length); // Сортируем для удобного отображения
        }
        return arr;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ElementOfGrid item = adapter.getItem(position);

        selectedItem(item);
    }

    public void selectedItem(ElementOfGrid item)
    {
        System.out.println("onItemClick.old_path: " + path );
        if(item.title.equals("..."))
            path = path.substring(0, path.lastIndexOf('/'));
        else if(item.isDir) // переходим в папку
            path +=  (path.endsWith("/") ? "" : '/') + item.title;
        else {
            if(mode == MODE_OPEN)
                sendPath(item.title);
            else if(mode == MODE_SAVE)
                sendPath(item.title);
            return;
        }

        //Toast.makeText(this,"onItemClick.new_path : " + path ,Toast.LENGTH_LONG).show();
        System.out.println("onItemClick.post_path: " + path );

        jumpToDir();
    }

    public void jumpToDir()
    {
        String[] arr;
        if(path.equals(ROOT_DIR) || path.equals(""))
            arr = listRoot(sFilter);
        else {
            arr = list(path,sFilter);
        }
        refreshList(arr);
    }

    /**
     * Обновляет список файлов
     * @param arr Массив в котором находятся файлы и директории
     */
    public void refreshList(String[] arr)
    {
        EditText editPath = (EditText) main.findViewById(R.id.path_edit);
        editPath.setText(path);

        adapter.clear();
        adapter.notifyDataSetChanged();

        for(int i = 0; i < arr.length; i++) {
            ElementOfGrid element = new ElementOfGrid();
            element.title = arr[i];
            File f = new File(path + '/' + arr[i]);
            element.isDir = element.title.equals("...") ? false : f.isDirectory();
            adapter.add(element);
        }
        adapter.sort(true); // сортируем с учетом папок
        adapter.notifyDataSetChanged(); // принимаем изменения в списке
    }

    /**
     * Нужен для мультивыбора файлов
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return всегда false
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.save_button)
        {
            // TODO локализировать
            createDialog(getString(R.string.file_dialog_button_save), new DialogInterface() {
                @Override
                public void callbackDialog(Dialog sender,boolean accept, String fileName) {
                    if(accept)
                        sendPath(fileName);
                    else
                        sender.dismiss();

                }
            }).show();
        }
    }

    public void sendPath(String fileName)
    {
        Intent i = new Intent();
        i.putExtra("file",path + (path.endsWith("/") ? "" : '/') + fileName);
        setResult(RESULT_OK,i);
        finish();
    }

    private Dialog createDialog(String title,final DialogInterface listener)
    {
        final Dialog d = new Dialog(this);
        d.setTitle(title);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_file_save);
        final EditText edit = (EditText) d.findViewById(R.id.dialog_file_save_file_name_edit);
        View.OnClickListener listener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag;
                String fileName;
                if(v.getId() == R.id.dialog_file_save_button) {
                    flag = true;
                    fileName = edit.getText().toString();
                }else
                {
                        flag = false;
                        fileName = null;
                }
                if(listener != null)
                    listener.callbackDialog(d,flag,fileName);
            }
        };
        d.findViewById(R.id.dialog_file_save_cancel_button).setOnClickListener(listener1);
        d.findViewById(R.id.dialog_file_save_button).setOnClickListener(listener1);
        return d;
    }

}

class MyAdapter extends BaseAdapter
{
    Context c;
    LayoutInflater inflater;
    ArrayList<ElementOfGrid> list;

    public MyAdapter(Context context) {
        c = context;

        inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        list = new ArrayList<>();
    }

    public int getCount()
    {
        return list.size();
    }

    public ElementOfGrid getItem(int index)
    {
        return list.get(index);
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean add(ElementOfGrid obj)
    {
        return list.add(obj);
    }

    public void add(int index, ElementOfGrid obj) {
        list.add(index,obj);
    }

    public  void sort(boolean dirUp)
    {
        if(dirUp)
        {
            ArrayList<ElementOfGrid> listDir = new ArrayList<>();
            ArrayList<ElementOfGrid> listFile = new ArrayList<>();
            for(ElementOfGrid item : list)
            {
                if(item.isDir)
                    listDir.add(item);
                else {
                    if (item.title.equals("..."))
                        return;
                    listFile.add(item);
                }
            }
            sort(listDir);
            sort(listFile);

            boolean flag = list.get(0).title.equals("...");
            list.removeAll(list);
            if(flag)
                list.add(new ElementOfGrid("...",false));
            list.addAll(listDir);
            list.addAll(listFile);
        }else
            sort(list);
    }

    private void sort(ArrayList<ElementOfGrid> list)
    {
        Collections.sort(list, new Comparator<ElementOfGrid>() {
            @Override
            public int compare(ElementOfGrid o1, ElementOfGrid o2) {
                return o1.title.compareTo(o2.title);
            }
        });
    }

    public void clear()
    {
        list.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ElementOfGrid element = getItem(position);
        //if(convertView == null)
        //{
        convertView = inflater.inflate(R.layout.element_of_grid, parent, false);
        //} - если расскоментировать грид при скролле будет отображаться не правильно
        TextView tv = (TextView) convertView.findViewById(R.id.element_of_grid_title);
        tv.setText(element.title);
        ImageView preview = (ImageView) convertView.findViewById(R.id.element_of_grid_preview);
        if(element.title.equals("..."))
            preview.setVisibility(View.GONE);
        else if(element.isDir)
            preview.setImageResource(R.mipmap.folder);
        else if(!element.isDir)
            preview.setImageResource(R.mipmap.file);
        return convertView;
    }

}

interface DialogInterface
{
    void callbackDialog(Dialog sender, boolean accept, String fileName);
}

