package coursework;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import model.Fitness;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Example of how to to run the {@link ExampleEvolutionaryAlgorithm} without the
 * need for the GUI This allows you to conduct multiple runs programmatically
 * The code runs faster when not required to update a user interface
 *
 */
public class StartNoGui
{
	public static void main(String[] args) throws IOException
	{
		final int n_tests = 10;
		final boolean run_tests = false;

		
		Parameters.setHidden(6);
		Parameters.popSize = 200;
		Parameters.mutateRate = 0.65;
		Parameters.mutateChange = 0.5;
		Parameters.replacement_rate = 0.1;
		Parameters.conf_threshold = 0.65;
		
		
		if (run_tests)
		{
			FileWriter fw = new FileWriter("data8 fast eval threshhold.csv");
			fw.write("conf threshold, training 1, test 1, training 2, test 2, training 3, test 3, training 4, test 4, training 5, test 5,"
							+ " training 6, test 6, training 7, test 7, training 8, test 8, training 9, test 9, training 10, test 10\n");
			


			for (double tr = 0.5; tr <= 0.99; tr += 0.01)
			{
				fw.write(tr + ", ");
				for (int test = 0; test < n_tests; test++)
				{
					
					Parameters.maxEvaluations = 20000; // Used to terminate the EA after this many generations
					Parameters.setDataSet(DataSet.Training);
					NeuralNetwork nn = new ExampleEvolutionaryAlgorithm();
					nn.run();

					System.out.println(nn.best);
					fw.write(nn.best.fitness + ",");

					Parameters.setDataSet(DataSet.Test);
					double fitness = Fitness.evaluate(nn);
					fw.write(fitness + ",");
					System.out.println("Fitness on " + Parameters.getDataSet() + " " + fitness);
				}
				fw.write("\n");
			}
			// results.add("Test fitness: " + fitness + "\n");

			/**
			 * Or We can reload the NN from the file generated during training and test it
			 * on a data set We can supply a filename or null to open a file dialog Note
			 * that files must be in the project root and must be named *-n.txt where "n" is
			 * the number of hidden nodes ie 1518461386696-5.txt was saved at timestamp
			 * 1518461386696 and has 5 hidden nodes Files are saved automatically at the end
			 * of training
			 * 
			 * Uncomment the following code and replace the name of the saved file to test a
			 * previously trained network
			 */

//		NeuralNetwork nn2 = NeuralNetwork.loadNeuralNetwork("1234567890123-5.txt");
//		Parameters.setDataSet(DataSet.Random);
//		double fitness2 = Fitness.evaluate(nn2);
//		System.out.println("Fitness on " + Parameters.getDataSet() + " " + fitness2);

			fw.close();
		} else
		{

			Parameters.maxEvaluations = 20000; // Used to terminate the EA after this many generations
			Parameters.setDataSet(DataSet.Training);
			NeuralNetwork nn = new ExampleEvolutionaryAlgorithm();
			nn.run();

			System.out.println(nn.best);

			Parameters.setDataSet(DataSet.Test);
			double fitness = Fitness.evaluate(nn);
			System.out.println("Fitness on " + Parameters.getDataSet() + " " + fitness);
		}

		System.out.println("DONE!");
	}
}
