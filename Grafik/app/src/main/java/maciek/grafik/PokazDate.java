package maciek.grafik;

import java.util.GregorianCalendar;

/*
 * Created by BHclothes on 17.09.2017.
 */

public class PokazDate {
    public String aktMiesiac, aktRok, aktDzien;

    public String pobierzDate() {
        String aktData;
        GregorianCalendar dzis = new GregorianCalendar();
        aktDzien = Integer.toString(dzis.get(GregorianCalendar.DAY_OF_MONTH));
        if (dzis.get(GregorianCalendar.MONTH) < 9) {
            aktMiesiac = "0" + (dzis.get(GregorianCalendar.MONTH) + 1);// +1 bo miesiace zaczynaja sie od 0
        } else {
            aktMiesiac = Integer.toString(dzis.get(GregorianCalendar.MONTH) + 1);// +1 bo miesiace zaczynaja sie od 0
        }
        aktRok = Integer.toString(dzis.get(GregorianCalendar.YEAR));
        aktData = aktDzien + aktMiesiac + aktRok;

        return (aktData);
    }
}
