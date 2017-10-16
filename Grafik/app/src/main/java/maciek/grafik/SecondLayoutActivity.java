package maciek.grafik;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SecondLayoutActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    String strMojaZmiana, strPoczatekCyklu;
    private GestureDetectorCompat gestureObjectDetector;
    private TextView textViewData, testy;
    private DBHelper mydb;
    private DBHelperSettings dbSettings;
    int miesiacWybrany = 100, rokWybrany, dzienAktualny, szerokoscOkienka, iloscWcisniecBack, screenHeight, screenWidth;
    int poczatekCykluOffset, licznikZaznaczen=0, zaznaczonyDzien1=0, zaznaczonyDzien2=0, licznikZwolnien=0;
    public SharedPreferences preferences;
    private static final String PREFERENCES_NAME = "myPreferences", DEBUG_TAG = "SimpleGestures_detected";//nazwa pliku
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_layout);

        //POBRANIE USTAWIEŃ Z BAZY DANYCH - jesli nie ma ustawien w bazie, wtedy uzytkownik jest przenoszony do okna ustawien
        dbSettings = new DBHelperSettings(this);
        boolean ustawienia = dbSettings.selectDataIfExist();//pobiera rekord z najwyzsza data startu ustawien
        if (ustawienia) {
            int poczatekCyklu = dbSettings.poczatekCyklu;
            int startUstawien = dbSettings.startUstawien;
            //stopUstawien = ustawienia.getInt(ustawienia.getColumnIndex("stopUstwien"));
            float stawka = dbSettings.stawka;
            String brygada = dbSettings.brygada;
        } else {
            final Intent settingsActivity2 = new Intent(this, SettingsActivity2.class);
            startActivity(settingsActivity2);
        }

        //POBRANIE WYSOKOŚCI I SZEROKOSCI EKRANU
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        //USTAWIENIE DETEKTORA GESTÓW, I POŁĄCZENIA Z DB.
        gestureObjectDetector = new GestureDetectorCompat(this, this);
        mydb = new DBHelper(this);
        testy = (TextView) findViewById(R.id.textViewTesty);
        testy.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rectangle_silver_to_white));

        final Intent intSettingsActivity = new Intent(this, SettingsActivity2.class);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSettings);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intSettingsActivity);
            }
        });

        //POBRANIE USTAWIEŃ APLIKACJI Z PLIKU PREFERENCES
        preferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
        strMojaZmiana = preferences.getString("MojaZmiana", "0");
        strPoczatekCyklu = preferences.getString("PoczatekCyklu", "20170101");//domyslna wartosc musi byc 8-cyfrowa, inaczej przy nowej instalacji wyrzuca blad.
        poczatekCykluOffset = rozpoczecieCykluOffset(strPoczatekCyklu, strMojaZmiana);// zwraca dzien poczatku cyklu wybranej zmiany.

        //USTAWIENIE TLA I SZEROKOSCI TABELI
        ConstraintLayout tloTabeli;
        tloTabeli = (ConstraintLayout) findViewById(R.id.constraintLayoutTable);
        tloTabeli.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.kolorBialyAlpha));
        tloTabeli.setMaxWidth(screenWidth);
        szerokoscOkienka = (screenWidth) / 7;
//-*************************************************************************************************
        //wypelnianie komorek naglowkowych
        // przypisanie nazw dni do zmiennych
        String[] dzienNazwa = {
                getResources().getString(R.string.day_1),
                getResources().getString(R.string.day_2),
                getResources().getString(R.string.day_3),
                getResources().getString(R.string.day_4),
                getResources().getString(R.string.day_5),
                getResources().getString(R.string.day_6),
                getResources().getString(R.string.day_7),
        };
        TextView textViewDays;
        for (int i = 1; i <= 7; i++) {
            final String textViewDayID = "tvDay" + i;
            int tvID = getResources().getIdentifier(textViewDayID, "id", getPackageName());
            textViewDays = (TextView) findViewById(tvID);
            textViewDays.setText(dzienNazwa[i - 1]);
            textViewDays.setGravity(Gravity.CENTER);
            textViewDays.setWidth(szerokoscOkienka);
            if (i == 7) {
                textViewDays.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.kolorCzerwony));
            }
        }
