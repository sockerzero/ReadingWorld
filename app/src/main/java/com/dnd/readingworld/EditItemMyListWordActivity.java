package com.dnd.readingworld;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dnd.readingworld.Init.Init;
import com.dnd.readingworld.Model.ContentWord;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import static com.dnd.readingworld.R.id.speech;

/**
 * Created by Asus on 12/2/2016.
 */

public class EditItemMyListWordActivity extends AppCompatActivity {
    ImageButton Start;
    Button imageButton, editButton;
    Spinner spinner;
    EditText Speech;
    ImageView imageView;
    String wordType;
    Dialog match_text_dialog;
    ListView textlist;
    ArrayList<String> matches_text;
    Intent intent;
    Bundle extras;

    private static final int REQUEST_CODE = 1234;
    static final int Edititemlistview = 1996;
    private static int SELECT_PHOTO = 1;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edititem_mylistword);



        //Anh xa
        Start = (ImageButton) findViewById(R.id.start_reg);
        Speech = (EditText) findViewById(speech);
        imageButton = (Button) findViewById(R.id.btnImage);
        imageView = (ImageView) findViewById(R.id.imageView);
        spinner = (Spinner) findViewById(R.id.spinner);
        editButton = (Button) findViewById(R.id.btnEdit);


        storage = FirebaseStorage.getInstance();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.wordtype_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyProcessEvent());

        //Nhận giá trị Edit trong listview của Mylistword
        //Đổ dữ liệu cũ lên
        intent = getIntent();
        extras = intent.getExtras();
        ContentWord content = (ContentWord) extras.getSerializable("ContentMustEdit");

        if(content.getWordContent()!="")
        {
            Speech.setText(content.getWordContent());
            int spinnerPosition = adapter.getPosition(content.getWordType());
            spinner.setSelection(spinnerPosition);
            Glide.with(this).load(content.getLinkImage()).asBitmap().fitCenter().into(imageView);
        }

        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Init.CheckConnect(EditItemMyListWordActivity.this)){
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                else{
                    Init.initToast(EditItemMyListWordActivity.this,"Plese Connect to Internet");

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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData())
                {
                    StorageReference storageRef = storage.getReferenceFromUrl(Init.URL_STORAGE_REFERENCE).child(Init.FOLDER_STORAGE_IMG);
                    sendFileFirebase(storageRef);
                }
                else
                {
                    Init.initToast(EditItemMyListWordActivity.this,"Please fill all textbox!");
                }
            }
        });
    }

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

    public boolean checkData()
    {
        if(Speech.getText().length()==0)
        {
            Speech.requestFocus();
            Speech.setError("FIELD CANNOT BE EMPTY");
            Init.initToast(EditItemMyListWordActivity.this,"Please Write or speech word content");
            return false;
        }
        else if(imageView.getDrawable() == null)
        {
            Init.initToast(EditItemMyListWordActivity.this,"Please Choose Image");
            return false;
        }
        else if (wordType.length()==0)
        {
            Init.initToast(EditItemMyListWordActivity.this,"Please choose Word Type");
            return false;
        }
        else return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            match_text_dialog = new Dialog(EditItemMyListWordActivity.this);
            match_text_dialog.setContentView(R.layout.fmspeechtotext_listviewofitemspeech);
            match_text_dialog.setTitle("Select Matching Text");
            textlist = (ListView)match_text_dialog.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter =    new ArrayAdapter<String>(EditItemMyListWordActivity.this,
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
                    Init.initToast(EditItemMyListWordActivity.this,"Loi SendFileFirebase");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    ContentWord contentWord = new ContentWord(Speech.getText().toString(),wordType,String.valueOf(downloadUrl));
                    String selectekey = extras.getString("SELECTE_KEY");
                    int position = extras.getInt("POSITION");
                    int positionaferfilter = extras.getInt("POSITIONAFTERFILTER");
                    Intent intents=new Intent();
                    Bundle extrass = new Bundle();
                    extrass.putString("SELECTE_KEY",selectekey);
                    extrass.putSerializable("ContentAfterEdit",contentWord);
                    extrass.putInt("POSITION",position);
                    extrass.putInt("POSITIONAFTERFILTER",positionaferfilter);
                    intents.putExtras(extrass);
                    setResult(Edititemlistview,intents);
                    Init.initToast(EditItemMyListWordActivity.this,"Edit success in Edit activity class!");
                    finish();
                }
            });
        }else{
            //IS NULL
        }
    }
}
