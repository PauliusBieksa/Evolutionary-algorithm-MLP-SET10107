package coursework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that
 * extends {@link NeuralNetwork}
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork
{

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run()
	{
		// Initialise a population of Individuals with random weights
		population = initialise();

		// Initialise confidence values
		HashMap<Individual, Double> confidence = new HashMap<Individual, Double>();
		for (Individual i : population)
			confidence.put(i, 1.0);

		// Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */

		while (evaluations < Parameters.maxEvaluations)
		{
			int n_children = (int) (Parameters.popSize * Parameters.replacement_rate);
			if (n_children < 1)
				n_children = 1;
			ArrayList<Individual> children = new ArrayList<Individual>();

			for (int i = 0; i < n_children; i++)
			{
				// Selection
				Individual parent1 = select_tournament(2);
				Individual parent2 = select_tournament(2);

				Individual child = k_point_crossover(parent1, parent2, 2);
				mutate(child);

				double similarity1 = 0.0;
				double similarity2 = 0.0;
				for (int g = 0; g < child.chromosome.length; g++)
				{
					double c = Math.abs(child.chromosome[g]);
					double p1 = Math.abs(parent1.chromosome[g]);
					double p2 = Math.abs(parent2.chromosome[g]);
					similarity1 += Math.min(c, p1) / Math.max(c, p1);
					similarity2 += Math.min(c, p2) / Math.max(c, p2);
				}
				similarity1 /= child.chromosome.length;
				similarity2 /= child.chromosome.length;

				child.fitness = (similarity1 * confidence.get(parent1) * parent1.fitness + similarity2 * confidence.get(parent2) * parent2.fitness)
						/ (similarity1 * confidence.get(parent1) + similarity2 * confidence.get(parent2));
				double conf = (similarity1 * confidence.get(parent1) * similarity1 * confidence.get(parent1) + similarity2 * confidence.get(parent2) * similarity2 * confidence.get(parent2))
						/ (similarity1 * confidence.get(parent1) + similarity2 * confidence.get(parent2));
				
//				if (conf > 0.7)
//					conf = conf;
				
				confidence.put(child, conf);

				// Crossover
				children.add(child);
			}

			// Mutation
			//mutate(children);

			// Evaluation
			//evaluateIndividuals(children);
			fast_evaluate(children, confidence);

			replace(children);

			// Get best individual
			best = getBest();

			// Does something that the jar won't let me see. May be not important
			// setChanged();
			// notifyObservers(best.copy());
			outputStats();
		}

		// save the trained network to disk
		saveNeuralNetwork();
	}

	
	
	
	// Evaluates individuals if confidence value below threshold
	private void fast_evaluate(ArrayList<Individual> individuals, HashMap<Individual, Double> conf)
	{
		for (Individual individual : individuals)
		{
			if (conf.get(individual) < Parameters.conf_threshold)
				individual.fitness = Fitness.evaluate(individual, this);
		}
	}



	// Produces a list of children by crossover of 2 parents
	private ArrayList<Individual> reproduce(Individual parent1, Individual parent2)
	{
		ArrayList<Individual> children = new ArrayList<>();
		children.add(k_point_crossover(parent1, parent2, 3));
		children.add(k_point_crossover(parent2, parent1, 3));
		return children;
	}



	// Tournament selection. Uses n members chosen randomly.
	private Individual select_tournament(int n)
	{
		ArrayList<Individual> parents = new ArrayList<Individual>();
		for (int i = 0; i < n; i++)
			parents.add(select_random());

		double best_fitness = 99.0;
		int best = 0;
		for (int i = 1; i < parents.size(); i++)
			if (parents.get(i).fitness < best_fitness)
			{
				best_fitness = parents.get(i).fitness;
				best = i;
			}
		return parents.get(best);
	}



	// Random parent
	private Individual select_random()
	{
		Individual parent = population.get(Parameters.random.nextInt(Parameters.popSize));
		return parent;
	}



	// K point crossover for 2 given parents. If k is more than n of genes returns
	// parent1
	private Individual k_point_crossover(Individual parent1, Individual parent2, Integer k)
	{
		if (Parameters.getNumGenes() < k)
		{
			Individual child = new Individual();

			ArrayList<Integer> c_points = new ArrayList<Integer>();
			while (c_points.size() < k)
			{
				Integer point = Parameters.random.nextInt(k - 1) + 1;
				if (c_points.contains(point))
					continue;
				c_points.add(point);
			}

			boolean p1 = Parameters.random.nextBoolean();
			for (int i = 0; i < Parameters.getNumGenes(); i++)
			{
				if (c_points.contains(i))
					p1 = !p1;

				if (p1)
					child.chromosome[i] = parent1.chromosome[i];
				else
					child.chromosome[i] = parent2.chromosome[i];
			}
			return child;
		} else
			return parent1.copy();
	}
	
	
	
	//
	private Individual uniform_crossover(Individual parent1, Individual parent2)
	{
		Individual child = new Individual();
		for (int i = 0; i < parent1.chromosome.length; i++)
		{
			if (Parameters.random.nextBoolean())
				child.chromosome[i] = parent1.chromosome[i];
			else
				child.chromosome[i] = parent2.chromosome[i];
		}
		return child;
	}



	// Mutates an individual
	private Individual forced_mutate(Individual parent)
	{
		Individual child = parent.copy();
		while (Arrays.equals(child.chromosome, parent.chromosome))
		{
			for (int i = 0; i < child.chromosome.length; i++)
			{
				if (Parameters.random.nextDouble() < Parameters.mutateRate)
				{
					if (Parameters.random.nextBoolean())
					{
						child.chromosome[i] += (Parameters.mutateChange);
					} else
					{
						child.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}
		return child;

	}



	// Mutete single Individual
	private void mutate(Individual individual)
	{
		for (int i = 0; i < individual.chromosome.length; i++)
		{
			if (Parameters.random.nextDouble() < Parameters.mutateRate)
			{
				if (Parameters.random.nextBoolean())
				{
					individual.chromosome[i] += (Parameters.mutateChange);
				} else
				{
					individual.chromosome[i] -= (Parameters.mutateChange);
				}
			}
		}
	}



//	// Leaky ReLu
//	@Override
//	public double activationFunction(double x)
//	{
//		if (x < 0)
//		{
//			return x * 0.01;
//		} else
//		{
//			return x;
//		}
//	}

	@Override
	public double activationFunction(double x)
	{
		return x;
//		if (x < -20.0)
//		{
//			return -1.0;
//		} else if (x > 20.0)
//		{
//			return 1.0;
//		}
//		return Math.tanh(x);
	}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals)
	{
		for (Individual individual : individuals)
		{
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}



	/**
	 * Mutation
	 * 
	 * 
	 */
	private void mutate(ArrayList<Individual> individuals)
	{
		for (Individual individual : individuals)
		{
			for (int i = 0; i < individual.chromosome.length; i++)
			{
				if (Parameters.random.nextDouble() < Parameters.mutateRate)
				{
					if (Parameters.random.nextBoolean())
					{
						individual.chromosome[i] += (Parameters.mutateChange);
					} else
					{
						individual.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}
	}



	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise()
	{
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i)
		{
			// chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}



	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest()
	{
		best = null;
		;
		for (Individual individual : population)
		{
			if (best == null)
			{
				best = individual.copy();
			} else if (individual.fitness < best.fitness)
			{
				best = individual.copy();
			}
		}
		return best;
	}



	/**
	 * 
	 * Replaces the worst member of the population (regardless of fitness)
	 * 
	 */
	private void replace(ArrayList<Individual> individuals)
	{
		for (Individual individual : individuals)
		{
			int idx = getWorstIndex();
			population.set(idx, individual);
		}
	}



	/**
	 * Returns the index of the worst member of the population
	 * 
	 * @return
	 */
	private int getWorstIndex()
	{
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++)
		{
			Individual individual = population.get(i);
			if (worst == null)
			{
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness)
			{
				worst = individual;
				idx = i;
			}
		}
		return idx;
	}
}
