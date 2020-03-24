package com.example.uber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    private EditText edtSignUpUsername,edtSignUpPassword;
    private Button btnSignUp,btnLoginPage;
    private  RadioButton radioBtnDriver,radioBtnPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        setTitle("Sign Up");


        edtSignUpPassword=findViewById(R.id.edtPasswordSignUp);
        edtSignUpUsername=findViewById(R.id.edtUsernameSignUp);

        btnSignUp=findViewById(R.id.btnSignUp);
        btnLoginPage=findViewById(R.id.btnLoginPage);

        radioBtnDriver=findViewById(R.id.radiobtnDriver);
        radioBtnPassenger=findViewById(R.id.radiobtnPassenger);


        btnSignUp.setOnClickListener(this);
        btnLoginPage.setOnClickListener(this);


        edtSignUpPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN)
                {
                    onClick(btnSignUp);

                }

                return false;
            }
        });


        if(ParseUser.getCurrentUser()!=null)
        {
//            ParseUser.logOut();

            Toast.makeText(this,ParseUser.getCurrentUser().getUsername()+" Logged In",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(SignUp.this,Passenger.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btnSignUp:

                if((radioBtnPassenger.isChecked()==false&&radioBtnDriver.isChecked()==false)
                        || edtSignUpUsername.getText().toString().equals("")
                        || edtSignUpPassword.getText().toString().equals(""))
                {
                    Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();

                }
                else
                {

                    final ParseUser newUser=new ParseUser();

                    String category="";
                    if(radioBtnDriver.isChecked())
                    {
                        category="driver";
                    }
                    else if(radioBtnPassenger.isChecked())
                    {
                        category="passenger";
                    }

                    newUser.setUsername(edtSignUpUsername.getText().toString());
                    newUser.setPassword(edtSignUpPassword.getText().toString());
                    newUser.put("category",category);
                    Log.i("App Credential SignUp ", "\nSign Up\nCategory : " + category + "\nName : " + edtSignUpUsername.getText().toString() + "\n Password :" + edtSignUpPassword.getText().toString());


                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {

                            if(e==null)
                            {
                                Toast.makeText(SignUp.this, newUser.getUsername() + " Sign Up Successful", Toast.LENGTH_SHORT).show();

                                TransitionToHomepage();
                            }
                            else
                            {
                                Toast.makeText(SignUp.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();



                            }
                        }
                    });



                }



                break;

            case R.id.btnLoginPage:

                Intent intent=new Intent(SignUp.this,LogIn.class);
                startActivity(intent);
                finish();

                break;
        }

    }


    public void ConstraintLayoutClicked(View view)
    {

        try {
            InputMethodManager inputMethodManager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void TransitionToHomepage() {
        if (ParseUser.getCurrentUser().get("category").equals("passenger"))
        {
            Intent intent = new Intent(SignUp.this, Passenger.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this,"Activity for Driver is not defined yet",Toast.LENGTH_SHORT).show();
        }
    }
}
