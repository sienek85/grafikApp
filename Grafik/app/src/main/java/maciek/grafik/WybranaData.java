package maciek.grafik;

import java.util.GregorianCalendar;

/*
 * Created by BHclothes on 17.09.2017.
 */

public class WybranaData {
    public int pierwszyDzienMiesiaca, liczbaDniMiesiaca, ostatniDzienPoprzedniegoMiesiaca, pierwszyDzienBylegoMiesiacaJakoDzienRoku, pierwszyDzienObecnegoMiesiacaJakoDzienRoku, dataPoczatekMiesiaca, dataKoniecMiesiaca;

    public void generujDane(int rok, int miesiac) {
        int poprzedniMiesiac;
        GregorianCalendar wybranyMiesiac = new GregorianCalendar(rok, miesiac - 1, 1);
        liczbaDniMiesiaca = wybranyMiesiac.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        pierwszyDzienMiesiaca = wybranyMiesiac.get(GregorianCalendar.DAY_OF_WEEK);
        pierwszyDzienObecnegoMiesiacaJakoDzienRoku = wybranyMiesiac.get(GregorianCalendar.DAY_OF_YEAR);
        if (miesiac < 10) { //dodawanie zera przed miesiacem jesli jest on jedno cyfrowy
            dataPoczatekMiesiaca = Integer.parseInt(rok + "0" + miesiac + "01"); // generowanie 8 cyfrowej daty na poczatek miesiaca
            dataKoniecMiesiaca = Integer.parseInt(rok + "" + miesiac + "" + liczbaDniMiesiaca); // generowanie 8 cyfrowej daty na poczatek miesiaca
        } else {
            dataPoczatekMiesiaca = Integer.parseInt(rok + "" + miesiac + "01");
            dataKoniecMiesiaca = Integer.parseInt(rok + "" + miesiac + "" + liczbaDniMiesiaca);
        }
        if (miesiac == 1) {
            poprzedniMiesiac = 12;
        } else {
            poprzedniMiesiac = miesiac - 1;
        }
        GregorianCalendar dataPoprzedniMiesiac = new GregorianCalendar(rok, poprzedniMiesiac - 1, 1);
        ostatniDzienPoprzedniegoMiesiaca = dataPoprzedniMiesiac.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        pierwszyDzienBylegoMiesiacaJakoDzienRoku = dataPoprzedniMiesiac.get(GregorianCalendar.DAY_OF_YEAR);
    }
}
