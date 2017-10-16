package maciek.grafik;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;
import java.util.Locale;

public class SettingsActivity2 extends AppCompatActivity {

    ////////////////////////    DO EDIT TEXT MOŻNA DODAC LISTENERA I ZABRONIĆ WPISYWANIA ZBYT WIELU CYFR.
    private DBHelperSettings mydb;
    TextView tvPoczatekCyklu;
    EditText etStawka;
    Spinner spinner;
    Button buttonZapisz;
    int intBrygada, poczatekCyklu = 0, startUstawien = 0, stopUstawien = 0;
    float stawka = 0.0f;
    String brygada = "";
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);

        GregorianCalendar dateNow = new GregorianCalendar();
        int yearNow = dateNow.get(GregorianCalendar.YEAR);
        // nazwy kolumn tabeli ustawień: startCyklu, startUstawien, stopUstwien, stawka, brygada
        mydb = new DBHelperSettings(this);
        boolean ustawienia = mydb.selectDataIfExist();//pobiera rekord z najwyzsza data startu ustawien
        if (ustawienia) {
            poczatekCyklu = mydb.poczatekCyklu;
            startUstawien = mydb.startUstawien;
            //stopUstawien = ustawienia.getInt(ustawienia.getColumnIndex("stopUstwien"));
            stawka = mydb.stawka;
            brygada = mydb.brygada;
        } else {
            startUstawien = Integer.parseInt(yearNow+"0101");
        }
        switch (brygada) {
            case "A":
                intBrygada = 1;
                break;
            case "B":
                intBrygada = 2;
                break;
            case "C":
                intBrygada = 3;
                break;
            case "D":
                intBrygada = 4;
                break;
            default:
                intBrygada = 0;
                break;
        }
        //********************Obsluga listy rozwijanej**********************

        final String[] nazwyBrygad = new String[]{
                "","A", "B", "C", "D"
        };
        spinner = (Spinner)findViewById(R.id.spinnerZmian);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nazwyBrygad);
        spinner.setAdapter(adapter);
        spinner.setSelection(intBrygada);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int id, long position) {
                Toast.makeText(SettingsActivity2.this, "Wybrano opcję " + (id), Toast.LENGTH_SHORT).show();
                intBrygada = (int) position;
                brygada = nazwyBrygad[intBrygada];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // ta metoda wykonuje sie gdy lista zostanie wybrana, ale nie zostanie wybrany żaden element z listy
            }

        });

        tvPoczatekCyklu = (TextView)findViewById(R.id.textViewPoczatekCyklu);
        etStawka = (EditText)findViewById(R.id.editTextStawka);
        tvPoczatekCyklu.setText(String.format(Locale.ENGLISH, "%d", poczatekCyklu));
        etStawka.setText(String.format(Locale.ENGLISH, "%f", stawka));

        tvPoczatekCyklu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateCykl();
            }
        });

        buttonZapisz = (Button)findViewById(R.id.buttonZapisz);
        buttonZapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. sprawdzic czy w bazie nie istnieje nowszy rekord.
                //2. jesli istnieje to wyswietlic blad ewentualnie date minimalna.
                //3. jesli nie istnieje to zapisac dane do bazy
                stawka = Float.parseFloat(etStawka.getText().toString());
                mydb.wstawNoweDane(poczatekCyklu, startUstawien, stopUstawien, stawka, brygada);
                final Intent secondLayoutActivity = new Intent(getApplicationContext(), SecondLayoutActivity.class);
                startActivity(secondLayoutActivity);
            }
        });



    }
    private void showDateCykl() {

        final GregorianCalendar c = new GregorianCalendar();
        int mYear = c.get(GregorianCalendar.YEAR); // current year
        int mMonth = c.get(GregorianCalendar.MONTH); // current month
        int mDay = c.get(GregorianCalendar.DAY_OF_MONTH); // current day
        // date picker dialog
        datePickerDialog = new DatePickerDialog(SettingsActivity2.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String monthString, dayString;
                        if(monthOfYear<9){
                            monthString="0"+(monthOfYear+1);
                        } else {
                            monthString=Integer.toString(monthOfYear+1);
                        }
                        if(dayOfMonth<10){
                            dayString="0"+dayOfMonth;
                        } else {
                            dayString=Integer.toString(dayOfMonth);
                        }
                        // wyswietlenie wybranej daty w polu tekstowym
                        poczatekCyklu = Integer.parseInt(year+monthString+dayString);
                        tvPoczatekCyklu.setText(year+monthString+dayString);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void showDateUstawienia (){

        final GregorianCalendar c = new GregorianCalendar();
        int mYear = c.get(GregorianCalendar.YEAR); // current year
        int mMonth = c.get(GregorianCalendar.MONTH); // current month
        int mDay = c.get(GregorianCalendar.DAY_OF_MONTH); // current day
        // date picker dialog
        datePickerDialog = new DatePickerDialog(SettingsActivity2.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String monthString, dayString;
                        if(monthOfYear<9){
                            monthString="0"+(monthOfYear+1);
                        } else {
                            monthString=Integer.toString(monthOfYear+1);
                        }
                        if(dayOfMonth<10){
                            dayString="0"+dayOfMonth;
                        } else {
                            dayString=Integer.toString(dayOfMonth);
                        }
                        int tempDate = Integer.parseInt(year+monthString+dayString);
                        if(tempDate<startUstawien){
                            //ponowne wyswietlenie kalendarza i monitu o zdarzeniu
                            showDateUstawienia();
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed(){
        final Intent secondLayoutActivity = new Intent(this, SecondLayoutActivity.class);
        startActivity(secondLayoutActivity);
    }
}
