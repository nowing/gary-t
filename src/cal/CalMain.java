package cal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CalMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
        CalMain main = new CalMain();

        String name = main.readFilename();
        File test = new File(name);
        if (test.isDirectory()) {
            main.processDir(name);
        } else {
            main.processOneFile(name);
        }
    }

    // -------------- algorithm --------------------
    private double analyse(List<Double> t, List<Double> x1, List<Double> x2) {
        List<Double> z1 = step1(x1);
        List<Double> z2 = step1(x2);

        List<Double> z = step2(z1, z2);

        double r = step3(t, z);
        return r;
    }

    private double step3(List<Double> t, List<Double> z) {
        double sum = 0;
        for (int i = 0; i < z.size(); i++) {
            sum += z.get(i) * (t.get(i+1) - t.get(i));
        }

        double b_a = t.get(t.size() - 1) - t.get(0);
        return sum / b_a;

    }

    private List<Double> step2(List<Double> z1, List<Double> z2) {
        List<Double> z = new ArrayList();
        int n = z1.size();
        for (int i = 0; i < n; i++) {
            double z1i = z1.get(i);
            double z2i = z2.get(i);
            int sign = getSgn(z1i, z2i);

            double di;
            if (z1i == 0 && z2i == 0) {
                di = 1;
            } else {
                double part1 = Math.abs(Math.abs(z1i) - Math.abs(z2i)) / 2;

                double min = Math.min(Math.abs(z1i), Math.abs(z2i));
                double max = Math.max(Math.abs(z1i), Math.abs(z2i));
                double part2 = (1 - min / max) / 2;

                double part3 = 1 + part1 + part2;

                di = sign / part3;
            }
            z.add(di);
        }
        return z;
    }

    private List<Double> step1(List<Double> x) {

        List<Double> y = new ArrayList();
        for (int i = 1; i < x.size(); i++) {
            double yi = x.get(i) - x.get(i - 1);
            y.add(yi);
        }

        double sum = 0;
        for (int i = 0; i < y.size(); i++) {
            sum += Math.abs(y.get(i));
        }
        double d = sum / y.size();

        List<Double> z = new ArrayList();
        for (int i = 0; i < y.size(); i++) {
            z.add(y.get(i) / d);
        }

        return z;
    }

    // -------------- support method ---------------
    private void processDir(String dirname) {
        File dir = new File(dirname);
        if (!dir.isDirectory()) {
            System.out.println(dirname + " is not a folder.");
            return;
        }

        File[] files = dir.listFiles();
        for (File one : files) {
            String name = one.getName();
            if (name.endsWith(".csv")) {
                processOneFile(name);
            }
        }

    }

    private void processOneFile(String filename) {
        List data = readFile(filename);
        if (data == null) {
            debug("something is wrong, so stop");
            return;
        }

        List<Double> x0 = (List<Double>) data.get(0);
        List<Double> x1 = (List<Double>) data.get(1);
        List<Double> x2 = (List<Double>) data.get(2);

        double r = analyse(x0, x1, x2);

        String comment = "";
        if (data.size() >= 4) {
            comment = (String) (data.get(3));
        }

        System.out.println(comment + ": r=" + r);

    }

    private List readFile(String filename) {
        List ret = new ArrayList();

        int timeCol = -1;
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    filename), "UTF-8");
            BufferedReader reader = new BufferedReader(read);

            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;

                if (lineNum <= 3) {
                    line = line.trim();
                    if (line.startsWith("#") || line.startsWith("/")) {
                        continue;
                    }

                    String[] cols = line.split(",");
                    if (timeCol < 0) {
                        timeCol = cols.length;
                    } else {
                        if (timeCol != cols.length) {
                            debug("column num error!");
                            return null;
                        }
                    }

                    List<Double> one = new ArrayList();
                    for (int i = 0; i < cols.length; i++) {
                        double a = Double.valueOf(cols[i]);
                        one.add(a);
                    }
                    ret.add(one);
                } else {
                    line = line.trim();
                    if (!line.equals("")) {
                        ret.add(line);
                    }
                }

            }
            reader.close();
            if (ret.size() == 3) {
                ret.add(filename);
            }
        } catch (NumberFormatException e) {
            debug("wrong number format in file");
            return null;
        } catch (UnsupportedEncodingException e) {
            debug("file encoding is not utf-8");
            return null;
        } catch (FileNotFoundException e) {
            debug("file not found");
            return null;
        } catch (IOException e) {
            debug("read file error");
            return null;
        }

        if (ret.size() < 3) {
            debug("not enough line");
            return null;
        }

        if (timeCol < 2) {
            debug("can't analyse only one column");
            return null;
        }

        return ret;
    }

    private String readFilename() {
        System.out.println("please input data-file name/folder name: ");

        String str = "";
        Scanner in = new Scanner(System.in);
        str = in.nextLine();
        in.close();

        return str;
    }

    private int getSgn(double a, double b) {
        int sign;
        if (a * b >= 0) {
            sign = 1;
        } else {
            sign = -1;
        }
        return sign;

    }

    private static void debug(Object o) {
        System.out.println(o);
    }
}
