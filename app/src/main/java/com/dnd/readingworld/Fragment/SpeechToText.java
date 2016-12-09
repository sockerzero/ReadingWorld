package com.dnd.readingworld.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dnd.readingworld.Init.Init;
import com.dnd.readingworld.Model.ContentWord;
import com.dnd.readingworld.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.dnd.readingworld.R.id.speech;


public class SpeechToText extends Fragment {

    private static final int REQUEST_CODE = 1234;
    private static int SELECT_PHOTO = 1;
    static final String READING_REFERENCE = "readingworld";
    static final String READING_WORLD_REFERENCE = "readingworld_global_foradmin";
    ImageButton Start;
    Button imageButton, addButton;
    Spinner spinner;
    EditText Speech;
    ImageView imageView;
    Dialog match_text_dialog;
    ListView textlist;
    ArrayList<String> matches_text;
    //ContentWord contentWord;
    String wordType;

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speech_to_text, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Anh xa
        Start = (ImageButton) view.findViewById(R.id.start_reg);
        Speech = (EditText) view.findViewById(speech);
        imageButton = (Button) view.findViewById(R.id.btnImage);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        spinner = (Spinner) view.findViewById(R.id.spinner);
        addButton = (Button) view.findViewById(R.id.btnAdd);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.wordtype_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyProcessEvent());

        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Init.CheckConnect(getActivity())){
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                else{
                    Init.initToast(getActivity(),"Plese Connect to Internet");
                }}
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), SELECT_PHOTO);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData())
                {
                    StorageReference storageRef = storage.getReferenceFromUrl(Init.URL_STORAGE_REFERENCE).child(Init.FOLDER_STORAGE_IMG);
                    sendFileFirebase(storageRef);
                    Init.initToast(getActivity(),"Add success!");

                }
                else
                {
                    Init.initToast(getActivity(),"Please fill all textbox!");
                }
            }
        });
    }


    public boolean checkData()
    {
        if(Speech.getText().length()==0)
        {
            Speech.requestFocus();
            Speech.setError("FIELD CANNOT BE EMPTY");
            Init.initToast(getActivity(),"Please Write or speech word content");
            return false;
        }
        else if(imageView.getDrawable() == null)
        {
            Init.initToast(getActivity(),"Please Choose Image");
            return false;
        }
        else if (wordType.length()==0)
        {
            Init.initToast(getActivity(),"Please choose Word Type");
            return false;
        }
        else return true;
    }

    //Class tạo sự kiện
    private class MyProcessEvent implements AdapterView.OnItemSelectedListener {
        //Khi có chọn lựa thì vào hàm này
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            wordType = parent.getItemAtPosition(pos).toString();
        }

        //Nếu không chọn gì cả
        public void onNothingSelected(AdapterView<?> parent) {
            TextView errorText = (TextView)spinner.getSelectedView();
            errorText.setError("FIELD CANNOT BE EMPTY");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            match_text_dialog = new Dialog(this.getActivity());
            match_text_dialog.setContentView(R.layout.fmspeechtotext_listviewofitemspeech);
            match_text_dialog.setTitle("Select Matching Text");
            textlist = (ListView)match_text_dialog.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter =    new ArrayAdapter<String>(this.getActivity(),
                    android.R.layout.simple_list_item_1, matches_text);
            textlist.setAdapter(adapter);
            textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Speech.setText(matches_text.get(position));
                    match_text_dialog.hide();
                }
            });
            match_text_dialog.show();

        }

        else if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && null != data) {
            Uri selectedImages = data.getData();
            imageView.setImageURI(selectedImages);
        }
    }

    public void sendFileFirebase(StorageReference storageReference){
        if (storageReference != null){
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child(name+"_gallery"+".png");
            imageView.invalidate();
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = imageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = imageGalleryRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Init.initToast(getActivity(),"Loi SendFileFirebase");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    ContentWord contentWord = new ContentWord(Speech.getText().toString(),wordType,String.valueOf(downloadUrl));
                    mDatabase.child(READING_REFERENCE).child(mFirebaseUser.getUid()).child(Speech.getText().toString()+"_"+wordType).setValue(contentWord);
                    mDatabase.child(READING_WORLD_REFERENCE).child(mFirebaseUser.getUid()).child(Speech.getText().toString()+"_"+wordType).setValue(contentWord);
                    Speech.setText("");
                    imageView.setImageResource(0);
                }
            });
        }else{
            //IS NULL
        }
    }
}

