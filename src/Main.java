import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // Initialize the training and testing datasets
        List<List<String>> trainset = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("agaricus-lepiota.data"));
        String line;

        // Read and parse the training data file
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            trainset.add(Arrays.asList(values));
        }

        List<List<String>> testset = new ArrayList<>();
        br = new BufferedReader(new FileReader("agaricus-lepiota.test.data"));

        // Read and parse the test data file
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            testset.add(Arrays.asList(values));
        }

        int pTotal = 0;
        int eTotal = 0;

        // Count the total number of poisonous (p) and edible (e) mushrooms in the test set
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

        // Iterate through each entry in the training set
        for (int y = 0; y < trainset.size(); y++) {
            double pProb = (double) pTotal / testset.size();
            double eProb = (double) eTotal / testset.size();

            List<String> trainvec = trainset.get(y);

            // Calculate the probabilities for each attribute in the training set
            for (int i = 1; i < trainvec.size(); i++) {
                int pInstances = 0;
                int eInstances = 0;

                // Count matching instances in the test set for the current attribute
                for (List<String> testvec : testset) {
                    if (testvec.get(0).equals("p") && testvec.get(i).equals(trainvec.get(i))) {
                        pInstances++;
                    } else if (testvec.get(0).equals("e") && testvec.get(i).equals(trainvec.get(i))) {
                        eInstances++;
                    }
                }

                // Calculate probabilities with smoothing if necessary
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

            // Determine if the prediction is correct and update the counts accordingly
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

        // Calculate precision, recall, and F1-score
        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);

        // Print the accuracy, precision, recall, and F1-score
        System.out.println("Accuracy: " + (double) correct / trainset.size() * 100);
        System.out.println("Precision: " + precision * 100);
        System.out.println("Recall: " + recall * 100);
        System.out.println("F-Measure: " + (2 * precision * recall) / (precision + recall) * 100);
    }

    // Smoothing function to handle zero instances in the probability calculation
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
