/*
Изчертава зоната като слой. Моята добавка се състои в това да я запълни с цвят.
Това го осъществявам по следния начин. Изполвам функцията path на класа canvas.
Чрез отместване на вътре с по дебела четка запълвам зоната. Трудността в задачата се състои да определя втрепните точки.
За целта разглеждам 3 съседни точки.M1(x1,y1);M2(x2,y2);M2(x3,y3) През 2 точки минава права. Намирам ъгъла който първата правата сключва с оста Ох.
Използва се Декартовото уравнение на права y=kx+n, като коефициента n - отместването на правата от центъра не е важно, защото търсим ъгъл.
k - ъглов коефициент (не е ъгъла който сключва правата с оста Ох) k=tg(alpha)=dx/dy, където dx=x2-x1,dy=y2-y1.
ъгълът alpha=atan2(k)=atan2(dx,dy). Съответно по горните формули намирам ъглите на 2-те прави минаващи през точка M2.
Нека тези ъгли означим с alpha и beta. Нека с theta означим ъгълът който е заключен между правите. Той винаги е по-малък от 180.
Трябва да намеря координатите на четвърта точка намираща се на права която е ъглополовяща на двете прави. Т.е. тя минава през М2.
theta се намира по следната формула tg(theta)=(k2-k1)/(1-k1k2), следователно theta=atan2(k2-k1,1-k1k2), където k1 и k2 са ъгловите коефициенти на двете прави.
Ъгълът който новата ъглополовяща сключва с оста Ох е: (alpha+theta/2). Как ще намерим коордиинатите на новата точка, която е отместена на вътре по ъглополовящата.
Т.е. трябва ни отместването dx,dy от M2. Нека да го означим New_dx, New_dy. В зависимост от това колко на вътре искаме да бъде отместването изчисляваме отсечката
на отместването, която ще означим със c. Т.е. c=a/sin(theta/2). Ако (alpha+theta/2) е 0, 90 или 180 то отместването ще бъде дължината на отсечката (New_dx=0, New_dy=c).
В останалите случаи New_dx, New_dy се изчисляват използвайки питагоровата теорема(c^=New_dx^+New_dy^) и връзката New_k=New_dx/New_dy.
 */
package com.borkozic.util;

import android.graphics.PointF;

public class Geometric {
    /*
     * Work out the angle from the x horizontal winding anti-clockwise in screen space.
     * The value returned from the following should be 315.
     * <pre>
     * x,y -------------
     *     | x1,y1
     *     |    \
     *     |     \
     *     |    x2,y2
     * </pre>
     * @param p1
     * @param p2
     * @return - a double from (0 to 2PI) in radians or (0 to 360)
     * NOTE: Remember that most math has the Y axis as positive above the X. However, for screens we have Y as positive below. For this reason,
     * the Y values are inverted to get the expected results.
     */
    public static double angleOf(PointF p1, PointF p2) {
        final double dY = (p1.y - p2.y);
        final double dX = (p2.x - p1.x);
        final double inRads = Math.atan2(dY, dX);
       // final double result = Math.toDegrees(Math.atan2(dY, dX));
        return (inRads < 0) ? (2 * Math.PI + inRads) : inRads;
    }
    /*
     * Calculate the angle between two lines.
     * The value returned from the following should be 15.
     * <pre>
     * x,y -------------
     *     | x1,y1  x3,y3
     *     |    \   |
     *     |     \ |
     *     |      \
     *     |    x2,y2
     * </pre>
     * @param p1
     * @param p2
     * @param p3
     * @return - a double from (0 to 2PI) in radians or (0 to 360)
     * NOTE: Remember that most math has the Y axis as positive above the X. However, for screens we have Y as positive below. For this reason,
     * the Y values are inverted to get the expected results.
     */
    public static double angleBetween(PointF p1, PointF p2, PointF p3) {

        final double dY1 = (p1.y - p2.y);
        final double dX1 = (p2.x - p1.x);
        final double dY2 = (p2.y - p3.y);
        final double dX2 = (p3.x - p2.x);

        final double alphaInRads = Math.atan2(dY1, dX1);
        final double bethaInRads = Math.atan2(dY2, dX2);
        final double thetaInRads =bethaInRads-alphaInRads;
        return (thetaInRads < 0) ? (2 * Math.PI + thetaInRads) : thetaInRads;
    }

    public static int[] newPointF(int[] p1, int[] p2, int[] p3, double d) {

        final double dY1 = (p1[1] - p2[1]);
        final double dX1 = (p2[0] - p1[0]);
        final double dY2 = (p2[1] - p3[1]);
        final double dX2 = (p3[0] - p2[0]);

        final double alphaInRads = Math.atan2(dY1, dX1);
        final double bethaInRads = Math.atan2(dY2, dX2);
        final double thetaInRads = bethaInRads-alphaInRads;
        final double hipotenusa = d*Math.sin(thetaInRads/2);
        final double koef = Math.tan(alphaInRads+thetaInRads/2);
        final double New_dY = Math.sqrt(hipotenusa*hipotenusa - koef*koef - 1);
        final double New_dX = New_dY*koef;
        final int[] result = {(int) (Math.round(New_dY)),(int) Math.round(New_dX)};
        return result;
    }
    /*
     * Calculate distance between two points in latitude and longitude
     * Uses Haversine method as its base.
     * lat1, lon1 Start point lat2, lon2 End point
     * @returns Distance in Meters
     */
    public static double distanceBorko(double lat1, double lat2, double lon1, double lon2)
    {// WGS-84 ellipsoid
        final int R = 6378137; // Radius of the earth in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return  R * c ; // in meters

    }
}
