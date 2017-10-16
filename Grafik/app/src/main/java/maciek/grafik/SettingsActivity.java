package maciek.grafik;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class SettingsActivity extends AppCompatActivity {

    String[] nazwyZmian;
    String strMojaZmiana, strPoczatekCyklu;
    int intWybranaZmiana;
    int year, month, day;
    private static final String PREFERENCES_NAME = "myPreferences";
    private SharedPreferences preferences;
    private EditText etStawka, etGodziny;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //********************POBRANIE DANYCH Z PLIKU MYPREFERENCES I WYPEŁNIENIE ODPOWIEDNICH PÓL**************************
        etStawka = (EditText) findViewById(R.id.etStawka);
        etGodziny = (EditText) findViewById(R.id.et_iloscGodzin);
        preferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
        etStawka.setText(preferences.getString("Stawka", "0"));
        etGodziny.setText(preferences.getString("GodzinNaZmiane", "0"));
        strMojaZmiana = preferences.getString("MojaZmiana", "nd.");
        switch (strMojaZmiana) {
            case "A":
                intWybranaZmiana = 0;
                break;
            case "B":
                intWybranaZmiana = 1;
                break;
            case "C":
                intWybranaZmiana = 2;
                break;
            case "D":
                intWybranaZmiana = 3;
                break;
            default:
                intWybranaZmiana = 4;
                break;
        }
        strPoczatekCyklu = preferences.getString("PoczatekCyklu", "20170101");
        if (strPoczatekCyklu.length() >= 7) {
            year = Integer.parseInt(strPoczatekCyklu.substring(0, 4));//pobranie 4 pierwszych znakow z daty i przypisanie do zmiennej jako int
            month = Integer.parseInt(strPoczatekCyklu.substring(4, 6));
            day = Integer.parseInt(strPoczatekCyklu.substring(6));
            TextView tvCykl = (TextView) findViewById(R.id.tvPoczatekCyklu);
            tvCykl.setText(strPoczatekCyklu);
        }
        nazwyZmian = new String[]{
                //getResources().getString(R.string.zmiana_A),
                //getResources().getString(R.string.zmiana_B),
                //getResources().getString(R.string.zmiana_C),
                //getResources().getString(R.string.zmiana_D),
                "A", "B", "C", "D", "-"
        };
        //********************Obsluga listy rozwijanej**********************
        final Spinner spinner = (Spinner) findViewById(R.id.spinnerZmiany);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nazwyZmian);
        spinner.setAdapter(adapter);
        spinner.setSelection(intWybranaZmiana);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int id, long position) {
                Toast.makeText(SettingsActivity.this, "Wybrano opcję " + (id + 1), Toast.LENGTH_SHORT).show();
                intWybranaZmiana = (int) position;
                switch (intWybranaZmiana) {
                    case 0:
                        strMojaZmiana = "A";
                        break;
                    case 1:
                        strMojaZmiana = "B";
                        break;
                    case 2:
                        strMojaZmiana = "C";
                        break;
                    case 3:
                        strMojaZmiana = "D";
                        break;
                    default:
                        strMojaZmiana = "nd.";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // ta metoda wykonuje sie gdy lista zostanie wybrana, ale nie zostanie wybrany żaden element z listy
            }
        });

        //*********Obsluga wyboru daty***************


    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    public void saveButton(View v) {
        final Intent secondLayoutActivity = new Intent(this, SecondLayoutActivity.class);
        saveData();
        startActivity(secondLayoutActivity);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        GregorianCalendar dt = new GregorianCalendar();
        year = dt.get(Calendar.YEAR);
        month = dt.get(Calendar.MONTH);
        day = dt.get(Calendar.DAY_OF_MONTH);
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            showDate(year, month, dayOfMonth);
        }
    };

    private void showDate(int year, int month, int day) {
        TextView dateView = (TextView) findViewById(R.id.tvPoczatekCyklu);
        String strMonth, strDay;
        strMonth = Integer.toString(month + 1);
        if (strMonth.length() == 1) {
            strMonth = "0" + strMonth;
        }
        strDay = Integer.toString(day);
        if (strDay.length() == 1) {
            strDay = "0" + strDay;
        }
        dateView.setText(Integer.toString(year) + strMonth + strDay);
        strPoczatekCyklu = dateView.getText().toString();
    }

    private void saveData() {
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        String editTextStawka = etStawka.getText().toString();
        String editTextGodziny = etGodziny.getText().toString();
        preferenceEditor.putString("MojaZmiana", strMojaZmiana);//wpisanie zmiany
        preferenceEditor.putString("Stawka", editTextStawka);//wpisanie stawki
        preferenceEditor.putString("GodzinNaZmiane", editTextGodziny);//wpisanie stawki
        preferenceEditor.putString("PoczatekCyklu", strPoczatekCyklu);//wpisanie daty rozpoczecia cyklu zmiany A.
        preferenceEditor.apply(); // system podpowiada aby rozwazyc uzycie apply zamiast commit
    }

    // zamykanie settings activity po przejsciu do main activity
    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed() {
        final Intent secondLayoutActivity = new Intent(this, SecondLayoutActivity.class);
        startActivity(secondLayoutActivity);
    }
}
