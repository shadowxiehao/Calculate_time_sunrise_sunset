

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class SunRiseSet {

    private static int[] days_of_month_1 = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static int[] days_of_month_2 = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static double h = -0.833;//日出日落时太阳的位置(这里值可改,初始为167°太阳方位角)

    private final static double UTo = 180.0;//上次计算的日落日出时间，初始迭代值180.0

    //输入日期

//输入经纬度

//判断是否为闰年：若为闰年，返回1；若不是闰年,返回0

    public static boolean leap_year(int year) {

        if (((year % 400 == 0) || (year % 100 != 0) && (year % 4 == 0))) return true;

        else return false;

    }

//求从格林威治时间公元2000年1月1日到计算日天数days

    public static int days(int year, int month, int date) {

        int i, a = 0;

        for (i = 2000; i < year; i++) {

            if (leap_year(i)) a = a + 366;

            else a = a + 365;

        }

        if (leap_year(year)) {

            for (i = 0; i < month - 1; i++) {

                a = a + days_of_month_2[i];

            }

        } else {

            for (i = 0; i < month - 1; i++) {

                a = a + days_of_month_1[i];

            }

        }

        a = a + date;

        return a;

    }

//求格林威治时间公元2000年1月1日到计算日的世纪数t

    public static double t_century(int days, double UTo) {

        return ((double) days + UTo / 360) / 36525;

    }

//求太阳的平黄径

    public static double L_sun(double t_century) {

        return (280.460 + 36000.770 * t_century);

    }

//求太阳的平近点角

    public static double G_sun(double t_century) {

        return (357.528 + 35999.050 * t_century);

    }

//求黄道经度

    public static double ecliptic_longitude(double L_sun, double G_sun) {

        return (L_sun + 1.915 * Math.sin(G_sun * Math.PI / 180) + 0.02 * Math.sin(2 * G_sun * Math.PI / 180));

    }

//求地球倾角

    public static double earth_tilt(double t_century) {

        return (23.4393 - 0.0130 * t_century);

    }

//求太阳偏差

    public static double sun_deviation(double earth_tilt, double ecliptic_longitude) {

        return (180 / Math.PI * Math.asin(Math.sin(Math.PI / 180 * earth_tilt) * Math.sin(Math.PI / 180 * ecliptic_longitude)));

    }

//求格林威治时间的太阳时间角GHA

    public static double GHA(double UTo, double G_sun, double ecliptic_longitude) {

        return (UTo - 180 - 1.915 * Math.sin(G_sun * Math.PI / 180) - 0.02 * Math.sin(2 * G_sun * Math.PI / 180) + 2.466 * Math.sin(2 * ecliptic_longitude * Math.PI / 180) - 0.053 * Math.sin(4 * ecliptic_longitude * Math.PI / 180));

    }

//求修正值e

    public static double e(double h, double glat, double sun_deviation) {

        return 180 / Math.PI * Math.acos((Math.sin(h * Math.PI / 180) - Math.sin(glat * Math.PI / 180) * Math.sin(sun_deviation * Math.PI / 180)) / (Math.cos(glat * Math.PI / 180) * Math.cos(sun_deviation * Math.PI / 180)));

    }

//求日出时间

    public static double UT_rise(double UTo, double GHA, double glong, double e) {

        return (UTo - (GHA + glong + e));

    }

//求日落时间

    public static double UT_set(double UTo, double GHA, double glong, double e) {

        return (UTo - (GHA + glong - e));

    }

//判断并返回结果（日出）

    public static double result_rise(double UT, double UTo, double glong, double glat, int year, int month, int date) {

        double d;

        if (UT >= UTo) d = UT - UTo;

        else d = UTo - UT;

        if (d >= 0.1) {

            UTo = UT;

            UT = UT_rise(UTo,

                    GHA(UTo, G_sun(t_century(days(year, month, date), UTo)),

                            ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                    G_sun(t_century(days(year, month, date), UTo)))),

                    glong,

                    e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)),

                            ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                    G_sun(t_century(days(year, month, date), UTo))))));

            result_rise(UT, UTo, glong, glat, year, month, date);


        }

        return UT;

    }

