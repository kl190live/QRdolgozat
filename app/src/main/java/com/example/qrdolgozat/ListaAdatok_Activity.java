package com.example.qrdolgozat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListaAdatok_Activity extends AppCompatActivity {

    private Button buttonModify;
    private Button buttonBack;
    private EditText editTextId;
    private EditText editTextName;
    private EditText editTextGrade;
    private ListView listViewData;
    private LinearLayout linearLayoutForm;
    private ProgressBar progressBar;
    private List<Person> people = new ArrayList<>();
    private String url = "https://retoolapi.dev/5edtfW/dolgozat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_adatok);
        init();

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaAdatok_Activity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void init()
    {
        buttonModify=findViewById(R.id.buttonModify);
        buttonBack=findViewById(R.id.buttonBack);
        editTextId=findViewById(R.id.editTextId);
        editTextName=findViewById(R.id.editTextName);
        editTextGrade=findViewById(R.id.editTextGrade);
        listViewData=findViewById(R.id.listViewData);

    }

    private class PersonAdapter extends ArrayAdapter<Person> {
        public PersonAdapter() {
            super(ListaAdatok_Activity.this, R.layout.person_list_adapter, people);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.person_list_adapter, null, false);

            Person actualPerson = people.get(position);
            TextView textViewName = view.findViewById(R.id.textViewName);
            TextView textViewGrade = view.findViewById(R.id.textViewGrade);
            TextView textViewModify = view.findViewById(R.id.textViewModify);
            textViewName.setText(actualPerson.getName());
            textViewGrade.setText(String.valueOf(actualPerson.getGrade()));

            textViewModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayoutForm.setVisibility(View.VISIBLE);
                    editTextId.setText(String.valueOf(actualPerson.getId()));
                    editTextName.setText(actualPerson.getName());
                    editTextGrade.setText(String.valueOf(actualPerson.getGrade()));
                    buttonModify.setVisibility(View.VISIBLE);
                    buttonBack.setVisibility(View.VISIBLE);
                }
            });
            return view;
        }
    }

    private void emberModositas() {
        String name = editTextName.getText().toString();
        String grade = editTextGrade.getText().toString();
        String idText = editTextId.getText().toString();

        boolean valid = validacio();
        if (valid) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
        } else {
            int id = Integer.parseInt(idText);
            Person person = new Person(id, name, grade);
            Gson jsonConverter = new Gson();
            RequestTask task = new RequestTask(url + "/" + id, "PUT", jsonConverter.toJson(person));
            task.execute();
        }
    }

    private boolean validacio() {
        if (editTextName.getText().toString().isEmpty() ||  editTextGrade.getText().toString().isEmpty())
            return true;
        else return false;
    }

    private class RequestTask extends AsyncTask<Void, Void, Response> {
        String requestUrl;
        String requestType;
        String requestParams;

        public RequestTask(String requestUrl, String requestType, String requestParams) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
            this.requestParams = requestParams;
        }

        public RequestTask(String requestUrl, String requestType) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
        }

        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestType) {
                    case "GET":
                        response = RequestHandler.get(requestUrl);
                        break;
                    case "PUT":
                        response = RequestHandler.put(requestUrl, requestParams);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(ListaAdatok_Activity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        private void urlapAlaphelyzetbe() {
            editTextGrade.setText("");
            editTextName.setText("");
            linearLayoutForm.setVisibility(View.VISIBLE);
            buttonModify.setVisibility(View.VISIBLE);
            RequestTask task = new RequestTask(url, "GET");
            task.execute();
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(ListaAdatok_Activity.this, "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                Log.d("onPostExecuteError: ", response.getResponseMessage());
            }
            switch (requestType) {
                case "GET":
                    Person[] peopleArray = converter.fromJson(response.getResponseMessage(), Person[].class);
                    people.clear();
                    people.addAll(Arrays.asList(peopleArray));
                    break;
                case "PUT":
                    Person updatePerson = converter.fromJson(response.getResponseMessage(), Person.class);
                    people.replaceAll(person1 -> person1.getId() == updatePerson.getId() ? updatePerson : person1);
                    urlapAlaphelyzetbe();
                    break;
            }
        }
    }
}