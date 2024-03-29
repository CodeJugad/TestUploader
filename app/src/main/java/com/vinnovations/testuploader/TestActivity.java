package com.vinnovations.testuploader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class TestActivity extends AppCompatActivity {

    TextInputEditText edtTestName;
    Button btnTestName,btnAddQuestion;
    ImageView imageView;
    RadioGroup radioAnswer;
    FirebaseDatabase database;
    DatabaseReference myRef;
    int count = 0;
    String answer;
    String testName;
    // Inside your activity or fragment
    private static final int RC_IMAGE_PICK = 1;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        edtTestName = findViewById(R.id.edt_test_name);
        btnTestName = findViewById(R.id.btn_test_name);
        btnAddQuestion = findViewById(R.id.btn_add_question);
        imageView = findViewById(R.id.imageView);
        radioAnswer = findViewById(R.id.radio_answer);



        // Button click listener
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // use it to get image from file manager
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, RC_IMAGE_PICK);
            }
        });


        btnTestName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // code
                edtTestName.setVisibility(View.GONE);
                btnTestName.setVisibility(View.GONE);
                testName = String.valueOf(edtTestName.getText());
                // Write a message to the database
                database = FirebaseDatabase.getInstance();
                database.getReference().child("all-tests").child(testName).setValue(testName);
                myRef = database.getReference().child("all-tests").child(testName).child(String.valueOf(count));

            }
        });

        btnAddQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code
                int selectedId = radioAnswer.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                if(selectedId != -1){
                    RadioButton selected_radio_btn = findViewById(selectedId);
                    answer = String.valueOf(selected_radio_btn.getText());
                }
                radioAnswer.clearCheck();
//                myRef.child("image").
                uploadImage();
                myRef.child("answer").setValue(answer);

            }
        });
    }

    // Handle the result of image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            // Use the selectedImageUri to do further processing with the image
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imageView);

        }


    }

    void uploadImage(){

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

      StorageReference imageRef = storageRef.child(testName +"/" + "image"+count+".jpg");

        UploadTask uploadTask = imageRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(TestActivity.this,"image uploaded, Please wait a bit!",Toast.LENGTH_SHORT).show();
                imageView.setImageResource(R.drawable.ic_baseline_add_24);
                // Image uploaded successfully
                // You can retrieve the download URL for the image using taskSnapshot.getDownloadUrl()
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    // Store the download URL in the Realtime Database
                                    myRef.child("imageUrl").setValue(downloadUri.toString());
                                    Toast.makeText(TestActivity.this,"Now Proceed for other!",Toast.LENGTH_SHORT).show();
                                    count++;
                                    myRef = database.getReference().child("all-tests").child(testName).child(String.valueOf(count));
//                                    myRef.child("images").child(imageKey).child("imageUrl").setValue(downloadUri.toString());
//                                    myRef.child("image").setValue(downloadUri.toString());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                               // Handle the failure case
                               Toast.makeText(TestActivity.this,"image uri Failed",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TestActivity.this,"image upload Failed",Toast.LENGTH_SHORT).show();
                // Image upload failed
            }
        });



    }
}