//判断并返回结果（日落）

    public static double result_set(double UT, double UTo, double glong, double glat, int year, int month, int date) {

        double d;

        if (UT >= UTo) d = UT - UTo;

        else d = UTo - UT;

        if (d >= 0.1) {

            UTo = UT;

            UT = UT_set(UTo,

                    GHA(UTo, G_sun(t_century(days(year, month, date), UTo)),

                            ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                    G_sun(t_century(days(year, month, date), UTo)))),

                    glong,

                    e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)),

                            ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                    G_sun(t_century(days(year, month, date), UTo))))));

            result_set(UT, UTo, glong, glat, year, month, date);

        }

        return UT;

    }

//求时区

    public static int Zone(double glong) {

        if (glong >= 0) return (int) ((int) (glong / 15.0) + 1);

        else return (int) ((int) (glong / 15.0) - 1);

    }

//更改太阳方位角
    public static void angle(int angles){
        h = -1 + (angles/1000.0);
    }

//打印结果

// public static void output(double rise, double set, double glong){

//     if((int)(60*(rise/15+Zone(glong)-(int)(rise/15+Zone(glong))))<10)

//         System.out.println("The time at which the sunrise is: "+(int)(rise/15+Zone(glong))+":"+(int)(60*(rise/15+Zone(glong)-(int)(rise/15+Zone(glong))))+" .\n");

//     else System.out.println("The time at which the sunrise is: "+(int)(rise/15+Zone(glong))+":"+(int)(60*(rise/15+Zone(glong)-(int)(rise/15+Zone(glong))))+" .\n");

//

//     if((int)(60*(set/15+Zone(glong)-(int)(set/15+Zone(glong))))<10)

//         System.out.println("The time at which the sunset is: "+(int)(set/15+Zone(glong))+": "+(int)(60*(set/15+Zone(glong)-(int)(set/15+Zone(glong))))+" .\n");

//     else System.out.println("The time at which the sunset is: "+(int)(set/15+Zone(glong))+":"+(int)(60*(set/15+Zone(glong)-(int)(set/15+Zone(glong))))+" .\n");

// }

    public static float[] getSunrise(BigDecimal longitude, BigDecimal latitude, String dateTime,int UTC) {//将返回的日出时间转化为字符串形式友好化输出
        float[] out = new float[2];
        if (dateTime != null && longitude != null && latitude != null) {
            double sunrise, glong, glat;
            int year, month, date;

            String[] rq = dateTime.split("-");
            String y = rq[0];
            String m = rq[1];
            String d = rq[2];
            year = Integer.parseInt(y);
            if (m != null && m != "" && m.indexOf("0") == -1) {
                m = m.replaceAll("0", "");
            }

            month = Integer.parseInt(m);

            date = Integer.parseInt(d);

            glong = longitude.doubleValue();

            glat = latitude.doubleValue();

            sunrise = result_rise(UT_rise(UTo,

                    GHA(UTo, G_sun(t_century(days(year, month, date), UTo)),

                            ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                    G_sun(t_century(days(year, month, date), UTo)))),

                    glong,

                    e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)),

                            ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                    G_sun(t_century(days(year, month, date), UTo)))))), UTo, glong, glat, year, month, date);

//System.out.println("Sunrise is: "+(int)(sunrise/15+Zone(glong))+":"+(int)(60*(sunrise/15+Zone(glong)-(int)(sunrise/15+Zone(glong))))+" .\n");