//-*************************************************************************************************
        //POBRANIE AKTUALNEJ DATY
        PokazDate aktData = new PokazDate();
        aktData.pobierzDate();
//-*************************************************************************************************
        //WYŚWIETLENIE AKTUALNIE WYBRANEJ DATY W POLU TEXT VIEW DATA
        if (miesiacWybrany == 100) {
            miesiacWybrany = Integer.parseInt(aktData.aktMiesiac);
            rokWybrany = Integer.parseInt(aktData.aktRok);
            dzienAktualny = Integer.parseInt(aktData.aktDzien);
        }
        textViewData = (TextView) findViewById(R.id.textViewData);
        textViewData.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rectangle_silver_to_black));
        //wypelniania komorek
        wypelnijKomorki(rokWybrany, miesiacWybrany, 0, 0, null);
    }
    //////////    KONIEC ON CREATE     ///////////////////////

    // miesiac przyjmuje wartosci 1-12
    public void wypelnijKomorki(int rok, int miesiac, int poczatekZakresu, int koniecZakresu, String opcjaZakresu){
        //POBRANIE AKTUALNEJ DATY
        PokazDate aktData = new PokazDate();
        aktData.pobierzDate();

        int licznikGodzinNormalnych=0, licznikGodzinNocnych=0, licznikGodzin50=0, licznikGodzin100=0, licznikGodzinUrlopowych=0, licznikDniZwolnienia=0;
        String[] nazwaMiesiaca = {"Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"};
        textViewData.setText(rokWybrany + " " + nazwaMiesiaca[miesiacWybrany-1]);
        // wypenianie komorek z dniami
        int licznikDniowek100 = 0, licznikNocek100 = 0, licznikUrlopow = 0;
        int dzienMiesiaca = 0, dzienRokuPoprzedniMiesiac, dzienRokuObecnyMiesiac;
        licznikZaznaczen=0; //po zmianie miesiaca licznik zaznaczen jest resetowany.
        WybranaData biezacyMiesiac = new WybranaData();
        biezacyMiesiac.generujDane(rok, miesiac);
        dzienRokuObecnyMiesiac = biezacyMiesiac.pierwszyDzienObecnegoMiesiacaJakoDzienRoku;
        dzienRokuPoprzedniMiesiac = biezacyMiesiac.pierwszyDzienBylegoMiesiacaJakoDzienRoku;
        if (biezacyMiesiac.pierwszyDzienMiesiaca != 2) { //1-N, 2-Pn, 3-Wt, 4-Śr, 5-Cz, 6-Pt, 7-So.
            //jesli miesiac nie zaczyna sie od poniedzialku to
            // wyznaczamy ostatni poniedzialek poprzedniego miesiaca
            if (biezacyMiesiac.pierwszyDzienMiesiaca != 1) {
                //jesli dzien miesiaca = 1 2 3 4 5 6 7
                //to trzeba odjac      = 5 X 0 1 2 3 4
                dzienMiesiaca = biezacyMiesiac.ostatniDzienPoprzedniegoMiesiaca - (biezacyMiesiac.pierwszyDzienMiesiaca - 3);
            } else {
                dzienMiesiaca = biezacyMiesiac.ostatniDzienPoprzedniegoMiesiaca - 5;
            }
        }
        //******************warunki rozpoczecia oznaczania dni

//***** //sprawdzenie którym dniem roku jest pierwszy dzień widoczny w kalendarzu (moze to być obecny lub poprzedni miesiac)
//***** // jesli dzienMiesiaca jest inny niz 1 to znaczy ze szukamy w poprzednim miesiacu.
//***** // zmienne rok i miesiac sa danymi z aktualnie pokazywanego miesiaca. 1 to styczen.
        int offsetZmianWDanymMiesiacu, latOdOffsetu, rokWyznaczeniaOffsetu, dniOdOffsetu = 0;
        rokWyznaczeniaOffsetu = Integer.parseInt(strPoczatekCyklu.substring(0, 4));
        latOdOffsetu = rokWybrany - rokWyznaczeniaOffsetu;
        if (latOdOffsetu > 0) {
            int dniWRoku;
            for (int i = 0; i < latOdOffsetu; i++) {
                GregorianCalendar iRok = new GregorianCalendar(rokWyznaczeniaOffsetu + i, 11, 31);
                dniWRoku = iRok.get(GregorianCalendar.DAY_OF_YEAR);
                dniOdOffsetu += dniWRoku;
            }
        }

        if (dzienMiesiaca > 1) {
            if (miesiac != 1) {
                offsetZmianWDanymMiesiacu = (dniOdOffsetu + dzienRokuPoprzedniMiesiac + dzienMiesiaca - 1) % 4;
            } else {
                offsetZmianWDanymMiesiacu = (dniOdOffsetu + 31 - dzienMiesiaca + 2) % 4;
            }
        } else {
            offsetZmianWDanymMiesiacu = dniOdOffsetu + dzienRokuObecnyMiesiac % 4;
        }

        TextView textViews;
        float poziomAlfa; //poziom przezroczystosci okienka z dniem
        for (int i = 1; i <= 42; i++) {
            boolean liczenieGodzin;

            poziomAlfa = 1.0f;
            ///// USTAWIANIE PIERWSZEGO DNIA W OBECNYM I NASTEPNYM MIESIACU
            if ((i == 1 && biezacyMiesiac.pierwszyDzienMiesiaca == 2) ||//jesli miesiac zaczyna sie od poniedzialku
                    //(i==wybranaData.pierwszyDzienMiesiaca-1)|| //jesli wypelniana komorka wskazuje na dzien poczatku miesiaca
                    (dzienMiesiaca > biezacyMiesiac.liczbaDniMiesiaca && i > 20) || //jesli dzien miesiaca wykracza poza liczbe dni w danym m-cu
                    (dzienMiesiaca > biezacyMiesiac.ostatniDzienPoprzedniegoMiesiaca && i < 10)) {
                dzienMiesiaca = 1;
            }
            /////-**********************************************************************************
            // WYSZARZANIE POPRZEDNIEGO I NASTEPNEGO MIESIACA
            if ((i < 7 && dzienMiesiaca > 7) || (i > 28 && dzienMiesiaca < 15)) {
                poziomAlfa = 0.5f;
                liczenieGodzin = false;
            } else {
                liczenieGodzin = true;
            }
            //-*************************************************************************************
            final String textViewID = "tvTab" + i;
            int resID = getResources().getIdentifier(textViewID, "id", getPackageName());
            textViews = (TextView) findViewById(resID);
            textViews.setOnClickListener(null);// Konieczne do resetowania listenera, gdy w jednym miesiacu aktywne komórki w innym są nieaktywne
            textViews.setTypeface(null, Typeface.NORMAL);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) textViews.getLayoutParams();
            lp.width = screenWidth/7-8;
            lp.height = (screenHeight/2)/5;
            textViews.setLayoutParams(lp); //reczne ustawianie szerokosci TextView

            // KOLOROWANIE DNI
            if(liczenieGodzin && dzienMiesiaca==dzienAktualny && miesiacWybrany==Integer.parseInt(aktData.aktMiesiac)){
                textViews.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.kolorZielony));
                //textViews.setPaintFlags(textViews.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); //PODKREŚLENIE
                textViews.setTypeface(null, Typeface.BOLD_ITALIC);
            } else if (i % 7 == 0) {
                textViews.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.kolorCzerwony));
            } else {
                textViews.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.kolorCzarny));
            }
            //-*************************************************************************************
            textViews.setAlpha(poziomAlfa);
            textViews.setText(String.format(Locale.ENGLISH, "%d", dzienMiesiaca));
            textViews.setTextSize(20);
            textViews.setGravity(Gravity.CENTER);
            textViews.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.kolorBialy));

            String daneZmiany = mydb.selectingOptionIfDataExist(sklejDate(rok, miesiac, dzienMiesiaca));
            if (liczenieGodzin) {
                switch (daneZmiany) {
                    case "d":
                        licznikGodzinNormalnych+=12;
                        break;
                    case "d_r":
                        licznikGodzinNormalnych+=12;
                        licznikGodzin100+=12;
                        break;
                    case "d_4":
                        licznikGodzinNormalnych+=12;
                        licznikGodzin50+=4;
                        break;
                    case "d_8":
                        licznikGodzinNormalnych+=12;
                        licznikGodzin50+=4;
                        licznikGodzin100+=4;
                        break;
                    case "n":
                        licznikGodzinNormalnych+=12;
                        licznikGodzinNocnych+=9;  ///stawka to 20% stawki godzinowej wynikajacej z minimalnego wynagrodzenia 2017 - 2000PLN
                        break;
                    case "n_r":
                        licznikGodzinNormalnych+=12;
                        licznikGodzinNocnych+=9;
                        licznikGodzin100+=12;
                        break;
                    case "n_4":
                        licznikGodzinNormalnych+=12;
                        licznikGodzinNocnych+=9;
                        licznikGodzin100+=4;
                        break;
                    case "n_8":
                        licznikGodzinNormalnych+=12;
                        licznikGodzinNocnych+=9;
                        licznikGodzin100+=8;
                        break;
                    case "u":
                        licznikGodzinUrlopowych+=12;
                        break;
                    case "zw":
                        licznikDniZwolnienia++;
                        break;
                    default:
                        break;
                }
            }

            int pictureID = getResources().getIdentifier(daneZmiany, "drawable", getPackageName());

            if (daneZmiany.equals("") || !liczenieGodzin || (!daneZmiany.equals("") && opcjaZakresu!=null)) {

                if (offsetZmianWDanymMiesiacu % 4 == poczatekCykluOffset) {
                    if(liczenieGodzin && poczatekZakresu<=dzienMiesiaca && koniecZakresu>=dzienMiesiaca && opcjaZakresu!=null){
                        pictureID = getResources().getIdentifier(opcjaZakresu, "drawable", getPackageName());
                        textViews.setBackgroundResource(pictureID);
                        if(opcjaZakresu.equals("u")){
                            licznikGodzinUrlopowych+=12;
                        } else {
                            licznikDniZwolnienia++;
                        }
                        updateGrafik(Integer.parseInt(sklejDate( rok, miesiac, dzienMiesiaca)), opcjaZakresu);
                    } else {
                        textViews.setBackgroundResource(R.drawable.d);
                        if(liczenieGodzin){
                            licznikGodzinNormalnych+=12;
                        }
                    }
                } else if ((offsetZmianWDanymMiesiacu + 3) % 4 == poczatekCykluOffset) {
                    if(liczenieGodzin && poczatekZakresu<=dzienMiesiaca && koniecZakresu>=dzienMiesiaca && opcjaZakresu!=null){
                        pictureID = getResources().getIdentifier(opcjaZakresu, "drawable", getPackageName());
                        textViews.setBackgroundResource(pictureID);
                        if(opcjaZakresu.equals("u")){
                            licznikGodzinUrlopowych+=12;
                        } else {
                            licznikDniZwolnienia++;
                        }
                        updateGrafik(Integer.parseInt(sklejDate( rok, miesiac, dzienMiesiaca)), opcjaZakresu);
                    } else {
                        textViews.setBackgroundResource(R.drawable.n);
                        if (liczenieGodzin) {
                            licznikGodzinNormalnych+=12;
                            licznikGodzinNocnych+=9;
                        }
                    }
                } else {
                    if(liczenieGodzin && poczatekZakresu<=dzienMiesiaca && koniecZakresu>=dzienMiesiaca && opcjaZakresu.equals("zw")) {
                        pictureID = getResources().getIdentifier(opcjaZakresu, "drawable", getPackageName());
                        textViews.setBackgroundResource(pictureID);
                        licznikDniZwolnienia++;
                        updateGrafik(Integer.parseInt(sklejDate( rok, miesiac, dzienMiesiaca)), opcjaZakresu);
                    } else {
                        textViews.setBackgroundResource(R.drawable.w);
                    }
                }
            } else {
                textViews.setBackgroundResource(pictureID);
            }
            offsetZmianWDanymMiesiacu++;

            //klikniecie
            //metoda wywolywana po wcisnieciu ktoregokolwiek okienka tabeli
            if (poziomAlfa != 0.3f) { //klikniecie aktywne jest tylko dla danego miesiaca.
               //final TextView textViewCopy = textViews;
                if (liczenieGodzin) {
                    metodaOnClick(textViews);
                    metodaOnLongClick(textViews);
                }
            }
            textViews.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    gestureObjectDetector.onTouchEvent(event);
                    return false;
                }
            });
            dzienMiesiaca++;

            // !!!!!!!!!!!!!!!!!!!!  tu nalezaloby sprawdzic ilosc dniowek, nocek itp.
        }
        testy.setText("Godzin łącznie: " + (licznikGodzinNormalnych) + "\n" +
                "w tym nadgodzin: " + (licznikNocek100+licznikDniowek100)*12);
    }

    public String wybranaZmiana(String symbol) {
        String wynik;
        switch (symbol) {
            case "D":
                wynik = "d";
                break;
            case "DR":
                wynik = "d_r";
                break;
            case "N":
                wynik = "n";
                break;
            case "NR":
                wynik = "n_r";
                break;
            case "U":
                wynik = "u";
                break;
            case "Urlop":
                wynik = "u";
                break;
            case "D/8":
                wynik = "d_8";
                break;
            case "D/4":
                wynik = "d_4";
                break;
            case "N/8":
                wynik = "n_8";
                break;
            case "N/4":
                wynik = "n_4";
                break;
            case "Zwolnienie":
                wynik = "zw";
                break;
            default:
                wynik = "w";
                break;
        }

        return wynik;
    }

    //metoda zwracająca dzien roku z wybranej daty rozpoczecia cyklu********************************
    public int rozpoczecieCykluOffset(String poczatekCyklu, String mojaZmiana) {
        int zmianaAoffset, mojaZmianaOffset;
        int year = Integer.parseInt(poczatekCyklu.substring(0, 4));//pobranie 4 pierwszych znakow z daty i przypisanie do zmiennej jako int
        int month = (Integer.parseInt(poczatekCyklu.substring(4, 6))) - 1;
        int day = Integer.parseInt(poczatekCyklu.substring(6));
        GregorianCalendar data = new GregorianCalendar(year, month, day);
        zmianaAoffset = data.get(GregorianCalendar.DAY_OF_YEAR) % 4; //offset pierwszej dniowki dla zm. A
        switch (mojaZmiana) {
            case "A":
                mojaZmianaOffset = zmianaAoffset;
                break;
            case "B":
                mojaZmianaOffset = (zmianaAoffset + 1) % 4;
                break;
            case "C":
                mojaZmianaOffset = (zmianaAoffset + 2) % 4;
                break;
            case "D":
                mojaZmianaOffset = (zmianaAoffset + 3) % 4;
                break;
            default:
                mojaZmianaOffset = 0;
        }
        return mojaZmianaOffset;
    }

    @Override
    public void onBackPressed() {
        if (iloscWcisniecBack == 0) {
            iloscWcisniecBack++;
            Context context = getApplicationContext();
            CharSequence text = "Wciśnij ponownie, aby wyjść";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            System.exit(0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureObjectDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() > e2.getX()) {
            if (miesiacWybrany < 12) {
                miesiacWybrany = miesiacWybrany + 1;
            } else {
                miesiacWybrany = 1;
                rokWybrany = rokWybrany + 1;
            }
        } else if (e1.getX() < e2.getX()) {
            if (miesiacWybrany > 1) {
                miesiacWybrany = miesiacWybrany - 1;
            } else {
                if (rokWybrany > 2017) {
                    miesiacWybrany = 12;
                    rokWybrany = rokWybrany - 1;
                }
            }
        }

        Log.v(DEBUG_TAG, "onFling");
        wypelnijKomorki(rokWybrany, miesiacWybrany, 0,0, null);
        return false;
    }

    @Override
    protected void onDestroy() {
        mydb.close();
        super.onDestroy();
    }

    //FUNKCJA SKLEJAJACA ROK MIESIAC I DZIEN - dodaje tez zera wiodące do miesiecy i dni < 10
    public String sklejDate(int rok, int miesiac, int dzien) {
        String data;
        if (miesiac < 10) {
            data = rok + "0" + miesiac;
        } else {
            data = rok + "" + miesiac;
        }
        if (dzien < 10) {
            data = data + "0" + dzien;
        } else {
            data = data + "" + dzien;
        }
        return data;
    }

    public void refresh() {
        wypelnijKomorki(rokWybrany, miesiacWybrany, 0, 0, null);
    }

    public void metodaOnClick(final TextView textViews){
        licznikZaznaczen=0; //resetowanie licznika zaznaczen po kliknieciu
        textViews.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick (View v){
                opcjeDnia(textViews, textViews);
            }
        });
    }

    public void metodaOnLongClick(final TextView textViews){
        /// PONIŻEJ METODA WYWOŁANA PO KLIKNIĘCIU I PRZYTRZYMANIU ELEMENTU
        /// UŻYWANA JEST DO ZAZNACZANIA KILKU ELEMENTÓW NP. DO URLOPU, ZWOLNIENIA.
        textViews.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(licznikZaznaczen==0){
                    licznikZaznaczen++;
                    zaznaczonyDzien1= Integer.parseInt(textViews.getText().toString());
                } else if(licznikZaznaczen==1){
                    licznikZaznaczen++;
                    zaznaczonyDzien2 = Integer.parseInt(textViews.getText().toString());
                    //1.wybor opcji
                    //2.zapis wybranej opcji do bazy danych, ale najpierw trzeba sprawdzic co pod danym polem jest wpisane.

                    opcjeZakresuDni();

                    //wypelnijKomorki(rokWybrany, miesiacWybrany, zaznaczonyDzien1, zaznaczonyDzien2);
                }
                Context context = getApplicationContext();
                String text = "Zaznaczono " + textViews.getText();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
        });
    }

    private void opcjeDnia(View view, final TextView textViews) {
        final String[] opcje = {
                "D",
                "N",
                "DR",
                "NR",
                "W",
                "U",
                "D/8",
                "D/4",
                "N/8",
                "N/4",
                "Zwolnienie"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());//powinno byc this
        builder.setTitle("Wybierz opcję");
        builder.setItems(opcje, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // tu obrabiamy element który został wybrany.
                Context context = getApplicationContext();
                String symbol = wybranaZmiana(opcje[which]);
                int source = getResources().getIdentifier(symbol, "drawable", getPackageName());
                textViews.setBackgroundResource(source);
                CharSequence txt = textViews.getText();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, txt, duration);
                toast.show();
                CharSequence miesiacWybranyString, dzienWybranyString = txt;
                if (miesiacWybrany < 10) {
                    miesiacWybranyString = "0" + miesiacWybrany;
                } else {
                    miesiacWybranyString = "" + miesiacWybrany;
                }
                if (Integer.parseInt(txt.toString()) < 10) {
                    dzienWybranyString = "0" + txt;
                }
                int dataWartosc = Integer.parseInt(rokWybrany + "" + miesiacWybranyString + "" + dzienWybranyString);

                // sprawdzenie czy dana data ma juz modyfikacje jesli tak to zostanie zastąpiona.
                ArrayList<String> Values;
                Values = mydb.getAllRows(dataWartosc);
                if (Values.size() > 0) {
                    mydb.updateGrafik(dataWartosc, symbol);
                } else {
                    mydb.wstawZmiane(dataWartosc, symbol);
                }
                refresh();
            }
        });
        builder.create();
        builder.show();
    }
    private void opcjeZakresuDni() {
        final String[] opcje = {
                "Urlop",
                "Zwolnienie"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//powinno byc this
        builder.setTitle("Wybierz opcję");
        builder.setItems(opcje, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // tu obrabiamy element który został wybrany.
                String wybranaOpcjaDlaZakresu = wybranaZmiana(opcje[which]);
                wypelnijKomorki(rokWybrany, miesiacWybrany, zaznaczonyDzien1, zaznaczonyDzien2, wybranaOpcjaDlaZakresu);
                //testy.setText(rokWybrany+"\n"+miesiacWybrany+"\n"+zaznaczonyDzien1+"\n"+zaznaczonyDzien2+"\n"+wybranaOpcjaDlaZakresu);
            }
        });
        builder.create();
        builder.show();
    }

    private void updateGrafik(int dataWartosc, String symbol){
        ArrayList<String> Values;
        Values = mydb.getAllRows(dataWartosc);
        if (Values.size() > 0) {
            mydb.updateGrafik(dataWartosc, symbol);
        } else {
            mydb.wstawZmiane(dataWartosc, symbol);
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
}