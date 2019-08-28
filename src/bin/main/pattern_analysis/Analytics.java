package bin.main.pattern_analysis;

import bin.main.Gene_Main.Gene;
import bin.main.Gene_Main.GeneDatabase;
import bin.main.Patient_Information.Patient;
import bin.main.categorization.TestingCategory;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Contains methods pertaining to gene analysis, such as gene sequencing algorithms.
 *
 * This includes the Needleman-Wunsch Algorithm, and its scoring methods.
 */
public class Analytics {

    //Default scoring algorithm
    private final static int MATCH = 1;                   //Score given to the alignment matrix when the gene name matches
    private final static int MISMATCH_OR_INDEL = -1;      //Score given to the alignment matrix when the gene name mismatches
                                            //Score given to the alignment matrix when the gene name is the same from that movement

    private static String holder = "";

    /**
     * Calculates and determines the highest possible score of all possible sequence alignments.
     * @param a Values used to determine scoring.
     * @return The highest value of all alignments.
     */
    private static int getHighestScore(int... a){
        int[] holder_array = a.clone();
        Arrays.sort(a);
        for(int i = 0; i < holder_array.length; i++){
            if(holder_array[i] == a[a.length-1]) {
                switch (i) {
                    case 0:
                        holder += "\\";
                        break;
                    case 1:
                        holder += "^";
                        break;
                    case 2:
                        holder += "_";
                        break;
                }
            }
        }
        return a[a.length-1];
    }

    /**
     * Score algorithm determining calculations to find the highest alignment score possible.
     * @param top_left The top-left value.
     * @param above The above value.
     * @param left The left value.
     * @param g1 A gene to be aligned.
     * @param g2 A gene to be aligned.
     * @param column The current column to be checked.
     * @param row The current row to be checked.
     * @return The highest score possible with the given values.
     */
    private static int score_Algorithm(int top_left, int above, int left, Gene g1, Gene g2, int column, int row) {
        int score;

        if (g1.getGene_information()[column-1] == g2.getGene_information()[row-1]) {
            score = MATCH;
        }else
            score = MISMATCH_OR_INDEL;

        System.out.println("Column: " + g1.getGene_information()[column - 1] + " Row: " + g2.getGene_information()[row - 1] + " Index: " + (row-1) + "," + (column-1));
        System.out.println("Row: " + row + " Column: " + column + " "  + (top_left + score) + " " + (above + MISMATCH_OR_INDEL) + " " + (left + MISMATCH_OR_INDEL) + "\n");

        return getHighestScore(top_left + score, above + MISMATCH_OR_INDEL, left + MISMATCH_OR_INDEL);
    }

    /**
     * Implementation of the Needleman-Wunsch Algorithm.
     *
     * This algorithm is used primarily for gene sequencing looking for the optimal match between gene sequences.
     * This creates both an alignment matrix and pointer matrix.
     * @param g1 A gene to be aligned.
     * @param g2 A gene to be aligned.
     */
    public static void N_W_Algorithm(Gene g1, Gene g2) {
        int columns = g1.size() + 1;
        int rows = g2.size() + 1;

        //Initializes the Alignment Matrix
        int[][] alignment_Matrix = new int[rows][columns];

        //Initializes the Pointer Matrix
        String[][] pointer_Matrix = new String[rows][columns];

        for (int i = 0; i < rows; i++)
            alignment_Matrix[i][0] = -i;

        for (int i = 0; i < columns; i++)
            alignment_Matrix[0][i] = -i;

        for(int y = 1; y < rows; y++){
            for(int x = 1; x < columns; x++){
                System.out.println("TopLeft: " + alignment_Matrix[y-1][x-1]);
                System.out.println("Above: " + alignment_Matrix[y-1][x]);
                System.out.println("Left: " + alignment_Matrix[y][x-1]);
                alignment_Matrix[y][x] = score_Algorithm(alignment_Matrix[y-1][x-1], alignment_Matrix[y-1][x], alignment_Matrix[y][x-1], g1, g2, x, y);
                pointer_Matrix[y][x] = holder;
                holder = "";
            }

        }

        for(int y = 0; y < rows; y++){
            for(int x = 0; x < columns; x++) {
                System.out.print(alignment_Matrix[y][x]);
            }
            System.out.println();
        }

        for(int y = 1; y < rows; y++){
            for(int x = 1; x < columns; x++) {
                System.out.print(pointer_Matrix[y][x]);
            }
            System.out.println();
        }

    }

    //TODO Mutations Method needs to specify whether if has a point mutation or not. POINT MUTATIONS ARE WHAT WE CAN USE TO
    //IDENTIFY DELETION USING DELETION MAPPING

    public static void Mutations(Patient p, Integer pair_set){

        if(pair_set == 1) {
            for (Integer observing_chromosome : p.getPatient_data().getChromosome_information().getP1().keySet()) {
                System.out.println("Observing: " + observing_chromosome + " chromosome");
                for (Gene non_mutated_gene : GeneDatabase.nonMutatedGenome.get(observing_chromosome)) {
                    location_algorithm(observing_chromosome, non_mutated_gene, p.getPatient_data().getChromosome_information().getP1(), p);
                }
            }
        }else{
            for (Integer observing_chromosome : p.getPatient_data().getChromosome_information().getP2().keySet()) {
                System.out.println("Observing: " + observing_chromosome + " chromosome");
                for (Gene non_mutated_gene : GeneDatabase.nonMutatedGenome.get(observing_chromosome)) {
                    location_algorithm(observing_chromosome, non_mutated_gene, p.getPatient_data().getChromosome_information().getP2(), p);
                }
            }
        }

    }

    private static void location_algorithm(Integer observing_chromosome, Gene non_mutated_gene, HashMap<Integer, Gene> p2, Patient p) {
        int start = non_mutated_gene.getCytogenicLocation().getStart() - 1;
        int stop = non_mutated_gene.getCytogenicLocation().getStop() - 1;
        char[] gene_info = Arrays.copyOfRange(p2.get(observing_chromosome).getGene_information(), start, stop);
        if (!Arrays.equals(non_mutated_gene.getGene_information(), gene_info)) {
            System.out.println("Mutation Located at (" + non_mutated_gene.getCytogenicLocation().toString() + ") which is the (" + non_mutated_gene.getName() + ") gene.");
        }
    }


    /**
     * Based off of the information from the Mutations in the Patient we will try to recommend physical testing
     * for better diagnosis of the gene.
     * @param p This is the Patient who you would like to get the recommendation of testing for
     * @return Returns the Testing Category
     */
    public TestingCategory recomendation_of_Testing(Patient p){
        for(Integer i: p.getPatient_data().getChromosome_information().getP1().keySet()) {
            if (p.getPatient_data().getChromosome_information().getP1().get(i) != p.getPatient_data().getChromosome_information().getP2().get(i)){
                System.out.println(p.getPatient_name() + ": has an irregularity on the chromosome " + i);
                return TestingCategory.IRREGULARITY;
            }
        }

        System.out.print("No test is likely needed");
        return TestingCategory.NULL;
    }

}
