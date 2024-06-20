import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<List<String>> trainset = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("agaricus-lepiota.data"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            trainset.add(Arrays.asList(values));
        }

        List<List<String>> testset = new ArrayList<>();
        br = new BufferedReader(new FileReader("agaricus-lepiota.test.data"));
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            testset.add(Arrays.asList(values));
        }

        int pTotal = 0;
        int eTotal = 0;

        for (List<String> entry : testset) {
            if (entry.get(0).equals("p")) {
                pTotal++;
            } else if (entry.get(0).equals("e")) {
                eTotal++;
            }
        }

        int correct = 0;
        int tp = 0;
        int fp = 0;
        int fn = 0;
        for (int y = 0; y < trainset.size(); y++) {
            double pProb = (double) pTotal / testset.size();
            double eProb = (double) eTotal / testset.size();

            List<String> trainvec = trainset.get(y);

            for (int i = 1; i < trainvec.size(); i++) {
                int pInstances = 0;
                int eInstances = 0;
                for (List<String> testvec : testset) {
                    if (testvec.get(0).equals("p") && testvec.get(i).equals(trainvec.get(i))) {
                        pInstances++;
                    } else if (testvec.get(0).equals("e") && testvec.get(i).equals(trainvec.get(i))) {
                        eInstances++;
                    }
                }
                double a = (double) pInstances / pTotal;
                double b = (double) eInstances / eTotal;
                if (pInstances == 0) {
                    a = smooth(pTotal, testset, i);
                }
                if (eInstances == 0) {
                    b = smooth(eTotal, testset, i);
                }
                pProb *= a;
                eProb *= b;
            }

            if (pProb > eProb) {
                if (trainvec.get(0).equals("p")) {
                    correct++;
                    tp++;
                } else {
                    fp++;
                }
            } else if (pProb < eProb) {
                if (trainvec.get(0).equals("e")) {
                    correct++;
                } else {
                    fn++;
                }
            }
        }

        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);

        System.out.println("Dokladnosc: " + (double) correct / trainset.size() * 100);
        System.out.println("Precycja: " + precision * 100);
        System.out.println("Pelnosc: " + recall * 100);
        System.out.println("F1-miara: " + (2 * precision * recall) / (precision + recall) * 100);
    }

    private static double smooth(int counter, List<List<String>> trainTable, int i) {
        List<String> cases = new ArrayList<>();
        for (List<String> e : trainTable) {
            if (!cases.contains(e.get(i))) {
                cases.add(e.get(i));
            }
        }
        return 1.0 / (counter + cases.size());
    }
}