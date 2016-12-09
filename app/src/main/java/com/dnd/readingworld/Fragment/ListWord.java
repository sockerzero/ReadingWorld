package com.dnd.readingworld.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.dnd.readingworld.Adapter.MyListWord_ItemAdapter;
import com.dnd.readingworld.Init.Init;
import com.dnd.readingworld.Model.ContentWord;
import com.dnd.readingworld.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListWord extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    ListView lv;
    ArrayList<ContentWord> List_contentWord;
    MyListWord_ItemAdapter adapter;

    static final String READING_REFERENCE = "readingworld";
    static final String READING_WORLD_REFERENCE = "readingworld_global_foruser";
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    EditText edtSearch;

    public ListWord() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_word, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edtSearch = (EditText) view.findViewById(R.id.editTextSearch);

        lv = (ListView) view.findViewById(R.id.lvlistword);
        List_contentWord = new ArrayList<ContentWord>();
        adapter = new MyListWord_ItemAdapter(getActivity(), R.layout.fmmylistword_listview_item, List_contentWord);
        lv.setAdapter(adapter);
        if(mFirebaseUser!=null)
            loadData();

        lv.setOnItemLongClickListener(this);
        lv.setOnItemClickListener(this);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void loadData()
    {
        mDatabase.child(READING_WORLD_REFERENCE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ContentWord cont = dataSnapshot.getValue(ContentWord.class);
                List_contentWord.add(new ContentWord(cont.getWordContent(), cont.getWordType(), cont.getLinkImage()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {



        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_listview_mylistword_zoomitem);
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int width = metrics.widthPixels;
        int height = (4 *metrics.heightPixels)/5;
        dialog.getWindow().setLayout( width,  height);

        final ImageView imgView = (ImageView) dialog.findViewById(R.id.imgViewZoom);


        ContentWord pos = adapter.getItem(position);

        Glide.with(this).load(pos.getLinkImage()).asBitmap().fitCenter().into(imgView);

        dialog.show();
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("What do you want?");
        dialogBuilder.setPositiveButton("Add to MyListWorld", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ContentWord pos = adapter.getItem(position);
                String selectedKey = pos.getWordContent()+"_"+pos.getWordType();

                mDatabase.child(READING_REFERENCE).child(mFirebaseUser.getUid()).child(selectedKey).setValue(pos);
                
                Init.initToast(getActivity(), "Add success :).");

            }
        }).setNegativeButton("Nothing!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Init.initToast(getActivity(), "Thank you <3");
            }
        });

        dialogBuilder.create().show();
        return true;
    }
}