//        Log.d("Sunrise", "Sunrise is: "+(int)(sunrise/15+8)+":"+(int)(60*(sunrise/15+8-(int)(sunrise/15+8)))+" .\n");

            //return "Sunrise is: "+(int)(sunrise/15+Zone(glong))+":"+(int)(60*(sunrise/15+Zone(glong)-(int)(sunrise/15+Zone(glong))))+" .\n";

            out[0]=(int) (sunrise / 15 + UTC);
            out[1]=(float)Math.round(60 * (sunrise / 15 + UTC - (int) (sunrise / 15 + UTC))*10)/10;
            return out;
        }
        return null;
    }


    public static float[] getSunset(BigDecimal longitude, BigDecimal latitude, String dateTime,int UTC) {//将返回的日落时间转化为字符串形式友好化输出
        float[] out = new float[2];
        if (dateTime != null && latitude != null && longitude != null) {
            double sunset, glong, glat;
            int year, month, date;

            String[] rq = dateTime.split("-");
            String y = rq[0];
            String m = rq[1];
            String d = rq[2];
            year = Integer.parseInt(y);
            if (m != null && m != ""  && m.indexOf("0") == -1) {
                m = m.replaceAll("0", "");
            }
            month = Integer.parseInt(m);

            date = Integer.parseInt(d);

            glong = longitude.doubleValue();

            glat = latitude.doubleValue();

            sunset = result_set(
                    UT_set(UTo,
                            GHA(UTo, G_sun(t_century(days(year, month, date), UTo)),
                                    ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),
                                            G_sun(t_century(days(year, month, date), UTo
                                                    )
                                            )
                                    )
                            ),
                            glong,
                            e(
                                    h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)),

                                    ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)),

                                            G_sun(t_century(days(year, month, date), UTo)))))), UTo, glong, glat, year, month, date);

//System.out.println("The time at which the sunset is: "+(int)(sunset/15+Zone(glong))+":"+(int)(60*(sunset/15+Zone(glong)-(int)(sunset/15+Zone(glong))))+" .\n");

//        Log.d("Sunset", "Sunset is: "+(int)(sunset/15+8)+":"+(int)(60*(sunset/15+8-(int)(sunset/15+8)))+" .\n");

            //return "Sunset is: "+(int)(sunset/15+Zone(glong))+":"+(int)(60*(sunset/15+Zone(glong)-(int)(sunset/15+Zone(glong))))+" .\n";

            out[0]=(int) (sunset / 15 + UTC);//记录小时
            out[1]=Math.round((60 * (sunset / 15 + UTC - (int) (sunset / 15 + UTC)))*10)/10;
            return out;
        }
        return null;
    }

    public static void outcome(int year,int month,int day, double longitude, double latitude,int UTC,int angle1,int angle2){
        float[] out1;
        float[] out2;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateTime = sdf.format(new Date(year-1900,month-1,day));
        angle(angle1);
        out1 = SunRiseSet.getSunrise(new BigDecimal(longitude),new BigDecimal(latitude),dateTime,UTC);
        angle(angle2);
        out2= SunRiseSet.getSunset(new BigDecimal(longitude),new BigDecimal(latitude),dateTime,UTC);

        System.out.println("dateTime：" + dateTime);
        System.out.println("日出时间：" + (int)out1[0]+":"+out1[1]);
        System.out.println("日落时间：" + (int)out2[0]+":"+out2[1]);
    }

    public static void main(String[] args) {
        int year,month,day,angle1,angle2;
        int[] angles1 = {112,113,113,114,114,115,115,116,116,117,117,118,118,119,119,120,120,120,121,121,122,122,122,123,123,124,124,124,124,125};
        int[] angles2 = {248,247,246,246,245,245,244,244,243,243,242,242,242,241,241,240,240,239,239,239,238,238,237,237,237,236,236,236,235,235};
        Scanner sc = new Scanner(System.in);
        System.out.print("Year");

        for(int i=0;i<30;i++)
        outcome(2018,11,i+1,6.7766583,50.8993757,Zone(6.7766583),angles1[i],angles2[i]);
    }
}
