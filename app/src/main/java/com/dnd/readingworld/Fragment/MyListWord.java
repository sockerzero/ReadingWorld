package com.dnd.readingworld.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.dnd.readingworld.EditItemMyListWordActivity;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;


public class MyListWord extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    ListView lv;
    ArrayList<ContentWord> List_contentWord;
    MyListWord_ItemAdapter adapter;
    TextToSpeech textToSpeech;
    static final String READING_REFERENCE = "readingworld";
    static final String READING_WORLD_REFERENCE = "readingworld_global_foradmin";
    static final int Edititemlistview = 1996;
    static final int RESULT_OK = 1996;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    EditText edtSearch;



    public MyListWord() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_list_word, container, false);
        setHasOptionsMenu(true);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edtSearch = (EditText) view.findViewById(R.id.editTextSearch);
        lv = (ListView) view.findViewById(R.id.lvMylistword);

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
        mDatabase.child(READING_REFERENCE).child(mFirebaseUser.getUid()).addChildEventListener(new ChildEventListener() {
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("What do you want?");
        dialogBuilder.setPositiveButton("Kill You !!!...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ContentWord pos = adapter.getItem(position);
                String selectedKey = pos.getWordContent()+"_"+pos.getWordType();

                int i = 0;
                for(i=0;i<adapter.getList1().size();i++)
                {
                    if(List_contentWord.get(i).getWordContent().toLowerCase().contains(pos.getWordContent().toLowerCase())
                            &&  List_contentWord.get(i).getWordType().toLowerCase().contains(pos.getWordType().toLowerCase()))
                    {
                        break;
                    }
                }
                adapter.removeItemInarayContentAfterFilter(i);
                if(adapter.getList().size()!=adapter.getList1().size())
                    adapter.removeItemInarayContent(position);
                adapter.notifyDataSetChanged();

                ////Delete node Firebase
                mDatabase.child(READING_REFERENCE).child(mFirebaseUser.getUid()).child(selectedKey).runTransaction(new Transaction.Handler() {
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.setValue(null); // This removes the node.
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }

                });
                mDatabase.child(READING_WORLD_REFERENCE).child(mFirebaseUser.getUid()).child(selectedKey).runTransaction(new Transaction.Handler() {
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.setValue(null); // This removes the node.
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });


                Init.initToast(getActivity(), "Delete Success, Goodbye you :(.");

            }
        }).setNegativeButton("Nothing!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Init.initToast(getActivity(), "Thank you <3");
                    }
                }).setNeutralButton("Edit you!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialoginterface, int which) {

                            ContentWord pos = adapter.getItem(position);
                            String selectedKey = pos.getWordContent()+"_"+pos.getWordType();
                            int i = 0;
                            for(i=0;i<adapter.getList1().size();i++)
                            {
                                if(List_contentWord.get(i).getWordContent().toLowerCase().contains(pos.getWordContent().toLowerCase())
                                        &&  List_contentWord.get(i).getWordType().toLowerCase().contains(pos.getWordType().toLowerCase()))
                                {
                                    break;
                                }
                            }
                            Intent intent=new Intent(getActivity(), EditItemMyListWordActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("SELECTE_KEY",selectedKey);
                            extras.putInt("POSITIONAFTERFILTER",i);
                            extras.putSerializable("ContentMustEdit",pos);
                            extras.putInt("POSITION",position);
                            intent.putExtras(extras);
                            startActivityForResult(intent, Edititemlistview);
                        }
                    });
        dialogBuilder.create().show();
        return true;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Edititemlistview && resultCode == RESULT_OK && null != data)
        {
            Bundle extras = data.getExtras();
            final ContentWord content = (ContentWord) extras.getSerializable("ContentAfterEdit");
            int vitri = extras.getInt("POSITION");
            int vitritruockhisearch = extras.getInt("POSITIONAFTERFILTER");
            String selectedKey = extras.getString("SELECTE_KEY");

            adapter.removeItemInarayContentAfterFilter(vitritruockhisearch);
            if(adapter.getList().size()!=adapter.getList1().size())
                adapter.removeItemInarayContent(vitri);
            adapter.notifyDataSetChanged();


            mDatabase.child(READING_REFERENCE).child(mFirebaseUser.getUid()).child(selectedKey).runTransaction(new Transaction.Handler() {
                public Transaction.Result doTransaction(MutableData mutableData) {
                    mutableData.setValue(null);

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }

            });
            mDatabase.child(READING_WORLD_REFERENCE).child(mFirebaseUser.getUid()).child(selectedKey).runTransaction(new Transaction.Handler() {
                public Transaction.Result doTransaction(MutableData mutableData) {
                    mutableData.setValue(null);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });

            mDatabase.child(READING_REFERENCE).child(mFirebaseUser.getUid()).child(content.getWordContent().toString()+"_"+content.getWordType()).setValue(content);
            mDatabase.child(READING_WORLD_REFERENCE).child(mFirebaseUser.getUid()).child(content.getWordContent().toString()+"_"+content.getWordType()).setValue(content);

            Init.initToast(getActivity(), "Edit Success. In MylistWord class");

        }
    }


}

